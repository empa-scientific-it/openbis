/*
 * Copyright 2022 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ShareFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class CleanUpUnarchivingScratchShareTaskTest extends AbstractFileSystemTestCase
{
    private static final String CLAZZ = CleanUpUnarchivingScratchShareTask.class.getSimpleName();

    private static final String INFO_PREFIX = "INFO  OPERATION." + CLAZZ + " - ";

    private static final String WARN_PREFIX = "WARN  OPERATION." + CLAZZ + " - ";

    private static final String SCRATCH_SHARE_ID = "2";

    private static final String DSS_CODE = "DSS";

    private Mockery context;

    private IShareIdManager shareIdManager;

    private IDataSetDirectoryProvider directoryProvider;

    private IEncapsulatedOpenBISService openBISService;

    private IFreeSpaceProvider freeSpaceProvider;

    private IMultiDataSetArchiverReadonlyQueryDAO queryDAO;

    private CleanUpUnarchivingScratchShareTask task;

    private File store;

    private File archive;

    private BufferedAppender logRecorder;

    private IConfigProvider configProvider;

    private File scratchShare;

    @BeforeMethod
    public void setup()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, ".*"+CLAZZ);
        context = new Mockery();
        store = createFolder(workingDirectory, "store");
        scratchShare = createFolder(store, SCRATCH_SHARE_ID);
        FileUtilities.writeToFile(new File(scratchShare, ShareFactory.SHARE_PROPS_FILE),
                ShareFactory.UNARCHIVING_SCRATCH_SHARE_PROP + " = true");
        archive = new File(workingDirectory, "archive");
        archive.mkdirs();
        shareIdManager = context.mock(IShareIdManager.class);
        directoryProvider = context.mock(IDataSetDirectoryProvider.class);
        openBISService = context.mock(IEncapsulatedOpenBISService.class);
        freeSpaceProvider = context.mock(IFreeSpaceProvider.class);
        queryDAO = context.mock(IMultiDataSetArchiverReadonlyQueryDAO.class);
        configProvider = context.mock(IConfigProvider.class);
        task = createTask();
        context.checking(new Expectations()
            {
                {
                    allowing(configProvider).getDataStoreCode();
                    will(returnValue(DSS_CODE));
                }
            });
    }

    @Test
    public void testSetUpWithMissingArchiver()
    {
        // Given
        Properties properties = new Properties();

        try
        {
            // When
            task.setUp("", properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException e)
        {
            // Then
            assertEquals("Missing property: archiver.final-destination", e.getMessage());
        }
    }

    @Test
    public void testHappyCase()
    {
        // Given
        setUpTask();
        SimpleDataSetInformationDTO ds1 = new Builder(1)
                .archived().presentInArchive().presentInStore().withKnownSize().build();
        SimpleDataSetInformationDTO ds2 = new Builder(2)
                .available().presentInArchive().presentInStore().build();
        SimpleDataSetInformationDTO ds3 = new Builder(3)
                .archived().presentInArchive().withKnownSize().build();
        SimpleDataSetInformationDTO ds4 = new Builder(3)
                .archived().presentInStore().withKnownSize().build();
        SimpleDataSetInformationDTO ds5 = new Builder(5)
                .archived().presentInArchive().presentInStore().withKnownSize().build();
        prepareListDataSets(ds1, ds2, ds3, ds4, ds5);
        prepareUpdateShareIdAndSize(ds2, 22222);
        new ContainerBuilder(1, "container.data").add(ds1).add(ds5).exists(ds1.getDataSetSize() + ds5.getDataSetSize())
                .buildAndPrepare();
        prepareUpdateStatusAndDelete(ds5, ds1);
        assertEquals(true, getDataSetFolder(ds1, false).exists());
        assertEquals(true, getDataSetFolder(ds2, false).exists());
        assertEquals(true, getDataSetFolder(ds4, false).exists());
        assertEquals(true, getDataSetFolder(ds5, false).exists());

        // When
        task.execute();

        // Then
        assertEquals(INFO_PREFIX + "Obtained the list of all datasets in all shares in ? s.\n"
                + INFO_PREFIX + "Calculating size of " + getDataSetFolder(ds2, false) + "\n"
                + INFO_PREFIX + getDataSetFolder(ds2, false) + " contains 22222 bytes (calculated in ? msec)\n"
                + INFO_PREFIX + "Starting clean up. Scanning 5 data sets.\n"
                + INFO_PREFIX + "2 archived data sets in unarchiving scratch share found.\n"
                + INFO_PREFIX + "Remove the following data sets from share '2' "
                + "and set their archiving status back to ARCHIVED: [DS-5, DS-1]\n"
                + INFO_PREFIX + "Await for data set DS-5 to be unlocked.\n"
                + INFO_PREFIX + "Start deleting data set DS-5 at "
                + getDataSetFolder(ds5, false) + "\n"
                + INFO_PREFIX + "Data set DS-5 at "
                + getDataSetFolder(ds5, false) + " has been successfully deleted.\n"
                + INFO_PREFIX + "Await for data set DS-1 to be unlocked.\n"
                + INFO_PREFIX + "Start deleting data set DS-1 at "
                + getDataSetFolder(ds1, false) + "\n"
                + INFO_PREFIX + "Data set DS-1 at "
                + getDataSetFolder(ds1, false) + " has been successfully deleted.\n"
                + INFO_PREFIX + "The following data sets have been successfully removed from share '2' "
                + "and their archiving status has been successfully set back to ARCHIVED: [DS-5, DS-1]\n"
                + INFO_PREFIX + "2 archived data sets successfully removed from unarchiving scratch share.\n"
                + INFO_PREFIX + "Clean up task finished", getLogContent());
        assertEquals(false, getDataSetFolder(ds1, false).exists());
        assertEquals(true, getDataSetFolder(ds2, false).exists());
        assertEquals(true, getDataSetFolder(ds4, false).exists());
        assertEquals(false, getDataSetFolder(ds5, false).exists());
        context.assertIsSatisfied();
    }

    @Test
    public void testUnknownDataSets()
    {
        // Given
        setUpTask();
        SimpleDataSetInformationDTO ds1 = new Builder(1)
                .archived().presentInArchive().presentInStore().withKnownSize().build();
        SimpleDataSetInformationDTO ds2 = new Builder(2)
                .archived().presentInArchive().presentInStore().withKnownSize().build();
        prepareListDataSets(ds1, ds2);
        context.checking(new Expectations()
            {
                {
                    allowing(queryDAO).getDataSetForCode(ds1.getDataSetCode());
                    allowing(queryDAO).getDataSetForCode(ds2.getDataSetCode());
                }
            });

        // When
        task.execute();

        // Then
        assertEquals(INFO_PREFIX + "Obtained the list of all datasets in all shares in ? s.\n"
                + INFO_PREFIX + "Starting clean up. Scanning 2 data sets.\n"
                + WARN_PREFIX + "Data set DS-2 unknown by Multi Data Set database.\n"
                + WARN_PREFIX + "Data set DS-1 unknown by Multi Data Set database.\n"
                + INFO_PREFIX + "Clean up task finished", getLogContent());
        assertEquals(true, getDataSetFolder(ds1, false).exists());
        assertEquals(true, getDataSetFolder(ds2, false).exists());
        context.assertIsSatisfied();
    }

    @Test
    public void testContainerDoesNotExists()
    {
        // Given
        setUpTask();
        SimpleDataSetInformationDTO ds1 = new Builder(1)
                .archived().presentInArchive().presentInStore().withKnownSize().build();
        SimpleDataSetInformationDTO ds2 = new Builder(2)
                .archived().presentInArchive().presentInStore().withKnownSize().build();
        prepareListDataSets(ds1, ds2);
        new ContainerBuilder(1, "container1.data").add(ds1).buildAndPrepare();
        new ContainerBuilder(2, "container2.data").add(ds2).buildAndPrepare();

        // When
        task.execute();

        // Then
        assertEquals(INFO_PREFIX + "Obtained the list of all datasets in all shares in ? s.\n"
                + INFO_PREFIX + "Starting clean up. Scanning 2 data sets.\n"
                + WARN_PREFIX + "Container file for data set DS-2 does not exists: " + archive.getAbsolutePath()
                + "/container2.data\n"
                + WARN_PREFIX + "Container file for data set DS-1 does not exists: " + archive.getAbsolutePath()
                + "/container1.data\n"
                + INFO_PREFIX + "Clean up task finished", getLogContent());
        assertEquals(true, getDataSetFolder(ds1, false).exists());
        assertEquals(true, getDataSetFolder(ds2, false).exists());
        context.assertIsSatisfied();
    }

    @Test
    public void testWrongContainerSize()
    {
        // Given
        setUpTask();
        SimpleDataSetInformationDTO ds1 = new Builder(1)
                .archived().presentInArchive().presentInStore().withKnownSize().build();
        SimpleDataSetInformationDTO ds2 = new Builder(2)
                .archived().presentInArchive().presentInStore().withKnownSize().build();
        prepareListDataSets(ds1, ds2);
        new ContainerBuilder(1, "container1.data").add(ds1).exists((111 * ds1.getDataSetSize()) / 100).buildAndPrepare();
        new ContainerBuilder(2, "container2.data").add(ds2).exists((89 * ds2.getDataSetSize()) / 100).buildAndPrepare();

        // When
        task.execute();

        // Then
        assertEquals(INFO_PREFIX + "Obtained the list of all datasets in all shares in ? s.\n"
                + INFO_PREFIX + "Starting clean up. Scanning 2 data sets.\n"
                + WARN_PREFIX + "Size of container file " + archive.getAbsolutePath()
                + "/container2.data (which contains data set DS-2) doesn't match the size calculated from "
                + "the Multi Data Set database: 19777 vs 22222\n"
                + WARN_PREFIX + "Size of container file " + archive.getAbsolutePath()
                + "/container1.data (which contains data set DS-1) doesn't match the size calculated from "
                + "the Multi Data Set database: 12333 vs 11111\n"
                + INFO_PREFIX + "Clean up task finished", getLogContent());
        assertEquals(true, getDataSetFolder(ds1, false).exists());
        assertEquals(true, getDataSetFolder(ds2, false).exists());
        context.assertIsSatisfied();
    }

    private String getLogContent()
    {
        return logRecorder.getLogContent().replaceAll("\\d\\.\\d+ s", "? s").replaceAll("\\d+ msec", "? msec");
    }

    private void prepareListDataSets(SimpleDataSetInformationDTO... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(openBISService).listPhysicalDataSets();
                    will(returnValue(Arrays.asList(dataSets)));
                }
            });
    }

    private void prepareUpdateShareIdAndSize(SimpleDataSetInformationDTO dataSet, long size)
    {
        context.checking(new Expectations()
            {
                {
                    one(openBISService).updateShareIdAndSize(dataSet.getDataSetCode(), SCRATCH_SHARE_ID, size);
                }
            });
    }

    private void prepareUpdateStatusAndDelete(SimpleDataSetInformationDTO... dataSets)
    {
        List<String> dataSetCodes = Arrays.asList(dataSets).stream().map(SimpleDataSetInformationDTO::getDataSetCode)
                .collect(Collectors.toList());
        context.checking(new Expectations()
            {
                {
                    one(openBISService).updateDataSetStatuses(dataSetCodes, DataSetArchivingStatus.ARCHIVED, true);
                    for (SimpleDataSetInformationDTO dataSet : dataSets)
                    {
                        one(shareIdManager).await(dataSet.getDataSetCode());
                        one(directoryProvider).getDataSetDirectory(dataSet);
                        will(returnValue(getDataSetFolder(dataSet, false)));
                    }
                }
            });
    }

    private final class Builder
    {
        private SimpleDataSetInformationDTO dataSet;

        private int id;

        Builder(int id)
        {
            this.id = id;
            dataSet = new SimpleDataSetInformationDTO();
            dataSet.setDataStoreCode(DSS_CODE);
            dataSet.setDataSetShareId(SCRATCH_SHARE_ID);
            dataSet.setDataSetCode("DS-" + id);
        }

        Builder archived()
        {
            dataSet.setStatus(DataSetArchivingStatus.ARCHIVED);
            return this;
        }

        Builder available()
        {
            dataSet.setStatus(DataSetArchivingStatus.AVAILABLE);
            return this;
        }

        Builder presentInArchive()
        {
            dataSet.setPresentInArchive(true);
            return this;
        }

        Builder presentInStore()
        {
            dataSet.setDataSetLocation("ds-" + id);
            File dataSetFolder = getDataSetFolder(dataSet, true);
            String content = createDataSetContent();
            FileUtilities.writeToFile(new File(dataSetFolder, "data"), content);
            return this;
        }

        Builder withKnownSize()
        {
            dataSet.setDataSetSize((long) createDataSetContent().length());
            return this;
        }

        SimpleDataSetInformationDTO build()
        {
            return dataSet;
        }

        private String createDataSetContent()
        {
            return createContent(id * 11111);
        }
    }

    private final class ContainerBuilder
    {
        private MultiDataSetArchiverContainerDTO container;

        private List<MultiDataSetArchiverDataSetDTO> dataSets = new ArrayList<>();

        private long nextId;

        ContainerBuilder(long containerId, String path)
        {
            container = new MultiDataSetArchiverContainerDTO(containerId, path);
        }

        ContainerBuilder exists(long size)
        {
            return exists((int) size);
        }

        ContainerBuilder exists(int size)
        {
            FileUtilities.writeToFile(new File(archive, container.getPath()), createContent(size));
            return this;
        }

        ContainerBuilder add(SimpleDataSetInformationDTO dataSet)
        {
            return add(dataSet, dataSet.getDataSetSize());
        }

        ContainerBuilder add(SimpleDataSetInformationDTO dataSet, long size)
        {
            dataSets.add(new MultiDataSetArchiverDataSetDTO(nextId++, dataSet.getDataSetCode(),
                    container.getId(), size));
            return this;
        }

        void buildAndPrepare()
        {
            context.checking(new Expectations()
                {
                    {
                        for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
                        {
                            allowing(queryDAO).getDataSetForCode(dataSet.getCode());
                            will(returnValue(dataSet));
                        }
                        allowing(queryDAO).getContainerForId(container.getId());
                        will(returnValue(container));
                        allowing(queryDAO).listDataSetsForContainerId(container.getId());
                        will(returnValue(dataSets));
                    }
                });
        }
    }

    private void setUpTask()
    {
        Properties properties = new Properties();
        properties.setProperty(PluginTaskInfoProvider.ARCHIVER_SECTION_NAME + "."
                + MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY, archive.getPath());
        task.setUp("", properties);
    }

    private File getDataSetFolder(SimpleDataSetInformationDTO dataSet, boolean shouldBeCreated)
    {
        File folder = new File(scratchShare, dataSet.getDataSetLocation());
        if (shouldBeCreated)
        {
            folder.mkdirs();
        }
        return folder;
    }

    private File createFolder(File parent, String folderName)
    {
        File folder = new File(parent, folderName);
        folder.mkdirs();
        return folder;
    }

    private static String createContent(int size)
    {
        return String.join("", Collections.nCopies(size, "a"));
    }

    private CleanUpUnarchivingScratchShareTask createTask()
    {
        return new CleanUpUnarchivingScratchShareTask()
            {
                @Override
                protected IShareIdManager getShareIdManager()
                {
                    return shareIdManager;
                }

                @Override
                protected IDataSetDirectoryProvider getDirectoryProvider()
                {
                    return directoryProvider;
                }

                @Override
                protected File getStoreRoot()
                {
                    return store;
                }

                @Override
                protected IEncapsulatedOpenBISService getService()
                {
                    return openBISService;
                }

                @Override
                protected IFreeSpaceProvider createFreeSpaceProvider()
                {
                    return freeSpaceProvider;
                }

                @Override
                protected IMultiDataSetArchiverReadonlyQueryDAO getReadonlyQuery()
                {
                    return queryDAO;
                }

                @Override
                protected IConfigProvider getConfigProvider()
                {
                    return configProvider;
                }
            };
    }
}
