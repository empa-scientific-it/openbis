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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lemnik.eodsql.DataIterator;
import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.collection.IValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipSkeleton;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleSkeleton;

/**
 * A business object for providing lists of samples (more precisely sets of samples) for the purpose of showing them and browsing through them. It is
 * optimized for speed, using a custom strategy to get the samples from the database.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
{ ISampleListingQuery.class, SampleRecord.class, SampleRelationRecord.class })
public class SampleLister implements ISampleLister
{
    private final SampleListerDAO dao;

    private final SecondaryEntityDAO referencedEntityDAO;

    private final String baseIndexURL;

    private final Long userId;

    public static SampleLister create(IDAOFactory daoFactory, String baseIndexURL, Long userId)
    {
        SampleListerDAO sampleListerDAO = SampleListerDAO.create(daoFactory);
        SecondaryEntityDAO referencedEntityDAO = SecondaryEntityDAO.create(daoFactory);
        return new SampleLister(baseIndexURL, sampleListerDAO, referencedEntityDAO, userId);
    }

    private SampleLister(String baseIndexURL, SampleListerDAO dao,
            SecondaryEntityDAO referencedEntityDAO, Long userId)
    {
        this.baseIndexURL = baseIndexURL;
        this.dao = dao;
        this.referencedEntityDAO = referencedEntityDAO;
        this.userId = userId;
    }

    @Override
    public List<Sample> list(final ListOrSearchSampleCriteria criteria)
    {
        return SampleListingWorker.create(criteria, baseIndexURL, dao, referencedEntityDAO, userId)
                .load();
    }

    @Override
    public long getRelationshipTypeID(String code)
    {
        return SampleListingWorker.getRelationId(dao.getQuery(), code);
    }

    @Override
    public List<SampleSkeleton> listSampleBy(IValidator<SampleSkeleton> criteria)
    {
        DataIterator<SampleRecord> sampleSkeletons = dao.getQuery().getSampleSkeletons();
        List<SampleSkeleton> result = new ArrayList<SampleSkeleton>();
        for (SampleRecord sampleRecord : sampleSkeletons)
        {
            SampleSkeleton sampleSkeleton = new SampleSkeleton();
            sampleSkeleton.setId(sampleRecord.id);
            sampleSkeleton.setExperimentID(sampleRecord.expe_id);
            sampleSkeleton.setSpaceID(sampleRecord.space_id);
            sampleSkeleton.setTypeID(sampleRecord.saty_id);
            if (criteria.isValid(sampleSkeleton))
            {
                result.add(sampleSkeleton);
            }
        }
        return result;
    }

    @Override
    public List<SampleRelationshipSkeleton> listSampleRelationshipsBy(
            IValidator<SampleRelationshipSkeleton> criteria)
    {
        DataIterator<SampleRelationRecord> records =
                dao.getQuery().getSampleRelationshipSkeletons();
        List<SampleRelationshipSkeleton> result = new ArrayList<SampleRelationshipSkeleton>();
        for (SampleRelationRecord record : records)
        {
            SampleRelationshipSkeleton skeleton = new SampleRelationshipSkeleton();
            skeleton.setRelationshipTypeID(record.relationship_id);
            skeleton.setParentSampleID(record.sample_id_parent);
            skeleton.setChildSampleID(record.sample_id_child);
            if (criteria.isValid(skeleton))
            {
                result.add(skeleton);
            }
        }
        return result;
    }

    @Override
    public Map<Long, Set<Long>> getChildToParentsIdsMap(Collection<Long> childrenIds)
    {
        LongOpenHashSet ids = new LongOpenHashSet();
        for (Long id : childrenIds)
        {
            ids.add(id);
        }
        final long relationshipTypeID =
                getRelationshipTypeID(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
        DataIterator<SampleRelationRecord> relationships =
                dao.getQuery().getParentRelations(relationshipTypeID, ids);
        Map<Long, Set<Long>> map = new LinkedHashMap<Long, Set<Long>>();
        for (SampleRelationRecord relationship : relationships)
        {
            Set<Long> parents = map.get(relationship.sample_id_child);
            if (parents == null)
            {
                parents = new LinkedHashSet<Long>();
                map.put(relationship.sample_id_child, parents);
            }
            parents.add(relationship.sample_id_parent);
        }
        return map;
    }

    @Override
    public Map<Long, Set<Long>> getParentToChildrenIdsMap(Collection<Long> parentIds)
    {
        LongOpenHashSet ids = new LongOpenHashSet();
        for (Long id : parentIds)
        {
            ids.add(id);
        }
        final long relationshipTypeID =
                getRelationshipTypeID(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
        DataIterator<SampleRelationRecord> relationships =
                dao.getQuery().getChildrenRelations(relationshipTypeID, ids);
        Map<Long, Set<Long>> map = new LinkedHashMap<Long, Set<Long>>();
        for (SampleRelationRecord relationship : relationships)
        {
            Set<Long> children = map.get(relationship.sample_id_parent);
            if (children == null)
            {
                children = new LinkedHashSet<Long>();
                map.put(relationship.sample_id_parent, children);
            }
            children.add(relationship.sample_id_child);
        }
        return map;
    }

    @Override
    public Collection<TechId> listSamplesByMaterialProperties(Collection<TechId> materialIds)
    {
        ISampleListingQuery query = dao.getQuery();
        LongOpenHashSet ids = new LongOpenHashSet(TechId.asLongs(materialIds));
        DataIterator<Long> result = query.getSampleIdsByMaterialProperties(ids);
        Set<TechId> sampleIds = new HashSet<TechId>();
        for (Long sampleId : result)
        {
            sampleIds.add(new TechId(sampleId));
        }
        return sampleIds;
    }

    public Set<Long> listChildrenIdsSet(Collection<Long> parentIds)
    {
        LongOpenHashSet ids = new LongOpenHashSet();
        for (Long id : parentIds)
        {
            ids.add(id);
        }
        final long relationshipTypeID =
                getRelationshipTypeID(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
        DataIterator<Long> resultIterator = dao.getQuery().getChildrenIds(relationshipTypeID, ids);
        return new LongOpenHashSet(resultIterator);
    }

}
