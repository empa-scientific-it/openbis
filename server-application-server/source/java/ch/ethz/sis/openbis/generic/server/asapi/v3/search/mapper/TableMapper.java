/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes;

import java.util.*;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

/**
 * Extension of enum {@link EntityKind} to contain extra information about tables related to the entities which can
 * have parent-child relationships.<p/>
 * This enum also contains table information for non-entity search criteria (not related to MATERIAL, EXPERIMENT, SAMPLE, DATA_SET).
 *
 * @author Viktor Kovtun
 */
public enum TableMapper
{

    MATERIAL(MATERIALS_TABLE, MATERIAL_TYPE_COLUMN, PROPERTY_TYPES_TABLE, DATA_TYPE_COLUMN, MATERIAL_TYPES_TABLE,
            MATERIAL_TYPE_PROPERTY_TYPE_TABLE, MATERIAL_TYPE_COLUMN, PROPERTY_TYPE_COLUMN, MATERIAL_PROPERTIES_TABLE,
            MATERIAL_COLUMN, MATERIAL_TYPE_PROPERTY_TYPE_COLUMN, null, null, null, DATA_VIEW, MATERIAL_COLUMN,
            MATERIAL_COLUMN, EntityKind.MATERIAL, true, false),

    MATERIAL_TYPE(MATERIAL_TYPES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, EntityKind.MATERIAL, false, false),

    EXPERIMENT(EXPERIMENTS_VIEW, EXPERIMENT_TYPE_COLUMN, PROPERTY_TYPES_TABLE, DATA_TYPE_COLUMN, EXPERIMENT_TYPES_TABLE,
            EXPERIMENT_TYPE_PROPERTY_TYPE_TABLE, EXPERIMENT_TYPE_COLUMN, PROPERTY_TYPE_COLUMN,
            EXPERIMENT_PROPERTIES_TABLE, EXPERIMENT_COLUMN, EXPERIMENT_TYPE_PROPERTY_TYPE_COLUMN, null, null, null,
            DATA_VIEW, EXPERIMENT_COLUMN, EXPERIMENT_COLUMN, EntityKind.EXPERIMENT, true, true),

    EXPERIMENT_TYPE(EXPERIMENT_TYPES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, EntityKind.EXPERIMENT, false, false),

    SAMPLE(SAMPLES_VIEW, SAMPLE_TYPE_COLUMN, PROPERTY_TYPES_TABLE, DATA_TYPE_COLUMN, SAMPLE_TYPES_TABLE,
            SAMPLE_TYPE_PROPERTY_TYPE_TABLE, SAMPLE_TYPE_COLUMN, PROPERTY_TYPE_COLUMN, SAMPLE_PROPERTIES_TABLE,
            SAMPLE_COLUMN, SAMPLE_TYPE_PROPERTY_TYPE_COLUMN, SAMPLE_RELATIONSHIPS_VIEW, PARENT_SAMPLE_COLUMN,
            CHILD_SAMPLE_COLUMN, DATA_VIEW, SAMPLE_COLUMN, SAMPLE_COLUMN, EntityKind.SAMPLE, true, true),

    SAMPLE_TYPE(SAMPLE_TYPES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, EntityKind.SAMPLE, false, false),

    DATA_SET(DATA_VIEW, DATA_SET_TYPE_COLUMN, PROPERTY_TYPES_TABLE, DATA_TYPE_COLUMN, DATA_SET_TYPES_TABLE,
            DATA_SET_TYPE_PROPERTY_TYPE_TABLE, DATA_SET_TYPE_COLUMN, PROPERTY_TYPE_COLUMN, DATA_SET_PROPERTIES_TABLE,
            DATA_SET_COLUMN, DATA_SET_TYPE_PROPERTY_TYPE_COLUMN, DATA_SET_RELATIONSHIPS_VIEW, DATA_PARENT_COLUMN,
            DATA_CHILD_COLUMN, DATA_VIEW, ID_COLUMN, DATA_ID_COLUMN, EntityKind.DATA_SET, true, true),

    DATA_SET_TYPE(DATA_SET_TYPES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, EntityKind.DATA_SET, false, false),

    PERSON(PERSONS_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, true, false),

    PROJECT(PROJECTS_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, true, true),

    SPACE(SPACES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, true, false),

    TAG(METAPROJECTS_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, false, false),

    SEMANTIC_ANNOTATION(SEMANTIC_ANNOTATIONS_TABLE, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, false, false),

    PROPERTY_TYPE(PROPERTY_TYPES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, true, false),

    SAMPLE_PROPERTY_ASSIGNMENT(SAMPLE_TYPE_PROPERTY_TYPE_TABLE, null, null, null, null, null, SAMPLE_TYPE_COLUMN, null,
            null, null, null, null, null, null, null, null, null, EntityKind.SAMPLE, true, false),

    EXPERIMENT_PROPERTY_ASSIGNMENT(EXPERIMENT_TYPE_PROPERTY_TYPE_TABLE, null, null, null, null, null, EXPERIMENT_TYPE_COLUMN, null,
            null, null, null, null, null, null, null, null, null, EntityKind.EXPERIMENT, true, false),

    DATA_SET_PROPERTY_ASSIGNMENT(DATA_SET_TYPE_PROPERTY_TYPE_TABLE, null, null, null, null, null, DATA_SET_TYPE_COLUMN, null,
            null, null, null, null, null, null, null, null, null, EntityKind.DATA_SET, true, false),

    MATERIAL_PROPERTY_ASSIGNMENT(MATERIAL_TYPE_PROPERTY_TYPE_TABLE, null, null, null, null, null, MATERIAL_TYPE_COLUMN, null,
            null, null, null, null, null, null, null, null, null, EntityKind.MATERIAL, true, false),

    CONTENT_COPIES(CONTENT_COPIES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, true, false),

    EXTERNAL_DATA(EXTERNAL_DATA_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, false, false),

    EXTERNAL_DMS(EXTERNAL_DATA_MANAGEMENT_SYSTEMS_TABLE, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, false, false),

    FILE_FORMAT_TYPES(FILE_FORMAT_TYPES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, false, false),

    LOCATOR_TYPES(LOCATOR_TYPES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, false, false),

    CONTROLLED_VOCABULARY_TERMS(CONTROLLED_VOCABULARY_TERM_TABLE, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, true, false),

    EVENT(EVENTS_SEARCH_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, true, false);

    static
    {
        initSampleFieldToSQLTypeMap();
        initSampleSQLTypeToFieldsMap();

        initSampleTypeFieldToSQLTypeMap();
        initSampleTypeSQLTypeToFieldsMap();

        initPersonFieldToSQLTypeMap();
        initPersonSQLTypeToFieldsMap();

        initExperimentFieldToSQLTypeMap();
        initExperimentSQLTypeToFieldsMap();

        initExperimentTypeFieldToSQLTypeMap();
        initExperimentTypeSQLTypeToFieldsMap();

        initProjectFieldToSQLTypeMap();
        initProjectSQLTypeToFieldsMap();

        initSpaceFieldToSQLTypeMap();
        initSpaceSQLTypeToFieldsMap();

        initDataSetFieldToSQLTypeMap();
        initDataSetSQLTypeToFieldsMap();

        initMaterialsFieldToSQLTypeMap();
        initMaterialsSQLTypeToFieldsMap();

        initDataSetTypeFieldToSQLTypeMap();
        initDataSetTypeSQLTypeToFieldsMap();
    }

    /*
     * Entities Table
     */
    private String entitiesTable;

    private String entitiesTableEntityTypeIdField;

    private String attributeTypesTableDataTypeIdField;

    /*
     * Common Attribute definition Tables
     */
    private String attributeTypesTable;

    /*
     * Entity Attribute definition Tables
     */
    private String entityTypesTable;

    private String entityTypesAttributeTypesTable;

    private String entityTypesAttributeTypesTableEntityTypeIdField; //saty

    private String entityTypesAttributeTypesTableAttributeTypeIdField; //prty

    /*
     * Entity Values Tables
     */
    private String valuesTable;

    private String valuesTableEntityIdField;

    private String valuesTableEntityTypeAttributeTypeIdField;

    /*
     * Relationships Tables
     */
    private String relationshipsTable;

    private String relationshipsTableParentIdField;

    private String relationshipsTableChildIdField;

    /*
     * Files tables
     */
    private String dataTable;

    private String dataTableEntityIdField;

    /*
     * Other
     */

    private String metaprojectAssignmentsEntityIdField;

    /**
     * Entity kind related to this mapper.
     */
    private EntityKind entityKind;

    private Map<String, PSQLTypes> fieldToSQLTypeMap = new HashMap<>();

    private Map<PSQLTypes, Set<String>> sqlTypeToFieldsMap = new HashMap<>();

    private final boolean withRegistrator;

    private final boolean withModifier;

    TableMapper(final String entitiesTable, final String entitiesTableEntityTypeIdField, final String attributeTypesTable,
            final String attributeTypesTableDataTypeIdField, final String entityTypesTable, final String entityTypesAttributeTypesTable,
            final String entityTypesAttributeTypesTableEntityTypeIdField, final String entityTypesAttributeTypesTableAttributeTypeIdField,
            final String valuesTable, final String valuesTableEntityIdField, final String valuesTableEntityTypeAttributeTypeIdField,
            final String relationshipsTable, final String relationshipsTableParentIdField, final String relationshipsTableChildIdField,
            final String dataTable, final String dataTableEntityIdField,
            final String metaprojectAssignmentsEntityIdField, final EntityKind entityKind, final boolean withRegistrator,
            final boolean withModifier)
    {
        this.entitiesTable = entitiesTable;
        this.entitiesTableEntityTypeIdField = entitiesTableEntityTypeIdField;
        this.attributeTypesTable = attributeTypesTable;
        this.attributeTypesTableDataTypeIdField = attributeTypesTableDataTypeIdField;
        this.entityTypesTable = entityTypesTable;
        this.entityTypesAttributeTypesTable = entityTypesAttributeTypesTable;
        this.entityTypesAttributeTypesTableEntityTypeIdField = entityTypesAttributeTypesTableEntityTypeIdField;
        this.entityTypesAttributeTypesTableAttributeTypeIdField = entityTypesAttributeTypesTableAttributeTypeIdField;
        this.valuesTable = valuesTable;
        this.valuesTableEntityIdField = valuesTableEntityIdField;
        this.valuesTableEntityTypeAttributeTypeIdField = valuesTableEntityTypeAttributeTypeIdField;
        this.relationshipsTable = relationshipsTable;
        this.relationshipsTableParentIdField = relationshipsTableParentIdField;
        this.relationshipsTableChildIdField = relationshipsTableChildIdField;
        this.dataTable = dataTable;
        this.dataTableEntityIdField = dataTableEntityIdField;
        this.metaprojectAssignmentsEntityIdField = metaprojectAssignmentsEntityIdField;
        this.entityKind = entityKind;
        this.withRegistrator = withRegistrator;
        this.withModifier = withModifier;
    }

    public static TableMapper toEntityMapper(final EntityKind entityKind, final boolean isEntityType)
    {
        // By convention entity mapper values corresponding to types should end with "_TYPE"
        return TableMapper.valueOf(entityKind.name() + (isEntityType ? "_TYPE" : ""));
    }

    public String getEntitiesTable()
    {
        return entitiesTable;
    }

    public String getEntitiesTableEntityTypeIdField()
    {
        return entitiesTableEntityTypeIdField;
    }

    public String getAttributeTypesTable()
    {
        return attributeTypesTable;
    }

    public String getAttributeTypesTableDataTypeIdField()
    {
        return attributeTypesTableDataTypeIdField;
    }

    public String getEntityTypesTable()
    {
        return entityTypesTable;
    }

    public String getEntityTypesAttributeTypesTable()
    {
        return entityTypesAttributeTypesTable;
    }

    public String getEntityTypesAttributeTypesTableEntityTypeIdField()
    {
        return entityTypesAttributeTypesTableEntityTypeIdField;
    }

    public String getEntityTypesAttributeTypesTableAttributeTypeIdField()
    {
        return entityTypesAttributeTypesTableAttributeTypeIdField;
    }

    public String getValuesTable()
    {
        return valuesTable;
    }

    public String getValuesTableEntityIdField()
    {
        return valuesTableEntityIdField;
    }

    public String getValuesTableEntityTypeAttributeTypeIdField()
    {
        return valuesTableEntityTypeAttributeTypeIdField;
    }

    public String getRelationshipsTable()
    {
        return relationshipsTable;
    }

    public String getRelationshipsTableParentIdField()
    {
        return relationshipsTableParentIdField;
    }

    public String getRelationshipsTableChildIdField()
    {
        return relationshipsTableChildIdField;
    }

    public String getDataTable()
    {
        return dataTable;
    }

    public String getDataTableEntityIdField()
    {
        return dataTableEntityIdField;
    }

    public Map<String, PSQLTypes> getFieldToSQLTypeMap()
    {
        return fieldToSQLTypeMap;
    }

    public String getMetaprojectAssignmentsEntityIdField()
    {
        return metaprojectAssignmentsEntityIdField;
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public boolean hasRegistrator()
    {
        return withRegistrator;
    }

    public boolean hasModifier()
    {
        return withModifier;
    }

    private static void initSampleFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = SAMPLE.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(PERM_ID_COLUMN, PSQLTypes.VARCHAR);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
    }

    private static void initSampleTypeFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = SAMPLE_TYPE.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(MODIFICATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
    }

    private static void initExperimentFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = EXPERIMENT.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(PERM_ID_COLUMN, PSQLTypes.VARCHAR);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
    }

    private static void initExperimentTypeFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = EXPERIMENT_TYPE.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(MODIFICATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
    }

    private static void initPersonFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = PERSON.fieldToSQLTypeMap;
        fields.put(USER_COLUMN, PSQLTypes.VARCHAR);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
    }

    private static void initSampleSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = SAMPLE.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN, PERM_ID_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(REGISTRATION_TIMESTAMP_COLUMN)));
    }

    private static void initSampleTypeSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = SAMPLE_TYPE.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(MODIFICATION_TIMESTAMP_COLUMN)));
    }

    private static void initPersonSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = PERSON.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(USER_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(REGISTRATION_TIMESTAMP_COLUMN)));
    }

    private static void initExperimentSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = EXPERIMENT.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN, PERM_ID_COLUMN)));
    }

    private static void initExperimentTypeSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = SAMPLE_TYPE.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(MODIFICATION_TIMESTAMP_COLUMN)));
    }

    private static void initProjectFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = PROJECT.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
    }

    private static void initProjectSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = PROJECT.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN, DESCRIPTION_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(REGISTRATION_TIMESTAMP_COLUMN)));
    }

    private static void initSpaceFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = SPACE.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(DESCRIPTION_COLUMN, PSQLTypes.VARCHAR);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
    }

    private static void initSpaceSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = SPACE.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(REGISTRATION_TIMESTAMP_COLUMN)));
    }

    private static void initDataSetFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = DATA_SET.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
    }

    private static void initDataSetSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = DATA_SET.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(REGISTRATION_TIMESTAMP_COLUMN)));
    }

    private static void initMaterialsFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = MATERIAL.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
    }

    private static void initMaterialsSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = MATERIAL.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(REGISTRATION_TIMESTAMP_COLUMN)));
    }

    private static void initDataSetTypeFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = DATA_SET_TYPE.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(MODIFICATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
    }

    private static void initDataSetTypeSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = DATA_SET_TYPE.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(MODIFICATION_TIMESTAMP_COLUMN)));
    }

}
