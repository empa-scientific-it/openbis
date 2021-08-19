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

package ch.systemsx.cisd.openbis.generic.server.business;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.RelationshipUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * The unique {@link IRelationshipService} implementation.
 * 
 * @author anttil
 */
public class RelationshipService implements IRelationshipService, ApplicationContextAware
{
    private static final String ERR_SAMPLE_PARENT_RELATIONSHIP_NOT_FOUND =
            "Sample '%s' did not have parent '%s'";

    private static final String ERR_DATASET_PARENT_RELATIONSHIP_NOT_FOUND =
            "DataSet '%s' did not have parent '%s'";

    private static final String ERR_DATASET_CONTAINER_RELATIONSHIP_NOT_FOUND =
            "DataSet '%s' did not have container '%s'";

    private DAOFactory daoFactory;

    /**
     * Reference to this instance of service, but as a spring bean, so that we can call methods of this service and run the additional authorization.
     */
    private IRelationshipService service;

    private ApplicationContext applicationContext;

    @PostConstruct
    private void init()
    {
        this.service = applicationContext.getBean("relationship-service", IRelationshipService.class);
    }

    @Override
    public void assignExperimentToProject(IAuthSession session, ExperimentPE experiment,
            ProjectPE project)
    {
        Date timeStamp = getTransactionTimeStamp();
        SampleUtils.setSamplesSpace(experiment, project.getSpace());
        for (SamplePE sample : experiment.getSamples())
        {
            ProjectPE sampleProject = sample.getProject();
            if (sampleProject != null && EntityHelper.equalEntities(sampleProject, project) == false)
            {
                sample.setProject(project);
                RelationshipUtils.updateModificationDateAndModifier(sample, session, timeStamp);
            }
        }
        ProjectPE previousProject = experiment.getProject();
        RelationshipUtils.updateModificationDateAndModifier(previousProject, session, timeStamp);
        experiment.setProject(project);
        RelationshipUtils.updateModificationDateAndModifier(project, session, timeStamp);
        RelationshipUtils.updateModificationDateAndModifier(experiment, session, timeStamp);
    }

    @Override
    public void assignSampleToProject(IAuthSession session, SamplePE sample, ProjectPE project)
    {
        SampleUtils.assertProjectSamplesEnabled(sample, project);
        Date timeStamp = getTransactionTimeStamp();
        ProjectPE previousProject = sample.getProject();
        RelationshipUtils.updateModificationDateAndModifier(previousProject, session, timeStamp);
        sample.setProject(project);
        RelationshipUtils.updateModificationDateAndModifier(project, session, timeStamp);
        RelationshipUtils.updateModificationDateAndModifier(sample, session, timeStamp);
    }

    @Override
    public void assignProjectToSpace(IAuthSession session, ProjectPE project, SpacePE space)
    {
        project.setSpace(space);
        for (ExperimentPE experiment : project.getExperiments())
        {
            SampleUtils.setSamplesSpace(experiment, space);
        }
        List<SamplePE> samples = project.getSamples();
        for (SamplePE sample : samples)
        {
            sample.setSpace(space);
        }
    }

    @Override
    public void assignSampleToExperiment(IAuthSession session, SamplePE sample,
            ExperimentPE experiment)
    {
        Date timeStamp = getTransactionTimeStamp();
        ExperimentPE currentExperiment = sample.getExperiment();
        ProjectPE currentProject = sample.getProject();

        if (currentExperiment != null && false == currentExperiment.equals(experiment))
        {
            service.checkCanUnassignSampleFromExperiment(session, sample);
            currentProject = currentExperiment.getProject();
            RelationshipUtils.updateModificationDateAndModifierOfExperimentAndProject(currentExperiment,
                    currentProject, session, timeStamp);
        }

        sample.setExperiment(experiment);
        RelationshipUtils.updateModificationDateAndModifier(sample, session, timeStamp);
        RelationshipUtils.updateModificationDateAndModifierOfExperimentAndProject(experiment, currentProject,
                session, timeStamp);
    }

    @Override
    public void checkCanUnassignSampleFromExperiment(IAuthSession session, SamplePE sample)
    {
        // all the logic is done by the authorization mechanism
    }

    @Override
    public void unassignSampleFromExperiment(IAuthSession session, SamplePE sample)
    {
        ExperimentPE experiment = sample.getExperiment();
        if (experiment != null)
        {
            Date timeStamp = getTransactionTimeStamp();
            experiment.removeSample(sample);
            RelationshipUtils.updateModificationDateAndModifier(sample, session, timeStamp);
            RelationshipUtils.updateModificationDateAndModifier(experiment, session, timeStamp);
        }
    }

    @Override
    public void unassignSampleFromProject(IAuthSession session, SamplePE sample)
    {
        ProjectPE project = sample.getProject();
        if (project != null)
        {
            Date timeStamp = getTransactionTimeStamp();
            sample.setProject(null);
            RelationshipUtils.updateModificationDateAndModifier(sample, session, timeStamp);
            RelationshipUtils.updateModificationDateAndModifier(project, session, timeStamp);
        }
    }

    @Override
    public void assignSampleToSpace(IAuthSession session, SamplePE sample, SpacePE space)
    {
        Date timeStamp = getTransactionTimeStamp();
        sample.setSpace(space);
        RelationshipUtils.updateModificationDateAndModifier(sample, session, timeStamp);
    }

    @Override
    public void unshareSample(IAuthSession session, SamplePE sample, SpacePE space)
    {
        assignSampleToSpace(session, sample, space);
    }

    @Override
    public void shareSample(IAuthSession session, SamplePE sample)
    {
        Date timeStamp = getTransactionTimeStamp();
        sample.setSpace(null);
        RelationshipUtils.updateModificationDateAndModifier(sample, session, timeStamp);
    }

    @Override
    public void assignDataSetToExperiment(IAuthSession session, DataPE data, ExperimentPE experimentOrNull)
    {
        Date timeStamp = getTransactionTimeStamp();
        RelationshipUtils.setExperimentForDataSet(data, experimentOrNull, session, timeStamp);
    }

    @Override
    public void assignDataSetToSample(IAuthSession session, DataPE data, SamplePE sample)
    {
        SamplePE currentSample = data.tryGetSample();
        if (EntityHelper.equalEntities(currentSample, sample))
        {
            return;
        }
        Date timeStamp = getTransactionTimeStamp();
        RelationshipUtils.setSampleForDataSet(data, sample, session, timeStamp);
    }

    @Override
    public void addParentToSample(IAuthSession session, SamplePE sample, SamplePE parent)
    {
        PersonPE actor = session.tryGetPerson();
        RelationshipTypePE relationshipType =
                daoFactory.getRelationshipTypeDAO().tryFindRelationshipTypeByCode(
                        BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);

        Date timeStamp = getTransactionTimeStamp();
        RelationshipUtils.updateModificationDateAndModifier(sample, session, timeStamp);
        RelationshipUtils.updateModificationDateAndModifier(parent, session, timeStamp);
        sample.addParentRelationship(new SampleRelationshipPE(parent, sample, relationshipType,
                actor));
    }

    @Override
    public void setSampleParentChildAnnotations(IAuthSession session, SamplePE child, SamplePE parent,
            Map<String, String> childAnnotations, Map<String, String> parentAnnotations)
    {
        Date timeStamp = getTransactionTimeStamp();
        RelationshipUtils.updateModificationDateAndModifier(child, session, timeStamp);
        RelationshipUtils.updateModificationDateAndModifier(parent, session, timeStamp);
        for (SampleRelationshipPE relationship : child.getParentRelationships())
        {
            if (relationship.getParentSample() == parent)
            {
                relationship.setChildAnnotations(childAnnotations);
                relationship.setParentAnnotations(parentAnnotations);
            }
        }
    }

    @Override
    public void removeParentFromSample(IAuthSession session, SamplePE sample, SamplePE parent)
    {
        for (SampleRelationshipPE relationship : sample.getParentRelationships())
        {
            if (relationship.getParentSample().equals(parent))
            {
                Date timeStamp = getTransactionTimeStamp();
                RelationshipUtils.updateModificationDateAndModifier(relationship.getChildSample(),
                        session, timeStamp);
                RelationshipUtils.updateModificationDateAndModifier(relationship.getParentSample(),
                        session, timeStamp);
                sample.removeParentRelationship(relationship);
                return;
            }
        }
        throw UserFailureException.fromTemplate(ERR_SAMPLE_PARENT_RELATIONSHIP_NOT_FOUND,
                sample.getCode(), parent.getCode());
    }

    @Override
    public void assignSampleToContainer(IAuthSession session, SamplePE sample, SamplePE container)
    {
        Date timeStamp = getTransactionTimeStamp();
        RelationshipUtils.setContainerForSample(sample, container, session, timeStamp);
    }

    @Override
    public void removeSampleFromContainer(IAuthSession session, SamplePE sample)
    {
        Date timeStamp = getTransactionTimeStamp();
        RelationshipUtils.setContainerForSample(sample, null, session, timeStamp);
    }

    @Override
    public void addParentToDataSet(IAuthSession session, DataPE data, DataPE parent)
    {
        assignGenericParentChild(session, data, parent, getParentChildRelationshipType(), null);
    }

    private void assignGenericParentChild(IAuthSession session, DataPE child, DataPE parent, RelationshipTypePE type, Integer ordinalOrNull)
    {
        PersonPE actor = session.tryGetPerson();
        DataSetRelationshipPE relationship = new DataSetRelationshipPE(parent, child, type, ordinalOrNull, actor);
        child.addParentRelationship(relationship);
        if (parent.isChildrenRelationshipsInitialized())
        {
            parent.addChildRelationship(relationship);
        }
        Date timeStamp = getTransactionTimeStamp();
        RelationshipUtils.updateModificationDateAndModifier(child, session, timeStamp);
        RelationshipUtils.updateModificationDateAndModifier(parent, session, timeStamp);
    }

    @Override
    public void removeParentFromDataSet(IAuthSession session, DataPE data, DataPE parent)
    {
        releaseRelationship(session, BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP, data, parent,
                ERR_DATASET_PARENT_RELATIONSHIP_NOT_FOUND);
    }

    private void releaseRelationship(IAuthSession session, String relationshipTypeCode, DataPE child, DataPE parent, String errorTemplate)
    {
        DataSetRelationshipPE remove = null;
        for (DataSetRelationshipPE r : child.getParentRelationships())
        {
            if (r.getParentDataSet().equals(parent) && r.getRelationshipType().getCode().equals(relationshipTypeCode))
            {
                remove = r;
                break;
            }
        }

        if (remove != null)
        {
            child.removeParentRelationship(remove);
        } else
        {
            throw UserFailureException.fromTemplate(errorTemplate,
                    child.getCode(), parent.getCode());
        }
        Date timeStamp = getTransactionTimeStamp();
        RelationshipUtils.updateModificationDateAndModifier(child, session, timeStamp);
        RelationshipUtils.updateModificationDateAndModifier(parent, session, timeStamp);
    }

    @Override
    public void assignDataSetToContainer(IAuthSession session, DataPE data, DataPE container)
    {
        int ordinal = RelationshipUtils.getContainerComponentRelationships(container.getChildRelationships()).size();
        RelationshipTypePE relationshipType = RelationshipUtils.getContainerComponentRelationshipType(daoFactory.getRelationshipTypeDAO());
        assignGenericParentChild(session, data, container, relationshipType, ordinal);
    }

    @Override
    public void removeDataSetFromContainer(IAuthSession session, DataPE data, DataPE container)
    {
        releaseRelationship(session, BasicConstant.CONTAINER_COMPONENT_INTERNAL_RELATIONSHIP, data,
                container, ERR_DATASET_CONTAINER_RELATIONSHIP_NOT_FOUND);
    }

    public void setDaoFactory(DAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    private RelationshipTypePE getParentChildRelationshipType()
    {
        return RelationshipUtils.getParentChildRelationshipType(daoFactory.getRelationshipTypeDAO());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    private Date getTransactionTimeStamp()
    {
        return daoFactory.getTransactionTimestamp();
    }

}
