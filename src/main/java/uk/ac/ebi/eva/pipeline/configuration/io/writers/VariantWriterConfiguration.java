/*
 * Copyright 2015-2017 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.pipeline.configuration.io.writers;

import org.opencb.biodata.models.variant.VariantSource;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoOperations;
import uk.ac.ebi.eva.commons.models.data.Variant;
import uk.ac.ebi.eva.pipeline.Application;
import uk.ac.ebi.eva.pipeline.io.writers.VariantMongoWriter;
import uk.ac.ebi.eva.pipeline.parameters.DatabaseParameters;
import uk.ac.ebi.eva.pipeline.parameters.InputParameters;

import static uk.ac.ebi.eva.pipeline.configuration.BeanNames.VARIANT_WRITER;

@Configuration
public class VariantWriterConfiguration {

    @Bean(VARIANT_WRITER)
    @StepScope
    @Profile(Application.VARIANT_WRITER_MONGO_PROFILE)
    public ItemWriter<Variant> variantMongoWriter(InputParameters inputParameters, MongoOperations mongoOperations,
                                                  DatabaseParameters databaseParameters) {
        boolean includeSamples, includeStats;
        if (VariantSource.Aggregation.NONE.equals(inputParameters.getVcfAggregation())) {
            includeSamples = true;
            includeStats = false;
        } else {
            includeSamples = false;
            includeStats = true;
        }

        return new VariantMongoWriter(databaseParameters.getCollectionVariantsName(), mongoOperations, includeStats,
                includeSamples);
    }

}
