/*
 * Copyright 2014 ETH Zuerich, SIS
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

import static ch.systemsx.cisd.etlserver.plugins.BlastDatabaseCreationMaintenanceTask.BLAST_TEMP_FOLDER_PROPERTY;
import static ch.systemsx.cisd.etlserver.plugins.BlastDatabaseCreationMaintenanceTask.DATASET_TYPES_PROPERTY;
import static ch.systemsx.cisd.etlserver.plugins.BlastDatabaseCreationMaintenanceTask.ENTITY_SEQUENCE_PROPERTIES_PROPERTY;
import static ch.systemsx.cisd.etlserver.plugins.BlastDatabaseCreationMaintenanceTask.FILE_TYPES_PROPERTY;
import static ch.systemsx.cisd.etlserver.plugins.BlastDatabaseCreationMaintenanceTask.LAST_SEEN_DATA_SET_FILE_PROPERTY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.BlastUtils;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ContainerDataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;
import de.schlichtherle.io.File;

/**
 * @author Franz-Josef Elmer
 */
public class BlastDatabaseCreationMaintenanceTaskTest extends AbstractFileSystemTestCase
{
    private static final String INFO_PREFIX = "INFO  OPERATION." + BlastDatabaseCreationMaintenanceTask.class.getSimpleName() + " - ";

    private static final String ERROR_PREFIX = "ERROR OPERATION." + BlastDatabaseCreationMaintenanceTask.class.getSimpleName() + " - ";

    private static final class MockMaintenanceTask extends BlastDatabaseCreationMaintenanceTask
    {
        private class MockBlastUtils extends BlastUtils
        {

            public MockBlastUtils(Properties properties, java.io.File storeRoot)
            {
                super(properties, storeRoot);
            }

            @Override
            public boolean process(String... command)
            {
                commands.add(Arrays.asList(command));
                return processSuccesses[indexOfNextProcessSuccessValue++ % processSuccesses.length];
            }
        }

        private final List<List<String>> commands = new ArrayList<List<String>>();

        private final IConfigProvider configProvider;

        private final IEncapsulatedOpenBISService service;

        private final IHierarchicalContentProvider contentProvider;

        private boolean[] processSuccesses;

        private int indexOfNextProcessSuccessValue;

        MockMaintenanceTask(IConfigProvider configProvider, IEncapsulatedOpenBISService service,
                IHierarchicalContentProvider contentProvider)
        {
            this.configProvider = configProvider;
            this.service = service;
            this.contentProvider = contentProvider;
        }

        @Override
        protected BlastUtils getBlaster(Properties properties)
        {
            return new MockBlastUtils(properties, getConfigProvider().getStoreRoot());
        }

        void setProcessSuccesses(boolean... processSuccesses)
        {
            this.processSuccesses = processSuccesses;
            indexOfNextProcessSuccessValue = 0;
        }

        String getCommands()
        {
            StringBuilder builder = new StringBuilder();
            for (List<String> command : commands)
            {
                for (int i = 0; i < command.size(); i++)
                {
                    if (i > 0)
                    {
                        builder.append(' ');
                    }
                    builder.append(command.get(i));
                }
                builder.append('\n');
            }
            return builder.toString();
        }

        @Override
        IConfigProvider getConfigProvider()
        {
            return configProvider;
        }

        @Override
        IEncapsulatedOpenBISService getOpenBISService()
        {
            return service;
        }

        @Override
        IHierarchicalContentProvider getContentProvider()
        {
            return contentProvider;
        }
    }

    private BufferedAppender logRecorder;

    private Mockery context;

    private IConfigProvider configProvider;

    private IEncapsulatedOpenBISService service;

    private IHierarchicalContentProvider contentProvider;

    private MockMaintenanceTask maintenanceTask;

    private File store;

    private File blastDatabaseFolder;

    @BeforeMethod
    public void setUpTask()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        configProvider = context.mock(IConfigProvider.class);
        service = context.mock(IEncapsulatedOpenBISService.class);
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        maintenanceTask = new MockMaintenanceTask(configProvider, service, contentProvider);
        maintenanceTask.setProcessSuccesses(true);
        store = new File(workingDirectory, "store");
        blastDatabaseFolder = new File(store, BlastUtils.DEFAULT_BLAST_DATABASES_FOLDER);
        blastDatabaseFolder.mkdirs();
        context.checking(new Expectations()
            {
                {
                    allowing(configProvider).getStoreRoot();
                    will(returnValue(store));
                }
            });
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testSetUpMissingDataSetTypesPropertyAndEntitySequencePropertiesProperty()
    {
        try
        {
            maintenanceTask.setUp("", new Properties());
            fail("ConfigurationFailureException expected.");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("At least one of the two properties have to be defined: dataset-types, entity-sequence-properties",
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testSetUpInvalidDataSetTypeRegex()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, "[a-z");
        try
        {
            maintenanceTask.setUp("", properties);
            fail("ConfigurationFailureException expected.");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Property '" + DATASET_TYPES_PROPERTY + "' has invalid regular expression '[A-Z': "
                    + "Unclosed character class near index 3\n[A-Z\n   ^", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testWrongBlastToolsFolderNoDefaultBlast()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, ".+");
        maintenanceTask.setProcessSuccesses(false);

        maintenanceTask.setUp("", properties);

        assertEquals("ERROR OPERATION.BlastUtils - Property '" + BlastUtils.BLAST_TOOLS_DIRECTORY_PROPERTY
                + "' is not specified and BLAST isn't found in default location.\n"
                + INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                + INFO_PREFIX + "BLAST databases folder: " + blastDatabaseFolder + "\n"
                + INFO_PREFIX + "Temp folder '" + blastDatabaseFolder + "/tmp' created.", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testWrongBlastToolsFolderNoManualBlast()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, ".+");
        properties.setProperty(BlastUtils.BLAST_TOOLS_DIRECTORY_PROPERTY, "/no/such/blast");
        maintenanceTask.setProcessSuccesses(false);

        maintenanceTask.setUp("", properties);

        assertEquals(
                "ERROR OPERATION.BlastUtils - BLAST isn't installed in location '/no/such/blast' specified by the property 'blast-tools-directory.\n"
                        + INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                        + INFO_PREFIX + "BLAST databases folder: " + blastDatabaseFolder + "\n"
                        + INFO_PREFIX + "Temp folder '" + blastDatabaseFolder + "/tmp' created.", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testMinimumSetUp()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, ".+");

        maintenanceTask.setUp("BLAST databases creation", properties);

        assertEquals(INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                + INFO_PREFIX + "BLAST databases folder: " + blastDatabaseFolder + "\n"
                + INFO_PREFIX + "Temp folder '" + blastDatabaseFolder + "/tmp' created.",
                logRecorder.getLogContent());
        assertEquals("blastn -version\n", maintenanceTask.getCommands());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteOneDataSetWithThreeFastaFiles()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, "BLAST.*");
        maintenanceTask.setUp("", properties);
        AbstractExternalData ds1 = new ContainerDataSetBuilder(1L).type("BLAST_CONTAINER").code("DS-1").getContainerDataSet();
        AbstractExternalData ds2 = new DataSetBuilder(2L).type("NOBLAST").code("DS-2")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        AbstractExternalData ds3 = new DataSetBuilder(3L).type("BLAST").code("DS-3")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        AbstractExternalData ds4 = new DataSetBuilder(3L).type("BLAST").code("DS-4")
                .status(DataSetArchivingStatus.ARCHIVED).getDataSet();
        RecordingMatcher<TrackingDataSetCriteria> lastSeenIdMatcher = prepareListNewerDataSet(ds4, ds2, ds3, ds1);
        File dataSetFolder1 = new File(workingDirectory, "data-set-example1");
        File dataFolder = new File(dataSetFolder1, "data");
        dataFolder.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFolder1, "fasta.txt"), ">1\nGATTACA\n");
        FileUtilities.writeToFile(new File(dataSetFolder1, "fasta1.fa"), ">2\nGATTACA\nGATTACA\n");
        FileUtilities.writeToFile(new File(dataFolder, "fasta2.fastq"), "@3\nIAKKATA\n+\nznhjnxzx\n");
        prepareContentProvider(ds3, dataSetFolder1);
        prepareListsDeletedDataSets();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 11e5; i++)
        {
            stringBuilder.append("GATTACA".charAt(i % 7));
        }
        // Creating a big sequence dummy file in order to trigger index creation
        FileUtilities.writeToFile(new File(blastDatabaseFolder, "DS-3-prot.nsq"), stringBuilder.toString());

        maintenanceTask.execute();

        assertEquals(INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                + INFO_PREFIX + "BLAST databases folder: " + blastDatabaseFolder + "\n"
                + INFO_PREFIX + "Temp folder '" + blastDatabaseFolder + "/tmp' created.\n"
                + INFO_PREFIX + "Scan 4 data sets for creating BLAST databases.", logRecorder.getLogContent());
        assertEquals("blastn -version\n"
                + "makeblastdb -in " + blastDatabaseFolder.getAbsolutePath() + "/tmp/DS-3-nucl.fa"
                + " -dbtype nucl -title DS-3-nucl -out " + blastDatabaseFolder.getAbsolutePath() + "/DS-3-nucl\n"
                + "makeblastdb -in " + blastDatabaseFolder.getAbsolutePath() + "/tmp/DS-3-prot.fa"
                + " -dbtype prot -title DS-3-prot -out " + blastDatabaseFolder.getAbsolutePath() + "/DS-3-prot\n"
                + "makembindex -iformat blastdb -input " + blastDatabaseFolder.getAbsolutePath() + "/DS-3-prot"
                + " -old_style_index false\n",
                maintenanceTask.getCommands());
        assertEquals("TITLE all-nucl\nDBLIST DS-3-nucl",
                FileUtilities.loadToString(new File(blastDatabaseFolder, "all-nucl.nal")).trim());
        assertEquals("TITLE all-prot\nDBLIST DS-3-prot",
                FileUtilities.loadToString(new File(blastDatabaseFolder, "all-prot.pal")).trim());
        assertEquals("[]", Arrays.asList(new File(blastDatabaseFolder, "tmp").listFiles()).toString());
        assertEquals(0L, lastSeenIdMatcher.recordedObject().getLastSeenDataSetId());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteThreeDataSetsWhereTwoHaveFastaFiles()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, ".*");
        maintenanceTask.setUp("", properties);
        AbstractExternalData ds1 = new DataSetBuilder(1L).type("NOBLAST").code("DS1")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        AbstractExternalData ds2 = new DataSetBuilder(2L).type("BLAST").code("DS2")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        AbstractExternalData ds3 = new DataSetBuilder(3L).type("BLAST").code("DS3")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        RecordingMatcher<TrackingDataSetCriteria> lastSeenIdMatcher = prepareListNewerDataSet(ds3, ds2, ds1);
        File dataSetFolder1 = new File(workingDirectory, "data-set-example1");
        dataSetFolder1.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFolder1, "fasta.txt"), ">1\nGATTACA\n");
        prepareContentProvider(ds1, dataSetFolder1);
        File dataSetFolder2 = new File(workingDirectory, "data-set-example2");
        dataSetFolder2.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFolder2, "fasta.fa"), ">2\nGATTACA\nGATTACA\n");
        prepareContentProvider(ds2, dataSetFolder2);
        File dataSetFolder3 = new File(workingDirectory, "data-set-example3");
        dataSetFolder3.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFolder3, "fasta.fasta"), ">3\nGATTACA\nGATTACA\nGATTACA\n");
        prepareContentProvider(ds3, dataSetFolder3);
        prepareListsDeletedDataSets();

        maintenanceTask.execute();

        assertEquals(INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                + INFO_PREFIX + "BLAST databases folder: " + blastDatabaseFolder + "\n"
                + INFO_PREFIX + "Temp folder '" + blastDatabaseFolder + "/tmp' created.\n"
                + INFO_PREFIX + "Scan 3 data sets for creating BLAST databases.", logRecorder.getLogContent());
        assertEquals("blastn -version\n"
                + "makeblastdb -in " + blastDatabaseFolder.getAbsolutePath() + "/tmp/DS2-nucl.fa"
                + " -dbtype nucl -title DS2-nucl -out " + blastDatabaseFolder.getAbsolutePath() + "/DS2-nucl\n"
                + "makeblastdb -in " + blastDatabaseFolder.getAbsolutePath() + "/tmp/DS3-nucl.fa"
                + " -dbtype nucl -title DS3-nucl -out " + blastDatabaseFolder.getAbsolutePath() + "/DS3-nucl\n",
                maintenanceTask.getCommands());
        assertEquals("TITLE all-nucl\nDBLIST DS2-nucl DS3-nucl",
                FileUtilities.loadToString(new File(blastDatabaseFolder, "all-nucl.nal")).trim());
        assertEquals(false, new File(blastDatabaseFolder, "all-prot.nal").exists());
        assertEquals("[]", Arrays.asList(new File(blastDatabaseFolder, "tmp").listFiles()).toString());
        assertEquals(0L, lastSeenIdMatcher.recordedObject().getLastSeenDataSetId());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForSequencesInEntityProperties()
    {
        Properties properties = new Properties();
        properties.setProperty(ENTITY_SEQUENCE_PROPERTIES_PROPERTY, "SAMPLE+TS+PS, EXPERIMENT+TE+PE, DATA_SET+TD+PD");
        SampleBuilder s1 = new SampleBuilder("/A/S1").permID("1").property("A", "123");
        SampleBuilder s2 = new SampleBuilder("/A/S2").permID("2").property("PS", "GATTACA")
                .modificationDate(new Date(123456789));
        SampleBuilder s3 = new SampleBuilder("/A/S3").permID("3").property("PS", "CAGATAA")
                .modificationDate(new Date(987654321));
        prepareListSamples("TS", s1, s2, s3);
        ExperimentBuilder e1 = new ExperimentBuilder().identifier("/S/P/E1").permID("10").property("B", "GATTACA");
        ExperimentBuilder e2 = new ExperimentBuilder().identifier("/S/P/E2").permID("10").property("PE", "AFFE")
                .modificationDate(new Date(234567891));
        prepareListExperiments("TE", e1, e2);
        DataSetBuilder ds1 = new DataSetBuilder().code("ds1").property("C", "GATTACA");
        DataSetBuilder ds2 = new DataSetBuilder().code("ds2").property("PD", "APE")
                .modificationDate(new Date(345678912));
        DataSetBuilder ds3 = new DataSetBuilder().code("ds3").property("PD", "TACAGA")
                .modificationDate(new Date(456789123));
        prepareListDataSets("TD", ds1, ds2, ds3);
        prepareListsDeletedDataSets();
        maintenanceTask.setUp("", properties);

        maintenanceTask.execute();

        assertEquals(INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                + INFO_PREFIX + "BLAST databases folder: " + blastDatabaseFolder + "\n"
                + INFO_PREFIX + "Temp folder '" + blastDatabaseFolder + "/tmp' created.", logRecorder.getLogContent());
        assertEquals("blastn -version\n"
                + "makeblastdb -in "
                + blastDatabaseFolder.getAbsolutePath() + "/tmp/SAMPLE+TS+PS+19700112112054-nucl.fa"
                + " -dbtype nucl -title SAMPLE+TS+PS+19700112112054-nucl -out "
                + blastDatabaseFolder.getAbsolutePath() + "/SAMPLE+TS+PS+19700112112054-nucl\n"

                + "makeblastdb -in "
                + blastDatabaseFolder.getAbsolutePath() + "/tmp/EXPERIMENT+TE+PE+19700103180927-prot.fa"
                + " -dbtype prot -title EXPERIMENT+TE+PE+19700103180927-prot -out "
                + blastDatabaseFolder.getAbsolutePath() + "/EXPERIMENT+TE+PE+19700103180927-prot\n"

                + "makeblastdb -in "
                + blastDatabaseFolder.getAbsolutePath() + "/tmp/DATA_SET+TD+PD+19700106075309-nucl.fa"
                + " -dbtype nucl -title DATA_SET+TD+PD+19700106075309-nucl -out "
                + blastDatabaseFolder.getAbsolutePath() + "/DATA_SET+TD+PD+19700106075309-nucl\n"

                + "makeblastdb -in "
                + blastDatabaseFolder.getAbsolutePath() + "/tmp/DATA_SET+TD+PD+19700105010118-prot.fa"
                + " -dbtype prot -title DATA_SET+TD+PD+19700105010118-prot -out "
                + blastDatabaseFolder.getAbsolutePath() + "/DATA_SET+TD+PD+19700105010118-prot\n",
                maintenanceTask.getCommands());
        assertEquals("TITLE all-nucl\nDBLIST DATA_SET+TD+PD+19700106075309-nucl SAMPLE+TS+PS+19700112112054-nucl",
                FileUtilities.loadToString(new File(blastDatabaseFolder, "all-nucl.nal")).trim());
        assertEquals("TITLE all-prot\nDBLIST DATA_SET+TD+PD+19700105010118-prot EXPERIMENT+TE+PE+19700103180927-prot",
                FileUtilities.loadToString(new File(blastDatabaseFolder, "all-prot.pal")).trim());
        assertEquals("[]", Arrays.asList(new File(blastDatabaseFolder, "tmp").listFiles()).toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForSequencesInTwoSamplePropertiesRemovingPreviousVersion()
    {
        Properties properties = new Properties();
        properties.setProperty(ENTITY_SEQUENCE_PROPERTIES_PROPERTY, "SAMPLE+T+O, SAMPLE+T1+Q");
        SampleBuilder s1 = new SampleBuilder("/A/S1").permID("1").property("A", "123");
        SampleBuilder s2 = new SampleBuilder("/A/S2").permID("2").property("O", "GATTACA")
                .modificationDate(new Date(123456789));
        SampleBuilder s3 = new SampleBuilder("/A/S3").permID("3").property("Q", "CAGATAA")
                .modificationDate(new Date(987654321));
        prepareListSamples("T", s1, s2);
        prepareListSamples("T1", s3);
        prepareListsDeletedDataSets();
        File previousDatabaseFile = new File(blastDatabaseFolder, "SAMPLE+T+O+19700112112010-nucl.nhr");
        FileUtilities.writeToFile(previousDatabaseFile, "abc");
        FileUtilities.writeToFile(new File(blastDatabaseFolder, "all-nucl.nal"),
                "TITLE all-nucl\nDBLIST SAMPLE+T+O+19700112112010-nucl");
        maintenanceTask.setUp("", properties);

        maintenanceTask.execute();

        assertEquals(INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                + INFO_PREFIX + "BLAST databases folder: " + blastDatabaseFolder + "\n"
                + INFO_PREFIX + "Temp folder '" + blastDatabaseFolder + "/tmp' created.\n"
                + INFO_PREFIX + "BLAST database SAMPLE+T+O+19700112112010-nucl successfully deleted.",
                logRecorder.getLogContent());
        assertEquals("blastn -version\nmakeblastdb -in "
                + blastDatabaseFolder.getAbsolutePath() + "/tmp/SAMPLE+T+O+19700102111736-nucl.fa"
                + " -dbtype nucl -title SAMPLE+T+O+19700102111736-nucl -out "
                + blastDatabaseFolder.getAbsolutePath() + "/SAMPLE+T+O+19700102111736-nucl\n"
                + "makeblastdb -in " + blastDatabaseFolder.getAbsolutePath()
                + "/tmp/SAMPLE+T1+Q+19700112112054-nucl.fa"
                + " -dbtype nucl -title SAMPLE+T1+Q+19700112112054-nucl -out "
                + blastDatabaseFolder.getAbsolutePath() + "/SAMPLE+T1+Q+19700112112054-nucl\n",
                maintenanceTask.getCommands());
        assertEquals("TITLE all-nucl\nDBLIST SAMPLE+T+O+19700102111736-nucl SAMPLE+T1+Q+19700112112054-nucl",
                FileUtilities.loadToString(new File(blastDatabaseFolder, "all-nucl.nal")).trim());
        assertEquals(false, previousDatabaseFile.exists());
        assertEquals(false, new File(blastDatabaseFolder, "all-prot.nal").exists());
        assertEquals("[]", Arrays.asList(new File(blastDatabaseFolder, "tmp").listFiles()).toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForSequencesInSamplePropertiesWithNoUpdateNeeded()
    {
        Properties properties = new Properties();
        properties.setProperty(ENTITY_SEQUENCE_PROPERTIES_PROPERTY, "SAMPLE+T+O");
        SampleBuilder s1 = new SampleBuilder("/A/S1").permID("1").property("A", "123");
        SampleBuilder s2 = new SampleBuilder("/A/S2").permID("2").property("O", "GATTACA")
                .modificationDate(new Date(123456789));
        SampleBuilder s3 = new SampleBuilder("/A/S3").permID("3").property("O", "CAGATAA")
                .modificationDate(new Date(987654321));
        prepareListSamples("T", s1, s2, s3);
        prepareListsDeletedDataSets();
        File previousDatabaseFile = new File(blastDatabaseFolder, "SAMPLE+T+O+19700112112054-nucl.nhr");
        FileUtilities.writeToFile(previousDatabaseFile, "abc");
        FileUtilities.writeToFile(new File(blastDatabaseFolder, "all-nucl.nal"),
                "TITLE all-nucl\nDBLIST SAMPLE+T+O+19700112112054-nucl");
        maintenanceTask.setUp("", properties);

        maintenanceTask.execute();

        assertEquals(INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                + INFO_PREFIX + "BLAST databases folder: " + blastDatabaseFolder + "\n"
                + INFO_PREFIX + "Temp folder '" + blastDatabaseFolder + "/tmp' created.",
                logRecorder.getLogContent());
        assertEquals("blastn -version\n", maintenanceTask.getCommands());
        assertEquals("TITLE all-nucl\nDBLIST SAMPLE+T+O+19700112112054-nucl",
                FileUtilities.loadToString(new File(blastDatabaseFolder, "all-nucl.nal")).trim());
        assertEquals(true, previousDatabaseFile.exists());
        assertEquals(false, new File(blastDatabaseFolder, "all-prot.nal").exists());
        assertEquals("[]", Arrays.asList(new File(blastDatabaseFolder, "tmp").listFiles()).toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteFaileds()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, ".*");
        maintenanceTask.setUp("", properties);
        AbstractExternalData ds1 = new DataSetBuilder(1L).type("BLAST").code("DS1")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        RecordingMatcher<TrackingDataSetCriteria> lastSeenIdMatcher = prepareListNewerDataSet(ds1);
        File dataSetFolder1 = new File(workingDirectory, "data-set-example1");
        dataSetFolder1.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFolder1, "fasta.fastq"), "@1\nGATTACA\n+\nznhjnxzx\n");
        prepareContentProvider(ds1, dataSetFolder1);
        prepareListsDeletedDataSets();
        maintenanceTask.setProcessSuccesses(false);

        maintenanceTask.execute();

        assertEquals(INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                + INFO_PREFIX + "BLAST databases folder: " + blastDatabaseFolder + "\n"
                + INFO_PREFIX + "Temp folder '" + blastDatabaseFolder + "/tmp' created.\n"
                + INFO_PREFIX + "Scan 1 data sets for creating BLAST databases.\n"
                + ERROR_PREFIX + "Creation of BLAST database 'DS1-nucl' failed. Temporary fasta file: "
                + blastDatabaseFolder + "/tmp/DS1-nucl.fa", logRecorder.getLogContent());
        assertEquals("blastn -version\n"
                + "makeblastdb -in " + blastDatabaseFolder.getAbsolutePath() + "/tmp/DS1-nucl.fa"
                + " -dbtype nucl -title DS1-nucl -out " + blastDatabaseFolder.getAbsolutePath() + "/DS1-nucl\n",
                maintenanceTask.getCommands());
        assertEquals(false, new File(blastDatabaseFolder, "all-nucl.nal").exists());
        assertEquals(false, new File(blastDatabaseFolder, "all-prot.nal").exists());
        assertEquals("[]", Arrays.asList(new File(blastDatabaseFolder, "tmp").listFiles()).toString());
        assertEquals(0L, lastSeenIdMatcher.recordedObject().getLastSeenDataSetId());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteTwiceWithNonDefaultParameters()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, ".*");
        File blastDatabasesFolder = new File(workingDirectory, "blast-dbs");
        properties.setProperty(BlastUtils.BLAST_DATABASES_FOLDER_PROPERTY, blastDatabasesFolder.toString());
        File tempFolder = new File(workingDirectory, "temp");
        properties.setProperty(BLAST_TEMP_FOLDER_PROPERTY, tempFolder.toString());
        properties.setProperty(BlastUtils.BLAST_TOOLS_DIRECTORY_PROPERTY, "/usr/bin/blast");
        properties.setProperty(FILE_TYPES_PROPERTY, ".txt .f");
        File lastSeenFile = new File(workingDirectory, "last.txt");
        properties.setProperty(LAST_SEEN_DATA_SET_FILE_PROPERTY, lastSeenFile.toString());
        maintenanceTask.setUp("", properties);
        AbstractExternalData ds1 = new DataSetBuilder(11L).type("BLAST").code("DS1")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        RecordingMatcher<TrackingDataSetCriteria> lastSeenIdMatcher = prepareListNewerDataSet(ds1);
        File dataSetFolder1 = new File(workingDirectory, "data-set-example1");
        dataSetFolder1.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFolder1, "fasta.txt"), ">1\nGATTACA\n");
        prepareContentProvider(ds1, dataSetFolder1);
        prepareListsDeletedDataSets();
        maintenanceTask.execute();
        File ds1DatabaseFileDummy = new File(blastDatabasesFolder, "DS1-nucl.nhr");
        FileUtilities.writeToFile(ds1DatabaseFileDummy, "blabla");
        prepareListsDeletedDataSets(ds1.getCode());
        AbstractExternalData ds2 = new DataSetBuilder(12L).type("BLAST").code("DS2")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        lastSeenIdMatcher = prepareListNewerDataSet(ds2);
        prepareContentProvider(ds2, dataSetFolder1);

        maintenanceTask.execute();

        assertEquals(INFO_PREFIX + "File types: [.txt, .f]\n"
                + INFO_PREFIX + "BLAST databases folder: " + blastDatabasesFolder + "\n"
                + INFO_PREFIX + "Temp folder '" + tempFolder + "' created.\n"
                + INFO_PREFIX + "Scan 1 data sets for creating BLAST databases.\n"
                + INFO_PREFIX + "Scan 1 data sets for creating BLAST databases.\n"
                + INFO_PREFIX + "BLAST database DS1-nucl successfully deleted.", logRecorder.getLogContent());
        assertEquals("/usr/bin/blast/blastn -version\n"
                + "/usr/bin/blast/makeblastdb -in " + tempFolder.getAbsolutePath() + "/DS1-nucl.fa"
                + " -dbtype nucl -title DS1-nucl -out " + blastDatabasesFolder.getAbsolutePath() + "/DS1-nucl\n"
                + "/usr/bin/blast/makeblastdb -in " + tempFolder.getAbsolutePath() + "/DS2-nucl.fa"
                + " -dbtype nucl -title DS2-nucl -out " + blastDatabasesFolder.getAbsolutePath() + "/DS2-nucl\n",
                maintenanceTask.getCommands());
        assertEquals("TITLE all-nucl\nDBLIST DS2-nucl",
                FileUtilities.loadToString(new File(blastDatabasesFolder, "all-nucl.nal")).trim());
        assertEquals(false, new File(blastDatabaseFolder, "all-prot.nal").exists());
        assertEquals(false, ds1DatabaseFileDummy.exists());
        assertEquals("[]", Arrays.asList(tempFolder.listFiles()).toString());
        assertEquals(11L, lastSeenIdMatcher.recordedObject().getLastSeenDataSetId());
        assertEquals("12", FileUtilities.loadToString(lastSeenFile).trim());
        context.assertIsSatisfied();
    }

    private void prepareContentProvider(final AbstractExternalData dataSet, final File dataSetFolder)
    {
        context.checking(new Expectations()
            {
                {
                    one(contentProvider).asContent(dataSet);
                    will(returnValue(new DefaultFileBasedHierarchicalContentFactory().asHierarchicalContent(dataSetFolder, null)));
                }
            });
    }

    private void prepareListsDeletedDataSets(final String... dataSetCodes)
    {
        context.checking(new Expectations()
            {
                {
                    List<DeletedDataSet> result = new ArrayList<DeletedDataSet>();
                    for (String dataSetCode : dataSetCodes)
                    {
                        result.add(new DeletedDataSet(0, dataSetCode));
                    }
                    one(service).listDeletedDataSets(null, null);
                    will(returnValue(result));
                }
            });
    }

    private void prepareListSamples(final String sampleType, final SampleBuilder... samples)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).searchForSamples(createCriteria(sampleType));
                    List<Sample> result = new ArrayList<Sample>();
                    for (SampleBuilder sample : samples)
                    {
                        result.add(sample.getSample());
                    }
                    will(returnValue(result));
                }
            });
    }

    private void prepareListExperiments(final String experimentType, final ExperimentBuilder... experiments)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).searchForExperiments(createCriteria(experimentType));
                    List<Experiment> result = new ArrayList<Experiment>();
                    for (ExperimentBuilder experiment : experiments)
                    {
                        result.add(experiment.getExperiment());
                    }
                    will(returnValue(result));
                }
            });
    }

    private void prepareListDataSets(final String dataSetType, final DataSetBuilder... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).searchForDataSets(createCriteria(dataSetType));
                    List<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
                    for (DataSetBuilder dataSet : dataSets)
                    {
                        result.add(dataSet.getDataSet());
                    }
                    will(returnValue(result));
                }
            });
    }

    private RecordingMatcher<TrackingDataSetCriteria> prepareListNewerDataSet(final AbstractExternalData... dataSets)
    {
        final RecordingMatcher<TrackingDataSetCriteria> recordingMatcher = new RecordingMatcher<TrackingDataSetCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(service).listNewerDataSets(with(recordingMatcher));
                    will(returnValue(new ArrayList<AbstractExternalData>(Arrays.asList(dataSets))));
                }
            });
        return recordingMatcher;
    }

    private SearchCriteria createCriteria(final String type)
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, type));
        return searchCriteria;
    }

}
