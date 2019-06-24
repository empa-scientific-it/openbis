/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.JDBCSQLExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.COMMA;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.DELETE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FROM;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.INSERT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.INTO;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.VALUES;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHERE;

public class DBTestHelper
{

    public static final String ID_DELIMITER = "/";

    public static final String CONTAINER_DELIMITER = ":";

    public static final long DATA_TYPE_ID = 1L;

    public static final long USER_ID = 2L;

    public static final long SAMPLE_ID1 = 1001L;

    public static final long SAMPLE_ID2 = 1002L;

    public static final long SAMPLE_ID3 = 1003L;

    public static final long SPACE_ID1 = 10000L;

    public static final long SPACE_ID2 = 10001L;

    public static final long PROJECT_ID = 10002L;

    public static final long EXPERIMENT_ID = 10003L;

    public static final long EXPERIMENT_TYPE_ID = 10004L;

    public static final long SAMPLE_PROPERTY_ID2 = 1002L;

    public static final long SAMPLE_TYPE_PROPERTY_TYPE_ID2 = 1002L;

    public static final long SAMPLE_PROPERTY_TYPE_ID2 = 1002L;

    public static final long SAMPLE_TYPE_PROPERTY_TYPE_ORDINAL2 = 2L;

    public static final long SAMPLE_TYPE_ID2 = 1002L;

    public static final String SAMPLE_PROPERTY_CODE2 = "TEST.LONG";

    public static final String DEFAULT_PERM_ID = "20190612105000000-0";

    public static final String PERM_ID1 = "20190612105000000-1001";

    public static final String PERM_ID2 = "20190612105000001-1001";

    public static final String PERM_ID3 = "20190612105000000-1003";

    public static final String DEFAULT_CODE = "DEFAULT_UNIQUE_CODE";

    public static final String PROJECT_CODE = "PROJECT_CODE";

    public static final String SPACE_CODE1 = "MY_SPACE_UNIQUE_CODE1";

    public static final String SPACE_CODE2 = "MY_SPACE_UNIQUE_CODE2";

    public static final String CODE1 = "MY_UNIQUE_CODE1";

    public static final String CODE2 = "ANOTHER_UNIQUE_CODE2";

    public static final String CODE3 = "ANOTHER_UNIQUE_CODE3";

    public static final int DEFAULT_VERSION = 10;

    public static final int VERSION1 = 101;

    public static final int VERSION2 = 102;

    public static final int VERSION3 = 103;

    public static final Date REGISTRATION_DATE1 = new Date(119, Calendar.JUNE, 11, 10, 50, 0);

    public static final Date REGISTRATION_DATE2 = new Date(119, Calendar.JUNE, 12, 10, 50, 0);

    public static final Date REGISTRATION_DATE3 = new Date(119, Calendar.JUNE, 13, 10, 50, 0);

    public static final String REGISTRATION_DATE_STRING1 = "2019-06-11 10:50:00 +0200";

    public static final String REGISTRATION_DATE_STRING2 = "2019-06-12 10:50:00 +0200";

    public static final String REGISTRATION_DATE_STRING3 = "2019-06-13 10:50:00 +0200";

    public static final Date MODIFICATION_DATE1 = new Date(118, Calendar.JUNE, 11, 10, 50, 0);

    public static final Date MODIFICATION_DATE2 = new Date(118, Calendar.JUNE, 12, 10, 50, 0);

    public static final Date MODIFICATION_DATE3 = new Date(118, Calendar.JUNE, 13, 10, 50, 0);

    public static final String MODIFICATION_DATE_STRING1 = "2018-06-11 10:50:00 +0200";

    public static final String MODIFICATION_DATE_STRING2 = "2018-06-12 10:50:00 +0200";

    public static final String MODIFICATION_DATE_STRING3 = "2018-06-13 10:50:00 +0200";

    public static final Date DEFAULT_DATE = new Date(119, Calendar.JUNE, 10, 10, 50, 0);

    private JDBCSQLExecutor sqlExecutor;

    private Connection connection;

    public ISQLExecutor getSqlExecutor()
    {
        return sqlExecutor;
    }

    public void setUp() throws Exception
    {
        initConnection(false);

        populateDB();
    }

    /**
     * Initializes {@link #connection} and {@link #sqlExecutor}.
     *
     * @throws SQLException if an SQL exception occurs.
     */
    private void initConnection(final boolean autocommit) throws SQLException
    {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/openbis_dev", "postgres", "");
        connection.setAutoCommit(autocommit);
        sqlExecutor = new JDBCSQLExecutor(connection);
    }

    private void populateDB() throws SQLException
    {
        try
        {
            createSpaces();
            createProject();
            createExperimentType();
            createExperiment();
            createSamples();
            createProperties();

            connection.commit();
        } catch (final Exception e)
        {
            connection.rollback();
            throw e;
        }
    }

    private void createProperties()
    {
        createSampleProperty(SAMPLE_ID2, SAMPLE_PROPERTY_ID2, SAMPLE_TYPE_ID2, SAMPLE_TYPE_PROPERTY_TYPE_ID2,
                SAMPLE_PROPERTY_TYPE_ID2, SAMPLE_PROPERTY_CODE2, String.valueOf(101L));
    }

    private void createSampleProperty(final long sampleId, final long samplePropertyId, final long sampleTypeId,
            final long sampleTypePropertyTypeId, final long propertyTypeId, final String code, final String value)
    {
        final Map<String, Object> sampleTypesValues = new HashMap<>();
        sampleTypesValues.put(ColumnNames.ID_COLUMN, sampleTypeId);
        sampleTypesValues.put(ColumnNames.CODE_COLUMN, code);
        insertRecord(TableNames.SAMPLE_TYPES_TABLE, sampleTypesValues);

        final Map<String, Object> propertyTypesValues = new HashMap<>();
        propertyTypesValues.put(ColumnNames.ID_COLUMN, propertyTypeId);
        propertyTypesValues.put(ColumnNames.CODE_COLUMN, code);
        propertyTypesValues.put(ColumnNames.LABEL_COLUMN, code);
        propertyTypesValues.put(ColumnNames.DESCRIPTION_COLUMN, code);
        propertyTypesValues.put(ColumnNames.DATA_TYPE_COLUMN, DATA_TYPE_ID);
        propertyTypesValues.put(ColumnNames.PERSON_REGISTERER_COLUMN, USER_ID);
        insertRecord(TableNames.PROPERTY_TYPES_TABLE, propertyTypesValues);

        final Map<String, Object> stptValues = new HashMap<>();
        stptValues.put(ColumnNames.ID_COLUMN, sampleTypePropertyTypeId);
        stptValues.put(ColumnNames.PROPERTY_TYPE_COLUMN, propertyTypeId);
        stptValues.put(ColumnNames.SAMPLE_TYPE_COLUMN, sampleTypeId);
        stptValues.put(ColumnNames.PERSON_REGISTERER_COLUMN, USER_ID);
        stptValues.put(ColumnNames.ORDINAL_COLUMN, SAMPLE_TYPE_PROPERTY_TYPE_ORDINAL2);
        insertRecord(TableNames.SAMPLE_TYPE_PROPERTY_TYPE_TABLE, stptValues);

        final Map<String, Object> samplePropertiesValues = new HashMap<>();
        samplePropertiesValues.put(ColumnNames.ID_COLUMN, samplePropertyId);
        samplePropertiesValues.put(ColumnNames.SAMPLE_COLUMN, sampleId);
        samplePropertiesValues.put(ColumnNames.SAMPLE_TYPE_PROPERTY_TYPE_COLUMN, sampleTypePropertyTypeId);
        samplePropertiesValues.put(ColumnNames.VALUE_COLUMN, value);
        samplePropertiesValues.put(ColumnNames.PERSON_REGISTERER_COLUMN, USER_ID);
        samplePropertiesValues.put(ColumnNames.PERSON_AUTHOR_COLUMN, USER_ID);
        insertRecord(TableNames.SAMPLE_PROPERTIES_TABLE, samplePropertiesValues);
    }

    private void createSamples()
    {
        final Map<String, Object> valuesMap1 = getDefaultValuesMap();
        valuesMap1.put(ColumnNames.ID_COLUMN, SAMPLE_ID1);
        valuesMap1.put(ColumnNames.PERM_ID_COLUMN, PERM_ID1);
        valuesMap1.put(ColumnNames.VERSION_COLUMN, VERSION1);
        valuesMap1.put(ColumnNames.CODE_COLUMN, CODE1);
        valuesMap1.put(ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, REGISTRATION_DATE1);
        valuesMap1.put(ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, MODIFICATION_DATE1);
        valuesMap1.put(ColumnNames.SPACE_COLUMN, SPACE_ID1);
        insertRecord(TableNames.SAMPLES_ALL_TABLE, valuesMap1);

        final Map<String, Object> valuesMap2 = getDefaultValuesMap();
        valuesMap2.put(ColumnNames.PERM_ID_COLUMN, PERM_ID2);
        valuesMap2.put(ColumnNames.ID_COLUMN, SAMPLE_ID2);
        valuesMap2.put(ColumnNames.VERSION_COLUMN, VERSION2);
        valuesMap2.put(ColumnNames.CODE_COLUMN, CODE2);
        valuesMap2.put(ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, REGISTRATION_DATE2);
        valuesMap2.put(ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, MODIFICATION_DATE2);
        valuesMap2.put(ColumnNames.SPACE_COLUMN, SPACE_ID2);
        valuesMap2.put(ColumnNames.PROJECT_COLUMN, PROJECT_ID);
        valuesMap2.put(ColumnNames.PART_OF_SAMPLE_COLUMN, SAMPLE_ID1);
        insertRecord(TableNames.SAMPLES_ALL_TABLE, valuesMap2);

        final Map<String, Object> valuesMap3 = getDefaultValuesMap();
        valuesMap3.put(ColumnNames.PERM_ID_COLUMN, PERM_ID3);
        valuesMap3.put(ColumnNames.ID_COLUMN, SAMPLE_ID3);
        valuesMap3.put(ColumnNames.VERSION_COLUMN, VERSION3);
        valuesMap3.put(ColumnNames.CODE_COLUMN, CODE3);
        valuesMap3.put(ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, MODIFICATION_DATE3);
        valuesMap3.put(ColumnNames.PROJECT_COLUMN, PROJECT_ID);
        valuesMap3.put(ColumnNames.EXPERIMENT_COLUMN, EXPERIMENT_ID);
        valuesMap3.put(ColumnNames.PART_OF_SAMPLE_COLUMN, SAMPLE_ID1);
        insertRecord(TableNames.SAMPLES_ALL_TABLE, valuesMap3);
    }

    private void createSpaces()
    {
        final Map<String, Object> valuesMap1 = new HashMap<>();
        valuesMap1.put(ColumnNames.ID_COLUMN, SPACE_ID1);
        valuesMap1.put(ColumnNames.CODE_COLUMN, SPACE_CODE1);
        valuesMap1.put(ColumnNames.DESCRIPTION_COLUMN, null);
        valuesMap1.put(ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, DEFAULT_DATE);
        valuesMap1.put(ColumnNames.PERSON_REGISTERER_COLUMN, USER_ID);
        valuesMap1.put(ColumnNames.FROZEN_COLUMN, false);
        valuesMap1.put(ColumnNames.FROZEN_FOR_PROJECT_COLUMN, false);
        valuesMap1.put(ColumnNames.FROZEN_FOR_SAMPLE_COLUMN, false);
        insertRecord(TableNames.SPACES_TABLE, valuesMap1);

        final Map<String, Object> valuesMap2 = new HashMap<>();
        valuesMap2.put(ColumnNames.ID_COLUMN, SPACE_ID2);
        valuesMap2.put(ColumnNames.CODE_COLUMN, SPACE_CODE2);
        valuesMap2.put(ColumnNames.DESCRIPTION_COLUMN, null);
        valuesMap2.put(ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, DEFAULT_DATE);
        valuesMap2.put(ColumnNames.PERSON_REGISTERER_COLUMN, USER_ID);
        valuesMap2.put(ColumnNames.FROZEN_COLUMN, false);
        valuesMap2.put(ColumnNames.FROZEN_FOR_PROJECT_COLUMN, false);
        valuesMap2.put(ColumnNames.FROZEN_FOR_SAMPLE_COLUMN, false);
        insertRecord(TableNames.SPACES_TABLE, valuesMap2);
    }

    private void createProject()
    {
        final Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(ColumnNames.ID_COLUMN, PROJECT_ID);
        valuesMap.put(ColumnNames.PERM_ID_COLUMN, "20190301152050019-11");
        valuesMap.put(ColumnNames.CODE_COLUMN, PROJECT_CODE);
        valuesMap.put(ColumnNames.SPACE_COLUMN, SPACE_ID1);
        valuesMap.put(ColumnNames.PERSON_LEADER_COLUMN, null);
        valuesMap.put(ColumnNames.DESCRIPTION_COLUMN, null);
        valuesMap.put(ColumnNames.PERSON_REGISTERER_COLUMN, USER_ID);
        valuesMap.put(ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, DEFAULT_DATE);
        valuesMap.put(ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, DEFAULT_DATE);
        valuesMap.put(ColumnNames.PERSON_MODIFIER_COLUMN, null);
        valuesMap.put(ColumnNames.VERSION_COLUMN, DEFAULT_VERSION);
        valuesMap.put(ColumnNames.FROZEN_COLUMN, false);
        valuesMap.put(ColumnNames.FROZEN_FOR_EXPERIMENT_COLUMN, false);
        valuesMap.put(ColumnNames.FROZEN_FOR_SAMPLE_COLUMN, false);
        valuesMap.put(ColumnNames.SPACE_FROZEN_COLUMN, false);
        insertRecord(TableNames.PROJECTS_TABLE, valuesMap);
    }

    private void createExperiment()
    {
        final Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(ColumnNames.ID_COLUMN, EXPERIMENT_ID);
        valuesMap.put(ColumnNames.PERM_ID_COLUMN, DEFAULT_PERM_ID);
        valuesMap.put(ColumnNames.CODE_COLUMN, DEFAULT_CODE);
        valuesMap.put(ColumnNames.EXPERIMENT_TYPE_COLUMN, EXPERIMENT_TYPE_ID);
        valuesMap.put(ColumnNames.PERSON_REGISTERER_COLUMN, USER_ID);
        valuesMap.put(ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, DEFAULT_DATE);
        valuesMap.put(ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, DEFAULT_DATE);
        valuesMap.put(ColumnNames.PROJECT_COLUMN, PROJECT_ID);
        valuesMap.put(ColumnNames.DELETION_COLUMN, null);
        valuesMap.put(ColumnNames.ORIGINAL_DELETION_COLUMN, null);
        valuesMap.put(ColumnNames.IS_PUBLIC, false);
        valuesMap.put(ColumnNames.PERSON_MODIFIER_COLUMN, null);
        valuesMap.put(ColumnNames.VERSION_COLUMN, DEFAULT_VERSION);
        valuesMap.put(ColumnNames.FROZEN_COLUMN, false);
        valuesMap.put(ColumnNames.FROZEN_FOR_SAMPLE_COLUMN, false);
        valuesMap.put(ColumnNames.FROZEN_FOR_DATA_SET_COLUMN, false);
        valuesMap.put(ColumnNames.PROJECT_FROZEN_COLUMN, false);
        insertRecord(TableNames.EXPERIMENTS_ALL_TABLE, valuesMap);
    }

    private void createExperimentType()
    {
        final Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(ColumnNames.ID_COLUMN, EXPERIMENT_TYPE_ID);
        valuesMap.put(ColumnNames.CODE_COLUMN, DEFAULT_CODE);
        valuesMap.put(ColumnNames.DESCRIPTION_COLUMN, null);
        valuesMap.put(ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, DEFAULT_DATE);
        valuesMap.put(ColumnNames.VALIDATION_SCRIPT_ID_COLUMN, null);
        insertRecord(TableNames.EXPERIMENT_TYPES_TABLE, valuesMap);
    }

    /**
     * Creates a map what contains default values for an object to be stored in DB.
     *
     * @return a map that represents an object to be stored in the DB with default values.
     */
    private Map<String, Object> getDefaultValuesMap()
    {
        final Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(ColumnNames.CODE_COLUMN, DEFAULT_CODE);
        valuesMap.put(ColumnNames.EXPERIMENT_COLUMN, null);
        valuesMap.put(ColumnNames.SAMPLE_TYPE_COLUMN, 1);
        valuesMap.put(ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, DEFAULT_DATE);
        valuesMap.put(ColumnNames.PERSON_REGISTERER_COLUMN, 1);
        valuesMap.put(ColumnNames.DELETION_COLUMN, null);
        valuesMap.put(ColumnNames.ORIGINAL_DELETION_COLUMN, null);
        valuesMap.put(ColumnNames.SPACE_COLUMN, null);
        valuesMap.put(ColumnNames.PART_OF_SAMPLE_COLUMN, null);
        valuesMap.put(ColumnNames.PERSON_MODIFIER_COLUMN, null);
        valuesMap.put(ColumnNames.VERSION_COLUMN, 0);
        valuesMap.put(ColumnNames.PROJECT_COLUMN, null);
        valuesMap.put(ColumnNames.FROZEN_COLUMN, false);
        valuesMap.put(ColumnNames.FROZEN_FOR_COMPONENT_COLUMN, false);
        valuesMap.put(ColumnNames.FROZEN_FOR_CHILDREN_COLUMN, false);
        valuesMap.put(ColumnNames.FROZEN_FOR_PARENTS_COLUMN, false);
        valuesMap.put(ColumnNames.FROZEN_FOR_DATA_SET_COLUMN, false);
        valuesMap.put(ColumnNames.SPACE_FROZEN_COLUMN, false);
        valuesMap.put(ColumnNames.PROJECT_FROZEN_COLUMN, false);
        valuesMap.put(ColumnNames.EXPERIMENT_FROZEN_COLUMN, false);
        valuesMap.put(ColumnNames.CONTAINER_FROZEN_COLUMN, false);
        return valuesMap;
    }

    private void insertRecord(final String tableName, final Map<String, Object> valuesMap)
    {
        final StringBuilder columnNames = new StringBuilder();
        final StringBuilder questionMarks = new StringBuilder();
        final List<Object> values = new ArrayList<>(valuesMap.size());

        final AtomicBoolean first = new AtomicBoolean(true);
        valuesMap.forEach((key, value) -> {
            if (first.get())
            {
                first.set(false);
            } else {
                columnNames.append(COMMA).append(SP);
                questionMarks.append(COMMA).append(SP);
            }

            columnNames.append(key);
            questionMarks.append(QU);
            values.add(value);
        });

        sqlExecutor.executeUpdate(INSERT + SP + INTO + SP + tableName + NL +
                LP + columnNames + RP + NL +
                VALUES + SP + LP + questionMarks + RP, values);
    }

    public void cleanDB() throws Exception
    {
        try
        {
            connection.setAutoCommit(true);

            deleteRecord(TableNames.PROPERTY_TYPES_TABLE, ColumnNames.ID_COLUMN, SAMPLE_PROPERTY_TYPE_ID2);
            deleteRecord(TableNames.SAMPLE_PROPERTIES_TABLE, ColumnNames.ID_COLUMN, SAMPLE_PROPERTY_ID2);
            deleteRecord(TableNames.SAMPLE_TYPE_PROPERTY_TYPE_TABLE, ColumnNames.ID_COLUMN, SAMPLE_TYPE_PROPERTY_TYPE_ID2);
            deleteRecord(TableNames.SAMPLE_TYPES_TABLE, ColumnNames.ID_COLUMN, SAMPLE_TYPE_ID2);

            deleteRecord(TableNames.SAMPLES_ALL_TABLE, ColumnNames.ID_COLUMN, SAMPLE_ID3);
            deleteRecord(TableNames.SAMPLES_ALL_TABLE, ColumnNames.ID_COLUMN, SAMPLE_ID2);
            deleteRecord(TableNames.SAMPLES_ALL_TABLE, ColumnNames.ID_COLUMN, SAMPLE_ID1);

            deleteRecord(TableNames.EXPERIMENTS_ALL_TABLE, ColumnNames.ID_COLUMN, EXPERIMENT_ID);
            deleteRecord(TableNames.EXPERIMENT_TYPES_TABLE, ColumnNames.ID_COLUMN, EXPERIMENT_TYPE_ID);
            deleteRecord(TableNames.PROJECTS_TABLE, ColumnNames.ID_COLUMN, PROJECT_ID);
            deleteRecord(TableNames.SPACES_TABLE, ColumnNames.ID_COLUMN, SPACE_ID1);
            deleteRecord(TableNames.SPACES_TABLE, ColumnNames.ID_COLUMN, SPACE_ID2);
        } finally
        {
            closeConnection();
        }
    }

    public void resetConnection() throws SQLException
    {
        closeConnection();

        // A different connection should be used to be able to clean up even when the transaction is set for rollback.
        initConnection(true);
    }

    private void closeConnection() throws SQLException
    {
        if (connection != null)
        {
            connection.close();
        }
    }

    private void deleteRecord(final String tableName, final String key, final Object value)
    {
        sqlExecutor.executeUpdate(DELETE + SP + FROM + SP + tableName + NL +
                WHERE + SP + key + EQ + QU, Collections.singletonList(value));
    }

}
