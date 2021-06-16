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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.ExperimentPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.ExperimentTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * The unique {@link IExperimentBO} implementation.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentBO extends AbstractBusinessObject implements IExperimentBO
{
    @Private
    static final String ERR_PROJECT_NOT_FOUND =
            "No project for experiment '%s' could be found in the database.";

    @Private
    static final String ERR_EXPERIMENT_TYPE_NOT_FOUND =
            "No experiment type with code '%s' could be found in the database.";

    private ExperimentPE experiment;

    private boolean dataChanged;

    private final List<AttachmentPE> attachments = new ArrayList<AttachmentPE>();

    public ExperimentBO(final IDAOFactory daoFactory, final Session session,
            IRelationshipService relationshipService,
            IEntityInformationProvider entityInformationProvider,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker)
    {
        super(daoFactory, session, EntityKind.EXPERIMENT, entityInformationProvider, managedPropertyEvaluatorFactory,
                dataSetTypeChecker, relationshipService);
    }

    ExperimentBO(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, entityPropertiesConverter, managedPropertyEvaluatorFactory,
                dataSetTypeChecker, relationshipService);
    }

    @SuppressWarnings("unused")
    private final ExperimentTypePE getExperimentType(final String code) throws UserFailureException
    {
        final EntityTypePE experimentType =
                getEntityTypeDAO(EntityKind.EXPERIMENT).tryToFindEntityTypeByCode(code);
        if (experimentType == null)
        {
            throw UserFailureException.fromTemplate(ERR_EXPERIMENT_TYPE_NOT_FOUND, code);
        }
        return (ExperimentTypePE) experimentType;
    }

    @Override
    public final ExperimentPE getExperiment()
    {
        checkExperimentLoaded();
        return experiment;
    }

    private void checkExperimentLoaded()
    {
        if (experiment == null)
        {
            throw new IllegalStateException("Unloaded experiment.");
        }
    }

    @Private
    static final String PROPERTY_TYPES = "experimentType.experimentTypePropertyTypesInternal";

    @Override
    public void loadDataByTechId(TechId experimentId)
    {
        String[] connections =
                { PROPERTY_TYPES };
        experiment = getExperimentDAO().tryGetByTechId(experimentId, connections);
        if (experiment == null)
        {
            throw new UserFailureException(String.format("Experiment with ID '%s' does not exist.",
                    experimentId));
        }
        dataChanged = false;
        HibernateUtils.initialize(experiment.getExperimentType().getExperimentTypePropertyTypes());
    }

    @Override
    public final void loadByExperimentIdentifier(final ExperimentIdentifier identifier)
    {
        experiment = getExperimentByIdentifier(identifier);
        dataChanged = false;
    }

    @Override
    public final ExperimentPE tryFindByExperimentIdentifier(final ExperimentIdentifier identifier)
    {
        final ProjectPE project = tryGetProject(identifier);
        if (project == null)
        {
            return null;
        }
        return tryGetExperiment(identifier, project);
    }

    @Override
    public ExperimentPE tryFindByExperimentId(IExperimentId experimentId)
    {
        if (experimentId == null)
        {
            throw new IllegalArgumentException("Experiment id cannot be null");
        }
        if (experimentId instanceof ExperimentIdentifierId)
        {
            ExperimentIdentifierId identifierId = (ExperimentIdentifierId) experimentId;
            ExperimentIdentifier identifier =
                    new ExperimentIdentifierFactory(identifierId.getIdentifier())
                            .createIdentifier();
            return tryFindByExperimentIdentifier(identifier);
        } else if (experimentId instanceof ExperimentPermIdId)
        {
            ExperimentPermIdId permIdId = (ExperimentPermIdId) experimentId;
            return getExperimentDAO().tryGetByPermID(permIdId.getPermId());
        } else if (experimentId instanceof ExperimentTechIdId)
        {
            ExperimentTechIdId techIdId = (ExperimentTechIdId) experimentId;
            return getExperimentDAO().tryGetByTechId(new TechId(techIdId.getTechId()));
        } else
        {
            throw new IllegalArgumentException("Unsupported experiment id: " + experimentId);
        }
    }

    private ExperimentPE getExperimentByIdentifier(final ExperimentIdentifier identifier)
    {
        assert identifier != null : "Experiment identifier unspecified.";
        final ProjectPE project = tryGetProject(identifier);
        if (project == null)
        {
            throw new UserFailureException("Unkown experiment because of unkown project: "
                    + identifier);
        }
        final ExperimentPE exp = tryGetExperiment(identifier, project);
        if (exp == null)
        {
            throw new UserFailureException("Unkown experiment: " + identifier);
        }
        return exp;
    }

    private ExperimentPE tryGetExperiment(final ExperimentIdentifier identifier,
            final ProjectPE project)
    {
        return getExperimentDAO().tryFindByCodeAndProject(project, identifier.getExperimentCode());
    }

    private ProjectPE tryGetProject(final ExperimentIdentifier identifier)
    {
        return getProjectDAO().tryFindProject(
                identifier.getSpaceCode(), identifier.getProjectCode());
    }

    @Override
    public final void enrichWithProperties()
    {
        if (experiment != null)
        {
            HibernateUtils.initialize(experiment.getProperties());
        }
    }

    @Override
    public final void enrichWithAttachments()
    {
        if (experiment != null)
        {
            experiment.ensureAttachmentsLoaded();
        }
    }

    @Override
    public AttachmentPE tryGetExperimentFileAttachment(String filename, Integer versionOrNull)
    {
        checkExperimentLoaded();
        experiment.ensureAttachmentsLoaded();

        AttachmentPE att =
                versionOrNull == null ? getAttachment(filename) : getAttachment(filename,
                        versionOrNull);
        if (att != null)
        {
            HibernateUtils.initialize(att.getAttachmentContent());
            return att;
        } else
        {
            return null;
        }
    }

    @Override
    public AttachmentPE getExperimentFileAttachment(final String filename,
            final Integer versionOrNull)
    {
        AttachmentPE attachment = tryGetExperimentFileAttachment(filename, versionOrNull);

        if (attachment != null)
        {
            return attachment;
        } else
        {
            throw new UserFailureException("Attachment '"
                    + filename
                    + "' "
                    + (versionOrNull == null ? "(latestaa version)" : "(version '" + versionOrNull
                            + "')")
                    + " not found in experiment '" + experiment.getIdentifier() + "'.");
        }
    }

    private AttachmentPE getAttachment(String filename, final int version)
    {
        final Set<AttachmentPE> attachmentsSet = experiment.getAttachments();
        for (AttachmentPE att : attachmentsSet)
        {
            if (att.getFileName().equals(filename) && att.getVersion() == version)
            {
                return att;
            }
        }

        return null;
    }

    private AttachmentPE getAttachment(String filename)
    {
        AttachmentPE latest = null;
        final Set<AttachmentPE> attachmentsSet = experiment.getAttachments();
        for (AttachmentPE att : attachmentsSet)
        {
            if (att.getFileName().equals(filename))
            {
                if (latest == null || latest.getVersion() < att.getVersion())
                {
                    latest = att;
                }
            }
        }

        return latest;
    }

    @Override
    public void deleteByTechIds(List<TechId> experimentIds, String reason)
            throws UserFailureException
    {
        try
        {
            getSessionFactory().getCurrentSession().flush();
            getSessionFactory().getCurrentSession().clear();
            getExperimentDAO().delete(experimentIds, session.tryGetPerson(), reason);
        } catch (final DataAccessException ex)
        {
            throwException(ex, "Experiment", EntityKind.EXPERIMENT);
        }
    }

    public static EventPE createDeletionEvent(ExperimentPE experiment, PersonPE registrator,
            String reason)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.EXPERIMENT);
        event.setIdentifiers(Collections.singletonList(experiment.getPermId()));
        event.setDescription(getDeletionDescription(experiment));
        event.setReason(reason);
        event.setRegistrator(registrator);

        return event;
    }

    private static String getDeletionDescription(ExperimentPE experiment)
    {
        return String.format("%s [%s]", experiment.getIdentifier(), experiment.getPermId());
    }

    @Override
    public void define(NewExperiment newExperiment)
    {
        assert newExperiment != null : "Unspecified new experiment.";

        final ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifierFactory(newExperiment.getIdentifier()).createIdentifier();
        experiment = new ExperimentPE();
        final PersonPE registrator = findPerson();
        experiment.setCode(experimentIdentifier.getExperimentCode());
        experiment.setRegistrator(registrator);
        defineExperimentType(newExperiment);
        defineExperimentProperties(newExperiment.getExperimentTypeCode(),
                newExperiment.getProperties(), registrator);
        defineExperimentProject(newExperiment, experimentIdentifier);
        experiment.setPermId(getOrCreatePermID(newExperiment));
        setMetaprojects(experiment, newExperiment.getMetaprojectsOrNull());
        addAttachments(experiment, newExperiment.getAttachments(), attachments);
        RelationshipUtils.updateModificationDateAndModifier(experiment, session, getTransactionTimeStamp());
        dataChanged = true;
    }

    @Override
    public void addAttachment(AttachmentPE attachment)
    {
        assert experiment != null : "no experiment has been loaded";
        prepareAttachment(experiment, attachment);
        attachments.add(attachment);
    }

    private void defineExperimentProject(NewExperiment newExperiment,
            final ExperimentIdentifier experimentIdentifier)
    {
        ProjectPE project = tryGetProject(experimentIdentifier);
        if (project == null)
        {
            throw UserFailureException.fromTemplate(ERR_PROJECT_NOT_FOUND, newExperiment);
        }
        experiment.setProject(project);
        RelationshipUtils.updateModificationDateAndModifier(project, session, getTransactionTimeStamp());
    }

    private void defineExperimentType(NewExperiment newExperiment)
    {
        final String experimentTypeCode = newExperiment.getExperimentTypeCode();
        final EntityTypePE experimentType =
                getEntityTypeDAO(EntityKind.EXPERIMENT).tryToFindEntityTypeByCode(
                        experimentTypeCode);
        if (experimentType == null)
        {
            throw UserFailureException.fromTemplate(ERR_EXPERIMENT_TYPE_NOT_FOUND,
                    experimentTypeCode);
        }
        experiment.setExperimentType((ExperimentTypePE) experimentType);
    }

    @Override
    public void save() throws UserFailureException
    {
        if (dataChanged)
        {
            try
            {
                getExperimentDAO().createOrUpdateExperiment(experiment, findPerson());
            } catch (final DataAccessException ex)
            {
                final String projectCode = experiment.getProject().getCode();
                final ExperimentIdentifier identifier =
                        new ExperimentIdentifier(projectCode, experiment.getCode());
                throwException(ex, String.format("Experiment '%s'", identifier));
            }
            dataChanged = false;
        }
        saveAttachment(experiment, attachments);
        checkBusinessRules();
    }

    private void checkBusinessRules()
    {
        entityPropertiesConverter.checkMandatoryProperties(experiment.getProperties(),
                experiment.getExperimentType());
    }

    private final void defineExperimentProperties(final String experimentTypeCode,
            final IEntityProperty[] experimentProperties, PersonPE registrator)
    {
        final List<ExperimentPropertyPE> properties =
                entityPropertiesConverter.convertProperties(experimentProperties,
                        experimentTypeCode, registrator);
        for (final ExperimentPropertyPE experimentProperty : properties)
        {
            experiment.addProperty(experimentProperty);
        }
    }

    @Override
    public void update(ExperimentUpdatesDTO updates)
    {
        loadDataByTechId(updates.getExperimentId());
        if (updates.getVersion() != experiment.getVersion())
        {
            throwModifiedEntityException("Experiment");
        }
        updateProperties(experiment.getEntityType(), updates.getProperties(), extractPropertiesCodes(updates.getProperties()), experiment,
                experiment);

        if (updates.getProjectIdentifier() != null)
        {
            ProjectPE project = findProject(updates.getProjectIdentifier());
            ProjectPE previousProject = experiment.getProject();
            if (project.equals(previousProject) == false)
            {
                relationshipService.assignExperimentToProject(session, experiment, project);
            }
        }

        for (NewAttachment attachment : updates.getAttachments())
        {
            attachments.add(prepareAttachment(experiment, attachment));
        }
        updateSamples(updates);

        setMetaprojects(experiment, updates.getMetaprojectsOrNull());

        dataChanged = true;
    }

    private void updateSamples(ExperimentUpdatesDTO updates)
    {
        String[] sampleCodes = updates.getSampleCodes();
        if (sampleCodes != null)
        {
            if (updates.isRegisterSamples())
            {
                attachSamples(sampleCodes);
            } else
            {
                String[] originalSampleCodes = Code.extractCodesToArray(experiment.getSamples());
                updateSamples(originalSampleCodes, sampleCodes);
            }
        }
    }

    private void updateSamples(String[] originalSampleCodes, String[] sampleCodes)
    {
        Set<String> samplesToAdd = asSet(sampleCodes);
        samplesToAdd.removeAll(Arrays.asList(originalSampleCodes));
        addToExperiment(findSamplesByCodes(samplesToAdd, true));

        Set<String> samplesToRemove = asSet(originalSampleCodes);
        samplesToRemove.removeAll(Arrays.asList(sampleCodes));
        removeFromExperiment(findSamplesByCodes(samplesToRemove, false));
    }

    @Private
    // attaches specified existing samples to the experiment
    void attachSamples(String[] sampleCodes)
    {
        List<SamplePE> samplesToAdd = findSamplesByCodes(asSet(sampleCodes), true);
        addToExperiment(samplesToAdd);
    }

    private List<SamplePE> findSamplesByCodes(Set<String> codesToAdd, boolean unassigned)
    {
        return findSamples(getSampleDAO(), codesToAdd, experiment.getProject(), unassigned);
    }

    private void removeFromExperiment(List<SamplePE> samples)
    {
        for (SamplePE sample : samples)
        {
            List<DataPE> dataSets = getDataDAO().listDataSets(sample);
            checkDataSetsDoNotNeedAnExperiment(sample.getIdentifier(), dataSets);
            relationshipService.unassignSampleFromExperiment(session, sample);
            for (DataPE dataSet : dataSets)
            {
                if (dataSet.getExperiment() != null)
                {
                    relationshipService.assignDataSetToExperiment(session, dataSet, null);
                }
            }
        }
    }

    private void addToExperiment(List<SamplePE> samples)
    {
        for (SamplePE sample : samples)
        {
            assignSampleAndRelatedDataSetsToExperiment(sample, experiment);
        }
    }

    // Finds samples in the specified project or project space. Throws exception if some samples do not exist.
    // Throws exception if any sample code specified is already assigned to an experiment.
    private static List<SamplePE> findSamples(ISampleDAO sampleDAO, Set<String> sampleCodes,
            ProjectPE project, boolean unassigned) throws UserFailureException
    {
        List<SamplePE> samples = new ArrayList<SamplePE>();
        List<String> missingSamples = new ArrayList<String>();
        for (String code : sampleCodes)
        {
            SamplePE sample = sampleDAO.tryfindByCodeAndProject(code, project);
            if (sample == null)
            {
                sample = sampleDAO.tryFindByCodeAndSpace(code, project.getSpace(), true);
            }
            if (sample == null)
            {
                missingSamples.add(code);
            } else
            {
                if (unassigned)
                {
                    checkSampleUnassigned(sample.getIdentifier(), sample);
                }
                samples.add(sample);
            }
        }
        if (missingSamples.size() > 0)
        {
            throw UserFailureException.fromTemplate(
                    "Samples with following codes do not exist in the project '%s' nor in the space '%s': '%s'.",
                    project.getIdentifier(), project.getSpace().getCode(), 
                    CollectionUtils.abbreviate(missingSamples, 10));
        } else
        {
            return samples;
        }
    }

    private static void checkSampleUnassigned(String identifier, SamplePE sample)
    {
        if (sample.getExperiment() != null)
        {
            throw UserFailureException.fromTemplate(
                    "Sample '%s' is already assigned to the experiment '%s'.", identifier, sample
                            .getExperiment().getIdentifier());
        }
    }

    private static Set<String> asSet(String[] objects)
    {
        return new HashSet<String>(Arrays.asList(objects));
    }

    private ProjectPE findProject(ProjectIdentifier newProjectIdentifier)
    {
        ProjectPE project =
                getProjectDAO().tryFindProject(
                        newProjectIdentifier.getSpaceCode(), newProjectIdentifier.getProjectCode());
        if (project == null)
        {
            throw UserFailureException.fromTemplate(ERR_PROJECT_NOT_FOUND, newProjectIdentifier);
        }
        return project;
    }

    @Override
    public void updateManagedProperty(IManagedProperty managedProperty)
    {
        final Set<ExperimentPropertyPE> existingProperties = experiment.getProperties();
        final ExperimentTypePE type = experiment.getExperimentType();
        final PersonPE registrator = findPerson();
        experiment.setProperties(entityPropertiesConverter.updateManagedProperty(
                existingProperties, type, managedProperty, registrator));

        dataChanged = true;
    }

}
