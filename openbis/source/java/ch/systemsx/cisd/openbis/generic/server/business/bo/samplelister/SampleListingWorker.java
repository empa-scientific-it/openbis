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

package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesHolderResolver;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.AbstractLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.ExperimentProjectSpaceCodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaProjectWithEntityId;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.DataIterator;

/**
 * A business worker object for fast sample listing. It has only one interface method, which is {@link #load()}. This method deals with
 * <ul>
 * <li>information stored in the sample table</li>
 * <li>sample type information</li>
 * <li>assigned experiment, project, group and database instance</li>
 * <li>sample relationships (parent-child and contained-container)</li>
 * </ul>
 * It delegates the work of enriching the samples with properties to an implementation of a {@link IEntityPropertiesEnricher}. The worker follows the
 * logic that only the samples specified by the {@link ListSampleCriteria}, the <i>primary samples</i>, should be enriched with properties (as only
 * primary samples are shown in a separate row of the list). From <i>dependent samples</i>, only basic information is obtained.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
{ ExperimentProjectSpaceCodeRecord.class, SampleRecord.class, SampleRelationRecord.class,
        ISampleListingQuery.class })
final class SampleListingWorker extends AbstractLister
{
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SampleListingWorker.class);

    //
    // Input
    //

    private final ListOrSearchSampleCriteria criteria;

    private final String baseIndexURL;

    //
    // Output
    //

    private final List<Sample> sampleList = new ArrayList<Sample>(ISampleListingQuery.FETCH_SIZE);

    //
    // Working interfaces
    //

    private final ISampleListingQuery query;

    private final IEntityPropertiesEnricher samplePropertiesEnricherOrNull;

    private final SecondaryEntityDAO referencedEntityDAO;

    //
    // Working data structures
    //

    /**
     * A record for storing a samples and a related (parent or container) sample id.
     */
    private static class RelatedSampleRecord
    {
        Sample sample;

        long relatedSampleId;

        RelatedSampleRecord(Sample sample, long relatedSampleId)
        {
            this.sample = sample;
            this.relatedSampleId = relatedSampleId;
        }
    }

    private final Long2ObjectOpenHashMap<SampleType> sampleTypes =
            new Long2ObjectOpenHashMap<SampleType>();

    private final Long2ObjectMap<Project> projects = new Long2ObjectOpenHashMap<Project>();

    private final LongSet idsOfSamplesAwaitingParentResolution = new LongOpenHashSet();

    private final List<RelatedSampleRecord> samplesAwaitingParentResolution =
            new ArrayList<RelatedSampleRecord>();

    private final Long2ObjectMap<RelatedSampleRecord> samplesAwaitingContainerResolution =
            new Long2ObjectOpenHashMap<RelatedSampleRecord>();

    private final LongSet idsOfRequestedContainerSamples = new LongOpenHashSet();

    private final LongSet idsOfRequestedParentSamples = new LongOpenHashSet();

    private boolean singleSampleTypeMode;

    private boolean enrichDependentSamples;

    private int maxSampleParentResolutionDepth;

    private final Long2ObjectMap<Sample> sampleMap = new Long2ObjectOpenHashMap<Sample>();

    private final Long2ObjectMap<Space> spaceMap = new Long2ObjectOpenHashMap<Space>();

    private final long parentRelationhipTypeId;

    private final Long userId;

    public static SampleListingWorker create(ListOrSearchSampleCriteria criteria,
            String baseIndexURL, SampleListerDAO dao, SecondaryEntityDAO referencedEntityDAO,
            Long userId)
    {
        ISampleListingQuery query = dao.getQuery();
        EntityPropertiesEnricher propertiesEnricher =
                new EntityPropertiesEnricher(query, dao.getPropertySetQuery());
        return new SampleListingWorker(criteria, baseIndexURL, dao.getDatabaseInstanceId(),
                dao.getDatabaseInstance(), query, propertiesEnricher, referencedEntityDAO, userId);
    }

    //
    // Constructors
    //

    // For unit tests
    SampleListingWorker(final ListOrSearchSampleCriteria criteria, final String baseIndexURL,
            final long databaseInstanceId, final DatabaseInstance databaseInstance,
            final ISampleListingQuery query,
            IEntityPropertiesEnricher samplePropertiesEnricherOrNull,
            SecondaryEntityDAO referencedEntityDAO, Long userId)
    {
        super(referencedEntityDAO);
        assert criteria != null;
        assert baseIndexURL != null;
        assert databaseInstance != null;
        assert query != null;

        this.criteria = criteria;
        this.baseIndexURL = baseIndexURL;
        this.query = query;
        this.samplePropertiesEnricherOrNull = samplePropertiesEnricherOrNull;
        this.enrichDependentSamples = criteria.isEnrichDependentSamplesWithProperties();
        this.referencedEntityDAO = referencedEntityDAO;
        this.parentRelationhipTypeId =
                getRelationId(query, BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
        this.userId = userId;
    }

    static long getRelationId(ISampleListingQuery query, String fullRelationCode)
    {
        return query.getRelationshipTypeId(CodeConverter.tryToDatabase(fullRelationCode),
                CodeConverter.isInternalNamespace(fullRelationCode));
    }

    //
    // Public interface
    //

    /**
     * Load the samples defined by the criteria given to the constructor. The samples will be enriched with sample properties and dependencies to
     * parents and container will be resolved.
     */
    public List<Sample> load()
    {
        final StopWatch watch = new StopWatch();
        watch.start();
        loadGroups();
        loadSampleTypes();
        retrievePrimaryBasicSamples(tryGetIteratorForSamplesByIds());
        retrievePrimaryBasicSamples(tryGetIteratorForSamplesByCodes());
        retrievePrimaryBasicSamples(tryGetIteratorForContainerSamplesByCodes());
        retrievePrimaryBasicSamples(tryGetIteratorForSamplesByPermIds());
        retrievePrimaryBasicSamples(tryGetIteratorForContainerSamplesByPermIds());
        retrievePrimaryBasicSamples(tryGetIteratorForSpaceSamples());
        retrievePrimaryBasicSamples(tryGetIteratorForSharedSamples());
        retrievePrimaryBasicSamples(tryGetIteratorForExperimentSamples());
        retrievePrimaryBasicSamples(tryGetIteratorForContainedSamples());
        retrievePrimaryBasicSamples(tryGetIteratorForParentSamples());
        retrievePrimaryBasicSamples(tryGetIteratorForChildSamples());
        retrievePrimaryBasicSamples(tryGetIteratorForNewTrackedProcessedSamples());
        retrievePrimaryBasicSamples(tryGetIteratorForNewTrackedToBeProcessedSamples());
        retrievePrimaryBasicSamples(tryGetIteratorForMetaprojectSamples());
        if (operationLog.isDebugEnabled())
        {
            watch.stop();
            operationLog.debug(String.format("Basic retrieval of %d samples took %s s",
                    sampleList.size(), watch.toString()));
            watch.reset();
            watch.start();
        }
        if (enrichDependentSamples == false)
        {
            // only 'primary' samples were retrieved up to this point
            enrichRetrievedSamplesWithProperties(watch);
        }
        retrieveDependentSamplesRecursively(true);
        resolveParents();
        resolveContainers();
        if (enrichDependentSamples)
        {
            // dependent samples will also be enriched
            enrichRetrievedSamplesWithProperties(watch);
        }

        enrichWithMetaProjects();

        return sampleList;
    }

    private void enrichWithMetaProjects()
    {
        for (MetaProjectWithEntityId metaProject : query
                .getMetaprojects(sampleMap.keySet(), userId))
        {
            Metaproject mp = new Metaproject();
            mp.setId(metaProject.id);
            mp.setCreationDate(metaProject.creation_date);
            mp.setDescription(metaProject.description);
            mp.setIdentifier("/" + metaProject.owner_name + "/" + metaProject.name);
            mp.setName(metaProject.name);
            mp.setOwnerId(metaProject.owner_name);
            mp.setPrivate(metaProject.is_private);

            Sample sample = sampleMap.get(metaProject.entity_id);

            if (sample != null)
            {
                Collection<Metaproject> mps = sample.getMetaprojects();
                if (mps == null)
                {
                    mps = new HashSet<Metaproject>();
                    sample.setMetaprojects(mps);
                }
                mps.add(mp);
            }
        }

    }

    private static void log(StopWatch watch, String message)
    {
        if (operationLog.isDebugEnabled())
        {
            watch.stop();
            operationLog.debug(String.format("%s took %s s", message, watch.toString()));
            watch.reset();
            watch.start();
        }
    }

    //
    // Private worker methods
    //

    private void enrichRetrievedSamplesWithProperties(StopWatch watch)
    {
        if (samplePropertiesEnricherOrNull != null)
        {
            samplePropertiesEnricherOrNull.enrich(sampleMap.keySet(),
                    new IEntityPropertiesHolderResolver()
                        {
                            @Override
                            public Sample get(long id)
                            {
                                return sampleMap.get(id);
                            }
                        });
            log(watch, "Enrichment with properties");
        }
    }

    private void loadGroups()
    {
        // all groups are needed for parent samples identifiers
        final Space[] spaces = referencedEntityDAO.getAllSpaces();
        for (Space space : spaces)
        {
            spaceMap.put(space.getId(), space);
        }
    }

    private void loadSampleTypes()
    {
        final SampleType sampleTypeOrNull = tryGetSingleModeSampleType();
        this.singleSampleTypeMode = (sampleTypeOrNull != null);
        // all sample types are needed for parents
        for (SampleType type : query.getSampleTypes())
        {
            sampleTypes.put(type.getId(), type);
            if (singleSampleTypeMode == false)
            {
                maxSampleParentResolutionDepth =
                        Math.max(maxSampleParentResolutionDepth,
                                type.getGeneratedFromHierarchyDepth());
            }
        }
        sampleTypes.trim();

        if (singleSampleTypeMode)
        {
            this.maxSampleParentResolutionDepth = sampleTypeOrNull.getGeneratedFromHierarchyDepth();
        }

    }

    private Project createAndSaveProject(final long projectId)
    {
        final Project project = referencedEntityDAO.tryGetProject(projectId);
        projects.put(projectId, project);
        return project;
    }

    private SampleType tryGetSingleModeSampleType()
    {
        final SampleType sampleTypeOrNull = criteria.getSampleType();
        return (sampleTypeOrNull == null || sampleTypeOrNull.isAllTypesCode()) ? null
                : sampleTypeOrNull;
    }

    private long getSampleTypeId()
    {
        final SampleType sampleTypeOrNull = tryGetSingleModeSampleType();
        assert sampleTypeOrNull != null;
        return sampleTypeOrNull.getId();
    }

    private Iterable<SampleRecord> tryGetIteratorForSamplesByIds()
    {
        Collection<Long> ids = criteria.getSampleIds();
        if (ids == null)
        {
            return null;
        }
        return query.getSamples(new LongOpenHashSet(ids));
    }

    private Iterable<SampleRecord> tryGetIteratorForSamplesByCodes()
    {
        if (criteria.isSearchForContainerSamplesOnly())
        {
            return null;
        }
        String[] codes = criteria.trySampleCodes();
        if (codes == null)
        {
            return null;
        }
        return query.getSamplesForCodes(codes);
    }

    private Iterable<SampleRecord> tryGetIteratorForContainerSamplesByCodes()
    {
        if (false == criteria.isSearchForContainerSamplesOnly())
        {
            return null;
        }
        String[] codes = criteria.trySampleCodes();
        if (codes == null)
        {
            return null;
        }
        return query.getContainerSamplesForCodes(codes);
    }

    private Iterable<SampleRecord> tryGetIteratorForSamplesByPermIds()
    {
        if (criteria.isSearchForContainerSamplesOnly())
        {
            return null;
        }
        String[] permIds = criteria.trySamplePermIds();
        if (permIds == null)
        {
            return null;
        }
        return query.getSamplesForPermIds(permIds);
    }

    private Iterable<SampleRecord> tryGetIteratorForContainerSamplesByPermIds()
    {
        if (false == criteria.isSearchForContainerSamplesOnly())
        {
            return null;
        }
        String[] permIds = criteria.trySamplePermIds();
        if (permIds == null)
        {
            return null;
        }
        return query.getContainerSamplesForPermIds(permIds);
    }

    private Iterable<SampleRecord> tryGetIteratorForSpaceSamples()
    {
        if (criteria.isIncludeSpace() == false)
        {
            return null;
        }
        if (criteria.isExcludeWithoutExperiment())
        {
            if (singleSampleTypeMode)
            {
                return getSpaceSampleForSampleTypeWithExperiment();
            } else
            {
                return getSpaceSamplesWithExperiment();
            }
        } else
        {
            if (singleSampleTypeMode)
            {
                return getSpaceSamplesForSampleType();
            } else
            {
                return getSpaceSamples();
            }
        }
    }

    private DataIterator<SampleRecord> getSpaceSamples()
    {
        String groupCode = criteria.getSpaceCode();
        if (groupCode == null)
        {
            return query.getAllListableSpaceSamples();
        }
        return query.getListableSpaceSamples(groupCode);
    }

    private Iterable<SampleRecord> getSpaceSamplesForSampleType()
    {
        final long sampleTypeId = getSampleTypeId();
        String groupCode = criteria.getSpaceCode();
        if (groupCode == null)
        {
            return query.getAllSpaceSamplesForSampleType(sampleTypeId);
        }
        return query.getSpaceSamplesForSampleType(groupCode, sampleTypeId);
    }

    private DataIterator<SampleRecord> getSpaceSamplesWithExperiment()
    {
        String groupCode = criteria.getSpaceCode();
        if (groupCode == null)
        {
            return query.getAllSpaceSamplesWithExperiment();
        }
        return query.getSpaceSamplesWithExperiment(groupCode);
    }

    private DataIterator<SampleRecord> getSpaceSampleForSampleTypeWithExperiment()
    {
        final long sampleTypeId = getSampleTypeId();
        String groupCode = criteria.getSpaceCode();
        if (groupCode == null)
        {
            return query.getAllSpaceSamplesForSampleTypeWithExperiment(
                    sampleTypeId);
        }
        return query.getSpaceSamplesForSampleTypeWithExperiment(groupCode,
                sampleTypeId);
    }

    private Iterable<SampleRecord> tryGetIteratorForExperimentSamples()
    {
        final TechId experimentTechId = criteria.getExperimentId();
        if (experimentTechId == null)
        {
            return null;
        }
        if (criteria.isOnlyDirectlyConnected())
        {
            return query.getListableSamplesForExperiment(experimentTechId.getId());
        }
        return query.getListableSamplesAndDescendentsForExperiment(experimentTechId.getId());
    }

    private Iterable<SampleRecord> tryGetIteratorForMetaprojectSamples()
    {
        final Long metaprojectId = criteria.getMetaprojectId();
        if (metaprojectId == null)
        {
            return null;
        }
        return query.getSamplesForMetaproject(metaprojectId);
    }

    private Iterable<SampleRecord> tryGetIteratorForContainedSamples()
    {
        Collection<Long> containerTechId = criteria.getContainerSampleIds();
        if (containerTechId == null)
        {
            return null;
        }
        return query.getSamplesForContainer(new LongOpenHashSet(containerTechId));
    }

    private Iterable<SampleRecord> tryGetIteratorForChildSamples()
    {
        final TechId parentTechId = criteria.getParentSampleId();
        if (parentTechId == null)
        {
            return null;
        }
        return query.getChildrenSamplesForParent(parentRelationhipTypeId, parentTechId.getId());
    }

    private Iterable<SampleRecord> tryGetIteratorForParentSamples()
    {
        Collection<Long> ids = criteria.getChildrenSampleIds();
        if (ids.isEmpty())
        {
            return null;
        }
        return query.getParentSamplesForChildren(parentRelationhipTypeId, new LongOpenHashSet(ids));
    }

    private Iterable<SampleRecord> tryGetIteratorForSharedSamples()
    {
        if (criteria.isIncludeInstance() == false)
        {
            return null;
        }
        if (singleSampleTypeMode)
        {
            final long sampleTypeId = getSampleTypeId();
            return query.getSharedSamplesForSampleType(sampleTypeId);
        } else
        {
            return query.getListableSharedSamples();
        }
    }

    private Iterable<SampleRecord> tryGetIteratorForNewTrackedProcessedSamples()
    {
        if (criteria.getSampleTypeCode() == null)
        {
            return null;
        }
        final long sampleTypeId =
                referencedEntityDAO.getSampleTypeIdForSampleTypeCode(criteria.getSampleTypeCode());
        final String propertyTypeCode = criteria.getPropertyTypeCode();
        final String propertyValue = criteria.getPropertyValue();
        return query.getSamplesWithPropertyValue(sampleTypeId, propertyTypeCode, propertyValue,
                new LongOpenHashSet(criteria.getAlreadyTrackedSampleIds()));

    }

    private Iterable<SampleRecord> tryGetIteratorForNewTrackedToBeProcessedSamples()
    {
        if (criteria.getSampleTypeCode() == null)
        {
            return null;
        }
        final long sampleTypeId =
                referencedEntityDAO.getSampleTypeIdForSampleTypeCode(criteria.getSampleTypeCode());
        final String propertyTypeCode = criteria.getPropertyTypeCode();
        final String propertyValue = criteria.getPropertyValue();
        return query.getSamplesWithControlledVocabularyWithPropertyValue(sampleTypeId, propertyTypeCode, propertyValue,
                new LongOpenHashSet(criteria.getAlreadyTrackedSampleIds()));
    }

    private void retrievePrimaryBasicSamples(final Iterable<SampleRecord> sampleIteratorOrNull)
    {
        assert sampleList != null;
        retrieveBasicSamples(sampleIteratorOrNull, sampleList);
    }

    private void retrieveDependentBasicSamples(final Iterable<SampleRecord> sampleIteratorOrNull)
    {
        retrieveBasicSamples(sampleIteratorOrNull, null);
    }

    private void retrieveBasicSamples(final Iterable<SampleRecord> sampleIteratorOrNull,
            final List<Sample> sampleListOrNull)
    {
        if (sampleIteratorOrNull == null)
        {
            return;
        }
        final boolean primarySample = (sampleListOrNull != null);

        List<SampleRecord> records = new ArrayList<>();
        LongSet expIds = new LongOpenHashSet();
        for (SampleRecord row : sampleIteratorOrNull)
        {
            records.add(row);
            if (row.expe_id != null)
            {
                expIds.add(row.expe_id);
            }
        }
        Long2ObjectMap<Experiment> experimentsById = referencedEntityDAO.getExperiments(expIds);
        for (SampleRecord row : records)
        {
            final Sample sampleOrNull = tryCreateSample(row, experimentsById, primarySample);
            if (sampleOrNull != null) // null == different db instance
            {
                sampleMap.put(sampleOrNull.getId(), sampleOrNull);
                if (sampleListOrNull != null)
                {
                    sampleListOrNull.add(sampleOrNull);
                }
            }
        }
    }

    private Sample tryCreateSample(SampleRecord row, Long2ObjectMap<Experiment> experimentsById, 
            final boolean primarySample)
    {
        final Sample sample = new Sample();
        sample.setId(row.id);
        sample.setPermId(row.perm_id);
        sample.setVersion(row.version);
        sample.setCode(IdentifierHelper.convertCode(row.code, null));
        sample.setSubCode(IdentifierHelper.convertSubCode(row.code));
        sample.setSampleType(sampleTypes.get(row.saty_id));

        // set project, space or instance
        if (row.proj_id != null)
        {
            Project project = getOrCreateProject(row);
            setProject(sample, project);
            setSpace(sample, project.getSpace());
        } else if (row.space_id == null)
        {
            setDatabaseInstance(sample);
        } else
        {
            final Space spaceOrNull = spaceMap.get(row.space_id);
            if (spaceOrNull != null)
            {
                setSpace(sample, spaceMap.get(row.space_id));
            } else
            // different db instance
            {
                return null;
            }
        }
        enrichWithDeletion(sample, row); // this is cheap even for dependent samples
        // set properties needed for primary samples
        // (dependent samples too if they need to be enriched e.g. for entity tracking)
        if (primarySample || enrichDependentSamples)
        {
            // initializing property collection - without this enricher will not work properly
            sample.setProperties(new ArrayList<IEntityProperty>());
            sample.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL, EntityKind.SAMPLE,
                    row.perm_id));
            sample.setRegistrationDate(row.registration_timestamp);
            sample.setModificationDate(row.modification_timestamp);
            sample.setRegistrator(getOrCreateActor(row.pers_id_registerer));
            sample.setModifier(getOrCreateActor(row.pers_id_modifier));
            if (row.expe_id != null)
            {
                sample.setExperiment(experimentsById.get(row.expe_id));
            }
        }
        // prepare loading related samples
        if (maxSampleParentResolutionDepth > 0)
        {
            if (idsOfSamplesAwaitingParentResolution.contains(row.id) == false)
            {
                idsOfSamplesAwaitingParentResolution.add(row.id);
            }
        }
        // even though sample container resolution depth may be 0 we still need to load container
        // to create a 'full' code with container code part
        if (row.samp_id_part_of != null)
        {
            if (samplesAwaitingContainerResolution.containsKey(row.id) == false)
            {
                samplesAwaitingContainerResolution.put(row.id, new RelatedSampleRecord(sample,
                        row.samp_id_part_of));
            }
            addRelatedContainerSampleToRequested(row.samp_id_part_of);
        }

        return sample;
    }

    // NOTE: this just marks the sample as invalid without loading any details
    private void enrichWithDeletion(final Sample sample, SampleRecord row)
    {
        if (row.del_id != null)
        {
            final Deletion deletion = new Deletion();
            sample.setDeletion(deletion);
        }
    }

    private void setProject(final Sample sample, Project project)
    {
        sample.setProject(project);
        sample.setSpace(project.getSpace());
        sample.setIdentifier(IdentifierHelper.createSampleIdentifier(sample).toString());
    }

    private void setSpace(final Sample sample, final Space space)
    {
        sample.setSpace(space);
        // final DatabaseInstanceIdentifier dbId =
        // databaseInstance.isHomeDatabase() ? DatabaseInstanceIdentifier.HOME_INSTANCE
        // : new DatabaseInstanceIdentifier(databaseInstance.getCode());
        // final GroupIdentifier groupId = new GroupIdentifier(dbId, space.getCode());
        sample.setIdentifier(IdentifierHelper.createSampleIdentifier(sample).toString());
    }

    private void setDatabaseInstance(final Sample sample)
    {
        // sample.setDatabaseInstance(databaseInstance);
        // final DatabaseInstanceIdentifier dbId =
        // databaseInstance.isHomeDatabase() ? DatabaseInstanceIdentifier.HOME_INSTANCE
        // : new DatabaseInstanceIdentifier(databaseInstance.getCode());
        sample.setIdentifier(IdentifierHelper.createSampleIdentifier(sample).toString());
        // new SampleIdentifier(dbId, sample.getCode()).toString());
    }

    private void addRelatedContainerSampleToRequested(long containerId)
    {
        idsOfRequestedContainerSamples.add(containerId);
    }

    private void addRelatedParentSampleToRequested(long parentId)
    {
        idsOfRequestedParentSamples.add(parentId);
    }

    private Project getOrCreateProject(SampleRecord row)
    {
        Project project = projects.get(row.proj_id);
        if (project == null)
        {
            project = createAndSaveProject(row.proj_id);
        }
        return project;
    }

    private void retrieveDependentSamplesRecursively(boolean primary)
    {
        if (primary)
        {
            addParentsToRequested();
        }
        idsOfRequestedContainerSamples.removeAll(sampleMap.keySet());
        idsOfRequestedParentSamples.removeAll(sampleMap.keySet());
        if (idsOfRequestedContainerSamples.size() + idsOfRequestedParentSamples.size() == 0)
        {
            return;
        }
        retrieveDependentBasicSamples(query.getSamples(idsOfRequestedContainerSamples));
        retrieveDependentBasicSamples(query.getSamples(idsOfRequestedParentSamples));
        retrieveDependentSamplesRecursively(false);
    }

    private void addParentsToRequested()
    {
        Iterable<SampleRelationRecord> parentRelations =
                query.getParentRelations(parentRelationhipTypeId,
                        idsOfSamplesAwaitingParentResolution);
        for (SampleRelationRecord relation : parentRelations)
        {
            samplesAwaitingParentResolution.add(new RelatedSampleRecord(sampleMap
                    .get(relation.sample_id_child), relation.sample_id_parent));
            addRelatedParentSampleToRequested(relation.sample_id_parent);
        }
    }

    private void resolveParents()
    {
        for (RelatedSampleRecord record : samplesAwaitingParentResolution)
        {
            final Sample parent = sampleMap.get(record.relatedSampleId);
            record.sample.addParent(parent);
        }
    }

    private void resolveContainers()
    {
        for (Long2ObjectMap.Entry<RelatedSampleRecord> e : samplesAwaitingContainerResolution
                .long2ObjectEntrySet())
        {
            final RelatedSampleRecord record = e.getValue();
            final Sample container = sampleMap.get(record.relatedSampleId);
            record.sample.setContainer(container);
            record.sample.setCode(IdentifierHelper.convertCode(record.sample.getSubCode(),
                    container.getCode()));
            record.sample.setIdentifier(IdentifierHelper.createSampleIdentifier(record.sample)
                    .toString());
        }
    }

}
