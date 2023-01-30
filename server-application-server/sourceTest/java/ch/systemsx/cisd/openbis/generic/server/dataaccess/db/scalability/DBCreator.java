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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.scalability;

import static org.testng.AssertJUnit.assertNotNull;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.test.annotation.Rollback;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ILocatorTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.SampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * This class has one test method called main that can be run as a TestNG test using "create scalability DB" run configuration in eclipse and creates
 * a DB for scalability testing. The test is in "scalability" group which is included in nightly builds. No rollback is done after this test. <br>
 * <br>
 * At the beginning it creates a TSV file with materials in a given TSV directory that will be used in the next step. Then the DB is created from
 * scratch (with new materials added from the TSV file). Afterwards it:
 * <ul>
 * <li>doesn't create any new properties nor attaches properties to any created entity because it can be easily done by GUI to a certain EntityType
 * <li>creates Experiments of one new ExperimentType
 * <li>creates Samples of one new SampleType - these samples are connected to an experiment created in the previous step. Each experiment will have
 * one or more samples connected to it.
 * <li>creates DataSets of one new DataSetType - these data sets are connected to a sample and an experiment created in previous steps. Each sample
 * will have one or more data sets connected to it.
 * </ul>
 * <br>
 * IMPORTANT - to make it faster try:
 * <ul>
 * <li>commenting out flush() in create methods for in {@link IDataDAO} and {@link SampleDAO}
 * <li>turning off logging (doesn't make a big difference) - change root logging priority in log.xml from "info" to "error".
 * </ul>
 * To log the current state of particular entity creation change static LOG variable value to true.
 * 
 * @author Piotr Buczek
 */
@Test(groups = { "scalability" })
public final class DBCreator extends AbstractDAOTest
{
    /** a sufix that will be used in the created DB name */
    private static final String DB_KIND = "test_scalability_80000";

    /** directory with TSV files used to create the DB from scratch */
    // private static final String TSV_DIRECTORY =
    // "sourceTest/sql/postgresql/" + DatabaseVersionHolder.getDatabaseVersion();
    // number properties
    /** a factor for scaling number of all created entities */

    // private static final int FACTOR = 1;
    /** the overall number of Materials created */
    // private static final int MATERIALS_NO = 0;// FACTOR * 1000;
    // private static final int EXPERIMENTS_NO = 5;
    // private static final int BIG_EXPERIMENTS_NO = 1;
    // private static final int BIG_SAMPLES_NO = 1;
    // private static final int DEFAULT_EXPERIMENT_SAMPLES_SIZE = 0;
    // private static final int BIG_EXPERIMENT_SAMPLES_SIZE = 10;
    // private static final int DEFAULT_SAMPLE_DATASETS_SIZE = 100;
    // private static final int BIG_SAMPLE_DATASETS_SIZE = 400;
    private static final int EXPERIMENTS_NO = 200;

    private static final int BIG_EXPERIMENTS_NO = 0;

    private static final int BIG_SAMPLES_NO = 0;

    private static final int DEFAULT_EXPERIMENT_SAMPLES_SIZE = 200;

    private static final int BIG_EXPERIMENT_SAMPLES_SIZE = DEFAULT_EXPERIMENT_SAMPLES_SIZE;

    private static final int DEFAULT_SAMPLE_DATASETS_SIZE = 2;

    private static final int BIG_SAMPLE_DATASETS_SIZE = DEFAULT_SAMPLE_DATASETS_SIZE;

    //

    private static final boolean LOG = false;

    private static void log(String format, Object... objects)
    {
        if (LOG)
        {
            System.err.println(String.format(format, objects));
        }
    }

    @BeforeTest()
    public void beforeTest()
    {
        // changes DB configuration to create a new DB
        System.setProperty("database.kind", DB_KIND);
        System.setProperty("database.create-from-scratch", "true");
        System.setProperty("authorization-component-factory", "no-authorization");
        System.setProperty("hibernate.search.batch-size", "10");
        // MaterialHelper.createMaterialsTSVFile(TSV_DIRECTORY, MATERIALS_NO);
        log("created materials TSV file");
    }

    @Test
    @Rollback(value = false)
    public final void main() throws Exception
    {
        hibernateTemplate = new HibernateTemplate(sessionFactory);

        createAndSetDefaultEntities();
        createExperimentsWithSamplesAndDataSets();
    }

    // Hibernate Session

    private HibernateTemplate hibernateTemplate;

    /** flushes and clears {@link Session} (makes creation of new objects faster) */
    private final void flushAndClearSession()
    {
        hibernateTemplate.flush();
        hibernateTemplate.clear();
    }

    // default entities

    private SampleTypePE defaultSampleType;

    private ExperimentTypePE defaultExperimentType;

    private DataSetTypePE defaultDataSetType;

    private SpacePE defaultGroup;

    private ProjectPE defaultProject;

    // Default

    private final void createAndSetDefaultEntities()
    {
        defaultGroup = createDefaultGroup();
        defaultProject = createDefaultProject();

        defaultDataSetType = createDefaultDataSetType();
        defaultExperimentType = createDefaultExperimentType();
        defaultSampleType = createDefaultSampleType();
    }

    private final ExperimentTypePE createDefaultExperimentType()
    {
        return createEntityType(new ExperimentTypePE(), EntityKind.EXPERIMENT,
                CreatedEntityKind.EXPERIMENT_TYPE);
    }

    private final SampleTypePE createDefaultSampleType()
    {
        SampleTypePE newSampleType = new SampleTypePE();
        newSampleType.setGeneratedFromHierarchyDepth(0);
        newSampleType.setContainerHierarchyDepth(0);
        newSampleType.setListable(true);

        return createEntityType(newSampleType, EntityKind.SAMPLE, CreatedEntityKind.SAMPLE_TYPE);
    }

    private final DataSetTypePE createDefaultDataSetType()
    {
        return createEntityType(new DataSetTypePE(), EntityKind.DATA_SET,
                CreatedEntityKind.DATA_SET_TYPE);
    }

    private final <T extends EntityTypePE> T createEntityType(T newEntityType,
            EntityKind entityKind, CreatedEntityKind createdEntityTypeKind)
    {
        final IEntityTypeDAO entityTypeDAO = daoFactory.getEntityTypeDAO(entityKind);
        final T entityType = newEntityType;
        entityType.setCode(CodeGenerator.generateDefaultCode(createdEntityTypeKind));
        entityTypeDAO.createOrUpdateEntityType(entityType);

        return entityType;
    }

    private final SpacePE createDefaultGroup()
    {
        final String code = CodeGenerator.generateDefaultCode(CreatedEntityKind.GROUP);
        return createSpace(code);
    }

    private final ProjectPE createDefaultProject()
    {
        final String code = CodeGenerator.generateDefaultCode(CreatedEntityKind.PROJECT);
        final ProjectPE project = new ProjectPE();
        project.setCode(code);
        project.setPermId(daoFactory.getPermIdDAO().createPermId());
        project.setSpace(defaultGroup);
        project.setProjectLeader(getSystemPerson());
        project.setRegistrator(getSystemPerson());
        daoFactory.getProjectDAO().createProject(project, getTestPerson());

        return project;
    }

    // Experiments

    private void createExperimentsWithSamplesAndDataSets()
    {
        for (int i = 1; i <= EXPERIMENTS_NO; i++)
        {
            long start = System.currentTimeMillis();
            log("creating experiment: %d/%d", i, EXPERIMENTS_NO);
            ExperimentPE experiment = generateExperiment();
            daoFactory.getExperimentDAO().createOrUpdateExperiment(experiment, getTestPerson());
            createSamplesWithDataSetsForExperiment(experiment);
            flushAndClearSession();
            long time = (System.currentTimeMillis() - start) / 1000;
            System.err.println(String.format("Created %s out of %s (%s sec,to go: %s min)", i,
                    EXPERIMENTS_NO, time, (EXPERIMENTS_NO - i) * time / 60));
        }
        log("created experiments");
    }

    private ExperimentPE generateExperiment()
    {
        ExperimentTypePE type = defaultExperimentType;
        String code = CodeGenerator.generateCode(CreatedEntityKind.EXPERIMENT);

        final ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(code);
        experiment.setPermId(daoFactory.getPermIdDAO().createPermId());
        experiment.setExperimentType(type);
        experiment.setProject(defaultProject);
        experiment.setRegistrationDate(new Date());
        experiment.setRegistrator(getSystemPerson());
        return experiment;
    }

    private void createSamplesWithDataSetsForExperiment(ExperimentPE experiment)
    {
        final int size = SizeHelper.getNextSamplesPerExperimentSize();
        for (int i = 1; i <= size; i++)
        {
            log("creating sample: %d/%d", i, size);
            SamplePE sample = generateSampleForExperiment(experiment);
            daoFactory.getSampleDAO().createOrUpdateSample(sample, getTestPerson());
            createDataSetsForSample(sample);
        }
        log("created samples");
    }

    private SamplePE generateSampleForExperiment(ExperimentPE experiment)
    {
        SampleTypePE type = defaultSampleType;
        String code = CodeGenerator.generateCode(CreatedEntityKind.SAMPLE);

        final SamplePE sample = new SamplePE();
        sample.setCode(code);
        sample.setSampleType(type);
        sample.setRegistrationDate(new Date());
        sample.setRegistrator(getSystemPerson());
        sample.setSpace(defaultGroup); // not shared
        sample.setExperiment(experiment);
        sample.setPermId(daoFactory.getPermIdDAO().createPermId());
        return sample;
    }

    // DataSets

    private void createDataSetsForSample(SamplePE sample)
    {
        final int size = SizeHelper.getNextDataSetsPerSampleSize();
        for (int i = 1; i <= size; i++)
        {
            log("creating dataset: %d/%d", i, size);
            ExternalDataPE dataSet = generateDataSetForSample(sample);
            daoFactory.getDataDAO().createDataSet(dataSet, getTestPerson());
        }
        log("created datasets");

    }

    private ExternalDataPE generateDataSetForSample(SamplePE sample)
    {
        ExternalDataPE externalData = new ExternalDataPE();
        String dataSetCode = daoFactory.getPermIdDAO().createPermId();
        externalData.setCode(dataSetCode);
        externalData.setDataSetType(defaultDataSetType);
        externalData.setExperiment(sample.getExperiment());
        externalData.setSampleAcquiredFrom(sample);
        externalData.setFileFormatType(pickAFileFormatType());
        externalData.setLocatorType(pickALocatorType());
        String location = CodeGenerator.generateCode(CreatedEntityKind.DATA_SET);
        externalData.setLocation(location);
        externalData.setStorageFormatVocabularyTerm(pickAStorageFormatVocabularyTerm());
        externalData.setDataStore(pickADataStore());

        return externalData;
    }

    // code from ExternalDataDAOTest

    private DataStorePE pickADataStore()
    {
        return daoFactory.getDataStoreDAO().tryToFindDataStoreByCode("STANDARD");
    }

    private LocatorTypePE pickALocatorType()
    {
        ILocatorTypeDAO locatorTypeDAO = daoFactory.getLocatorTypeDAO();
        LocatorTypePE locatorType = locatorTypeDAO.tryToFindLocatorTypeByCode("RELATIVE_LOCATION");
        assertNotNull(locatorType);
        return locatorType;
    }

    protected FileFormatTypePE pickAFileFormatType()
    {
        IFileFormatTypeDAO fileFormatTypeDAO = daoFactory.getFileFormatTypeDAO();
        FileFormatTypePE fileFormatType = fileFormatTypeDAO.tryToFindFileFormatTypeByCode("TIFF");
        assertNotNull(fileFormatType);
        return fileFormatType;
    }

    protected VocabularyTermPE pickAStorageFormatVocabularyTerm()
    {
        String code = StorageFormat.VOCABULARY_CODE;
        VocabularyPE vocabulary = daoFactory.getVocabularyDAO().tryFindVocabularyByCode(code);
        assertNotNull(vocabulary);
        return vocabulary.getTerms().iterator().next();
    }

    // Helper classes

    private enum CreatedEntityKind
    {
        DATA_SET, EXPERIMENT, SAMPLE, MATERIAL, DATA_SET_TYPE, EXPERIMENT_TYPE, SAMPLE_TYPE, GROUP,
        PROJECT;
    }

    /**
     * A generator of codes for {@link CreatedEntityKind}
     * 
     * @author Piotr Buczek
     */
    private static class CodeGenerator
    {
        private static final String CODE_PREFIX = "my_";

        private static int counter = 1000000;

        public static String generateCode(CreatedEntityKind kind)
        {
            return CODE_PREFIX + kind.name().charAt(0) + counter++;
        }

        public static String generateDefaultCode(CreatedEntityKind kind)
        {
            return CODE_PREFIX + kind.name();
        }
    }

    /**
     * A helper class which counts how many big Experiments/Samples has been created and returns sizes for the new ones.
     * 
     * @author Piotr Buczek
     */
    private static class SizeHelper
    {

        private static int bigExperimentsCounter = 0;

        private static int bigSamplesCounter = 0;

        public static final int getNextSamplesPerExperimentSize()
        {
            final int size;
            if (bigExperimentsCounter < BIG_EXPERIMENTS_NO)
            {
                size = BIG_EXPERIMENT_SAMPLES_SIZE;
                bigExperimentsCounter++;
            } else
            {
                size = DEFAULT_EXPERIMENT_SAMPLES_SIZE;
            }
            return size;
        }

        public static final int getNextDataSetsPerSampleSize()
        {
            final int size;
            if (bigSamplesCounter < BIG_SAMPLES_NO)
            {
                size = BIG_SAMPLE_DATASETS_SIZE;
                bigSamplesCounter++;
            } else
            {
                size = DEFAULT_SAMPLE_DATASETS_SIZE;
            }
            return size;
        }

    }

    /**
     * A helper class which creates a temporary TSV file for Materials.
     * 
     * @author Piotr Buczek
     */
    public static class MaterialHelper
    {

        private static final String TSV_FILENAME = "new=materials.tsv";

        //

        private static final Integer MATERIAL_TYPE_ID = 1;

        private static final Integer REGISTRATOR_ID = 1;

        private static final String REGISTRATION_TIMESTAMP = "2007-12-04 15:50:54.111";

        private static final Integer DB_INSTANCE = 1;

        private static final String MODIFICATION_TIMESTAMP = "2008-12-04 15:50:54.111";

        /**
         * Creates a temporary TSV file with given number of materials in given directory. The file is deleted automatically on JVM exit.
         */
        public static void createMaterialsTSVFile(String directory, int size)
        {
            PrintWriter pw = null;
            try
            {
                File file = new File(directory, TSV_FILENAME);
                file.deleteOnExit();
                pw = new PrintWriter(file);

                int id = 10000000;
                for (int i = 0; i < size; i++)
                {
                    pw.println(createMaterialString(id++));
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally
            {
                if (pw != null)
                {
                    pw.close();
                }
            }
        }

        private static String createMaterialString(int id)
        {
            StringBuilder sb = new StringBuilder();
            appendColumn(sb, id);
            appendColumn(sb, CodeGenerator.generateCode(CreatedEntityKind.MATERIAL));
            appendColumn(sb, MATERIAL_TYPE_ID);
            appendColumn(sb, REGISTRATOR_ID);
            appendColumn(sb, REGISTRATION_TIMESTAMP);
            appendColumn(sb, DB_INSTANCE);
            sb.append(MODIFICATION_TIMESTAMP);
            return sb.toString();
        }

        private static void appendColumn(StringBuilder sb, Object value)
        {
            sb.append((value == null) ? "\\N" : value);
            sb.append("\t");
        }
    }
}
