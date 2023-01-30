/*
 * Copyright 2007 ETH Zuerich, CISD
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
 * This class lists the tables that can be found in the database.
 * 
 * @author Christian Ribeaud
 */
public final class TableNames
{

    public static final String CONTROLLED_VOCABULARY_TABLE = "controlled_vocabularies";

    public static final String CONTROLLED_VOCABULARY_TERM_TABLE = "controlled_vocabulary_terms";

    public static final String CORE_PLUGINS_TABLE = "core_plugins";

    public static final String DATA_SET_RELATIONSHIPS_ALL_TABLE = "data_set_relationships_all";

    public static final String DATA_SET_RELATIONSHIPS_HISTORY_TABLE = "data_set_relationships_history";

    public static final String DATA_SET_RELATIONSHIPS_VIEW = "data_set_relationships";

    public static final String DATA_SET_TYPES_TABLE = "data_set_types";

    public static final String DATA_STORES_TABLE = "data_stores";

    public static final String EXTERNAL_DATA_MANAGEMENT_SYSTEMS_TABLE =
            "external_data_management_systems";

    public static final String DATA_STORE_SERVICES_TABLE = "data_store_services";

    public static final String DATA_STORE_SERVICES_DATASET_TYPES_TABLE =
            "DATA_STORE_SERVICE_DATA_SET_TYPES";

    public static final String DATA_VIEW = "data"; // view

    public static final String DELETED_DATA_VIEW = "data_deleted"; // view

    public static final String DATA_ALL_TABLE = "data_all";

    public static final String DATA_SET_PROPERTIES_TABLE = "data_set_properties";

    public static final String DATA_SET_PROPERTIES_HISTORY_TABLE = "data_set_properties_history";

    public static final String DATA_SET_HISTORY_VIEW = "data_set_history_view";

    public static final String DATA_SET_TYPE_PROPERTY_TYPE_TABLE = "data_set_type_property_types";

    public static final String DATA_TYPES_TABLE = "data_types";

    public static final String DATABASE_INSTANCES_TABLE = "database_instances";

    public static final String FILTERS_TABLE = "filters";

    public static final String GRID_CUSTOM_COLUMNS_TABLE = "grid_custom_columns";

    public static final String ATTACHMENT_CONTENT_TABLE = "attachment_contents";

    public static final String ATTACHMENTS_TABLE = "attachments";

    public static final String EXPERIMENT_PROPERTIES_TABLE = "experiment_properties";

    public static final String EXPERIMENT_PROPERTIES_HISTORY_TABLE =
            "experiment_properties_history";

    public static final String EXPERIMENT_HISTORY_VIEW = "experiment_history_view";

    public static final String EXPERIMENT_TYPE_PROPERTY_TYPE_TABLE =
            "experiment_type_property_types";

    public static final String EXPERIMENT_TYPES_TABLE = "experiment_types";

    public static final String EXPERIMENTS_VIEW = "experiments"; // view

    public static final String DELETED_EXPERIMENTS_VIEW = "experiments_deleted"; // view

    public static final String EXPERIMENTS_ALL_TABLE = "experiments_all";

    public static final String EXTERNAL_DATA_TABLE = "external_data";

    public static final String CONTENT_COPIES_TABLE = "content_copies";

    public static final String LINK_DATA_TABLE = "link_data";

    public static final String FILE_FORMAT_TYPES_TABLE = "file_format_types";

    public static final String SPACES_TABLE = "spaces";

    public static final String DELETIONS_TABLE = "deletions";

    public static final String LOCATOR_TYPES_TABLE = "locator_types";

    public static final String MATERIAL_BATCHES_TABLE = "material_batches";

    public static final String MATERIAL_PROPERTIES_TABLE = "material_properties";

    public static final String MATERIAL_PROPERTIES_HISTORY_TABLE = "material_properties_history";

    public static final String MATERIAL_TYPE_PROPERTY_TYPE_TABLE = "material_type_property_types";

    public static final String MATERIAL_TYPES_TABLE = "material_types";

    public static final String MATERIALS_TABLE = "materials";

    public static final String PERSONS_TABLE = "persons";

    public static final String PROJECTS_TABLE = "projects";

    public static final String PROPERTY_TYPES_TABLE = "property_types";

    public static final String ROLE_ASSIGNMENTS_TABLE = "role_assignments";

    public static final String SAMPLE_INPUTS_TABLE = "sample_inputs";

    public static final String SAMPLE_MATERIAL_BATCHES_TABLE = "sample_material_batches";

    public static final String SAMPLE_PROPERTIES_TABLE = "sample_properties";

    public static final String SAMPLE_PROPERTIES_HISTORY_TABLE = "sample_properties_history";

    public static final String SAMPLE_HISTORY_VIEW = "sample_history_view";

    public static final String SAMPLE_TYPE_PROPERTY_TYPE_TABLE = "sample_type_property_types";

    public static final String SAMPLE_TYPES_TABLE = "sample_types";

    public static final String SAMPLES_VIEW = "samples"; // view

    public static final String DELETED_SAMPLES_VIEW = "samples_deleted"; // view

    public static final String SAMPLES_ALL_TABLE = "samples_all";

    public static final String SAMPLE_RELATIONSHIPS_VIEW = "sample_relationships";

    public static final String SAMPLE_RELATIONSHIPS_ALL_TABLE = "sample_relationships_all";

    public static final String RELATIONSHIP_TYPES_TABLE = "relationship_types";

    public static final String EVENTS_TABLE = "events";

    public static final String EVENTS_SEARCH_TABLE = "events_search";

    public static final String AUTHORIZATION_GROUPS_TABLE = "authorization_groups";

    public static final String AUTHORIZATION_GROUP_PERSONS_TABLE = "authorization_group_persons";

    public static final String QUERIES_TABLE = "queries";

    public static final String SCRIPTS_TABLE = "scripts";

    public static final String POST_REGISTRATION_DATASET_QUEUE_TABLE =
            "post_registration_dataset_queue";

    public static final String ENTITY_OPERATIONS_LOG_TABLE = "entity_operations_log";

    public static final String METAPROJECTS_TABLE = "metaprojects";

    public static final String METAPROJECT_ASSIGNMENTS_VIEW = "metaproject_assignments";

    public static final String METAPROJECT_ASSIGNMENTS_ALL_TABLE = "metaproject_assignments_all";

    public static final String OPERATION_EXECUTIONS_TABLE = "operation_executions";
    
    public static final String SEMANTIC_ANNOTATIONS_TABLE = "semantic_annotations";

    private TableNames()
    {
        // This class can not be instantiated.
    }

}
