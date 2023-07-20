/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IEntityOperationChecker;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.LocalExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * The unique {@link ISampleBO} implementation.
 * 
 * @author Tomasz Pylak
 */
public final class SampleTable extends AbstractSampleBusinessObject implements ISampleTable
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SampleTable.class);
    private List<SamplePE> samples;

    private boolean dataChanged;

    private boolean businessRulesChecked;

    public SampleTable(final IDAOFactory daoFactory, final Session session,
            IRelationshipService relationshipService,
            IEntityOperationChecker entityOperationChecker,
            IEntityInformationProvider entityInformationProvider,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker)
    {
        super(daoFactory, session, relationshipService, entityOperationChecker, entityInformationProvider,
                managedPropertyEvaluatorFactory, dataSetTypeChecker);
    }

    @Override
    public final void loadSamplesByCriteria(final ListSamplesByPropertyCriteria criteria)
    {
        onlyNewSamples = false;
        SpacePE group = findGroup(criteria.getSpaceCode());
        List<SamplePE> foundSamples =
                getSampleDAO().listSamplesBySpaceAndProperty(criteria.getPropertyCode(),
                        criteria.getPropertyValue(), group);
        LocalExperimentIdentifier localExperimentIdentifier =
                criteria.tryGetLocalExperimentIdentifier();
        if (localExperimentIdentifier != null)
        {
            String experimentPropertyCode = localExperimentIdentifier.tryGetPropertyCode();
            if (experimentPropertyCode != null)
            {
                foundSamples =
                        filterSamplesByExperiment(foundSamples, criteria.getProjectIdentifier(),
                                experimentPropertyCode,
                                localExperimentIdentifier.getPropertyValue());
            } else
            {
                ExperimentIdentifier ident =
                        new ExperimentIdentifier(criteria.getProjectIdentifier(),
                                localExperimentIdentifier.getExperimentCode());
                foundSamples = filterSamplesByExperiment(foundSamples, ident);
            }
        }
        samples = foundSamples;
        attachmentHolderPermIdToAttachmentsMap = null;
    }

    private List<SamplePE> filterSamplesByExperiment(List<SamplePE> foundSamples,
            ProjectIdentifier projectIdentifier, String experimentPropertyCode, String propertyValue)
    {
        ProjectPE project = findProject(projectIdentifier);
        ExperimentPE expectedExperiment =
                findExperiment(project, experimentPropertyCode, propertyValue);
        return filterSamplesByExperiment(foundSamples, expectedExperiment);
    }

    private ExperimentPE findExperiment(ProjectPE project, String propertyCode, String propertyValue)
    {
        List<ExperimentPE> experiments =
                getExperimentDAO().listExperimentsByProjectAndProperty(propertyCode, propertyValue,
                        project);
        if (experiments.size() != 1)
        {
            throw UserFailureException
                    .fromTemplate(
                            "It was expected that there is exactly one experiment "
                                    + "in the '%s/%s' project with property '%s' set to '%s', but %d were found!",
                            project.getSpace().getCode(), project.getCode(), propertyCode,
                            propertyValue, experiments.size());
        }
        return experiments.get(0);
    }

    private List<SamplePE> filterSamplesByExperiment(List<SamplePE> foundSamples,
            ExperimentIdentifier experimentIdentifier)
    {
        ExperimentPE expectedExperiment = findExperiment(experimentIdentifier);
        return filterSamplesByExperiment(foundSamples, expectedExperiment);
    }

    private static List<SamplePE> filterSamplesByExperiment(List<SamplePE> foundSamples,
            ExperimentPE expectedExperiment)
    {
        List<SamplePE> filteredSamples = new ArrayList<SamplePE>();
        for (SamplePE sample : foundSamples)
        {
            if (expectedExperiment.equals(sample.getExperiment()))
            {
                filteredSamples.add(sample);
            }
        }
        return filteredSamples;
    }

    private SpacePE findGroup(String groupCode)
    {
        SpacePE group =
                getSpaceDAO().tryFindSpaceByCode(groupCode);
        if (group == null)
        {
            throw UserFailureException
                    .fromTemplate("No group with the name '%s' found!", groupCode);
        }
        return group;
    }

    @Override
    public final List<SamplePE> getSamples()
    {
        return samples;
    }

    @Override
    public void prepareForRegistration(List<NewSample> newSamples, PersonPE registratorOrNull)
            throws UserFailureException
    {
        assertInstanceSampleCreationAllowed(newSamples);

        onlyNewSamples = true;
        samples = new ArrayList<SamplePE>(newSamples.size());
        attachmentHolderPermIdToAttachmentsMap = new HashMap<String, List<AttachmentPE>>(samples.size());

        setBatchUpdateMode(true);

        final Map<String, SampleTypePE> sampleTypeCache = new HashMap<String, SampleTypePE>();
        final Map<String, ExperimentPE> experimentCache = new HashMap<String, ExperimentPE>();
        final Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache =
                new HashMap<SampleOwnerIdentifier, SampleOwner>();
        for (NewSample sample : newSamples)
        {
            add(sample, sampleTypeCache, sampleOwnerCache, experimentCache, registratorOrNull);
        }

        setBatchUpdateMode(false);
    }

    private void add(final NewSample newSample, final Map<String, SampleTypePE> sampleTypeCache,
            final Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache,
            Map<String, ExperimentPE> experimentCache, PersonPE registratorOrNull)
            throws UserFailureException
    {
        assert newSample != null : "Unspecified new sample.";
        final SamplePE samplePE =
                createSample(newSample, sampleTypeCache, sampleOwnerCache, experimentCache,
                        registratorOrNull);
        samples.add(samplePE);
        final List<AttachmentPE> attachments = new ArrayList<AttachmentPE>();
        addAttachments(samplePE, newSample.getAttachments(), attachments);
        putAttachments(samplePE.getPermId(), attachments);
        dataChanged = true;
    }

    @Override
    public void save() throws UserFailureException
    {
        save(false);
    }

    @Override
    public void save(boolean clearCache) throws UserFailureException
    {
        assert samples != null : "Samples not loaded.";

        if (dataChanged)
        {
            try
            {
                if (businessRulesChecked == false)
                {
                    checkAllBusinessRules();
                }
                getSampleDAO().createOrUpdateSamples(samples, findPerson(), clearCache);
            } catch (final DataAccessException ex)
            {
                throwException(ex, String.format("One of samples"));
            }
            dataChanged = false;
            businessRulesChecked = false;
            onlyNewSamples = false;
        }
        saveAttachments(samples, attachmentHolderPermIdToAttachmentsMap);
    }

    private void checkAllBusinessRules()
    {
        final Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache =
                new HashMap<EntityTypePE, List<EntityTypePropertyTypePE>>();
        for (SamplePE s : samples)
        {
            checkAllBusinessRules(s, getDataDAO(), cache, true);
        }
    }

    private void prepareBatchUpdate(SamplePE sample, List<AttachmentPE> attachments,
            SampleBatchUpdatesDTO updates,
            Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache,
            Map<String, ExperimentPE> experimentCache,
            Map<EntityTypePE, List<EntityTypePropertyTypePE>> propertiesCache)
    {
        if (sample == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample could be found with given identifier '%s'.",
                    updates.getOldSampleIdentifierOrNull());
        }

        ExperimentPE oldExperiment = sample.getExperiment();
        ProjectPE oldProject = sample.getProject();

        SampleBatchUpdateDetails details = updates.getDetails();
        batchUpdateProperties(sample, updates.getProperties(), details.getPropertiesToUpdate());
        checkPropertiesBusinessRules(sample, propertiesCache);

        if (details.isExperimentUpdateRequested())
        {
            updateSpace(sample, updates.getSampleIdentifier(), sampleOwnerCache);
            updateExperiment(sample, updates.getExperimentIdentifierOrNull(), experimentCache);
            checkExperimentBusinessRules(sample);
        }
        if (details.isParentsUpdateRequested())
        {
            final String[] parents = updates.getModifiedParentCodesOrNull();
            if (parents != null)
            {
                setParents(sample, parents, updates.tryGetDefaultSpaceIdentifier());
            }
        }
        if (details.isContainerUpdateRequested())
        {
            setContainer(updates.getSampleIdentifier(), sample,
                    updates.getContainerIdentifierOrNull(), updates.tryGetDefaultSpaceIdentifier());
        }

        addAttachments(sample, updates.getAttachments(), attachments);

        // NOTE: Checking business rules with relationships is expensive.
        // Don't perform them unless relevant data were changed.
        if (details.isExperimentUpdateRequested() || details.isParentsUpdateRequested())
        {
            checkParentBusinessRules(sample);
        }
        if (details.isExperimentUpdateRequested() || details.isContainerUpdateRequested())
        {
            checkContainerBusinessRules(sample);
        }

        if ((oldExperiment != null || oldProject != null) && (sample.getExperiment() == null && sample.getProject() == null))
        {
            relationshipService.assignSampleToSpace(session, sample, sample.getSpace());
        }

        RelationshipUtils.updateModificationDateAndModifier(sample, session, getTransactionTimeStamp());
    }

    /**
     * Prepare a batch update using the SampleUpdatesDTO, not the SampleBatchUpdatesDTO. This version assumes that all the properties are provided in
     * the updates object, not just those that should explicitly be updated.
     */
    private void prepareBatchUpdate(SamplePE sample, List<AttachmentPE> attachments,
            SampleUpdatesDTO updates, Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache,
            Map<String, ExperimentPE> experimentCache, Map<String, ProjectPE> projectCache,
            Map<EntityTypePE, List<EntityTypePropertyTypePE>> propertiesCache)
    {
        if (sample == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample could be found with given identifier '%s'.",
                    updates.getSampleIdentifier());
        }

        ExperimentPE oldExperiment = sample.getExperiment();
        ProjectPE oldProject = sample.getProject();

        operationLog.info("||> prepareBatchUpdate for:"+sample);
        updateProperties(sample, updates.getProperties());
        checkPropertiesBusinessRules(sample, propertiesCache);

        updateSpace(sample, updates.getSampleIdentifier(), sampleOwnerCache);
        ExperimentIdentifier experimentIdentifier = updates.getExperimentIdentifierOrNull();
        if (updates.isUpdateExperimentLink())
        {
            updateExperiment(sample, experimentIdentifier, experimentCache);
            checkExperimentBusinessRules(sample);
        }

        if (experimentIdentifier == null)
        {
            updateProject(sample, updates.getProjectIdentifier(), projectCache);
        }

        boolean parentsUpdated = updateParents(sample, updates);

        boolean containerUpdated = updateContainer(sample, updates);

        addAttachments(sample, updates.getAttachments(), attachments);

        // NOTE: Checking business rules with relationships is expensive.
        // Don't perform them unless relevant data were changed.
        if (updates.isUpdateExperimentLink() || parentsUpdated)
        {
            checkParentBusinessRules(sample);
        }
        if (updates.isUpdateExperimentLink() || containerUpdated)
        {
            checkContainerBusinessRules(sample);
        }

        if ((oldExperiment != null || oldProject != null) && (sample.getExperiment() == null && sample.getProject() == null))
        {
            relationshipService.assignSampleToSpace(session, sample, sample.getSpace());
        }

        RelationshipUtils.updateModificationDateAndModifier(sample, session, getTransactionTimeStamp());
    }

    private boolean updateContainer(SamplePE sample, SampleUpdatesDTO updates)
    {
        SamplePE container = sample.getContainer();
        String oldContainerIdentifierOrNull =
                (null == container) ? null : container.getIdentifier();
        // Figure out if we need to update the container
        if (oldContainerIdentifierOrNull == null)
        {
            if (updates.getContainerIdentifierOrNull() == null)
            {
                return false;
            }
        } else
        {
            if (oldContainerIdentifierOrNull.equals(updates.getContainerIdentifierOrNull()))
            {
                return false;
            }
        }
        setContainer(updates.getSampleIdentifier(), sample, updates.getContainerIdentifierOrNull(),
                null);
        return true;
    }

    /**
     * Update parents and return whether or not something was changed.
     * 
     * @return True if the parents were changed, false if nothing was changed
     */
    private boolean updateParents(SamplePE sample, SampleUpdatesDTO updates)
    {
        final String[] newParents = updates.getModifiedParentCodesOrNull();
        // If the parents are null, don't touch them
        if (null == newParents)
        {
            return false;
        }

        // Compare the old and new parents
        Set<String> oldParentsSet = new HashSet<String>();
        for (SamplePE parent : sample.getParents())
        {
            oldParentsSet.add(parent.getCode());
        }
        Set<String> newParentsSet = new HashSet<String>();
        newParentsSet.addAll(Arrays.asList(newParents));

        // Nothing to change
        if (oldParentsSet.equals(newParentsSet))
        {
            return false;
        }

        setParents(sample, newParents, null);
        return true;
    }

    private void updateProperties(SamplePE sample, List<IEntityProperty> properties)
    {
        final Set<SamplePropertyPE> existingProperties = sample.getProperties();
        final SampleTypePE type = sample.getSampleType();
        sample.setProperties(convertProperties(type, existingProperties, properties, extractPropertiesCodes(properties)));
    }

    private void batchUpdateProperties(SamplePE sample, List<IEntityProperty> properties,
            Set<String> propertiesToUpdate)
    {
        final Set<SamplePropertyPE> existingProperties = sample.getProperties();
        final SampleTypePE type = sample.getSampleType();
        final PersonPE registrator = findPerson();
        sample.setProperties(entityPropertiesConverter.updateProperties(existingProperties, type,
                properties, registrator, propertiesToUpdate));
    }

    @Override
    public void prepareForUpdate(List<SampleBatchUpdatesDTO> updates)
    {
        assert updates != null : "Unspecified samples.";

        setBatchUpdateMode(true);
        final Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache =
                new HashMap<SampleOwnerIdentifier, SampleOwner>();
        final Map<String, ExperimentPE> experimentCache = new HashMap<String, ExperimentPE>();
        final Map<EntityTypePE, List<EntityTypePropertyTypePE>> propertiesCache =
                new HashMap<EntityTypePE, List<EntityTypePropertyTypePE>>();
        samples = loadSamples(updates, sampleOwnerCache);
        attachmentHolderPermIdToAttachmentsMap = new HashMap<String, List<AttachmentPE>>(samples.size());

        assertInstanceSampleUpdateAllowed(samples);

        final Map<SampleIdentifier, SamplePE> samplesByIdentifiers =
                new HashMap<SampleIdentifier, SamplePE>();
        for (SamplePE sample : samples)
        {
            samplesByIdentifiers.put(sample.getSampleIdentifier(), sample);
        }
        for (SampleBatchUpdatesDTO sampleUpdates : updates)
        {
            final SamplePE sample =
                    samplesByIdentifiers.get(sampleUpdates.getOldSampleIdentifierOrNull());
            final List<AttachmentPE> attachments = new ArrayList<AttachmentPE>();
            prepareBatchUpdate(sample, attachments, sampleUpdates, sampleOwnerCache,
                    experimentCache, propertiesCache);
            putAttachments(sample.getPermId(), attachments);
        }

        dataChanged = true;
        businessRulesChecked = true;

        setBatchUpdateMode(false);
    }

    @Override
    public void checkBeforeUpdate(List<SampleUpdatesDTO> updates) throws UserFailureException
    {
        if (updates == null)
        {
            throw new IllegalArgumentException("Sample updates list cannot be null.");
        }

        Map<Long, Integer> versionsMap = new HashMap<Long, Integer>();
        for (SamplePE sample : loadSamplesByTechId(updates))
        {
            versionsMap.put(sample.getId(), sample.getVersion());
        }

        for (SampleUpdatesDTO update : updates)
        {
            if (update.getSampleIdOrNull() == null || update.getSampleIdOrNull().getId() == null)
            {
                throw new UserFailureException("Sample with identifier "
                        + update.getSampleIdentifier()
                        + " doesn't have a specified id and therefore cannot be updated.");
            }

            Integer version = versionsMap.get(update.getSampleIdOrNull().getId());

            if (version == null)
            {
                throw new UserFailureException("Sample with identifier "
                        + update.getSampleIdentifier()
                        + " is not in the database and therefore cannot be updated.");
            } else if (version != update.getVersion())
            {
                StringBuffer sb = new StringBuffer();
                sb.append("Sample ");
                sb.append(update.getSampleIdentifier());
                sb.append(" has been updated since it was retrieved.\n");
                sb.append("[Current: " + version);
                sb.append(", Retrieved: " + update.getVersion());
                sb.append("]");
                throw new UserFailureException(sb.toString());
            }
        }
    }

    @Override
    public void prepareForUpdateWithSampleUpdates(List<SampleUpdatesDTO> updates)
    {
        assert updates != null : "Unspecified samples.";

        setBatchUpdateMode(true);
        final Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache =
                new HashMap<SampleOwnerIdentifier, SampleOwner>();
        final Map<String, ExperimentPE> experimentCache = new HashMap<String, ExperimentPE>();
        final Map<String, ProjectPE> projectCache = new HashMap<String, ProjectPE>();
        final Map<EntityTypePE, List<EntityTypePropertyTypePE>> propertiesCache =
                new HashMap<EntityTypePE, List<EntityTypePropertyTypePE>>();
        samples = loadSamplesByTechId(updates);
        attachmentHolderPermIdToAttachmentsMap = new HashMap<String, List<AttachmentPE>>(samples.size());

        assertInstanceSampleUpdateAllowed(samples);

        final Map<Long, SamplePE> samplesById = new HashMap<Long, SamplePE>();
        for (SamplePE sample : samples)
        {
            samplesById.put(sample.getId(), sample);
        }
        for (SampleUpdatesDTO sampleUpdates : updates)
        {
            TechId id = sampleUpdates.getSampleIdOrNull();
            if (null == id)
            {
                throw new UserFailureException("Sample with identifier "
                        + sampleUpdates.getSampleIdentifier()
                        + " is not in the database and therefore cannot be updated.");
            }

            final SamplePE sample = samplesById.get(id.getId());
            if (null == sample)
            {
                throw new UserFailureException("Sample with identifier "
                        + sampleUpdates.getSampleIdentifier()
                        + " is not in the database and therefore cannot be updated.");
            }
            final List<AttachmentPE> attachments = new ArrayList<AttachmentPE>();
            prepareBatchUpdate(sample, attachments, sampleUpdates, sampleOwnerCache,
                    experimentCache, projectCache, propertiesCache);
            putAttachments(sample.getPermId(), attachments);
        }

        dataChanged = true;
        businessRulesChecked = true;

        setBatchUpdateMode(false);
    }

    private List<SamplePE> loadSamplesByTechId(List<SampleUpdatesDTO> updates)
    {
        final List<SamplePE> results = new ArrayList<SamplePE>();
        List<TechId> identifiers = new ArrayList<TechId>();
        for (SampleUpdatesDTO sampleUpdates : updates)
        {
            TechId id = sampleUpdates.getSampleIdOrNull();
            if (id != null)
            {
                identifiers.add(id);
            }
        }
        results.addAll(listSamplesByTechIds(identifiers));
        return results;
    }

    private List<SamplePE> loadSamples(List<SampleBatchUpdatesDTO> updates,
            Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache)
    {
        final List<SamplePE> results = new ArrayList<SamplePE>();
        List<SampleIdentifier> identifiers = new ArrayList<SampleIdentifier>();
        for (SampleBatchUpdatesDTO sampleUpdates : updates)
        {
            SampleIdentifier identifier = sampleUpdates.getOldSampleIdentifierOrNull();
            identifiers.add(identifier);
        }
        results.addAll(listSamplesByIdentifiers(identifiers, sampleOwnerCache));
        return results;
    }

    @Override
    public void deleteByTechIds(List<TechId> sampleIds, String reason) throws UserFailureException
    {
        try
        {
            getSessionFactory().getCurrentSession().flush();
            getSessionFactory().getCurrentSession().clear();
            getSampleDAO().delete(sampleIds, session.tryGetPerson(), reason);
        } catch (final DataAccessException ex)
        {
            throwException(ex, "Sample", EntityKind.SAMPLE);
        }
    }

}
