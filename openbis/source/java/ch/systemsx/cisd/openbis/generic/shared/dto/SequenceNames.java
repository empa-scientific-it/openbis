/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

/**
 * The database sequence names.
 * 
 * @author Christian Ribeaud
 */
public final class SequenceNames
{

    public static final String CONTROLLED_VOCABULARY_SEQUENCE = "CONTROLLED_VOCABULARY_ID_SEQ";

    public static final String CONTROLLED_VOCABULARY_TERM_SEQUENCE = "CVTE_ID_SEQ";

    public static final String DATA_SEQUENCE = "DATA_ID_SEQ";

    public static final String PERM_ID_SEQUENCE = "PERM_ID_SEQ";

    public static final String DATA_SET_RELATIONSHIP_SEQUENCE = "DATA_SET_RELATIONSHIP_ID_SEQ";

    public static final String DATA_SET_RELATIONSHIPS_HISTORY_SEQUENCE = "DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ";

    public static final String DATA_SET_TYPE_SEQUENCE = "DATA_SET_TYPE_ID_SEQ";

    public static final String DATA_STORE_SEQUENCE = "DATA_STORE_ID_SEQ";

    public static final String EXTERNAL_DATA_MANAGEMENT_SYSTEM_SEQUENCE =
            "EXTERNAL_DATA_MANAGEMENT_SYSTEM_ID_SEQ";

    public static final String DATA_STORE_SERVICE_SEQUENCE = "DATA_STORE_SERVICES_ID_SEQ";

    public static final String DATA_TYPE_SEQUENCE = "DATA_TYPE_ID_SEQ";

    public static final String DATABASE_INSTANCE_SEQUENCE = "DATABASE_INSTANCE_ID_SEQ";

    public static final String ATTACHMENT_CONTENT_SEQUENCE = "ATTACHMENT_CONTENT_ID_SEQ";

    public static final String ATTACHMENT_SEQUENCE = "ATTACHMENT_ID_SEQ";

    public static final String EXPERIMENT_PROPERTY_SEQUENCE = "EXPERIMENT_PROPERTY_ID_SEQ";

    public static final String DATA_SET_PROPERTY_SEQUENCE = "DATA_SET_PROPERTY_ID_SEQ";

    public static final String EXPERIMENT_SEQUENCE = "EXPERIMENT_ID_SEQ";

    public static final String EXPERIMENT_TYPE_PROPERTY_TYPE_SEQUENCE = "ETPT_ID_SEQ";

    public static final String DATA_SET_TYPE_PROPERTY_TYPE_SEQUENCE = "DSTPT_ID_SEQ";

    public static final String EXPERIMENT_TYPE_SEQUENCE = "EXPERIMENT_TYPE_ID_SEQ";

    public static final String FILE_FORMAT_TYPE_SEQUENCE = "FILE_FORMAT_TYPE_ID_SEQ";

    public static final String RELATIONSHIP_TYPE_SEQUENCE = "RELATIONSHIP_TYPE_ID_SEQ";

    public static final String SPACE_SEQUENCE = "SPACE_ID_SEQ";

    public static final String FILTER_SEQUENCE = "FILTER_ID_SEQ";

    public static final String GRID_CUSTOM_COLUMNS_SEQUENCE = "GRID_CUSTOM_COLUMNS_ID_SEQ";

    public static final String QUERY_SEQUENCE = "QUERY_ID_SEQ";

    public static final String SCRIPT_SEQUENCE = "SCRIPT_ID_SEQ";

    public static final String DELETION_SEQUENCE = "DELETION_ID_SEQ";

    public static final String LOCATOR_TYPE_SEQUENCE = "LOCATOR_TYPE_ID_SEQ";

    public static final String MATERIAL_BATCH_SEQUENCE = "MATERIAL_BATCH_ID_SEQ";

    public static final String MATERIAL_PROPERTY_SEQUENCE = "MATERIAL_PROPERTY_ID_SEQ";

    public static final String SAMPLE_RELATIONSHIPS_SEQUENCE = "SAMPLE_RELATIONSHIP_ID_SEQ";

    public static final String MATERIAL_SEQUENCE = "MATERIAL_ID_SEQ";

    public static final String MATERIAL_TYPE_PROPERTY_TYPE_SEQUENCE = "MTPT_ID_SEQ";

    public static final String MATERIAL_TYPE_SEQUENCE = "MATERIAL_TYPE_ID_SEQ";

    public final static String PERSON_SEQUENCE = "PERSON_ID_SEQ";

    public static final String PROJECT_SEQUENCE = "PROJECT_ID_SEQ";

    public static final String PROPERTY_TYPES_SEQUENCE = "PROPERTY_TYPE_ID_SEQ";

    public static final String ROLE_ASSIGNMENT_SEQUENCE = "ROLE_ASSIGNMENT_ID_SEQ";

    public static final String SAMPLE_INPUT_SEQUENCE = "SAMPLE_INPUT_ID_SEQ";

    public static final String SAMPLE_MATERIAL_BATCH_SEQUENCE = "SAMPLE_MATERIAL_BATCH_ID_SEQ";

    public static final String SAMPLE_PROPERTY_SEQUENCE = "SAMPLE_PROPERTY_ID_SEQ";

    public static final String SAMPLE_SEQUENCE = "SAMPLE_ID_SEQ";

    public static final String SAMPLE_TYPE_PROPERTY_TYPE_SEQUENCE = "STPT_ID_SEQ";

    public static final String SAMPLE_TYPE_SEQUENCE = "SAMPLE_TYPE_ID_SEQ";

    public static final String EVENT_SEQUENCE = "EVENT_ID_SEQ";

    public static final String EVENTS_SEARCH_SEQUENCE = "EVENTS_SEARCH_ID_SEQ";

    public static final String CODE_SEQUENCE = "CODE_SEQ";

    public static final String EXPERIMENT_CODE_SEQUENCE = "EXPERIMENT_CODE_SEQ";

    public static final String SAMPLE_CODE_SEQUENCE = "SAMPLE_CODE_SEQ";

    public static final String AUTHORIZATION_GROUP_ID_SEQUENCE = "AUTHORIZATION_GROUP_ID_SEQ";

    public static final String AUTHORIZATION_GROUP_PERSON_ID_SEQUENCE =
            "AUTHORIZATION_GROUP_PERSON_ID_SEQ";

    public static final String CORE_PLUGIN_SEQUENCE = "CORE_PLUGIN_ID_SEQ";

    public static final String POST_REGISTRATION_DATASET_QUEUE_SEQUENCE =
            "POST_REGISTRATION_DATASET_QUEUE_ID_SEQ";

    public static final String ENTITY_OPERATIONS_LOG_SEQUENCE = "ENTITY_OPERATIONS_LOG_ID_SEQ";

    public static final String METAPROJECTS_SEQUENCE = "METAPROJECT_ID_SEQ";

    public static final String METAPROJECT_ASSIGNMENTS_SEQUENCE = "METAPROJECT_ASSIGNMENT_ID_SEQ";

    public static final String OPERATION_EXECUTIONS_SEQUENCE = "OPERATION_EXECUTIONS_ID_SEQ";

    public static final String CONTENT_COPY_SEQUENCE = "CONTENT_COPIES_ID_SEQ";
    
    public static final String SEMANTIC_ANNOTATIONS_SEQUENCE = "SEMANTIC_ANNOTATION_ID_SEQ";

    private SequenceNames()
    {
        // Can not be instantiated.
    }
}
