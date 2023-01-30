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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.ARCHIVED;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.ARCHIVE_PENDING;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.AVAILABLE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.LOCKED;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.UNARCHIVE_PENDING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.multiplexer.IMultiplexer;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.event.DeleteDataSetEventBuilder;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * Test cases for corresponding {@link DataSetTable} class.
 * 
 * @author Christian Ribeaud
 * @author Piotr Buczek
 */
@Friend(toClasses = DataSetTable.class)
public final class DataSetTableTest extends AbstractBOTest
{
    private Map<String, String> options = new HashMap<>();

    private IDataStoreServiceFactory dssFactory;

    private DataStorePE dss1;

    private DataStorePE dss2;

    private DataStorePE dss3;

    private IDataStoreService dataStoreService1;

    private IDataStoreService dataStoreService2;

    private IDataStoreService dataStoreService3;

    private IDataStoreService dataStoreServiceConversational1;

    private IDataStoreService dataStoreServiceConversational2;

    private IDataStoreService dataStoreServiceConversational3;

    private IMultiplexer multiplexer;

    private final DataSetTable createDataSetTable()
    {
        return new DataSetTable(daoFactory, dssFactory, ManagerTestTool.EXAMPLE_SESSION,
                relationshipService, conversationClient, null, managedPropertyEvaluatorFactory,
                multiplexer, new DataSetTypeWithoutExperimentChecker(new Properties()));
    }

    @BeforeMethod
    @Override
    public void beforeMethod()
    {
        super.beforeMethod();
        dssFactory = context.mock(IDataStoreServiceFactory.class);
        dss1 = createDataStore("dss1", false);
        dss2 = createDataStore("dss2", true);
        dss3 = createDataStore("dss3", true);
        dataStoreService1 = context.mock(IDataStoreService.class, "dataStoreService1");
        dataStoreService2 = context.mock(IDataStoreService.class, "dataStoreService2");
        dataStoreService3 = context.mock(IDataStoreService.class, "dataStoreService3");
        multiplexer = context.mock(IMultiplexer.class);

        dataStoreServiceConversational1 =
                context.mock(IDataStoreService.class, "dataStoreServiceConversational1");
        dataStoreServiceConversational2 =
                context.mock(IDataStoreService.class, "dataStoreServiceConversational2");
        dataStoreServiceConversational3 =
                context.mock(IDataStoreService.class, "dataStoreServiceConversational3");

        context.checking(new Expectations()
            {
                {
                    allowing(dssFactory).create(dss1.getRemoteUrl());
                    will(returnValue(dataStoreService1));

                    allowing(dssFactory).create(dss2.getRemoteUrl());
                    will(returnValue(dataStoreService2));

                    allowing(dssFactory).create(dss3.getRemoteUrl());
                    will(returnValue(dataStoreService3));

                    allowing(conversationClient).getDataStoreService(dss1.getRemoteUrl(),
                            ManagerTestTool.EXAMPLE_SESSION.getSessionToken());
                    will(returnValue(dataStoreServiceConversational1));

                    allowing(conversationClient).getDataStoreService(dss2.getRemoteUrl(),
                            ManagerTestTool.EXAMPLE_SESSION.getSessionToken());
                    will(returnValue(dataStoreServiceConversational2));

                    allowing(conversationClient).getDataStoreService(dss3.getRemoteUrl(),
                            ManagerTestTool.EXAMPLE_SESSION.getSessionToken());
                    will(returnValue(dataStoreServiceConversational3));
                }
            });
    }

    @Test
    public final void testLoadBySampleTechIdWithNullSampleId()
    {
        final DataSetTable dataSetTable = createDataSetTable();
        boolean fail = true;
        try
        {
            dataSetTable.loadBySampleTechId(null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            dataSetTable.getDataSets();
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testLoadBySampleTechId()
    {
        final DataSetTable dataSetTable = createDataSetTable();
        final TechId sampleId = CommonTestUtils.TECH_ID;
        final String sampleCode = "CP-01";
        final SamplePE sample = new SamplePE();
        sample.setId(sampleId.getId());
        sample.setCode(sampleCode);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    one(sampleDAO).getByTechId(sampleId);
                    will(returnValue(sample));

                    one(dataDAO).listDataSets(sample);
                }
            });
        dataSetTable.loadBySampleTechId(sampleId);
    }

    @Test
    public void testLoadByExperimentTechId()
    {
        final TechId experimentId = CommonTestUtils.TECH_ID;
        final ExperimentIdentifier identifier =
                new ExperimentIdentifier(new ProjectIdentifier("group", "project"), "exp");
        final ExperimentPE experimentPE = CommonTestUtils.createExperiment(identifier);
        experimentPE.setId(experimentId.getId());

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));

                    one(experimentDAO).getByTechId(experimentId);
                    will(returnValue(experimentPE));

                    one(dataDAO).listDataSets(experimentPE);
                }
            });

        DataSetTable dataSetTable = createDataSetTable();
        dataSetTable.loadByExperimentTechId(experimentId);
    }

    @Test
    public void testLoadByDataSetCodes()
    {
        final ExternalDataPE d1 = createDataSet("d1", dss1);
        final ExternalDataPE d2 = createDataSet("d2", dss2);

        prepareFindFullDatasets(new ExternalDataPE[] { d1, d2 }, new ExternalDataPE[] { d1 }, false, false);

        DataSetTable dataSetTable = createDataSetTable();
        dataSetTable.loadByDataSetCodes(Arrays.asList(d1.getCode(), d2.getCode()), false, false);

        assertEquals(1, dataSetTable.getDataSets().size());
        assertSame(d1, dataSetTable.getDataSets().get(0));
    }

    private void prepareFindFullDatasets(final ExternalDataPE[] search,
            final boolean withProperties, final boolean lockForUpdate)
    {
        prepareFindFullDatasets(search, search, withProperties, lockForUpdate);
    }

    private void prepareFindFullDatasets(final ExternalDataPE[] searched,
            final ExternalDataPE[] results, final boolean withProperties,
            final boolean lockForUpdate)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryToFindFullDataSetsByCodes(
                            Code.extractCodes(Arrays.asList(searched)), withProperties,
                            lockForUpdate);
                    will(returnValue(Arrays.asList(results)));
                }
            });
    }

    // this test is to document, that even if the dataset storage does not exist - it can still be
    // deleted from database
    public void testDeleteLoadedDataSetsButOneDataSetIsUnknown()
    {
        final ExternalDataPE d1 = createDataSet("d1", dss1);
        final ExternalDataPE d2 = createDataSet("d2", dss2);
        d2.setSpeedHint(13);
        context.checking(new Expectations()
            {
                {
                    prepareFindFullDatasets(new ExternalDataPE[] { d1, d2 }, false, false);
                }
            });

        DataSetTable dataSetTable = createDataSetTable();
        dataSetTable.loadByDataSetCodes(Arrays.asList(d1.getCode(), d2.getCode()), false, false);
        dataSetTable.deleteLoadedDataSets("", false);
    }

    @Test
    public void testDeleteLoadedDataSetsButNotAllAreAvailable()
    {
        final ExternalDataPE d1 = createDataSet("d1", dss1, AVAILABLE);
        final ExternalDataPE d2 = createDataSet("d2", dss2, LOCKED);
        final ExternalDataPE d3 = createDataSet("d3n", dss2, ARCHIVED);
        final ExternalDataPE d4 = createDataSet("d4n", dss2, ARCHIVE_PENDING);
        final ExternalDataPE d5 = createDataSet("d5n", dss2, UNARCHIVE_PENDING);
        final ExternalDataPE[] allDataSets =
                { d1, d2, d3, d4, d5 };
        context.checking(new Expectations()
            {
                {
                    prepareFindFullDatasets(allDataSets, false, false);
                }
            });

        DataSetTable dataSetTable = createDataSetTable();
        dataSetTable
                .loadByDataSetCodes(Code.extractCodes(Arrays.asList(allDataSets)), false, false);
        try
        {
            dataSetTable.deleteLoadedDataSets("", false);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            Pattern pattern =
                    Pattern.compile("Deletion failed because the following data sets are "
                            + "required by a background process \\(their status is pending\\): "
                            + "\\[(.*)\\]. ");
            Matcher matcher = pattern.matcher(e.getMessage());

            assertTrue("Invalid error message:" + e.getMessage(), matcher.matches());

            List<String> pendingIds = Arrays.asList(matcher.group(1).split(", "));
            Collections.sort(pendingIds);

            assertEquals("[d4n, d5n]", pendingIds.toString());
        }
    }

    @Test
    public void testDeleteLoadedDataSets()
    {
        final ExternalDataPE d1 = createDataSet("d1", dss1, AVAILABLE);
        final ExternalDataPE d2 = createDataSet("d2", dss2, LOCKED);
        final String reason = "reason";
        context.checking(new Expectations()
            {
                {
                    prepareFindFullDatasets(new ExternalDataPE[] { d1, d2 }, false, false);

                    PersonPE person = EXAMPLE_SESSION.tryGetPerson();
                    one(eventDAO).persist(createDeletionEvent(d1, person, reason));
                    one(dataDAO).delete(d1);
                    one(eventDAO).persist(createDeletionEvent(d2, person, reason));
                    one(dataDAO).delete(d2);
                }
            });

        DataSetTable dataSetTable = createDataSetTable();
        dataSetTable.loadByDataSetCodes(Arrays.asList(d1.getCode(), d2.getCode()), false, false);
        dataSetTable.deleteLoadedDataSets(reason, false);
    }

    private EventPE createDeletionEvent(ExternalDataPE dataset, PersonPE person, String reason)
    {
        DeleteDataSetEventBuilder builder = new DeleteDataSetEventBuilder(dataset, person);
        builder.setReason(reason);
        return builder.getEvent();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUploadDataSets()
    {
        final ExternalDataPE d1PE = createDataSet("d1", dss1, AVAILABLE);
        final ExternalDataPE d2PE = createDataSet("d2", dss2, LOCKED);
        final DataSetUploadContext uploadContext = new DataSetUploadContext();
        uploadContext.setCifexURL("cifexURL");
        uploadContext.setUserID(EXAMPLE_SESSION.getUserName());
        uploadContext.setPassword("pwd");
        uploadContext.setUserEMail(EXAMPLE_SESSION.getPrincipal().getEmail());
        uploadContext.setComment(DataSetTable.createUploadComment(Arrays.asList(d1PE, d2PE)));
        context.checking(new Expectations()
            {
                {
                    prepareFindFullDatasets(new ExternalDataPE[] { d1PE, d2PE }, true, false);

                    one(dataStoreServiceConversational2).uploadDataSetsToCIFEX(
                            with(equal(dss2.getSessionToken())),
                            with(new BaseMatcher<List<AbstractExternalData>>()
                                {

                                    @Override
                                    public boolean matches(Object item)
                                    {
                                        List<AbstractExternalData> list =
                                                (List<AbstractExternalData>) item;
                                        if (list.size() != 1)
                                        {
                                            return false;
                                        }
                                        AbstractExternalData data = list.get(0);
                                        return d2PE.getCode().equals(data.getCode());
                                    }

                                    @Override
                                    public void describeTo(Description description)
                                    {
                                        description.appendText("Data set d2");
                                    }
                                }),
                            with(same(uploadContext)));
                }
            });

        DataSetTable dataSetTable = createDataSetTable();
        dataSetTable.loadByDataSetCodes(Arrays.asList(d1PE.getCode(), d2PE.getCode()), true, false);
        String message = dataSetTable.uploadLoadedDataSetsToCIFEX(uploadContext);

        assertEquals(
                "The following data sets couldn't been uploaded because of unkown data store: d1",
                message);
    }

    @Test
    public void testUploadDataSetsButNotAllAreAvailable()
    {
        final ExternalDataPE d1 = createDataSet("d1", dss1, AVAILABLE);
        final ExternalDataPE d2 = createDataSet("d2", dss2, LOCKED);
        final ExternalDataPE d3 = createDataSet("d3n", dss2, ARCHIVED);
        final ExternalDataPE d4 = createDataSet("d4n", dss2, ARCHIVE_PENDING);
        final ExternalDataPE d5 = createDataSet("d5n", dss2, UNARCHIVE_PENDING);
        final ExternalDataPE[] allDataSets =
                { d1, d2, d3, d4, d5 };
        final DataSetUploadContext uploadContext = new DataSetUploadContext();
        uploadContext.setCifexURL("cifexURL");
        uploadContext.setUserID(EXAMPLE_SESSION.getUserName());
        uploadContext.setPassword("pwd");
        uploadContext.setUserEMail(EXAMPLE_SESSION.getPrincipal().getEmail());
        uploadContext
                .setComment(DataSetTable.createUploadComment(Arrays.asList(d1, d2, d3, d4, d5)));
        context.checking(new Expectations()
            {
                {
                    prepareFindFullDatasets(allDataSets, true, false);
                }
            });

        DataSetTable dataSetTable = createDataSetTable();
        dataSetTable.loadByDataSetCodes(Code.extractCodes(Arrays.asList(allDataSets)), true, false);
        try
        {
            dataSetTable.uploadLoadedDataSetsToCIFEX(uploadContext);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Operation failed because following data sets are not available "
                    + "(they are archived or their status is pending): [d3n, d4n, d5n]. "
                    + "Unarchive these data sets or filter them out using data set status "
                    + "before performing the operation once again.", e.getMessage());
        }
    }

    @Test
    public void testCreateUploadComment()
    {
        createAndCheckUploadComment(18, 50, 18);
        createAndCheckUploadComment(18, 50, 19);
        createAndCheckUploadComment(18, 50, 20);
        createAndCheckUploadComment(18, 50, 21);

        createAndCheckUploadComment(17, 51, 29);
        createAndCheckUploadComment(17, 52, 29);
        createAndCheckUploadComment(17, 53, 29);
        createAndCheckUploadComment(17, 54, 29);
        createAndCheckUploadComment(16, 54, 129);
    }

    private void createAndCheckUploadComment(int expectedCodesShown, int codeLength,
            int dataSetCount)
    {
        List<ExternalDataPE> dataSets = new ArrayList<ExternalDataPE>(dataSetCount);
        StringBuilder builder = new StringBuilder(DataSetTable.UPLOAD_COMMENT_TEXT);
        for (int i = 0; i < dataSetCount; i++)
        {
            ExternalDataPE dataSet = new ExternalDataPE();
            String code = generateDataSetCode(codeLength, i);
            dataSet.setCode(code);
            dataSets.add(dataSet);
            if (i < expectedCodesShown)
            {
                builder.append(DataSetTable.NEW_LINE);
                builder.append(code);
            } else if (i == expectedCodesShown)
            {
                builder.append(DataSetTable.NEW_LINE);
                builder.append(String.format(DataSetTable.AND_MORE_TEMPLATE, dataSetCount
                        - expectedCodesShown));
            }
        }
        String comment = DataSetTable.createUploadComment(dataSets);
        System.out.println(comment.length() + ":" + comment);
        assertEquals(builder.toString(), comment);
        assertTrue(comment.length() <= BasicConstant.MAX_LENGTH_OF_CIFEX_COMMENT);
    }

    private String generateDataSetCode(int codeLength, int codeIndex)
    {
        String result = "-" + (codeIndex + 1);
        String sequence = StringUtils.repeat("1234567890", (codeLength / 10) + 1);
        return sequence.substring(0, codeLength - result.length()) + result;
    }

    private ExternalDataPE createDataSet(String code, DataStorePE dataStore)
    {
        ExternalDataPE data = new ExternalDataPE();
        data.setId((long) code.hashCode());
        data.setCode(code);
        data.setDataStore(dataStore);
        data.setLocation("here/" + code);
        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode("exp1");
        experiment.setExperimentType(new ExperimentTypePE());
        ProjectPE project = new ProjectPE();
        project.setCode("p1");
        SpacePE group = new SpacePE();
        group.setCode("g1");
        project.setSpace(group);
        experiment.setProject(project);
        data.setExperiment(experiment);
        DataSetTypePE type = new DataSetTypePE();
        data.setDataSetType(type);
        data.setDataSetKind(DataSetKind.PHYSICAL.name());
        FileFormatTypePE fileFormatType = new FileFormatTypePE();
        fileFormatType.setCode("fileFormat");
        data.setFileFormatType(fileFormatType);
        return data;
    }

    private ExternalDataPE createDataSet(String code, DataStorePE dataStore,
            DataSetArchivingStatus status)
    {
        ExternalDataPE data = createDataSet(code, dataStore);
        data.setStatus(status);
        return data;
    }

    private DataStorePE createDataStore(String code, boolean withRemoteURL)
    {
        DataStorePE dataStore = new DataStorePE();
        dataStore.setCode(code);
        if (withRemoteURL)
        {
            dataStore.setRemoteUrl("http://" + code);
        }
        dataStore.setSessionToken("session-" + code);
        return dataStore;
    }

    @Test
    public void testArchiveDataSets()
    {
        final ExternalDataPE d2Available1 = createDataSet("d2a1", dss2, AVAILABLE);
        final ExternalDataPE d2Available2 = createDataSet("d2a2", dss2, AVAILABLE);
        final ExternalDataPE d2NonAvailable1 = createDataSet("d2n1", dss2, UNARCHIVE_PENDING);
        final ExternalDataPE d2NonAvailable2 = createDataSet("d2n2", dss2, ARCHIVE_PENDING);
        final ExternalDataPE d2NonAvailable3 = createDataSet("d2n3", dss2, LOCKED);
        final ExternalDataPE d3Available = createDataSet("d3a", dss3, AVAILABLE);
        final ExternalDataPE d3NonAvailable = createDataSet("d3n", dss3, ARCHIVED);
        final ExternalDataPE[] allDataSets =
                { d2Available1, d2Available2, d2NonAvailable1, d2NonAvailable2, d3Available,
                        d3NonAvailable, d2NonAvailable3 };
        context.checking(new Expectations()
            {
                {
                    prepareFindFullDatasets(allDataSets, false, true);

                    prepareUpdateDatasetStatuses(new ExternalDataPE[] { d2Available1, d2Available2, d3Available }, ARCHIVE_PENDING);

                    // prepareFlush();

                    prepareArchiving(dataStoreService2, dss2, d2Available1, d2Available2);
                    prepareArchiving(dataStoreService3, dss3, d3Available);
                }

            });

        DataSetTable dataSetTable = createDataSetTable();
        dataSetTable.loadByDataSetCodes(Code.extractCodes(Arrays.asList(allDataSets)), false, true);
        int archived = dataSetTable.archiveDatasets(true, options);
        assertEquals(3, archived);
    }

    @Test
    public void testUnarchiveDataSets()
    {
        final ExternalDataPE d2Archived1 = createDataSet("d2a1", dss2, ARCHIVED);
        final ExternalDataPE d2Archived2 = createDataSet("d2a2", dss2, ARCHIVED);
        final ExternalDataPE d2NonArchived1 = createDataSet("d2n1", dss2, UNARCHIVE_PENDING);
        final ExternalDataPE d2NonArchived2 = createDataSet("d2n2", dss2, ARCHIVE_PENDING);
        final ExternalDataPE d2NonAvailable3 = createDataSet("d2n3", dss2, LOCKED);
        final ExternalDataPE d3Archived = createDataSet("d3a", dss3, ARCHIVED);
        final ExternalDataPE d3NonArchived = createDataSet("d3n", dss3, AVAILABLE);
        final ExternalDataPE[] allDataSets =
                { d2Archived1, d2Archived2, d2NonArchived1, d2NonArchived2, d3Archived,
                        d3NonArchived, d2NonAvailable3 };
        context.checking(new Expectations()
            {
                {
                    prepareFindFullDatasets(allDataSets, false, true);

                    preparePrepareForUnarchiving(dataStoreService2, dss2, d2Archived1, d2Archived2, d2NonArchived1, d2NonArchived2, d2NonAvailable3);
                    preparePrepareForUnarchiving(dataStoreService3, dss3, d3Archived, d3NonArchived);

                    prepareUpdateDatasetStatuses(new ExternalDataPE[] { d2Archived1, d2Archived2, d3Archived }, UNARCHIVE_PENDING);

                    prepareUnarchiving(dataStoreService2, dss2, d2Archived1, d2Archived2);
                    prepareUnarchiving(dataStoreService3, dss3, d3Archived);
                }
            });

        DataSetTable dataSetTable = createDataSetTable();
        dataSetTable.loadByDataSetCodes(Code.extractCodes(allDataSets), false, true);
        int unarchived = dataSetTable.unarchiveDatasets();
        assertEquals(3, unarchived);
    }

    @Test
    public void testUnarchiveDataSetsWithEnhancedDataSets()
    {
        final ExternalDataPE d2Archived1 = createDataSet("d2a1", dss2, ARCHIVED);
        final ExternalDataPE d2Archived2 = createDataSet("d2a2", dss2, ARCHIVED);
        final ExternalDataPE[] argumentDataSets =
                { d2Archived1 };
        final ExternalDataPE[] allDataSets =
                { d2Archived1, d2Archived2 };

        context.checking(new Expectations()
            {
                {
                    prepareFindFullDatasets(argumentDataSets, false, true);

                    preparePrepareForUnarchivingWithEnhancedDataSets(dataStoreService2, dss2, d2Archived1, d2Archived2);

                    prepareFindFullDatasets(allDataSets, false, true);

                    prepareUpdateDatasetStatuses(new ExternalDataPE[] { d2Archived1, d2Archived2 }, UNARCHIVE_PENDING);

                    prepareUnarchiving(dataStoreService2, dss2, d2Archived1, d2Archived2);
                }
            });

        DataSetTable dataSetTable = createDataSetTable();
        dataSetTable.loadByDataSetCodes(Code.extractCodes(argumentDataSets), false, true);
        int unarchived = dataSetTable.unarchiveDatasets();
        assertEquals(2, unarchived);
    }

    void preparePrepareForUnarchiving(final IDataStoreService service, final DataStorePE store, final ExternalDataPE... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    List<String> codes = Code.extractCodes(dataSets);
                    String userSessionToken = ManagerTestTool.EXAMPLE_SESSION.getSessionToken();
                    one(service).getDataSetCodesForUnarchiving(with(equal(store.getSessionToken())),
                            with(equal(userSessionToken)),
                            with(equal(codes)),
                            with(equal(ManagerTestTool.EXAMPLE_PERSON.getUserId())));
                    will(returnValue(codes));
                }
            });
    }

    /**
     * Gets only the first external data, but returns for unarchiving all of them.
     */
    void preparePrepareForUnarchivingWithEnhancedDataSets(final IDataStoreService service, final DataStorePE store, final ExternalDataPE... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    List<String> codes = Code.extractCodes(dataSets);
                    String userSessionToken = ManagerTestTool.EXAMPLE_SESSION.getSessionToken();
                    one(service).getDataSetCodesForUnarchiving(with(equal(store.getSessionToken())),
                            with(equal(userSessionToken)),
                            with(equal(Collections.singletonList(codes.get(0)))),
                            with(equal(ManagerTestTool.EXAMPLE_PERSON.getUserId())));
                    will(returnValue(codes));
                }
            });
    }

    @Test
    public void testArchiveStatusNotChangedOnLocalFailure()
    {
        final DataStorePE dssX = createDataStore("dssX", true);
        final ExternalDataPE d1 = createDataSet("d1", dssX, AVAILABLE);
        final ExternalDataPE[] allDataSets =
                { d1 };
        final String errorMessage = "unexpected local error";
        context.checking(new Expectations()
            {
                {
                    allowing(dssFactory).create(dssX.getRemoteUrl());
                    will(throwException(new RuntimeException(errorMessage)));

                    prepareFindFullDatasets(allDataSets, false, false);
                }
            });

        DataSetTable dataSetTable = createDataSetTable();
        dataSetTable
                .loadByDataSetCodes(Code.extractCodes(Arrays.asList(allDataSets)), false, false);
        try
        {
            dataSetTable.archiveDatasets(true, options);
            fail("RuntimeException expected");
        } catch (RuntimeException re)
        {
            assertEquals(errorMessage, re.getMessage());
        }
    }

    @Test
    public void testArchiveStatusRevertedOnRemoteFailure()
    {
        final ExternalDataPE d2 = createDataSet("d2", dss2, AVAILABLE);
        final ExternalDataPE d3 = createDataSet("d3", dss3, AVAILABLE);
        final ExternalDataPE[] d2Array =
                { d2 };
        final ExternalDataPE[] d3Array =
                { d3 };
        final ExternalDataPE[] allDataSets =
                { d2, d3 };
        context.checking(new Expectations()
            {
                {
                    prepareFindFullDatasets(allDataSets, false, false);

                    prepareUpdateDatasetStatuses(allDataSets, ARCHIVE_PENDING);

                    String userSessionToken = ManagerTestTool.EXAMPLE_SESSION.getSessionToken();
                    one(dataStoreService2).isArchivingPossible(dss2.getSessionToken());
                    will(returnValue(true));
                    allowing(dataStoreService2).archiveDatasets(
                            with(equal(dss2.getSessionToken())), with(equal(userSessionToken)),
                            with(createDatasetDescriptionsMatcher(d2Array)),
                            with(equal(ManagerTestTool.EXAMPLE_PERSON.getUserId())),
                            with(equal(ManagerTestTool.EXAMPLE_PERSON.getEmail())),
                            with(equal(true)),
                            with(equal(options)));
                    will(throwException(new RuntimeException()));

                    one(dataStoreService3).isArchivingPossible(dss3.getSessionToken());
                    will(returnValue(true));
                    allowing(dataStoreService3).archiveDatasets(
                            with(equal(dss3.getSessionToken())), with(equal(userSessionToken)),
                            with(createDatasetDescriptionsMatcher(d3Array)),
                            with(equal(ManagerTestTool.EXAMPLE_PERSON.getUserId())),
                            with(equal(ManagerTestTool.EXAMPLE_PERSON.getEmail())),
                            with(equal(true)),
                            with(equal(options)));
                    will(throwException(new RuntimeException()));

                    // expect statuses to be reverted after an error
                    prepareUpdateDatasetStatuses(allDataSets, AVAILABLE);
                }
            });

        DataSetTable dataSetTable = createDataSetTable();
        dataSetTable
                .loadByDataSetCodes(Code.extractCodes(Arrays.asList(allDataSets)), false, false);
        try
        {
            dataSetTable.archiveDatasets(true, options);
            fail("UserFailureException expected");
        } catch (UserFailureException ufe)
        {
            assertTrue(ufe.getMessage().indexOf("Archiver may not be configured properly.") >= 0);
        }
    }

    private void prepareUpdateDatasetStatuses(final ExternalDataPE[] dataSets,
            final DataSetArchivingStatus newStatus)
    {
        prepareUpdateDatasetStatuses(Code.extractCodes(Arrays.asList(dataSets)), newStatus);
    }

    private void prepareUpdateDatasetStatuses(final List<String> dataSetCodes,
            final DataSetArchivingStatus newStatus)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).updateDataSetStatuses(with(createUnorderedMarcher(dataSetCodes)),
                            with(equal(newStatus)));
                }
            });
    }

    private void prepareArchiving(final IDataStoreService service, final DataStorePE store,
            final ExternalDataPE... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    String userSessionToken = ManagerTestTool.EXAMPLE_SESSION.getSessionToken();
                    one(service).isArchivingPossible(store.getSessionToken());
                    will(returnValue(true));
                    one(service).archiveDatasets(with(equal(store.getSessionToken())),
                            with(equal(userSessionToken)),
                            with(createDatasetDescriptionsMatcher(dataSets)),
                            with(equal(ManagerTestTool.EXAMPLE_PERSON.getUserId())),
                            with(equal(ManagerTestTool.EXAMPLE_PERSON.getEmail())),
                            with(equal(true)),
                            with(equal(options)));
                }
            });
    }

    private void prepareUnarchiving(final IDataStoreService service, final DataStorePE store,
            final ExternalDataPE... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    String userSessionToken = ManagerTestTool.EXAMPLE_SESSION.getSessionToken();
                    one(service).unarchiveDatasets(with(equal(store.getSessionToken())),
                            with(equal(userSessionToken)),
                            with(createDatasetDescriptionsMatcher(dataSets)),
                            with(equal(ManagerTestTool.EXAMPLE_PERSON.getUserId())),
                            with(equal(ManagerTestTool.EXAMPLE_PERSON.getEmail())));
                }
            });
    }

    @SuppressWarnings("unchecked")
    private BaseMatcher<List<DatasetDescription>> createDatasetDescriptionsMatcher(
            final ExternalDataPE... dataSets)
    {
        return new BaseMatcher<List<DatasetDescription>>()
            {

                @Override
                public boolean matches(Object item)
                {
                    List<DatasetDescription> list = (List<DatasetDescription>) item;
                    assertEquals(dataSets.length, list.size());
                    for (int i = 0; i < list.size(); i++)
                    {
                        assertEquals("data set " + i, dataSets[i].getCode(), list.get(i)
                                .getDataSetCode());
                        assertEquals("data set " + i, dataSets[i].getSpeedHint(), list.get(i)
                                .getSpeedHint());
                    }
                    return true;
                }

                @Override
                public void describeTo(Description description)
                {
                    description.appendText("[");
                    for (ExternalDataPE dataSet : dataSets)
                    {
                        description.appendText("Dataset '" + dataSet.getCode() + "', ");
                    }
                    description.appendText("]");
                }
            };
    }

    @SuppressWarnings("unchecked")
    private BaseMatcher<List<String>> createUnorderedMarcher(final List<String> list)
    {
        return new BaseMatcher<List<String>>()
            {

                @Override
                public boolean matches(Object item)
                {
                    List<String> match = (List<String>) item;

                    Collections.sort(list);
                    Collections.sort(match);
                    assertEquals(list, match);

                    return true;
                }

                @Override
                public void describeTo(Description description)
                {
                    description.appendText(list.toString());
                }
            };
    }

}
