/*

 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

/**
 * Enum for all generic {@link IDisplayTypeIDGenerator}s.
 * 
 * @author Franz-Josef Elmer
 */
public enum DisplayTypeIDGenerator implements IDisplayTypeIDGenerator
{
    ATTACHMENT_BROWSER_GRID("attachment-browser-grid"),

    ENTITY_BROWSER_GRID("entity-browser-grid"),

    DATA_SET_DETAILS_GRID("data-set-details-grid"),

    EXPERIMENT_DETAILS_GRID("experiment-details-grid"),

    METAPROJECT_DETAILS_GRID("metaproject-details-grid"),

    SAMPLE_DETAILS_GRID("sample-details-grid"),

    TYPE_BROWSER_GRID("type-browser-grid"),

    SEARCH_RESULT_GRID("search-result-grid"),

    DATA_SET_SEARCH_RESULT_GRID("data-set-search-result-grid"),

    SAMPLE_SEARCH_RESULT_GRID("sample-search-result-grid"),

    RELATED_DATA_SET_GRID("related-data-set-grid"),

    DATA_SET_REPORTING_GRID("data-set-reporting-grid"),

    PROJECT_BROWSER_GRID("project-browser-grid"),

    DELETION_BROWSER_GRID("deletion-browser-grid"),

    FILTER_BROWSER_GRID("filter-browser-grid"),

    CUSTOM_GRID_COLUMN_GRID("custom-grid-column-browser-grid"),

    AUTHORIZATION_GROUP_BROWSER_GRID("authorization-group-browser-grid"),

    PERSON_BROWSER_GRID("person-browser-grid"),

    PLUGIN_TASKS_BROWSER_GRID("plugin-tasks-browser-grid"),

    SPACES_BROWSER_GRID("space-browser-grid"),

    SCRIPTS_BROWSER_GRID("script-browser-grid"),

    ROLE_ASSIGNMENT_BROWSER_GRID("role-assignment-browser-grid"),

    PROPERTY_TYPE_BROWSER_GRID("property-type-browser-grid"),

    ENTITY_HISTORY_BROWSER_GRID("entity-property-history-browser-grid"),

    PROPERTY_TYPE_ASSIGNMENT_BROWSER_GRID("property-type-assignment-browser-grid"),

    VOCABULARY_BROWSER_GRID("vocabulary-browser-grid"),

    VOCABULARY_TERMS_GRID("vocabulary-terms-grid"),

    FILE_FORMAT_TYPE_BROWSER_GRID("file-format-type-browser-grid"),

    SAMPLE_TYPE_BROWSER("sample-type-browser"),

    METAPROJECT_CHOOSER_GRID("metaproject-chooser-grid"),

    // -------------- Sections

    ATTACHMENT_SECTION("attachment-section"),

    EXPERIMENT_SAMPLES_SECTION("experiment-sample-section"),

    CONTAINER_SAMPLES_SECTION("container-sample-section"),

    DERIVED_SAMPLES_SECTION("derived-samples-section"),

    PARENT_SAMPLES_SECTION("parent-samples-section"),

    EXPERIMENTS_SECTION("experiments-section"),

    SAMPLES_SECTION("samples-section"),

    DATA_SETS_SECTION("data-sets-section"),

    MATERIALS_SECTION("materials-section"),

    ENTITY_HISTORY_SECTION("entity-history-section"),

    DATA_SET_PARENTS_SECTION("data-set-parents-section"),

    DATA_SET_CHILDREN_SECTION("data-set-children-section"),

    DATA_SET_CONTAINED_SECTION("data-set-contained-section"),

    DATA_SET_DATA_SECTION("data-set-data-section"),

    MODULE_SECTION("module-section"),

    WEBAPP_SECTION("webapp-section"),

    // -------------- Viewers (detail view)

    GENERIC_DATASET_VIEWER("generic_dataset_viewer"),

    GENERIC_EXPERIMENT_VIEWER("generic_experiment_viewer"),

    GENERIC_SAMPLE_VIEWER("generic_sample_viewer"),

    GENERIC_MATERIAL_VIEWER("generic_material_viewer"),

    ;

    private final String genericNameOrPrefix;

    private DisplayTypeIDGenerator(String genericNameOrPrefix)
    {
        this.genericNameOrPrefix = genericNameOrPrefix;
    }

    @Override
    public String createID()
    {
        return genericNameOrPrefix;
    }

    @Override
    public String createID(String suffix)
    {
        return createID() + suffix;
    }
}
