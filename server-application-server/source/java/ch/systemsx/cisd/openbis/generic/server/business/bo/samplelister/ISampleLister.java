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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.collection.IValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipSkeleton;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleSkeleton;

/**
 * A role for fast sample listing.
 * 
 * @author Bernd Rinn
 */
public interface ISampleLister
{

    /**
     * Returns a sorted list of {@link Sample}s that match given criteria.
     */
    public List<Sample> list(ListOrSearchSampleCriteria criteria);

    /**
     * Returns the id of the relationship type of specified code. If code starts with an '$' it is interpreted as an internally defined relationship
     * type.
     * 
     * @throws IllegalArgumentException if code not known.
     */
    public long getRelationshipTypeID(String code);

    /**
     * Returns all samples as skeletons (thats is, only primary and foreign keys) fulfilling specified criteria.
     */
    public List<SampleSkeleton> listSampleBy(IValidator<SampleSkeleton> criteria);

    /**
     * Returns all sample relation ships as skeletons (thats is, only primary and foreign keys) fulfilling specified criteria.
     * 
     * @deprecated This way of loading relationships is slow. There is no filtering on DB level. If the <code>criteria</code> use only a collection of
     *             parent/children ids than use getParentToChildrenIdsMap/getChildToParentsIdsMap.
     */
    @Deprecated
    public List<SampleRelationshipSkeleton> listSampleRelationshipsBy(
            IValidator<SampleRelationshipSkeleton> criteria);

    /** Returns a map from child id to set of parents ids for specified children. */
    public Map<Long, Set<Long>> getChildToParentsIdsMap(Collection<Long> childrenIds);

    /** Returns a map from parent id to set of children ids for specified parents. */
    public Map<Long, Set<Long>> getParentToChildrenIdsMap(Collection<Long> parentIds);

    /**
     * Returns the technical ids of all samples which have at least one property of type MATERIAL referring to one of the specified materials.
     */
    public Collection<TechId> listSamplesByMaterialProperties(Collection<TechId> materialIds);

}
