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

package ch.systemsx.cisd.openbis.systemtest.optimistic_locking;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.util.TimeIntervalChecker;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * @author Franz-Josef Elmer
 */
public class ToolBox
{
    public static final String USER_ID = "optimist";

    public static final String SPACE_1 = "OPTIMISTIC_LOCKING_1";

    public static final String SPACE_2 = "OPTIMISTIC_LOCKING_2";

    public static final String EXPERIMENT_TYPE_CODE = "SIRNA_HCS";

    public static final String SAMPLE_TYPE_CODE = "NORMAL";

    public static final String DATA_STORE_CODE = "STANDARD";

    public static final String REGISTERED = "registered";

    public static final String FIRST_REGISTERED = "First registered";

    public static final List<IEntityProperty> NO_PROPERTIES = Collections
            .<IEntityProperty> emptyList();

    public static final List<NewAttachment> NO_ATTACHMENTS = Collections
            .<NewAttachment> emptyList();

    private static final LocatorType LOCATOR_TYPE = new LocatorType(
            LocatorType.DEFAULT_LOCATOR_TYPE_CODE);

    private static final FileFormatType FILE_FORMAT_TYPE = new FileFormatType("XML");

    private static final DataSetType DATA_SET_TYPE = new DataSetType("UNKNOWN");

    private final ICommonServer commonServer;

    private final IGenericServer genericServer;

    private final IETLLIMSService etlService;

    private final String systemSessionToken;

    public Space space1;

    public Space space2;

    public Project project1;

    public Project project2;

    public ToolBox(ICommonServer commonServer, IGenericServer genericServer,
            IETLLIMSService etlService, String systemSessionToken)
    {
        this.commonServer = commonServer;
        this.genericServer = genericServer;
        this.etlService = etlService;
        this.systemSessionToken = systemSessionToken;

    }

    void createSpacesAndProjects()
    {
        space1 = findOrCreateSpace(SPACE_1);
        space2 = findOrCreateSpace(SPACE_2);
        project1 = findOrCreateProject("/" + SPACE_1 + "/P1");
        project2 = findOrCreateProject("/" + SPACE_2 + "/P2");
        createInstanceAdmin(USER_ID);
    }

    void deleteSpaces()
    {
        deleteSpace(space1);
        deleteSpace(space2);
    }

    private void deleteSpace(Space space)
    {
        trashExperiments(space);
        trashSamples(space);
        emptyTrashCan();
        deleteProjects(space);
        commonServer.deleteSpaces(systemSessionToken, Arrays.asList(new TechId(space.getId())),
                "cleanup");
    }

    private void deleteProjects(Space space)
    {
        List<Project> projects = commonServer.listProjects(systemSessionToken);
        List<TechId> projectIds = new ArrayList<TechId>();
        for (Project project : projects)
        {
            if (project.getSpace().getCode().equals(space.getCode()))
            {
                projectIds.add(new TechId(project));
            }
        }
        commonServer.deleteProjects(systemSessionToken, projectIds, "cleanup");
    }

    private void emptyTrashCan()
    {
        List<Deletion> deletions = commonServer.listDeletions(systemSessionToken, false);
        commonServer.deletePermanently(systemSessionToken, TechId.createList(deletions));
    }

    private void trashSamples(Space space)
    {
        ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setSampleType(SampleType.createAllSampleType(Collections.<SampleType> emptyList(),
                false));
        criteria.setSpaceCode(space.getCode());
        criteria.setIncludeSpace(true);
        criteria.setIncludeInstance(false);
        criteria.setExcludeWithoutExperiment(false);
        List<Sample> samples = commonServer.listSamples(systemSessionToken, criteria);
        commonServer.deleteSamples(systemSessionToken, TechId.createList(samples), "cleanup",
                DeletionType.TRASH);
    }

    private void trashExperiments(Space space)
    {
        List<Experiment> experiments =
                commonServer.listExperiments(systemSessionToken,
                        new ExperimentTypeBuilder().code(EXPERIMENT_TYPE_CODE).getExperimentType(),
                        new SpaceIdentifierFactory(space.getIdentifier()).createIdentifier());
        commonServer.deleteExperiments(systemSessionToken, TechId.createList(experiments),
                "cleanup", DeletionType.TRASH);
    }

    public void createInstanceAdmin(String userId)
    {
        List<Person> persons = commonServer.listPersons(systemSessionToken);
        for (Person person : persons)
        {
            if (person.getUserId().equals(userId))
            {
                return;
            }
        }
        commonServer.registerPerson(systemSessionToken, userId);
        commonServer.registerInstanceRole(systemSessionToken, RoleCode.ADMIN,
                Grantee.createPerson(userId));
    }

    public Project findOrCreateProject(String projectIdentifier)
    {
        Project project = tryToFindProject(projectIdentifier);
        if (project != null)
        {
            return project;
        }
        commonServer.registerProject(systemSessionToken, new ProjectIdentifierFactory(
                projectIdentifier).createIdentifier(), "A test project", null, Collections
                .<NewAttachment> emptyList());
        return tryToFindProject(projectIdentifier);
    }

    public Project tryToFindProject(String projectIdentifier)
    {
        List<Project> projects = commonServer.listProjects(systemSessionToken);
        for (Project project : projects)
        {
            if (project.getIdentifier().equals(projectIdentifier))
            {
                return project;
            }
        }
        return null;
    }

    public Space findOrCreateSpace(String spaceCode)
    {
        Space space = tryToFindSpace(spaceCode);
        if (space != null)
        {
            return space;
        }
        commonServer.registerSpace(systemSessionToken, spaceCode, "A test space");
        return tryToFindSpace(spaceCode);
    }

    public Space tryToFindSpace(String spaceCode)
    {
        DatabaseInstanceIdentifier identifier = new DatabaseInstanceIdentifier(null);
        List<Space> spaces = commonServer.listSpaces(systemSessionToken, identifier);
        for (Space space : spaces)
        {
            if (space.getCode().equals(spaceCode))
            {
                return space;
            }
        }
        return null;
    }

    NewExperiment experiment(int number)
    {
        NewExperiment experiment =
                new NewExperiment(project1.getIdentifier() + "/OLT-E" + number,
                        EXPERIMENT_TYPE_CODE);
        experiment.setAttachments(Collections.<NewAttachment> emptyList());
        experiment.setProperties(new IEntityProperty[]
            { new PropertyBuilder("DESCRIPTION").value("hello " + number).getProperty() });
        return experiment;
    }

    public List<String> extractCodes(List<? extends ICodeHolder> codeHolders)
    {
        List<String> result = new ArrayList<String>();
        for (ICodeHolder codeHolder : codeHolders)
        {
            result.add(codeHolder.getCode());
        }
        Collections.sort(result);
        return result;
    }

    public ProjectIdentifier createProjectIdentifier(String identifier)
    {
        return new ProjectIdentifierFactory(identifier).createIdentifier();
    }

    public Experiment createAndLoadExperiment(int number)
    {
        NewExperiment experiment = experiment(number);
        genericServer.registerExperiment(systemSessionToken, experiment, NO_ATTACHMENTS);
        return loadExperiment(experiment);
    }

    public Experiment loadExperiment(final IIdentifierHolder experiment)
    {
        return commonServer.getExperimentInfo(systemSessionToken,
                ExperimentIdentifierFactory.parse(experiment.getIdentifier()));
    }

    public NewSample sample(int number, IIdentifierHolder experiment)
    {
        NewSample sample = sample(number);
        sample.setExperimentIdentifier(experiment.getIdentifier());
        return sample;
    }

    public NewSample sample(int number)
    {
        NewSample sample = new NewSample();
        sample.setIdentifier("/" + ToolBox.SPACE_1 + "/OLT-S" + number);
        sample.setSampleType(new SampleTypeBuilder().code(SAMPLE_TYPE_CODE).getSampleType());
        return sample;
    }

    public Sample loadSample(IIdentifierHolder sample)
    {
        return etlService.tryGetSampleWithExperiment(systemSessionToken,
                SampleIdentifierFactory.parse(sample.getIdentifier()));
    }

    public NewDataSet dataSet(String code, Experiment experiment)
    {
        NewDataSet dataSet = dataSet(code);
        dataSet.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(experiment
                .getIdentifier()));
        return dataSet;
    }

    private NewDataSet dataSet(String code)
    {
        NewDataSet dataSet = new NewDataSet();
        dataSet.setCode(code);
        dataSet.setDataSetType(DATA_SET_TYPE);
        dataSet.setFileFormatType(FILE_FORMAT_TYPE);
        dataSet.setDataSetProperties(Collections.<NewProperty> emptyList());
        dataSet.setLocation("a/b/c/" + code);
        dataSet.setLocatorType(LOCATOR_TYPE);
        dataSet.setStorageFormat(StorageFormat.PROPRIETARY);
        dataSet.setDataStoreCode(DATA_STORE_CODE);
        return dataSet;
    }

    public void checkModifierAndModificationDateOfProject1(TimeIntervalChecker timeIntervalChecker)
    {
        checkModifierAndModificationDateOfProject1(timeIntervalChecker, "test");
    }

    public void checkModifierAndModificationDateOfProject1(TimeIntervalChecker timeIntervalChecker,
            String modifier)
    {
        ProjectIdentifier projectIdentifier = createProjectIdentifier(project1.getIdentifier());
        Project p = commonServer.getProjectInfo(systemSessionToken, projectIdentifier);
        assertEquals("system", p.getRegistrator().getUserId());
        assertEquals(project1.getRegistrationDate(), p.getRegistrationDate());
        assertEquals(modifier, p.getModifier().getUserId());
        timeIntervalChecker.assertDateInInterval(p.getModificationDate());
    }

}
