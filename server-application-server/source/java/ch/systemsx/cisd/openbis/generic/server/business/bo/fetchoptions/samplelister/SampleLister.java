/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.samplelister;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.EntityMetaprojectRelationRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaprojectCreator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaprojectRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.PropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author Franz-Josef Elmer
 */
public class SampleLister implements ISampleLister
{
    private static final Comparator<Sample> SAMPLE_COMPARATOR = new Comparator<Sample>()
    {
        @Override
        public int compare(Sample s1, Sample s2)
        {
            return s1.getIdentifier().compareTo(s2.getIdentifier());
        }
    };

    private static final Comparator<SampleRecord> SAMPLE_COMPARATOR2 =
            new Comparator<SampleRecord>()
            {
                @Override
                public int compare(SampleRecord s1, SampleRecord s2)
                {
                    return getIdentifier(s1).compareTo(getIdentifier(s2));
                }

                private String getIdentifier(SampleRecord sampleRecord)
                {
                    String spaceCode = sampleRecord.sp_code;
                    String sampleCode = sampleRecord.s_code;
                    return spaceCode == null ? "/" + sampleCode : "/" + spaceCode + "/"
                            + sampleCode;
                }
            };

    private static final IKeyExtractor<Long, SampleRecord> ID_EXTRACTOR =
            new IKeyExtractor<Long, SampleRecord>()
            {
                @Override
                public Long getKey(SampleRecord s)
                {
                    return s.s_id;
                }
            };

    private final ISampleListingQuery query;

    private Long relationID;

    private PersonPE person;

    public SampleLister(IDAOFactory daoFactory, PersonPE person)
    {
        this(QueryTool.getManagedQuery(ISampleListingQuery.class), person);
    }

    SampleLister(ISampleListingQuery query, PersonPE person)
    {
        this.query = query;
        this.person = person;
    }

    private Long getRelationShipType()
    {
        if (relationID == null)
        {
            String code =
                    CodeConverter.tryToDatabase(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
            boolean internalNamespace =
                    CodeConverter
                            .isInternalNamespace(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
            relationID = query.getRelationshipTypeId(code, internalNamespace);
        }
        return relationID;
    }

    @Override
    public List<Sample> getSamples(Collection<Long> sampleIDs,
            EnumSet<SampleFetchOption> fetchOptions, IValidator<IIdentifierHolder> filter)
    {
        if (sampleIDs.isEmpty())
        {
            return Collections.emptyList();
        }
        LongSet sampleIdSet = new LongOpenHashSet(sampleIDs);
        LongSet rootSampleIdSet = new LongOpenHashSet(sampleIDs);
        List<SampleRelationshipRecord> descendants = Collections.emptyList();
        if (fetchOptions.contains(SampleFetchOption.DESCENDANTS))
        {
            descendants = query.getDescendants(getRelationShipType(), rootSampleIdSet);
        } else if (fetchOptions.contains(SampleFetchOption.CHILDREN))
        {
            descendants = query.getChildren(getRelationShipType(), rootSampleIdSet);
        }
        LongSet descendentIdSet = new LongOpenHashSet();
        for (SampleRelationshipRecord relationShip : descendants)
        {
            sampleIdSet.add(relationShip.sample_id_child);
            descendentIdSet.add(relationShip.sample_id_child);
        }
        List<SampleRelationshipRecord> ancestors = Collections.emptyList();
        if (fetchOptions.contains(SampleFetchOption.ANCESTORS))
        {
            ancestors = query.getAncestors(getRelationShipType(), rootSampleIdSet);
        } else if (fetchOptions.contains(SampleFetchOption.PARENTS))
        {
            ancestors = query.getParents(getRelationShipType(), rootSampleIdSet);
        }
        LongSet ancestorIdSet = new LongOpenHashSet();
        for (SampleRelationshipRecord relationShip : ancestors)
        {
            sampleIdSet.add(relationShip.sample_id_parent);
            ancestorIdSet.add(relationShip.sample_id_parent);
        }
        TableMap<Long, SampleRecord> sampleRecords =
                getAllSamples(sampleIdSet, rootSampleIdSet, descendentIdSet, ancestorIdSet,
                        fetchOptions);
        if (fetchOptions.contains(SampleFetchOption.PROPERTIES))
        {
            enrichWithProperties(sampleRecords, sampleIdSet);
        }
        enrichWithDescendants(descendants, sampleRecords,
                fetchOptions.contains(SampleFetchOption.DESCENDANTS));
        enrichWithAncestors(ancestors, sampleRecords,
                fetchOptions.contains(SampleFetchOption.ANCESTORS));
        List<Sample> samples = new ArrayList<Sample>();
        if (fetchOptions.contains(SampleFetchOption.METAPROJECTS))
        {
            enrichWithMetaprojects(sampleRecords, sampleIdSet);
        }

        Map<Long, Sample> repository = new HashMap<Long, Sample>(); // Repository of all unlinked
                                                                    // samples
        for (Long rootSampleID : sampleIDs)
        {
            SampleRecord sampleRecord = sampleRecords.tryGet(rootSampleID);
            if (sampleRecord != null)
            {
                Sample sample = createSample(sampleRecord, repository, filter);
                if (filter.isValid(person, sample))
                {
                    samples.add(sample);
                }
            }
        }
        // "Tying the knot": Resolving all references between samples.
        for (Sample sample : repository.values())
        {
            if (sample.getRetrievedFetchOptions().contains(SampleFetchOption.CHILDREN))
            {
                sample.getChildren();
            }
            if (sample.getRetrievedFetchOptions().contains(SampleFetchOption.PARENTS))
            {
                sample.getParents();
            }
        }
        // Repository now longer needed
        repository.clear();
        Collections.sort(samples, SAMPLE_COMPARATOR);
        return samples;
    }

    private TableMap<Long, SampleRecord> getAllSamples(LongSet sampleIdSet,
            LongSet rootSampleIdSet, LongSet descendentIdSet, LongSet ancestorIdSet,
            EnumSet<SampleFetchOption> fetchOptions)
    {
        List<SampleRecord> list = query.listSamplesByIds(sampleIdSet);
        TableMap<Long, SampleRecord> sampleRecords =
                new TableMap<Long, SampleRecord>(list, ID_EXTRACTOR);
        for (SampleRecord sampleRecord : sampleRecords)
        {
            sampleRecord.fetchOptions = createAppropriateFetchOptions(fetchOptions);
            if (rootSampleIdSet.contains(sampleRecord.s_id))
            {
                if (fetchOptions.contains(SampleFetchOption.CHILDREN)
                        || fetchOptions.contains(SampleFetchOption.DESCENDANTS))
                {
                    sampleRecord.fetchOptions.add(SampleFetchOption.CHILDREN);
                }
                if (fetchOptions.contains(SampleFetchOption.PARENTS)
                        || fetchOptions.contains(SampleFetchOption.ANCESTORS))
                {
                    sampleRecord.fetchOptions.add(SampleFetchOption.PARENTS);
                }
            } else if (fetchOptions.contains(SampleFetchOption.ANCESTORS)
                    && ancestorIdSet.contains(sampleRecord.s_id))
            {
                sampleRecord.fetchOptions.add(SampleFetchOption.PARENTS);
            } else if (fetchOptions.contains(SampleFetchOption.DESCENDANTS)
                    && descendentIdSet.contains(sampleRecord.s_id))
            {
                sampleRecord.fetchOptions.add(SampleFetchOption.CHILDREN);
            }
        }
        return sampleRecords;
    }

    private void enrichWithAncestors(List<SampleRelationshipRecord> ancestors,
            TableMap<Long, SampleRecord> sampleRecords, boolean allAncestors)
    {
        for (SampleRelationshipRecord ancestor : ancestors)
        {
            SampleRecord parent = sampleRecords.tryGet(ancestor.sample_id_parent);
            SampleRecord child = sampleRecords.tryGet(ancestor.sample_id_child);
            if (parent != null && child != null)
            {
                if (child.parents == null)
                {
                    child.parents = new LinkedList<SampleRecord>();
                    if (allAncestors)
                    {
                        child.fetchOptions.add(SampleFetchOption.PARENTS);
                    }
                }
                child.parents.add(parent);
            }
        }
    }

    private void enrichWithDescendants(List<SampleRelationshipRecord> descendants,
            TableMap<Long, SampleRecord> sampleRecords, boolean allDescendants)
    {
        for (SampleRelationshipRecord descendant : descendants)
        {
            SampleRecord parent = sampleRecords.tryGet(descendant.sample_id_parent);
            SampleRecord child = sampleRecords.tryGet(descendant.sample_id_child);
            if (parent != null && child != null)
            {
                if (parent.children == null)
                {
                    parent.children = new LinkedList<SampleRecord>();
                    if (allDescendants)
                    {
                        parent.fetchOptions.add(SampleFetchOption.CHILDREN);
                    }
                }
                parent.children.add(child);
            }
        }
    }

    private void enrichWithProperties(TableMap<Long, SampleRecord> sampleRecords,
            LongSet sampleIdSet)
    {
        List<PropertyRecord> properties = query.getProperties(sampleIdSet);
        for (PropertyRecord propertyRecord : properties)
        {
            SampleRecord sampleRecord = sampleRecords.tryGet(propertyRecord.entity_id);
            if (sampleRecord != null)
            {
                if (sampleRecord.properties == null)
                {
                    sampleRecord.properties = new HashMap<String, String>();
                }
                String value = propertyRecord.getValue();
                if (sampleRecord.properties.containsKey(propertyRecord.code))
                {
                    String ss = sampleRecord.properties.get(propertyRecord.code);
                    value = ss + ", " + value;
                }
                sampleRecord.properties.put(propertyRecord.code, value);
            }
        }
    }

    private void enrichWithMetaprojects(TableMap<Long, SampleRecord> sampleRecords,
            LongSet sampleIdSet)
    {
        List<EntityMetaprojectRelationRecord> sampleMetaprojectRelations =
                query.getMetaprojectAssignments(sampleIdSet, person.getId());
        LongSet metaprojectIdSet = new LongOpenHashSet();
        for (EntityMetaprojectRelationRecord record : sampleMetaprojectRelations)
        {
            metaprojectIdSet.add(record.metaproject_id);
        }

        List<MetaprojectRecord> metaprojects = query.getMetaprojects(metaprojectIdSet);
        TableMap<Long, MetaprojectRecord> metaprojectRecords =
                new TableMap<Long, MetaprojectRecord>(metaprojects,
                        new IKeyExtractor<Long, MetaprojectRecord>()
                        {
                            @Override
                            public Long getKey(MetaprojectRecord mr)
                            {
                                return mr.id;
                            }
                        });

        for (EntityMetaprojectRelationRecord record : sampleMetaprojectRelations)
        {
            SampleRecord sampleRecord = sampleRecords.tryGet(record.entity_id);
            if (sampleRecord != null)
            {
                if (sampleRecord.metaprojects == null)
                {
                    sampleRecord.metaprojects = new ArrayList<MetaprojectRecord>();
                }
                MetaprojectRecord metaprojectRecord =
                        metaprojectRecords.tryGet(record.metaproject_id);
                if (metaprojectRecord != null)
                {
                    sampleRecord.metaprojects.add(metaprojectRecord);
                }
            }
        }
    }

    private Sample createSample(SampleRecord sampleRecord, Map<Long, Sample> repository,
            IValidator<IIdentifierHolder> filter)
    {
        Sample sample = repository.get(sampleRecord.s_id);
        if (sample != null)
        {
            return sample;
        }
        Sample.SampleInitializer initializer = new Sample.SampleInitializer();
        initializer.setId(sampleRecord.s_id);
        initializer.setStub(false);
        String spaceCode = sampleRecord.sp_code;
        initializer.setSpaceCode(spaceCode);
        String sampleCode = sampleRecord.s_code;
        String containerCode = sampleRecord.container_code;
        if (containerCode != null)
        {
            sampleCode = containerCode + ":" + sampleCode;
        }
        initializer.setCode(sampleCode);
        if (sampleRecord.samp_proj_code != null)
        {
            initializer.setIdentifier(
                    "/" + spaceCode + "/" + sampleRecord.samp_proj_code + "/" + sampleCode);
        } else
        {
            initializer.setIdentifier(spaceCode == null ? "/" + sampleCode : "/" + spaceCode + "/"
                    + sampleCode);
        }
        initializer.setPermId(sampleRecord.s_perm_id);
        EntityRegistrationDetailsInitializer detailsInitializer =
                new EntityRegistrationDetailsInitializer();
        detailsInitializer.setRegistrationDate(sampleRecord.s_registration_timestamp);
        detailsInitializer.setModificationDate(sampleRecord.s_modification_timestamp);
        detailsInitializer.setUserId(sampleRecord.pe_user_id);
        detailsInitializer.setEmail(sampleRecord.pe_email);
        detailsInitializer.setFirstName(sampleRecord.pe_first_name);
        detailsInitializer.setLastName(sampleRecord.pe_last_name);
        detailsInitializer.setModifierUserId(sampleRecord.mod_user_id);
        detailsInitializer.setModifierEmail(sampleRecord.mod_email);
        detailsInitializer.setModifierFirstName(sampleRecord.mod_first_name);
        detailsInitializer.setModifierLastName(sampleRecord.mod_last_name);
        initializer.setRegistrationDetails(new EntityRegistrationDetails(detailsInitializer));
        initializer.setSampleTypeId(sampleRecord.st_id);
        initializer.setSampleTypeCode(sampleRecord.st_code);
        if (sampleRecord.exp_code != null)
        {
            initializer.setExperimentIdentifierOrNull("/" + sampleRecord.proj_space_code + "/"
                    + sampleRecord.proj_code + "/" + sampleRecord.exp_code);
        }
        Map<String, String> properties = sampleRecord.properties;
        if (properties != null)
        {
            for (Entry<String, String> entry : properties.entrySet())
            {
                initializer.putProperty(entry.getKey(), entry.getValue());
            }
        }
        if (sampleRecord.metaprojects != null)
        {
            for (MetaprojectRecord metaprojectRecord : sampleRecord.metaprojects)
            {
                initializer.addMetaproject(MetaprojectCreator.createMetaproject(metaprojectRecord,
                        person));
            }
        }
        initializer.setRetrievedFetchOptions(sampleRecord.fetchOptions);
        List<SampleRecord> linkedSampleRecords = new ArrayList<SampleRecord>();
        if (sampleRecord.children != null)
        {
            initializer.setChildReferences(getIDs(sampleRecord.children), repository);
            linkedSampleRecords.addAll(sampleRecord.children);
        }
        if (sampleRecord.parents != null)
        {
            initializer.setParentReferences(getIDs(sampleRecord.parents), repository);
            linkedSampleRecords.addAll(sampleRecord.parents);
        }
        sample = new Sample(initializer);
        if (filter.isValid(person, sample))
        {
            repository.put(sampleRecord.s_id, sample);
        }
        // Linked samples have to be created after the current sample has been added to the
        // repository, otherwise a stack overflow might occur because of a recursive endless loop.
        for (SampleRecord linkedSampleRecord : linkedSampleRecords)
        {
            createSample(linkedSampleRecord, repository, filter);
        }
        return sample;
    }

    private List<Long> getIDs(List<SampleRecord> records)
    {
        Collections.sort(records, SAMPLE_COMPARATOR2);
        List<Long> ids = new ArrayList<Long>();
        for (SampleRecord record : records)
        {
            ids.add(record.s_id);
        }
        return ids;
    }

    private EnumSet<SampleFetchOption> createAppropriateFetchOptions(
            EnumSet<SampleFetchOption> fetchOptions)
    {
        EnumSet<SampleFetchOption> result =
                EnumSet.of(fetchOptions.contains(SampleFetchOption.PROPERTIES) ?
                        SampleFetchOption.PROPERTIES
                        :
                        SampleFetchOption.BASIC);
        if (fetchOptions.contains(SampleFetchOption.METAPROJECTS))
        {
            result.add(SampleFetchOption.METAPROJECTS);
        }
        return result;
    }

}
