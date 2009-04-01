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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.annotation.Rollback;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ILocatorTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.SampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedureTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * A class that uses features of {@link AbstractDAOTest} but is not a real test of openbis (it is
 * excluded from all suites). It has one test method though called main witch should be run as a
 * TestNG test and creates a DB for scalability testing. No rollback is done after this method. <br>
 * <br>
 * It creates a DB from scratch and:
 * <ul>
 * <li>doesn't create any properties nor attaches properties to anything because it can be easily
 * done by GUI to a certain EntityType
 * <li>creates Materials of one new MaterialType
 * <li>creates Experiments of one new ExperimentType
 * <li>creates Samples of one new SampleType - these samples are connected to an experiment created
 * in the previous step through a new Procedure of one ProcedureType that should already be in the
 * DB. Each experiment will have one or more samples connected to it.
 * <li>creates DataSets of one new DataSetType - these data sets are connected to a sample and an
 * experiment created in previous steps. Each sample will have one or more data sets connected to
 * it.
 * </ul>
 * <br>
 * IMPORTANT - to make it faster try:
 * <ul>
 * <li>commenting out flush() in create methods for in {@link IExternalDataDAO} and
 * {@link SampleDAO}
 * <li>turning off logging (doesn't make a big difference) - change root logging priority in log.xml
 * from "info" to "error" and also change static variable LOG value to false.
 * </ul>
 * 
 * @author Piotr Buczek
 */
@Test(groups =
    { "scalability" })
public final class DBCreator extends AbstractDAOTest
{
    private static final String DB_KIND = "test_scalability";

    static
    {
        System.setProperty("database.kind", DB_KIND);
        System.setProperty("database.create-from-scratch", "true");
        System.setProperty("authorization-component-factory", "no-authorization");
    }

    private static final boolean LOG = true;

    private static void log(String format, Object... objects)
    {
        if (LOG)
        {
            System.err.println(String.format(format, objects));
        }
    }

    // number properties

    /** a factor for scaling number of all created entities */
    private static final int FACTOR = 100;

    /** the overall number of Materials created (including big ones) */
    private static final int MATERIALS_NO = FACTOR * 20000;

    /** the overall number of Experiments created (including big ones) */
    private static final int EXPERIMENTS_NO = FACTOR * 5;

    /** the number of big Experiments (with many Samples connected) created */
    private static final int BIG_EXPERIMENTS_NO = 1;

    /** the number of big Samples (with many DataSets connected) created */
    private static final int BIG_SAMPLES_NO = 1;

    /** the default number of Samples connected to a created Experiment */
    private static final int DEFAULT_EXPERIMENT_SAMPLES_SIZE = 10;

    /** the number of Samples connected to a created big Experiment */
    private static final int BIG_EXPERIMENT_SAMPLES_SIZE = DEFAULT_EXPERIMENT_SAMPLES_SIZE * FACTOR;

    /** the default number of DataSets connected to a created Sample */
    private static final int DEFAULT_SAMPLE_DATASETS_SIZE = 1;

    /** the number of DataSets connected to a created big Sample */
    private static final int BIG_SAMPLE_DATASETS_SIZE = DEFAULT_SAMPLE_DATASETS_SIZE * FACTOR * 10;

    @Test
    @Rollback(value = false)
    public final void main()
    {
        hibernateTemplate = new HibernateTemplate(sessionFactory);
        try
        {
            createAndSetDefaultEntities();
            createMaterials();
            createExperimentsWithSamplesAndDataSets();
        } catch (Exception e)
        {
            System.err.println("DB creation failed");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
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

    private MaterialTypePE defaultMaterialType;

    private SampleTypePE defaultSampleType;

    private ExperimentTypePE defaultExperimentType;

    private DataSetTypePE defaultDataSetType;

    private ProcedureTypePE defaultProcedureType;

    private GroupPE defaultGroup;

    private ProjectPE defaultProject;

    // Default

    private final void createAndSetDefaultEntities()
    {
        defaultGroup = createDefaultGroup();
        defaultProject = createDefaultProject();

        defaultDataSetType = createDefaultDataSetType();
        defaultMaterialType = createDefaultMaterialType();
        defaultExperimentType = createDefaultExperimentType();
        defaultSampleType = createDefaultSampleType();

        //
        defaultProcedureType = pickAProcedureType();
    }

    private final ProcedureTypePE pickAProcedureType()
    {
        ProcedureTypePE result =
                daoFactory.getProcedureTypeDAO().listProcedureTypes().iterator().next();
        AssertJUnit.assertNotNull(result);
        return result;
    }

    private final MaterialTypePE createDefaultMaterialType()
    {
        return createEntityType(new MaterialTypePE(), EntityKind.MATERIAL,
                CreatedEntityKind.MATERIAL_TYPE);
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
        entityType.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        entityTypeDAO.createEntityType(entityType);

        return entityType;
    }

    private final GroupPE createDefaultGroup()
    {
        final String code = CodeGenerator.generateDefaultCode(CreatedEntityKind.GROUP);
        return createGroup(code);
    }

    private final ProjectPE createDefaultProject()
    {
        final String code = CodeGenerator.generateDefaultCode(CreatedEntityKind.PROJECT);
        final ProjectPE project = new ProjectPE();
        project.setCode(code);
        project.setGroup(defaultGroup);
        project.setProjectLeader(getSystemPerson());
        project.setRegistrator(getSystemPerson());
        daoFactory.getProjectDAO().createProject(project);

        return project;
    }

    // Materials

    private void createMaterials()
    {
        List<MaterialPE> materials = new ArrayList<MaterialPE>();
        for (int i = 1; i <= MATERIALS_NO; i++)
        {
            log("generating material: %d/%d", i, MATERIALS_NO);
            materials.add(generateMaterial());
            // need to create materials in groups not to run out of java heap space
            // and clearing session makes the creation of new objects faster
            if (i % 1000 == 0)
            {
                daoFactory.getMaterialDAO().createMaterials(materials);
                materials.clear();
                flushAndClearSession();
            }
        }
        log("generated materials");
    }

    private MaterialPE generateMaterial()
    {
        MaterialTypePE type = defaultMaterialType;
        String code = CodeGenerator.generateCode(CreatedEntityKind.MATERIAL);

        final MaterialPE material = new MaterialPE();
        material.setCode(code);
        material.setMaterialType(type);
        material.setRegistrationDate(new Date());
        material.setRegistrator(getSystemPerson());
        material.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        return material;
    }

    // Experiments

    private void createExperimentsWithSamplesAndDataSets()
    {
        for (int i = 1; i <= EXPERIMENTS_NO; i++)
        {
            log("creating experiment: %d/%d", i, EXPERIMENTS_NO);
            ExperimentPE experiment = generateExperiment();
            daoFactory.getExperimentDAO().createExperiment(experiment);
            createConnectionsForExperiment(experiment);
            flushAndClearSession();
        }
        log("created experiments");
    }

    private ExperimentPE generateExperiment()
    {
        ExperimentTypePE type = defaultExperimentType;
        String code = CodeGenerator.generateCode(CreatedEntityKind.MATERIAL);

        final ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(code);
        experiment.setExperimentType(type);
        experiment.setProject(defaultProject);
        experiment.setRegistrationDate(new Date());
        experiment.setRegistrator(getSystemPerson());
        return experiment;
    }

    private void createConnectionsForExperiment(ExperimentPE experiment)
    {
        createProcedureForExperiment(experiment);
        createSamplesWithDataSetsForExperiment(experiment);
    }

    // Procedures

    private void createProcedureForExperiment(ExperimentPE experiment)
    {
        ProcedurePE procedure = generateProcedureForExperiment(experiment);
        daoFactory.getProcedureDAO().createProcedure(procedure);
    }

    private ProcedurePE generateProcedureForExperiment(ExperimentPE experiment)
    {
        final ProcedurePE procedure = new ProcedurePE();
        procedure.setProcedureType(defaultProcedureType);
        procedure.setRegistrationDate(new Date());
        procedure.setExperiment(experiment);

        return procedure;
    }

    // Samples

    private void createSamplesWithDataSetsForExperiment(ExperimentPE experiment)
    {
        final int size = SizeHelper.getNextSamplesPerExperimentSize();
        for (int i = 1; i <= size; i++)
        {
            log("creating sample: %d/%d", i, size);
            SamplePE sample = generateSampleForExperiment(experiment);
            daoFactory.getSampleDAO().createSample(sample);
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
        sample.setGroup(defaultGroup); // not shared
        sample.setProcedures(experiment.getProcedures());
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
            daoFactory.getExternalDataDAO().createDataSet(dataSet);
        }
        log("created datasets");

    }

    private ExternalDataPE generateDataSetForSample(SamplePE sample)
    {
        ExternalDataPE externalData = new ExternalDataPE();
        String dataSetCode = daoFactory.getExternalDataDAO().createDataSetCode();
        externalData.setCode(dataSetCode);
        externalData.setDataSetType(defaultDataSetType);
        externalData.setProcedure(getProcedureFromSample(sample));
        externalData.setSampleAcquiredFrom(sample);
        externalData.setFileFormatType(pickAFileFormatType());
        externalData.setLocatorType(pickALocatorType());
        String location = CodeGenerator.generateCode(CreatedEntityKind.DATA_SET);
        externalData.setLocation(location);
        externalData.setStorageFormatVocabularyTerm(pickAStorageFormatVocabularyTerm());

        return externalData;
    }

    private ProcedurePE getProcedureFromSample(SamplePE sample)
    {
        return sample.getProcedures().iterator().next();
    }

    // code from ExternalDataDAOTest

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
        DATA_SET, EXPERIMENT, MATERIAL, SAMPLE, GROUP, PROJECT, DATA_SET_TYPE, EXPERIMENT_TYPE,
        MATERIAL_TYPE, SAMPLE_TYPE;

    }

    /**
     * A generator of codes for {@link CreatedEntityKind}
     * 
     * @author Piotr Buczek
     */
    private static class CodeGenerator
    {
        private static final String CODE_PREFIX = "_";

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
     * A helper class which counts how many big Experiments/Samples has been created and returns
     * sizes for the new ones.
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
}
