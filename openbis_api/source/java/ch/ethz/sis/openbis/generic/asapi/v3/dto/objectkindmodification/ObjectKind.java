/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Enumeration of object kinds. An object kind is an attribute an object (like a sample, a data set type or a vocabulary term) which can be created,
 * deleted or updated.
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.objectkindmodification.ObjectKind")
public enum ObjectKind
{
    AUTHORIZATION_GROUP, SAMPLE, EXPERIMENT, MATERIAL, DATA_SET, SAMPLE_TYPE, EXPERIMENT_TYPE,
    MATERIAL_TYPE, DATASET_TYPE, FILE_FORMAT_TYPE, PROJECT, SPACE, PROPERTY_TYPE,
    PROPERTY_TYPE_ASSIGNMENT, VOCABULARY, VOCABULARY_TERM, ROLE_ASSIGNMENT, PERSON,
    GRID_CUSTOM_FILTER, GRID_CUSTOM_COLUMN, SCRIPT, DELETION, POSTREGISTRATION_QUEUE,
    QUERY, METAPROJECT, PERSONAL_ACCESS_TOKEN
}
