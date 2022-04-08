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

package ch.systemsx.cisd.sanofi.dss.test;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.DEFAULT_OVERVIEW_IMAGE_DATASET_TYPE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.DEFAULT_RAW_IMAGE_CONTAINER_DATASET_TYPE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.DEFAULT_SEGMENTATION_IMAGE_CONTAINER_DATASET_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileConstants;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.AbstractJythonDataSetHandlerTest;
import ch.systemsx.cisd.etlserver.registrator.ITestingDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.TestingDataSetHandlerExpectations;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.openbis.common.eodsql.MockDataSet;
import ch.systemsx.cisd.openbis.dss.etl.jython.v1.JythonPlateDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * <pre>
 * Things not tested
 * - skip well creation when plate library already exists
 * - skip material creation for preexisting materials
 * </pre>
 * 
 * @author Kaloyan Enimanev
 */
public class SanofiDropboxJythonRollbackTest extends AbstractJythonDataSetHandlerTest
{
    private static final String PLATE_CODE = "plateCode.variant";

    private static final String LIBRARY_TEMPLATE_PROPNAME = "LIBRARY_TEMPLATE";

    private static final String EXPERIMENT_RECIPIENTS_PROPNAME = "OBSERVER_EMAILS";

    private static final String[] USER_EMAILS = new String[]
    { "donald@duck.com", "mickey@mouse.org" };

    final String[] ADMIN_EMAILS = new String[]
    { "admin@sanofi.com", "admin@openbis.org" };

    final String[] ALL_EMAILS = new String[]
    { "admin@sanofi.com", "admin@openbis.org", "donald@duck.com", "mickey@mouse.org" };

    private static final String IMAGE_DATA_SET_DIR_NAME = "batchNr_plateCode.variant_2011.07.05";

    private static final String ANALYSIS_DATA_SET_FILE_NAME = "analysis";

    private static final String IMAGE_DATA_SET_CODE = "data-set-code";

    private static final DataSetType IMAGE_DATA_SET_TYPE = new DataSetType("HCS_IMAGE_RAW");

    private static final String OVERLAY_DATA_SET_CODE = "overlay-data-set-code";

    private static final DataSetType OVERLAY_DATA_SET_TYPE = new DataSetType(
            "HCS_IMAGE_SEGMENTATION");

    private static final String ANALYSIS_DATA_SET_CODE = "analysis-data-set-code";

    private static final DataSetType ANALYSIS_DATA_SET_TYPE = new DataSetType(
            "HCS_ANALYSIS_WELL_FEATURES");

    private static final String SANOFI_EXPERIMENT_IDENTIFIER = "/SANOFI/PROJECT/EXP";

    private static final String PLATE_IDENTIFIER = "/SANOFI/TEST-PLATE";

    private RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails;

    private RecordingMatcher<ListMaterialCriteria> materialCriteria;

    private RecordingMatcher<String> email;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();

        context.checking(new Expectations()
            {
                {
                    ignoring(openBisService).heartbeat();
                }
            });

        extendJythonLibPath(getRegistrationScriptsFolderPath());

        atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        materialCriteria = new RecordingMatcher<ListMaterialCriteria>();
        email = new RecordingMatcher<String>();
    }

    @Test
    public void testFileSystemUnavailableRollback() throws IOException
    {
        createDataSetHandler(false, true);
        final Sample plate = plateWithLibTemplateAndGeometry("0.75\tH\n54.12\tL", "8_WELLS_2X4");

        final MockDataSet<Map<String, Object>> queryResult = new MockDataSet<Map<String, Object>>();
        queryResult.add(createQueryResult("A1", "material-1"));
        queryResult.add(createQueryResult("B1", "material-1"));

        context.checking(new Expectations()
            {
                {
                    one(dataSourceQueryService).select(with(any(String.class)),
                            with(any(String.class)), with(anything()));
                    will(returnValue(queryResult));

                    exactly(2).of(openBisService).listMaterials(with(materialCriteria),
                            with(equal(true)));
                    will(returnValue(Collections.emptyList()));

                    exactly(4).of(openBisService).createPermId();
                    will(returnValue("well-permId"));

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    allowing(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    allowing(openBisService).tryGetDataSet(with(any(String.class)));
                    will(returnValue(null));

                    allowing(openBisService)
                            .tryGetPropertiesOfSample(sampleIdentifier);
                    will(returnValue(new IEntityProperty[0]));

                    one(openBisService).drawANewUniqueID();
                    will(returnValue(Long.valueOf(1)));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    CustomAction makeFileSystemUnavailable = new CustomAction("foo")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                makeFileSystemUnavailable(workingDirectory);
                                return null;
                            }
                        };
                    will(doAll(makeFileSystemUnavailable,
                            throwException(new AssertionError("Fail"))));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(ADMIN_EMAILS)));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(USER_EMAILS)));
                }
            });
        setUpDataSetExpectations();
        setUpListAdministratorExpectations();

        try
        {
            handler.handle(markerFile);
            fail("No IOException thrown");
        } catch (IOExceptionUnchecked e)
        {
            // Make the file system available again and rollback
            makeFileSystemAvailable(workingDirectory);
            DataSetRegistrationTransaction.rollbackDeadTransactions(workingDirectory);
        }

        File dataDirectory = new File("./sourceTest/examples/" + IMAGE_DATA_SET_DIR_NAME);
        String[] dataDirectoryList = dataDirectory.list();
        String[] dataSetList = new File(workingDirectory, IMAGE_DATA_SET_DIR_NAME).list();
        Arrays.sort(dataDirectoryList);
        Arrays.sort(dataSetList);

        assertEquals("The data set should equal the original after rollback",
                Arrays.asList(dataDirectoryList).toString(), Arrays.asList(dataSetList).toString());

        context.assertIsSatisfied();
    }

    public Sample plateWithLibTemplateAndGeometry(String libraryTemplate, String plateGeometry)
            throws IOException
    {
        Sample plate = createPlate(libraryTemplate, plateGeometry);
        setUpPlateSearchExpectations(plate);
        setUpLibraryTemplateExpectations(plate);
        return plate;
    }

    private void createDataSetHandler(boolean shouldRegistrationFail, boolean rethrowExceptions)
            throws IOException
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("dropbox-all-in-one-with-library.py");
        createHandler(properties, shouldRegistrationFail, rethrowExceptions);
        createData();
    }

    private void setUpDataSetExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createPermId();
                    will(returnValue("image-raw-thumnails"));

                    one(openBisService).createPermId();
                    will(returnValue(IMAGE_DATA_SET_CODE));

                    one(openBisService).createPermId();
                    will(returnValue("image-raw-container"));

                    one(openBisService).createPermId();
                    will(returnValue(ANALYSIS_DATA_SET_CODE));

                    one(openBisService).createPermId();
                    will(returnValue(ANALYSIS_DATA_SET_CODE + "-container"));

                    one(openBisService).createPermId();
                    will(returnValue("overlay-thumnails"));

                    one(openBisService).createPermId();
                    will(returnValue(OVERLAY_DATA_SET_CODE));

                    one(openBisService).createPermId();
                    will(returnValue("overlay-container"));

                    final DataSetType thumnailsDataSetType =
                            new DataSetType(DEFAULT_OVERVIEW_IMAGE_DATASET_TYPE);

                    one(dataSetValidator).assertValidDataSet(
                            thumnailsDataSetType,
                            new File(new File(stagingDirectory, "image-raw-thumnails"),
                                    "thumbnails.h5ar"));

                    one(dataSetValidator).assertValidDataSet(IMAGE_DATA_SET_TYPE,
                            new File(new File(stagingDirectory, IMAGE_DATA_SET_CODE), "original"));

                    one(dataSetValidator).assertValidDataSet(
                            new DataSetType(DEFAULT_RAW_IMAGE_CONTAINER_DATASET_TYPE), null);

                    one(dataSetValidator).assertValidDataSet(
                            thumnailsDataSetType,
                            new File(new File(stagingDirectory, "overlay-thumnails"),
                                    "thumbnails.h5ar"));

                    one(dataSetValidator)
                            .assertValidDataSet(
                                    OVERLAY_DATA_SET_TYPE,
                                    new File(new File(stagingDirectory, OVERLAY_DATA_SET_CODE),
                                            "original"));

                    one(dataSetValidator).assertValidDataSet(
                            new DataSetType(DEFAULT_SEGMENTATION_IMAGE_CONTAINER_DATASET_TYPE),
                            null);

                    one(dataSetValidator).assertValidDataSet(
                            ANALYSIS_DATA_SET_TYPE,
                            new File(new File(stagingDirectory, ANALYSIS_DATA_SET_CODE),
                                    ANALYSIS_DATA_SET_FILE_NAME));

                    one(dataSetValidator).assertValidDataSet(
                            new DataSetType(
                                    ScreeningConstants.DEFAULT_ANALYSIS_WELL_CONTAINER_DATASET_TYPE),
                            null);

                }
            });
    }

    private void setUpPlateSearchExpectations(final Sample plate)
    {
        context.checking(new Expectations()
            {
                {
                    SearchCriteria sc = new SearchCriteria();
                    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                            "PLATE"));
                    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                            PLATE_CODE));
                    oneOf(openBisService).searchForSamples(sc);

                    will(returnValue(Arrays.asList(plate)));
                }
            });
    }

    private void setUpLibraryTemplateExpectations(final Sample plate)
    {
        context.checking(new Expectations()
            {
                {
                    final String identifierString = plate.getExperiment().getIdentifier();
                    ExperimentIdentifier identifier =
                            ExperimentIdentifierFactory.parse(identifierString);
                    oneOf(openBisService).tryGetExperiment(identifier);
                    will(returnValue(plate.getExperiment()));
                }
            });
    }

    private void setUpListAdministratorExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(openBisService).listAdministrators();
                    List<String> adminEmailsList = Arrays.asList(ADMIN_EMAILS);
                    will(returnValue(createAdministrators(adminEmailsList)));
                }
            });
    }

    private List<Person> createAdministrators(List<String> adminEmails)
    {
        List<Person> result = new ArrayList<Person>();
        for (String adminEmail : adminEmails)
        {
            Person person = new Person();
            person.setEmail(adminEmail);
            result.add(person);
        }
        return result;
    }

    private void createData() throws IOException
    {
        File dataDirectory = new File("./sourceTest/examples/" + IMAGE_DATA_SET_DIR_NAME);
        FileUtils.copyDirectoryToDirectory(dataDirectory, workingDirectory);
        incomingDataSetFile = new File(workingDirectory, dataDirectory.getName());

        markerFile = new File(workingDirectory, FileConstants.IS_FINISHED_PREFIX + dataDirectory.getName());
        FileUtilities.writeToFile(markerFile, "");
    }

    private Sample createPlate(String libraryTemplate, String plateGeometry)
    {
        ExperimentBuilder experimentBuilder = new ExperimentBuilder();
        experimentBuilder.identifier(SANOFI_EXPERIMENT_IDENTIFIER);
        experimentBuilder.property(LIBRARY_TEMPLATE_PROPNAME, libraryTemplate);
        String recipients = StringUtils.join(Arrays.asList(USER_EMAILS), ",");
        experimentBuilder.property(EXPERIMENT_RECIPIENTS_PROPNAME, recipients);

        SampleBuilder sampleBuilder = new SampleBuilder();
        sampleBuilder.experiment(experimentBuilder.getExperiment());
        sampleBuilder.identifier(PLATE_IDENTIFIER);
        sampleBuilder.property(ScreeningConstants.PLATE_GEOMETRY, plateGeometry);

        final Sample plate = sampleBuilder.getSample();
        return plate;
    }

    private Map<String, Object> createQueryResult(String wellCode, String materialCode)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("WELL_CODE", wellCode);
        result.put("MATERIAL_CODE", materialCode);
        result.put("ABASE_COMPOUND_ID", wellCode + "_compound_id");
        result.put("ABASE_COMPOUND_BATCH_ID", wellCode + "_compound_batch_id");
        return result;
    }

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return "dist/etc/sanofi-dropbox/";
    }

    @Override
    protected void createHandler(Properties threadProperties, final boolean registrationShouldFail,
            boolean shouldReThrowException)
    {
        TopLevelDataSetRegistratorGlobalState globalState = createGlobalState(threadProperties);

        handler =
                new TestingPlateDataSetHandler(globalState, registrationShouldFail,
                        shouldReThrowException);

    }

    private class TestingPlateDataSetHandler extends JythonPlateDataSetHandler implements
            ITestingDataSetHandler
    {
        private final TestingDataSetHandlerExpectations expectations;

        public TestingPlateDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState,
                boolean shouldRegistrationFail, boolean shouldReThrowRollbackException)
        {
            super(globalState);
            expectations =
                    new TestingDataSetHandlerExpectations(shouldRegistrationFail,
                            shouldReThrowRollbackException);
        }

        @Override
        public void registerDataSetInApplicationServer(DataSetInformation dataSetInformation,
                NewExternalData data) throws Throwable
        {
            if (expectations.isShouldRegistrationFail())
            {
                throw new UserFailureException("Didn't work.");
            } else
            {
                super.registerDataSetInApplicationServer(dataSetInformation, data);
            }
        }

        @Override
        public void rollback(DataSetRegistrationService<DataSetInformation> service,
                Throwable throwable)
        {
            super.rollback(service, throwable);
            if (expectations.isShouldReThrowRollbackException())
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
            } else
            {
                throwable.printStackTrace();
            }
        }

        @Override
        public void didRollbackTransaction(DataSetRegistrationService<DataSetInformation> service,
                DataSetRegistrationTransaction<DataSetInformation> transaction,
                DataSetStorageAlgorithmRunner<DataSetInformation> algorithmRunner,
                Throwable throwable)
        {
            super.didRollbackTransaction(service, transaction, algorithmRunner, throwable);

            if (expectations.isShouldReThrowRollbackException())
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
            } else
            {
                throwable.printStackTrace();
            }
        }

        @Override
        public TestingDataSetHandlerExpectations getExpectations()
        {
            return expectations;
        }
    }

}