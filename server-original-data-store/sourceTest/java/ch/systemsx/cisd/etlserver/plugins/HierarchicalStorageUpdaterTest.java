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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Kaloyan Enimanev
 */
public class HierarchicalStorageUpdaterTest extends AbstractFileSystemTestCase
{

    private final static File STORE_ROOT_TEMPLATE = new File("./resource/test-data/"
            + HierarchicalStorageUpdaterTest.class.getSimpleName() + "/store-root");

    private final static String DATASET_TYPE = "dataset-type";

    private final static String SHARE_ID = "1";

    private final static FileFilter ignoreSVNFiles = new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.getName().equals(".svn") == false;
            }
        };

    private IEncapsulatedOpenBISService openBISService;

    private Mockery context;

    public HierarchicalStorageUpdaterTest()
    {
        super(false);
    }

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setUpMocks() throws Exception
    {

        prepareDirectoryStructures();

        context = new Mockery();
        openBISService = context.mock(IEncapsulatedOpenBISService.class);
        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);

        context.checking(new Expectations()
            {
                {
                    allowing(beanFactory).getBean("openBIS-service");
                    will(returnValue(openBISService));

                    allowing(openBISService).listPhysicalDataSets();
                    will(returnValue(listDataSets()));

                    allowing(openBISService).listDataSetsByCode(with(any(List.class)));
                    will(returnValue(listAbstractDataSets()));
                }
            });
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    @DataProvider(name = "Configs")
    protected Object[][] getConfigs()
    {
        return new Object[][] {
                { true }, { false } };
    }

    @Test(dataProvider = "Configs")
    public void testDataIsNotDeletedAfterReconfig(boolean linksOnly) throws Exception
    {

        updater(linksOnly).execute();

        // execute with different configuration and attempt to damage the data store
        reconfiguredUpdater(linksOnly).execute();

        assertDataStoreNotDamaged();

    }

    @Test(dataProvider = "Configs")
    public void testBrokenLinksAreDeleted(boolean linksOnly) throws Exception
    {

        HierarchicalStorageUpdater storageUpdater = createUpdater(true, linksOnly);
        storageUpdater.execute();

        File shareRoot = new File(getStoreRoot(), SHARE_ID);
        File dataSetSource = new File(shareRoot, "ds2");
        assertTrue(dataSetSource.isDirectory());

        File locationInHierarchicalStore =
                new File(getHierarchyRoot().getAbsolutePath()
                        + "/space/project/experiment/dataset-type+sample+ds2");

        File symlinkInside = new File(locationInHierarchicalStore, "data");
        File metaDataFileInside = new File(locationInHierarchicalStore, "meta-data.tsv");

        if (linksOnly)
        {
            assertTrue("Symbolic link should be created", FileUtilities.isSymbolicLink(locationInHierarchicalStore));
        } else
        {
            assertTrue("Directory should be created", locationInHierarchicalStore.isDirectory());
            assertTrue("Meta data file should be created", metaDataFileInside.isFile());
            assertTrue("Symlink should be created ", FileUtilities.isSymbolicLink(symlinkInside));
        }

        FileUtilities.deleteRecursively(dataSetSource);

        storageUpdater.execute();

        if (linksOnly)
        {
            assertTrue("Broken symlinks should be deleted", false == locationInHierarchicalStore.exists());
        } else
        {
            assertTrue("Directory should be kept", locationInHierarchicalStore.isDirectory());
            assertTrue("Meta data file should be kept", metaDataFileInside.isFile());
            assertTrue("Symlink should be deleted", false == symlinkInside.exists());
        }
    }

    @Test
    public void testMetaDataCreated() throws Exception
    {
        HierarchicalStorageUpdater storageUpdater = createUpdater(true, false);
        storageUpdater.execute();

        File shareRoot = new File(getStoreRoot(), SHARE_ID);
        File dataSetSource = new File(shareRoot, "ds2");
        assertTrue(dataSetSource.isDirectory());

        File directory =
                new File(getHierarchyRoot().getAbsolutePath()
                        + "/space/project/experiment/dataset-type+sample+ds2");
        assertTrue("Directory should be created", directory.isDirectory());

        File metaDataFile = new File(directory, "meta-data.tsv");
        assertTrue("metadata files created", metaDataFile.exists());
        List<String> content = FileUtilities.loadToStringList(metaDataFile);
        assertEquals("data_set\tcode\tds2", content.get(0));
    }

    private void prepareDirectoryStructures() throws IOException
    {
        FileUtils.copyDirectory(STORE_ROOT_TEMPLATE, getStoreRoot(), ignoreSVNFiles);
        getHierarchyRoot().mkdirs();
    }

    private File getStoreRoot()
    {
        return new File(workingDirectory, "store-root");
    }

    private File getHierarchyRoot()
    {
        return new File(workingDirectory, "hierarchy-root");
    }

    private void assertDataStoreNotDamaged() throws Exception
    {
        File cleanStorage = new File(workingDirectory, "clean-storage-nosvn");
        FileUtils.copyDirectory(STORE_ROOT_TEMPLATE, cleanStorage, ignoreSVNFiles);

        long templateSize = FileUtils.sizeOfDirectory(cleanStorage);
        long rootSize = FileUtils.sizeOfDirectory(getStoreRoot());

        String errMessage =
                String.format("The data-store root in '%s' has been damaged", getStoreRoot()
                        .getAbsolutePath());
        assertEquals(errMessage, templateSize, rootSize);
    }

    private HierarchicalStorageUpdater updater(boolean linksOnly)
    {
        return createUpdater(false, linksOnly);
    }

    private HierarchicalStorageUpdater reconfiguredUpdater(boolean linksOnly)
    {
        return createUpdater(true, linksOnly);
    }

    private HierarchicalStorageUpdater createUpdater(boolean linkFromFirstChild, boolean onlyLinks)
    {
        final String pluginName = "hierarchical-storage-updater";

        Properties properties = new Properties();
        properties.setProperty(DssPropertyParametersUtil.STOREROOT_DIR_KEY, getStoreRoot().getAbsolutePath());
        properties.setProperty(HierarchicalStorageUpdater.HIERARCHY_ROOT_DIR_KEY,
                getHierarchyRoot().getAbsolutePath());
        if (linkFromFirstChild)
        {
            properties.put(HierarchicalStorageUpdater.LINK_SOURCE_SUBFOLDER + "." + DATASET_TYPE,
                    "original");
            properties.put(HierarchicalStorageUpdater.LINK_FROM_FIRST_CHILD + "." + DATASET_TYPE,
                    "" + true);
        }

        properties.put(HierarchicalStorageUpdater.WITH_META_DATA, onlyLinks ? "false" : "true");

        HierarchicalStorageUpdater updater = new HierarchicalStorageUpdater();
        updater.setUp(pluginName, properties);
        return updater;
    }

    private List<SimpleDataSetInformationDTO> listDataSets()
    {
        final File shareRoot = new File(getStoreRoot(), SHARE_ID);
        final List<SimpleDataSetInformationDTO> result = new ArrayList<SimpleDataSetInformationDTO>();
        for (File directory : FileUtilities.listDirectories(shareRoot, false))
        {
            SimpleDataSetInformationDTO dataset = new SimpleDataSetInformationDTO();
            result.add(dataset);
            dataset.setDataSetType(DATASET_TYPE);
            dataset.setDataSetCode(directory.getName());
            dataset.setDataSetLocation(directory.getName());
            dataset.setDataSetShareId(SHARE_ID);
            dataset.setExperimentCode("experiment");
            dataset.setSpaceCode("space");
            dataset.setProjectCode("project");
            dataset.setSampleCode("sample");
        }
        return result;
    }

    List<AbstractExternalData> listAbstractDataSets()
    {
        final File shareRoot = new File(getStoreRoot(), SHARE_ID);
        final List<AbstractExternalData> result = new ArrayList<>();
        for (File directory : FileUtilities.listDirectories(shareRoot, false))
        {
            PhysicalDataSet dataset = new PhysicalDataSet();
            result.add(dataset);
            dataset.setDataSetType(new DataSetType(DATASET_TYPE));
            dataset.setCode(directory.getName());
            dataset.setLocation(directory.getName());
            dataset.setShareId(SHARE_ID);

            dataset.setPresentInArchive(false);
            dataset.setStatus(DataSetArchivingStatus.AVAILABLE);

            Space space = new Space();
            space.setCode("space");
            Project project = new Project();
            project.setSpace(space);
            project.setCode("project");
            Experiment experiment = new Experiment();
            experiment.setProject(project);
            experiment.setCode("experiment");

            ExperimentType experimentType = new ExperimentType();
            experimentType.setCode("experiment_type");
            experiment.setExperimentType(experimentType);
            dataset.setExperiment(experiment);

            Sample sample = new Sample();
            sample.setCode("sample");
            sample.setSpace(space);
            sample.setExperiment(experiment);
            SampleType sampleType = new SampleType();
            sampleType.setCode("sample_type");
            sample.setSampleType(sampleType);
            dataset.setSample(sample);

            dataset.setModificationDate(new Date());
        }
        return result;
    }

}
