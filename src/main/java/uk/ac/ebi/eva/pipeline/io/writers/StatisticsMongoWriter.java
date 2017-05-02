/*
 * Copyright 2016 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.pipeline.io.writers;

import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.data.mongodb.core.MongoOperations;

import uk.ac.ebi.eva.pipeline.model.PopulationStatistics;

/**
 * Write a list of {@link PopulationStatistics} into MongoDB
 */
public class StatisticsMongoWriter extends MongoItemWriter<PopulationStatistics> {

    public StatisticsMongoWriter(MongoOperations mongoOperations, String collection) {
        super();
        setCollection(collection);
        setTemplate(mongoOperations);
    }
}
