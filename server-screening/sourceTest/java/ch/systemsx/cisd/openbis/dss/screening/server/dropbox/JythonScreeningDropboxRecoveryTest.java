/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.screening.server.dropbox;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileConstants;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.AbstractJythonDataSetHandlerTest;
import ch.systemsx.cisd.etlserver.registrator.ITestingDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.TestingDataSetHandlerExpectations;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryInfo;
import ch.systemsx.cisd.openbis.dss.etl.jython.v2.JythonPlateDataSetHandlerV2;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author jakubs
 */
public class JythonScreeningDropboxRecoveryTest extends AbstractJythonDataSetHandlerTest
{
    private static final String SCRIPTS_FOLDER =
            "sourceTest/java/ch/systemsx/cisd/openbis/dss/screening/server/dropbox/";

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return SCRIPTS_FOLDER;
    }

    private void createDataSetHandler(boolean shouldRegistrationFail, boolean rethrowExceptions)
            throws IOException
    {
        setUpHomeDataBaseExpectations();
        context.checking(new Expectations()
            {
                {
                    ignoring(openBisService).heartbeat();
                }
            });

        Properties properties = createThreadPropertiesRelativeToScriptsFolder("hcs-simple-test.py");
        properties.put("TEST_V2_API", "");
        TopLevelDataSetRegistratorGlobalState globalState = createGlobalState(properties);
        handler = new TestingPlateDataSetHandlerV2(globalState);
        createData();
    }

    private class TestingPlateDataSetHandlerV2 extends JythonPlateDataSetHandlerV2 implements
            ITestingDataSetHandler
    {
        public TestingPlateDataSetHandlerV2(TopLevelDataSetRegistratorGlobalState globalState)
        {
            super(globalState);
        }

        @Override
        public TestingDataSetHandlerExpectations getExpectations()
        {
            return null;
        }

    }

    @Test
    public void testBasicRecovery() throws IOException
    {
        createDataSetHandler(true, false);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();

        // create expectations
        context.checking(new RegistrationFailureExpectations(atomicatOperationDetails));

        handleAndMakeRecoverableImmediately();

        // JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory,
        // "pre_metadata_registration" );

        assertOriginalMarkerFileExists();

        // this recovery should succeed
        handler.handle(markerFile);

        assertImagesStored(atomicatOperationDetails.recordedObject());

        assertNoOriginalMarkerFileExists();
        assertNoRecoveryMarkerFile();

        // JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory,
        // "post_metadata_registration", "post_storage");
    }

    protected void assertImagesStored(AtomicEntityOperationDetails recordedObject)
    {
        List<? extends NewExternalData> dataSetRegistrations = recordedObject.getDataSetRegistrations();
        TreeMap<String, NewExternalData> map = new TreeMap<String, NewExternalData>();
        for (NewExternalData dataSet : dataSetRegistrations)
        {
            map.put(dataSet.getDataSetType().getCode(), dataSet);
        }
        assertEquals("[HCS_IMAGE_CONTAINER_RAW, HCS_IMAGE_OVERVIEW, HCS_IMAGE_RAW]", map.keySet().toString());
        assertEquals(3, dataSetRegistrations.size());

        NewExternalData hcsImageOverviewDataSet = map.get("HCS_IMAGE_OVERVIEW");
        NewExternalData hcsImageRawDataSet = map.get("HCS_IMAGE_RAW");

        File rawDatasetLocation =
                new File(assertStorageDirectoryOfDataset(hcsImageRawDataSet), "original");
        File overviewDatasetLocation = assertStorageDirectoryOfDataset(hcsImageOverviewDataSet);

        System.out.println(hcsImageOverviewDataSet.getLocation());

        assertTrue("The plate directory exists", new File(rawDatasetLocation, "PLATE").exists());

        assertTrue("The thumbnails file is generated", new File(overviewDatasetLocation,
                "thumbnails.h5ar").exists());

        for (String file : allFilesInPlate)
        {
            assertTrue("The plate content file " + file, new File(new File(rawDatasetLocation,
                    "PLATE"), file).exists());
        }

    }

    /**
     * Checks that the storage directory exists and returns this directory
     */
    private File assertStorageDirectoryOfDataset(NewExternalData hcsImageRawDataSet)
    {
        File rawDatasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory,
                        hcsImageRawDataSet.getCode(),
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);

        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                rawDatasetLocation), hcsImageRawDataSet.getLocation());

        return rawDatasetLocation;
    }

    private void handleAndMakeRecoverableImmediately()
    {
        handler.handle(markerFile);
        Calendar c = Calendar.getInstance();
        c.set(2010, 1, 1);
        Date recoveryLastTry = c.getTime();
        setTheRecoveryInfo(1, recoveryLastTry);
    }

    /**
     * Use this method to update the retry count in the recovery info file.
     */
    private void setTheRecoveryInfo(int count, Date lastTryDate)
    {
        File file = getCreatedRecoveryMarkerFile();

        DataSetStorageRecoveryInfo recoveryInfo =
                handler.getGlobalState().getStorageRecoveryManager()
                        .getRecoveryFileFromMarker(file);
        // as the interface allow only increment, and not setting - we implement addition using
        // increment
        while (recoveryInfo.getTryCount() < count)
        {
            recoveryInfo.increaseTryCount();
        }

        recoveryInfo.setLastTry(lastTryDate);

        recoveryInfo.writeToFile(file);
    }

    private void assertNoRecoveryMarkerFile()
    {
        File file = getCreatedRecoveryMarkerFile();
        assertTrue("The recovery marker file should not exist! " + file, false == file.exists());
    }

    private File getCreatedRecoveryMarkerFile()
    {
        File originalIncoming =
                FileUtilities.removePrefixFromFileName(markerFile, FileConstants.IS_FINISHED_PREFIX);
        File recoveryMarkerFile =
                handler.getGlobalState().getStorageRecoveryManager()
                        .getProcessingMarkerFile(originalIncoming);
        return recoveryMarkerFile;
    }

    private void assertOriginalMarkerFileExists()
    {
        assertTrue(
                "The original registration marker file should not be deleted when entering recovery mode",
                markerFile.exists());
    }

    private void assertNoOriginalMarkerFileExists()
    {
        assertFalse("The original registration marker " + markerFile + " file should be deleted",
                markerFile.exists());
    }

    class RegistrationFailureExpectations extends Expectations
    {
        final Experiment experiment;

        final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails;

        public RegistrationFailureExpectations(
                final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails)
        {
            ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
            this.experiment = builder.getExperiment();
            this.atomicatOperationDetails = atomicatOperationDetails;
            prepareExpecatations();
        }

        private void prepareExpecatations()
        {
            one(openBisService).tryGetSampleWithExperiment(with(any(SampleIdentifier.class)));
            atLeast(1).of(openBisService).tryGetExperiment(
                    new ExperimentIdentifierFactory(experiment.getIdentifier()).createIdentifier());

            int permIds = 2;
            for (int i = 0; i < permIds; i++)
            {
                one(openBisService).createPermIds(with(any(Integer.class)));
                will(returnValue(Collections.singletonList("UNIQUE_PERM_ID_" + i)));
            }

            int dataSets = 3;
            for (int i = 0; i < dataSets; i++)
            {
                // create dataset
                one(openBisService).createPermIds(with(any(Integer.class)));
                will(returnValue(Collections.singletonList(DATA_SET_CODE + i)));

                one(dataSetValidator).assertValidDataSet(with(any(DataSetType.class)),
                        with(any(File.class)));

            }
            // initialExpectations();

            registerDataSetsAndThrow(true);
            one(openBisService).didEntityOperationsSucceed(with(any(TechId.class)));
            will(returnValue(EntityOperationsState.NO_OPERATION));
            // this is check at the retry phase - to trigger going into recovery mode

            one(openBisService).didEntityOperationsSucceed(with(any(TechId.class)));
            will(returnValue(EntityOperationsState.OPERATION_SUCCEEDED));

            // the recovery should happen here

            List<String> codes = new LinkedList<String>();
            for (int i = 0; i < dataSets; i++)
            {
                codes.add(DATA_SET_CODE + i);
            }
            one(openBisService).setStorageConfirmed(codes);
        }

        protected void registerDataSetsAndThrow(boolean canRecoverFromError)
        {
            one(openBisService).drawANewUniqueID();
            will(returnValue(Long.valueOf(1)));

            one(openBisService).performEntityOperations(with(atomicatOperationDetails));

            Exception e;
            if (canRecoverFromError)
            {
                e =
                        new EnvironmentFailureException(
                                "Potentially recoverable failure in registration");
            } else
            {
                e = new UserFailureException("Unrecoverable failure in registration");
            }

            will(throwException(e));
        }

    }

    private final String[] allFilesInPlate =
    { "PLATE1_A01_01_Cy3.jpg", "PLATE1_A01_01_DAPI.jpg", "PLATE1_A01_01_GFP.jpg",
            "PLATE1_A01_02_Cy3.jpg", "PLATE1_A01_02_DAPI.jpg", "PLATE1_A01_02_GFP.jpg",
            "PLATE1_A01_03_Cy3.jpg", "PLATE1_A01_03_DAPI.jpg", "PLATE1_A01_03_GFP.jpg",
            "PLATE1_A01_04_Cy3.jpg", "PLATE1_A01_04_DAPI.jpg", "PLATE1_A01_04_GFP.jpg",
            "PLATE1_A01_05_Cy3.jpg", "PLATE1_A01_05_DAPI.jpg", "PLATE1_A01_05_GFP.jpg",
            "PLATE1_A01_06_Cy3.jpg", "PLATE1_A01_06_DAPI.jpg", "PLATE1_A01_06_GFP.jpg" };

    /**
     * creates a simple images to import
     */
    private void createData() throws IOException
    {
        incomingDataSetFile = createDirectory(workingDirectory, "PLATE");

        File originalImage =
                new File(
                        "./sourceTest/java/ch/systemsx/cisd/openbis/dss/screening/server/dropbox/data/image.jpg");

        for (String file : allFilesInPlate)
        {
            FileUtils.copyFile(originalImage, new File(incomingDataSetFile, file));
        }

        markerFile = new File(workingDirectory, FileConstants.IS_FINISHED_PREFIX + "PLATE");
        FileUtilities.writeToFile(markerFile, "");
    }
}
