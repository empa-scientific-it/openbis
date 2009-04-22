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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.HierarchyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

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

    private final IEntityPropertiesConverter propertiesConverter;

    private ExperimentPE experiment;

    private boolean dataChanged;

    private final List<AttachmentPE> attachments = new ArrayList<AttachmentPE>();

    public ExperimentBO(final IDAOFactory daoFactory, final Session session)
    {
        this(daoFactory, session, new EntityPropertiesConverter(EntityKind.EXPERIMENT, daoFactory));
    }

    @Private
    ExperimentBO(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter)
    {
        super(daoFactory, session);
        propertiesConverter = entityPropertiesConverter;
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

    @SuppressWarnings("unused")
    private final void defineSampleProperties(final SampleProperty[] experimentProperties)
    {
        final String experimentTypeCode = experiment.getExperimentType().getCode();
        final List<ExperimentPropertyPE> properties =
                propertiesConverter.convertProperties(experimentProperties, experimentTypeCode,
                        experiment.getRegistrator());
        for (final ExperimentPropertyPE property : properties)
        {
            experiment.addProperty(property);
        }
    }

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

    public final void loadByExperimentIdentifier(final ExperimentIdentifier identifier)
    {
        experiment = getExperimentByIdentifier(identifier);
        if (experiment == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample could be found with given identifier '%s'.", identifier);
        }
        dataChanged = false;
    }

    private ExperimentPE getExperimentByIdentifier(final ExperimentIdentifier identifier)
    {
        assert identifier != null : "Experiment identifier unspecified.";
        final ProjectPE project =
                getProjectDAO().tryFindProject(identifier.getDatabaseInstanceCode(),
                        identifier.getGroupCode(), identifier.getProjectCode());
        final ExperimentPE exp =
                getExperimentDAO().tryFindByCodeAndProject(project, identifier.getExperimentCode());
        if (exp == null)
        {
            throw UserFailureException.fromTemplate(
                    "No experiment could be found for identifier '%s'.", identifier);
        }
        return exp;
    }

    public final void enrichWithProperties()
    {
        if (experiment != null)
        {
            HibernateUtils.initialize(experiment.getProperties());
        }
    }

    public final void enrichWithAttachments()
    {
        if (experiment != null)
        {
            experiment.ensureAttachmentsLoaded();
        }
    }

    public AttachmentPE getExperimentFileAttachment(final String filename, final int version)
    {
        checkExperimentLoaded();
        experiment.ensureAttachmentsLoaded();
        final Set<AttachmentPE> attachmentsSet = experiment.getAttachments();
        for (AttachmentPE att : attachmentsSet)
        {
            if (att.getFileName().equals(filename) && att.getVersion() == version)
            {
                HibernateUtils.initialize(att.getAttachmentContent());
                return att;
            }
        }
        throw new UserFailureException("Attachment '" + filename + "' (version '" + version
                + "') not found in experiment '" + experiment.getIdentifier() + "'.");
    }

    public void define(NewExperiment newExperiment)
    {
        assert newExperiment != null : "Unspecified new experiment.";

        final ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifierFactory(newExperiment.getIdentifier()).createIdentifier();
        experiment = new ExperimentPE();
        final PersonPE registrator = findRegistrator();
        experiment.setCode(experimentIdentifier.getExperimentCode());
        experiment.setRegistrator(registrator);
        defineExperimentType(newExperiment);
        defineExperimentProperties(newExperiment.getExperimentTypeCode(), newExperiment
                .getProperties(), registrator);
        defineExperimentProject(newExperiment, experimentIdentifier);
        dataChanged = true;
    }

    public final void addAttachment(final AttachmentPE experimentAttachment)
    {
        assert experiment != null : "no experiment has been loaded";
        experimentAttachment.setRegistrator(findRegistrator());
        escapeFileName(experimentAttachment);
        attachments.add(experimentAttachment);
    }

    private void escapeFileName(final AttachmentPE attachment)
    {
        if (attachment != null)
        {
            attachment.setFileName(ExperimentPE.escapeFileName(attachment.getFileName()));
        }
    }

    private void defineExperimentProject(NewExperiment newExperiment,
            final ExperimentIdentifier experimentIdentifier)
    {
        ProjectPE project =
                getProjectDAO().tryFindProject(experimentIdentifier.getDatabaseInstanceCode(),
                        experimentIdentifier.getGroupCode(), experimentIdentifier.getProjectCode());
        if (project == null)
        {
            throw UserFailureException.fromTemplate(ERR_PROJECT_NOT_FOUND, newExperiment);
        }
        experiment.setProject(project);
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

    public void save() throws UserFailureException
    {
        if (dataChanged)
        {
            try
            {
                getExperimentDAO().createExperiment(experiment);
            } catch (final DataAccessException ex)
            {
                final String projectCode = experiment.getProject().getCode();
                final ExperimentIdentifier identifier =
                        new ExperimentIdentifier(projectCode, experiment.getCode());
                throwException(ex, String.format("Experiment '%s'", identifier));
            }
            dataChanged = false;
        }
        if (attachments.isEmpty() == false)
        {
            final IAttachmentDAO attachmentDAO = getAttachmentDAO();
            for (final AttachmentPE property : attachments)
            {
                try
                {
                    attachmentDAO.createAttachment(property, experiment);
                } catch (final DataAccessException e)
                {
                    final String fileName = property.getFileName();
                    throwException(e, String.format("Filename '%s' for experiment '%s'", fileName,
                            createExperimentIdentifier()));
                }
            }
            attachments.clear();
        }
        checkBusinessRules();
    }

    private void checkBusinessRules()
    {
        propertiesConverter.checkMandatoryProperties(experiment.getProperties(), experiment
                .getExperimentType());
    }

    private ExperimentIdentifier createExperimentIdentifier()
    {
        return new ExperimentIdentifier(experiment.getProject().getCode(), experiment.getCode());
    }

    private final void defineExperimentProperties(final String experimentTypeCode,
            final ExperimentProperty[] experimentProperties, PersonPE registrator)
    {
        final List<ExperimentPropertyPE> properties =
                propertiesConverter.convertProperties(experimentProperties, experimentTypeCode,
                        registrator);
        for (final ExperimentPropertyPE experimentProperty : properties)
        {
            experiment.addProperty(experimentProperty);
        }
    }

    public void edit(ExperimentUpdatesDTO updates)
    {
        loadByExperimentIdentifier(updates.getExperimentIdentifier());
        if (experiment.getModificationDate().equals(updates.getVersion()) == false)
        {
            throw new UserFailureException("Experiment has been modified in the meantime.");
        }
        updateProperties(updates.getProperties());
        updateProject(updates.getProjectIdentifier());
        for (AttachmentPE a : updates.getAttachments())
        {
            addAttachment(a);
        }
        String[] sampleCodes = updates.getSampleCodes();
        if (sampleCodes != null)
        {
            updateSamples(sampleCodes);
        }
        dataChanged = true;
    }

    @Private
    void updateSamples(String[] sampleCodes)
    {
        List<SamplePE> samples = experiment.getSamples();
        String[] currentSampleCodes = extractCodes(samples);
        Set<String> currentSampleCodesSet = asSet(currentSampleCodes);

        Set<String> codesToAdd = asSet(sampleCodes);
        codesToAdd.removeAll(currentSampleCodesSet);
        GroupPE group = experiment.getProject().getGroup();
        List<SamplePE> samplesToAdd = findUnassignedSamples(getSampleDAO(), codesToAdd, group);
        addToExperiment(samplesToAdd);

        Set<String> codesToRemove = asSet(currentSampleCodes);
        codesToRemove.removeAll(asSet(sampleCodes));
        removeFromExperiment(filterSamples(samples, codesToRemove));
    }

    private static List<SamplePE> filterSamples(List<SamplePE> samples, Set<String> extractedCodes)
    {
        List<SamplePE> result = new ArrayList<SamplePE>();
        for (SamplePE sample : samples)
        {
            if (extractedCodes.contains(sample.getCode()))
            {
                result.add(sample);
            }
        }
        return result;
    }

    private void removeFromExperiment(List<SamplePE> samples)
    {
        for (SamplePE sample : samples)
        {
            SampleBO.checkSampleWithoutDatasets(getExternalDataDAO(), sample);
            experiment.removeSample(sample);
        }
    }

    private void addToExperiment(List<SamplePE> samples)
    {
        for (SamplePE sample : samples)
        {
            sample.setExperiment(experiment);
        }
    }

    // Finds samples in the specified group. Throws exception if some samples do not exist.
    // Throws exception if any sample is already assigned to an experiment.
    private static List<SamplePE> findUnassignedSamples(ISampleDAO sampleDAO,
            Set<String> sampleCodes, GroupPE group) throws UserFailureException
    {
        List<SamplePE> samples = new ArrayList<SamplePE>();
        List<String> missingSamples = new ArrayList<String>();
        for (String code : sampleCodes)
        {
            SamplePE sample = sampleDAO.tryFindByCodeAndGroup(code, group, HierarchyType.CHILD);
            if (sample == null)
            {
                missingSamples.add(code);
            } else
            {
                checkSampleUnassigned(code, sample);
                samples.add(sample);
            }
        }
        if (missingSamples.size() > 0)
        {
            throw UserFailureException.fromTemplate(
                    "Samples with following codes do not exist in the group '%s': '%s'.", group
                            .getCode(), CollectionUtils.abbreviate(missingSamples, 10));
        } else
        {
            return samples;
        }
    }

    private static void checkSampleUnassigned(String code, SamplePE sample)
    {
        if (sample.getExperiment() != null)
        {
            throw UserFailureException.fromTemplate(
                    "Sample '%s' is already assigned to the experiment '%s'.", code, sample
                            .getExperiment().getIdentifier());
        }
    }

    private static Set<String> asSet(String[] objects)
    {
        return new HashSet<String>(Arrays.asList(objects));
    }

    private static String[] extractCodes(List<SamplePE> samples)
    {
        String[] codes = new String[samples.size()];
        int i = 0;
        for (SamplePE sample : samples)
        {
            codes[i] = sample.getCode();
            i++;
        }
        return codes;
    }

    private void updateProject(ProjectIdentifier newProjectIdentifier)
    {
        ProjectPE project =
                getProjectDAO().tryFindProject(newProjectIdentifier.getDatabaseInstanceCode(),
                        newProjectIdentifier.getGroupCode(), newProjectIdentifier.getProjectCode());
        if (project == null)
        {
            throw UserFailureException.fromTemplate(ERR_PROJECT_NOT_FOUND, newProjectIdentifier);
        }
        experiment.setProject(project);
    }

    private void updateProperties(List<ExperimentProperty> properties)
    {
        final Set<ExperimentPropertyPE> existingProperties = experiment.getProperties();
        final ExperimentTypePE type = experiment.getExperimentType();
        final PersonPE registrator = findRegistrator();
        experiment.setProperties(propertiesConverter.updateProperties(existingProperties, type,
                properties, registrator));
    }

    public void setGeneratedCode()
    {
        final String code = createCode(EntityKind.EXPERIMENT);
        experiment.setCode(code);
    }
}
