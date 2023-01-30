/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.task;

import static ch.systemsx.cisd.openbis.generic.server.task.MaterialExternalDBSyncTask.INSERT_TIMESTAMP_SQL_KEY;
import static ch.systemsx.cisd.openbis.generic.server.task.MaterialExternalDBSyncTask.READ_TIMESTAMP_SQL_KEY;
import static ch.systemsx.cisd.openbis.generic.server.task.MaterialExternalDBSyncTask.UPDATE_TIMESTAMP_SQL_KEY;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.task.MaterialExternalDBSyncTask.MappingInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.VocabularyTermBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses =
{ MaterialExternalDBSyncTask.class, MappingInfo.class })
public class MaterialExternalDBSyncTaskTest extends AbstractFileSystemTestCase
{
    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private ICommonServerForInternalUse server;

    private DatabaseConfigurationContext dbConfigContext;

    private MaterialExternalDBSyncTask materialReportingTask;

    private String databaseName;

    private File mappingFile;

    private Properties properties;

    private ITimeProvider timeProvider;

    @BeforeMethod
    public void setUpMocks() throws Exception
    {
        context = new Mockery();
        server = context.mock(ICommonServerForInternalUse.class);
        timeProvider = context.mock(ITimeProvider.class);
        materialReportingTask = new MaterialExternalDBSyncTask(server, timeProvider);

        dbConfigContext = new DatabaseConfigurationContext();
        dbConfigContext.setDatabaseEngineCode("postgresql");
        dbConfigContext.setBasicDatabaseName("material_reporting_task");
        dbConfigContext.setDatabaseKind("test");
        dbConfigContext.setOwner(null);
        databaseName = dbConfigContext.getDatabaseName();
        dropTestDatabase();
        createTestDatabase();
        createTables(
                "create table timestamp (timestamp timestamp)",
                "create table report1 (id bigint, code varchar(20), description varchar(200), constraint pk_report1 primary key (code))",
                "create table report2 (code varchar(20), report1_code varchar(20), rank integer, greetings varchar(200), "
                        + "size double precision, organism varchar(100), material varchar(30), timestamp timestamp)",
                "alter table report2 add constraint r2_r1_fk foreign key (report1_code) references report1 (code)");
        dbConfigContext.closeConnections();
        mappingFile = new File(workingDirectory, "mapping-file.txt");
        properties = new Properties();
        properties.setProperty(READ_TIMESTAMP_SQL_KEY, "select timestamp from timestamp");
        properties.setProperty(UPDATE_TIMESTAMP_SQL_KEY, "update timestamp set timestamp = ?");
        properties.setProperty(INSERT_TIMESTAMP_SQL_KEY, "insert into timestamp values(?)");
        properties.setProperty("database-driver", "org.postgresql.Driver");
        properties.setProperty("database-url", "jdbc:postgresql://localhost/" + databaseName);
        properties.setProperty("database-username", "postgres");
        properties.setProperty(MaterialExternalDBSyncTask.MAPPING_FILE_KEY, mappingFile.getPath());
    }

    @AfterMethod
    public void tearDown() throws Exception
    {
        materialReportingTask.closeDatabaseConnections();
        dbConfigContext.closeConnections();
        context.assertIsSatisfied();
    }

    @Test
    public void testReadValidMappingFile() throws SQLException
    {
        FileUtilities.writeToFile(mappingFile, "# my mapping\n[ T1 :  TABLE1 , CODE ]\n\n"
                + "[T2: TABLE2, code]\n  P2 : Prop2   \n P1 :  PROP1  ");

        Map<String, MappingInfo> mapping =
                MaterialExternalDBSyncTask.readMappingFile(mappingFile.getPath());

        MappingInfo mappingInfo1 = mapping.get("T1");
        assertEquals("insert into table1 (code) values(?)", mappingInfo1.createInsertStatement());
        MappingInfo mappingInfo2 = mapping.get("T2");
        assertEquals("insert into table2 (code, prop2, prop1) values(?, ?, ?)",
                mappingInfo2.createInsertStatement());
        assertEquals(2, mapping.size());
    }

    @Test
    public void testReadMappingFileWithMissingInitialTableDefinition()
    {
        FileUtilities.writeToFile(mappingFile, "P2: prop2\nP1:prop1");

        try
        {
            MaterialExternalDBSyncTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 'P2: prop2': "
                    + "Missing first material type table definition of form "
                    + "'[<material type tode>: <table name>, <code column name>]'", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionMissingFinishingBracket()
    {
        FileUtilities.writeToFile(mappingFile, "[T1: TABLE, CODE\nP1: p1");

        try
        {
            MaterialExternalDBSyncTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T1: TABLE, CODE': Missing ']'", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionMissingColon()
    {
        FileUtilities.writeToFile(mappingFile, "[T1]\nP1: p1");

        try
        {
            MaterialExternalDBSyncTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T1]': 2 items separated by ':' expected.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionMissingCodeColumnName()
    {
        FileUtilities.writeToFile(mappingFile, "[T1: table]\nP1: p1");

        try
        {
            MaterialExternalDBSyncTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T1: table]': 2 items separated by ',' expected.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionEmptyMaterialTypeCode()
    {
        FileUtilities.writeToFile(mappingFile, "[ : table, code]\nP1: p1");

        try
        {
            MaterialExternalDBSyncTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[ : table, code]': Missing material type code.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionEmptyTableName()
    {
        FileUtilities.writeToFile(mappingFile, "[T : , code]\nP1: p1");

        try
        {
            MaterialExternalDBSyncTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T : , code]': Missing table name.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionCodeColumnName()
    {
        FileUtilities.writeToFile(mappingFile, "[T : t, ]\nP1: p1");

        try
        {
            MaterialExternalDBSyncTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T : t, ]': Missing code column name.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidPropertyMappingMissingColon()
    {
        FileUtilities.writeToFile(mappingFile, "[ T : t , c]\nP1");

        try
        {
            MaterialExternalDBSyncTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 2 "
                    + "'P1': 2 items separated by ':' expected.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidPropertyMappingMissungPropertyTypeCode()
    {
        FileUtilities.writeToFile(mappingFile, "[ T : t , c]\n : p");

        try
        {
            MaterialExternalDBSyncTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 2 "
                    + "' : p': Missing property type code.", ex.getMessage());
        }
    }

    @Test
    public void testDatabaseMetaDataMissingCodeColumn() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT1,C]");
        prepareListMaterialTypes("T1:A=REAL");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Missing column 'c' in table 'report1' of report database.",
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testDatabaseMetaDataMissingTable() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT3,C]");
        prepareListMaterialTypes("T1:A=REAL");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Missing table 'report3' in report database.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testDatabaseMetaDataCodeColumnOfWrongType() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT1,ID]");
        prepareListMaterialTypes("T1:A=REAL");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Column 'id' of table 'report1' is not of type VARCHAR.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testDatabaseMetaDataMissingPropertyColumn() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT2,CODE]\nP1:my_prop");
        prepareListMaterialTypes("T1:P1=REAL");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Missing column 'my_prop' in table 'report2' of report database.",
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testMappingFileWithUnknownMaterialType() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT2,CODE]\nP1:my_prop");
        prepareListMaterialTypes("T2:P1=REAL");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Mapping file refers to an unknown material type: T1", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testMappingFileWithUnknownPropertyType() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT2,CODE]\nP1:my_prop");
        prepareListMaterialTypes("T1:P2=REAL");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Mapping file refers to an unknown property type: P1", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testMappingFileWithNonmatchingDataTypeREAL() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT2,CODE]\nP1:rank");
        prepareListMaterialTypes("T1:P1=REAL");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Column 'rank' in table 'report2' of report database should be of "
                    + "a type which corresponds to REAL.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testMappingFileWithNonmatchingDataTypeINTEGER() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT2,CODE]\nP1:greetings");
        prepareListMaterialTypes("T1:P1=INTEGER");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Column 'greetings' in table 'report2' of report database should be of "
                    + "a type which corresponds to INTEGER.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testMappingFileWithNonmatchingDataTypeTIMESTAMP() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT2,CODE]\nP1:greetings");
        prepareListMaterialTypes("T1:P1=TIMESTAMP");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Column 'greetings' in table 'report2' of report database should be of "
                    + "a type which corresponds to TIMESTAMP.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testMappingFileWithNonmatchingDataTypeMATERIAL() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT2,CODE]\nP1:RANK");
        prepareListMaterialTypes("T1:P1=MATERIAL");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Column 'rank' in table 'report2' of report database should be of "
                    + "a type which corresponds to VARCHAR.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testMappingFileWithNonmatchingDataTypeCONTROLLEDVOCABULARY() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT2,CODE]\nP1:RANK");
        prepareListMaterialTypes("T1:P1=CONTROLLEDVOCABULARY");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Column 'rank' in table 'report2' of report database should be of "
                    + "a type which corresponds to VARCHAR.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testMappingFileWithNonmatchingDataTypeVARCHAR() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT2,CODE]\nP1:RANK");
        prepareListMaterialTypes("T1:P1=VARCHAR");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Column 'rank' in table 'report2' of report database should be of "
                    + "a type which corresponds to VARCHAR.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testTimestampReadingWriting() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "");
        prepareListMaterialTypes();
        properties.setProperty(INSERT_TIMESTAMP_SQL_KEY, "insert into b values(?)");

        try
        {
            materialReportingTask.setUp("", properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Couldn't save timestamp to report database. Property '"
                    + INSERT_TIMESTAMP_SQL_KEY + "' or '" + UPDATE_TIMESTAMP_SQL_KEY
                    + "' could be invalid.", ex.getMessage());
            assertEquals("PreparedStatementCallback; bad SQL grammar [insert into b values(?)]; "
                    + "nested exception is org.postgresql.util.PSQLException: "
                    + "ERROR: relation \"b\" does not exist\n" + "  Position: 13", ex.getCause()
                    .getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testTimestampReadingWritingForInvalidUpdateSqlStatement() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "");
        prepareListMaterialTypes();
        properties.setProperty(UPDATE_TIMESTAMP_SQL_KEY, "insert into b values(?)");

        try
        {
            materialReportingTask.setUp("", properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Couldn't save timestamp to report database. Property '"
                    + UPDATE_TIMESTAMP_SQL_KEY + "' could be invalid.", ex.getMessage());
            assertEquals("PreparedStatementCallback; bad SQL grammar [insert into b values(?)]; "
                    + "nested exception is org.postgresql.util.PSQLException: "
                    + "ERROR: relation \"b\" does not exist\n" + "  Position: 13", ex.getCause()
                    .getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testTimestampReadingWritingForInvalidReadSqlStatement() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "");
        prepareListMaterialTypes();
        properties.setProperty(READ_TIMESTAMP_SQL_KEY, "select blabla from timestamp");
        properties.setProperty(UPDATE_TIMESTAMP_SQL_KEY, "insert into timestamp values(?)");
        properties.remove(INSERT_TIMESTAMP_SQL_KEY);

        try
        {
            materialReportingTask.setUp("", properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Couldn't get timestamp from report database. Property '"
                    + READ_TIMESTAMP_SQL_KEY + "' could be invalid.", ex.getMessage());
            assertEquals("StatementCallback; bad SQL grammar [select blabla from timestamp]; "
                    + "nested exception is org.postgresql.util.PSQLException: "
                    + "ERROR: column \"blabla\" does not exist\n" + "  Position: 8", ex.getCause()
                    .getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testTimestampReadingWritingWithDifferentSchema() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "");
        prepareListMaterialTypes();
        prepareListMaterialTypes();
        properties.setProperty(READ_TIMESTAMP_SQL_KEY, "select max(timestamp) from timestamp");
        properties.setProperty(UPDATE_TIMESTAMP_SQL_KEY, "insert into timestamp values(?)");
        properties.remove(INSERT_TIMESTAMP_SQL_KEY);
        final RecordingMatcher<DetailedSearchCriteria> criteriaRecorder =
                new RecordingMatcher<DetailedSearchCriteria>();
        final Sequence timeSequence = context.sequence("time");
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(server).tryToAuthenticateAsSystem();
                    SessionContextDTO session = new SessionContextDTO();
                    session.setSessionToken(SESSION_TOKEN);
                    will(returnValue(session));

                    exactly(2).of(server).searchForMaterials(with(SESSION_TOKEN),
                            with(criteriaRecorder));

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(24L * 3600L * 1000L * 60L));
                    inSequence(timeSequence);

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(24L * 3600L * 1000L * 62L));
                    inSequence(timeSequence);
                }
            });

        materialReportingTask.setUp("", properties);
        materialReportingTask.execute();
        materialReportingTask.closeDatabaseConnections();
        materialReportingTask.setUp("", properties);
        materialReportingTask.execute();

        assertEquals("[{timestamp=1970-01-01 01:00:00.0}, {timestamp=1970-01-01 01:00:00.0}, "
                + "{timestamp=1970-03-02 01:00:00.0}, {timestamp=1970-03-02 01:00:00.0}, "
                + "{timestamp=1970-03-02 01:00:00.0}, {timestamp=1970-03-04 01:00:00.0}]",
                loadTable("timestamp", false).toString());
        List<DetailedSearchCriteria> recordedObjects = criteriaRecorder.getRecordedObjects();
        assertEquals("1970-01-01 01:00:00 +0100", recordedObjects.get(0).getCriteria().get(0)
                .getValue());
        assertEquals("1970-03-02 01:00:00 +0100", recordedObjects.get(1).getCriteria().get(0)
                .getValue());
        assertEquals(2, recordedObjects.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testInsert() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "# my mapping\n[T1:REPORT1,CODE]\n\n"
                + "[T2: REPORT2, code]\nM:MATERIAL\nS:size\nR1CODE: REPORT1_CODE\n"
                + "P2: GREETINGS\nP1:RANK\nORG:ORGANISM\n" + "T:timestamp");
        prepareListMaterialTypes("T1:D=VARCHAR",
                "T2:M=MATERIAL,S=REAL,R1CODE=VARCHAR,P2=VARCHAR,P1=INTEGER,ORG=CONTROLLEDVOCABULARY,T=TIMESTAMP");
        materialReportingTask.setUp("", properties);
        final Material m1 =
                new MaterialBuilder().code("M1").type("T1").property("P1", "42").getMaterial();
        MaterialBuilder mb2 = new MaterialBuilder().code("M2").type("T2");
        mb2.property("P1").type(DataTypeCode.INTEGER).value(42);
        mb2.property("S").type(DataTypeCode.REAL).value(1e7 + 0.5);
        mb2.property("ORG").type(DataTypeCode.CONTROLLEDVOCABULARY)
                .value(new VocabularyTermBuilder("FLY").getTerm());
        mb2.property("M").type(DataTypeCode.MATERIAL).value(m1);
        mb2.property("T").type(DataTypeCode.TIMESTAMP).value(new Date(24 * 3600L * 1000L * 33));
        final Material m2 = mb2.getMaterial();
        final Material m3 =
                new MaterialBuilder().code("M3").type("T2").property("P2", "hello")
                        .property("R1CODE", "M1").getMaterial();
        final RecordingMatcher<DetailedSearchCriteria> criteriaRecorder =
                new RecordingMatcher<DetailedSearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(server).tryToAuthenticateAsSystem();
                    SessionContextDTO session = new SessionContextDTO();
                    session.setSessionToken(SESSION_TOKEN);
                    will(returnValue(session));

                    one(server).searchForMaterials(with(SESSION_TOKEN), with(criteriaRecorder));
                    will(returnValue(Arrays.asList(m1, m2, m3)));

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(24L * 3600L * 1000L * 60L));
                }
            });

        materialReportingTask.execute();

        List<?> result = loadTable("report1");
        assertEquals("[{code=M1, description=null, id=null}]", result.toString());
        result = loadTable("report2");
        assertEquals(
                "[{code=M2, greetings=null, material=M1, organism=FLY, rank=42, report1_code=null, "
                        + "size=1.00000005E7, timestamp=1970-02-03 01:00:00.0}, "
                        + "{code=M3, greetings=hello, material=null, organism=null, rank=null, report1_code=M1, "
                        + "size=null, timestamp=null}]", result.toString());
        assertEquals("[{timestamp=1970-03-02 01:00:00.0}]", loadTable("timestamp", false)
                .toString());
        DetailedSearchCriterion detailedSearchCriterion =
                criteriaRecorder.recordedObject().getCriteria().get(0);
        assertEquals(CompareType.MORE_THAN_OR_EQUAL, detailedSearchCriterion.getType());
        assertEquals("MODIFICATION_DATE", detailedSearchCriterion.getField().getAttributeCode());
        assertEquals(DetailedSearchFieldKind.ATTRIBUTE, detailedSearchCriterion.getField()
                .getKind());
        assertEquals("1970-01-01 01:00:00 +0100", detailedSearchCriterion.getValue());
        context.assertIsSatisfied();
    }

    @Test
    public void testInsertUpdate() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT1,CODE]\n"
                + "[T2: REPORT2, code]\nM:MATERIAL\nS:size\nP1:RANK\nORG:ORGANISM\n"
                + "P2: GREETINGS\nT:timestamp");
        prepareListMaterialTypes("T1:D=VARCHAR",
                "T2:M=MATERIAL,S=REAL,P2=VARCHAR,P1=INTEGER,ORG=CONTROLLEDVOCABULARY,T=TIMESTAMP");
        materialReportingTask.setUp("", properties);
        final Material m1 =
                new MaterialBuilder().code("M1").type("T1").property("P1", "42").getMaterial();
        MaterialBuilder mb2 = new MaterialBuilder().code("M2").type("T2");
        mb2.property("P1").type(DataTypeCode.INTEGER).value(42);
        final Material m2 = mb2.getMaterial();
        final Material m3 =
                new MaterialBuilder().code("M3").type("T2").property("P2", "hello").getMaterial();
        MaterialBuilder mb2v2 = new MaterialBuilder().code("M2").type("T2");
        mb2v2.property("P1").type(DataTypeCode.INTEGER).value(137);
        mb2v2.property("ORG").type(DataTypeCode.CONTROLLEDVOCABULARY)
                .value(new VocabularyTermBuilder("TIGER").label("Tiger").getTerm());
        final Material m2v2 = mb2v2.property("P2", "blabla").getMaterial();
        final Material m4 =
                new MaterialBuilder().code("M4").type("T2").property("P2", "hi").getMaterial();
        final RecordingMatcher<DetailedSearchCriteria> criteriaRecorder =
                new RecordingMatcher<DetailedSearchCriteria>();
        final Sequence searchMaterialSequence = context.sequence("materials");
        final Sequence timeSequence = context.sequence("time");
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(server).tryToAuthenticateAsSystem();
                    SessionContextDTO session = new SessionContextDTO();
                    session.setSessionToken(SESSION_TOKEN);
                    will(returnValue(session));

                    one(server).searchForMaterials(with(SESSION_TOKEN), with(criteriaRecorder));
                    will(returnValue(Arrays.asList(m1, m2, m3)));
                    inSequence(searchMaterialSequence);

                    one(server).searchForMaterials(with(SESSION_TOKEN), with(criteriaRecorder));
                    will(returnValue(Arrays.asList(m1, m2v2, m4)));
                    inSequence(searchMaterialSequence);

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(24L * 3600L * 1000L * 60L));
                    inSequence(timeSequence);

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(24L * 3600L * 1000L * 62L));
                    inSequence(timeSequence);
                }
            });

        materialReportingTask.execute();
        materialReportingTask.execute();

        List<?> result = loadTable("report1");
        assertEquals("[{code=M1, description=null, id=null}]", result.toString());
        result = loadTable("report2");
        assertEquals("[{code=M2, greetings=blabla, material=null, organism=Tiger, "
                + "rank=137, report1_code=null, size=null, timestamp=null}, "
                + "{code=M3, greetings=hello, material=null, organism=null, "
                + "rank=null, report1_code=null, size=null, timestamp=null}, "
                + "{code=M4, greetings=hi, material=null, organism=null, "
                + "rank=null, report1_code=null, size=null, timestamp=null}]", result.toString());
        assertEquals("[{timestamp=1970-03-04 01:00:00.0}]", loadTable("timestamp", false)
                .toString());
        List<DetailedSearchCriteria> recordedObjects = criteriaRecorder.getRecordedObjects();
        assertEquals("1970-01-01 01:00:00 +0100", recordedObjects.get(0).getCriteria().get(0)
                .getValue());
        assertEquals("1970-03-02 01:00:00 +0100", recordedObjects.get(1).getCriteria().get(0)
                .getValue());
        assertEquals(2, recordedObjects.size());
        context.assertIsSatisfied();
    }

    private void prepareListMaterialTypes(final String... materialTypeDescriptions)
    {
        context.checking(new Expectations()
            {
                {
                    one(server).tryToAuthenticateAsSystem();
                    SessionContextDTO session = new SessionContextDTO();
                    session.setSessionToken(SESSION_TOKEN);
                    will(returnValue(session));

                    List<MaterialType> materialTypes = new ArrayList<MaterialType>();
                    for (String description : materialTypeDescriptions)
                    {
                        String[] split1 = description.split(":");
                        MaterialTypeBuilder builder = new MaterialTypeBuilder().code(split1[0]);
                        String[] split2 = split1[1].split(",");
                        for (String propertyDescription : split2)
                        {
                            String[] split3 = propertyDescription.split("=");
                            builder.propertyType(split3[0], split3[0],
                                    DataTypeCode.valueOf(split3[1]));
                        }
                        materialTypes.add(builder.getMaterialType());
                    }
                    one(server).listMaterialTypes(SESSION_TOKEN);
                    will(returnValue(materialTypes));
                }
            });
    }

    private List<?> loadTable(String tableName)
    {
        return loadTable(tableName, true);
    }

    private List<?> loadTable(String tableName, boolean orderByCode)
    {
        return new JdbcTemplate(dbConfigContext.getDataSource()).query("select * from " + tableName
                + (orderByCode ? " order by code" : ""), new ColumnMapRowMapper()
            {
                @SuppressWarnings("rawtypes")
                @Override
                protected Map createColumnMap(int columnCount)
                {
                    return new TreeMap();
                }

                @Override
                protected String getColumnKey(String columnName)
                {
                    return columnName.toLowerCase();
                }
            });
    }

    private void createTables(String... creationStatements) throws Exception
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            connection = dbConfigContext.getDataSource().getConnection();
            connection.setAutoCommit(true);
            statement = connection.createStatement();
            for (String creationStatement : creationStatements)
            {
                statement.execute(creationStatement);
            }
        } finally
        {
            if (statement != null)
            {
                statement.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
    }

    private void createTestDatabase() throws SQLException
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            connection = dbConfigContext.getAdminDataSource().getConnection();
            connection.setAutoCommit(true);
            statement = connection.createStatement();
            statement.execute("create database " + dbConfigContext.getDatabaseName()
                    + " with owner = \"" + dbConfigContext.getOwner() + "\"");
        } finally
        {
            if (statement != null)
            {
                statement.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
    }

    private void dropTestDatabase() throws SQLException
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            connection = dbConfigContext.getAdminDataSource().getConnection();
            connection.setAutoCommit(true);
            statement = connection.createStatement();
            statement.execute("drop database " + dbConfigContext.getDatabaseName());
        } catch (SQLException ex)
        {
            if (ex.getMessage().indexOf("does not exist") < 0)
            {
                throw ex;
            }
        } finally
        {
            if (statement != null)
            {
                statement.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
    }

}
