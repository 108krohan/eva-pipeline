/*
 * Copyright 2015-2016 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package embl.ebi.variation.eva.pipeline.steps.tasklet;

import embl.ebi.variation.eva.VariantJobsArgs;
import org.opencb.datastore.core.ObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.io.*;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

/**
 * Created by jmmut on 2015-12-09.
 *
 * @author Jose Miguel Mut Lopez &lt;jmmut@ebi.ac.uk&gt;
 * @author Cristina Yenyxe Gonzalez Garcia &lt;cyenyxe@ebi.ac.uk&gt;
 */
@Import({VariantJobsArgs.class})
public class VariantsAnnotCreate implements Tasklet {
    private static final Logger logger = LoggerFactory.getLogger(VariantsAnnotCreate.class);
    public static final String SKIP_ANNOT_CREATE = "annotation.create.skip";

    @Autowired
    private VariantJobsArgs variantJobsArgs;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ObjectMap pipelineOptions = variantJobsArgs.getPipelineOptions();

        if (pipelineOptions.getBoolean(SKIP_ANNOT_CREATE)) {
            logger.info("skipping annotation creation step, skipAnnotCreate is set to {} ",
                    pipelineOptions.getBoolean(SKIP_ANNOT_CREATE));
        } else {
            ProcessBuilder processBuilder = new ProcessBuilder("perl",
                    pipelineOptions.getString("app.vep.path"),
                    "--cache",
                    "--cache_version", pipelineOptions.getString("app.vep.cache.version"),
                    "-dir", pipelineOptions.getString("app.vep.cache.path"),
                    "--species", pipelineOptions.getString("app.vep.cache.species"),
                    "--fasta", pipelineOptions.getString("input.fasta"),
                    "--fork", pipelineOptions.getString("app.vep.num-forks"),
                    "-i", pipelineOptions.getString("vep.input"),
                    "-o", "STDOUT",
                    "--force_overwrite", 
                    "--offline", 
                    "--everything"
            );
            
            logger.debug("VEP annotation parameters = " + Arrays.toString(processBuilder.command().toArray()));
            
            logger.info("Starting read from VEP output");
            Process process = processBuilder.start();
            
            long written = connectStreams(
                    new BufferedInputStream(process.getInputStream()), 
                    new GZIPOutputStream(new FileOutputStream(pipelineOptions.getString("vep.output"))));
            
            int exitValue = process.waitFor();
            logger.info("Finishing read from VEP output, bytes written: " + written);
            
            if (exitValue > 0) {
                String errorLog = pipelineOptions.getString("vep.output") + ".errors.txt";
                connectStreams(new BufferedInputStream(process.getErrorStream()), new FileOutputStream(errorLog));
                throw new Exception("Error while running VEP (exit status " + exitValue + "). See "
                        + errorLog  + " for the errors description from VEP.");
            }
        }

        return RepeatStatus.FINISHED;
    }

    /**
     * read all the inputStream and write it into the outputStream
     *
     * TODO: optimize with buffers?
     * @throws IOException
     */
    private long connectStreams(InputStream inputStream, OutputStream outputStream) throws IOException {
        int read = inputStream.read();
        long written = 0;
        while (read != -1) {
            written++;
            outputStream.write(read);
            read = inputStream.read();
        }

        outputStream.close();
        inputStream.close();
        return written;
    }
}
