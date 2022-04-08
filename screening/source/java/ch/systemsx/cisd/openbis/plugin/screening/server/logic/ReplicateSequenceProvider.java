/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.comparators.NullComparator;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.GroupByMap;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.IGroupKeyExtractor;

/**
 * Provides sequence numbers for technical and biological well replicates for a fixed material.
 * <p>
 * E.g. in HCS: all wells which contain the same siRNA are technical replicates. All wells which contain different siRNAs which suppresses the saeme
 * gene are biological replicates.
 * </p>
 * 
 * @author Tomasz Pylak
 */
class ReplicateSequenceProvider
{
    private final List<String> biologicalReplicatePropertyTypeCodesOrNull;

    private final Map<Double/* subgroup key */, Integer/* biological replicate sequence number */> biologicalReplicateSeqMap;

    private final Map<Long/* well id */, Integer/* technical replicate sequence number */> technicalReplicateSeqMap;

    public ReplicateSequenceProvider(List<? extends IEntityPropertiesHolder> replicaWells,
            List<String> biologicalReplicatePropertyTypeCodesOrNull)
    {
        this.biologicalReplicatePropertyTypeCodesOrNull =
                biologicalReplicatePropertyTypeCodesOrNull;
        this.biologicalReplicateSeqMap = new LinkedHashMap<Double, Integer>();
        this.technicalReplicateSeqMap = new LinkedHashMap<Long, Integer>();

        GroupByMap<Double, IEntityPropertiesHolder> biologicalReplicates =
                groupByBiologicalReplicate(replicaWells);
        int biologicalReplicateSeq = 1;
        // NOTE: by sorting the keys we ensure that the replicate sequence will be always the same
        // for a fixes set of replicas.
        for (Double biologicalReplicateKey : createSortedCopy(biologicalReplicates.getKeys()))
        {
            if (biologicalReplicateKey != null)
            {
                biologicalReplicateSeqMap.put(biologicalReplicateKey, biologicalReplicateSeq++);
            }

            int technicalReplicateSeq = 1;
            List<IEntityPropertiesHolder> technicalReplicates =
                    biologicalReplicates.getOrDie(biologicalReplicateKey);
            for (IEntityPropertiesHolder technicalReplicate : technicalReplicates)
            {
                technicalReplicateSeqMap.put(technicalReplicate.getId(), technicalReplicateSeq++);
            }
        }
    }

    /** @return sequences of biological replicas, all keys are not null. */
    public Collection<Integer> getBiologicalReplicateSequences()
    {
        return createSortedCopy(biologicalReplicateSeqMap.values());
    }

    private GroupByMap<Double, IEntityPropertiesHolder> groupByBiologicalReplicate(
            List<? extends IEntityPropertiesHolder> wells)
    {
        return GroupByMap.create(wells, new IGroupKeyExtractor<Double, IEntityPropertiesHolder>()
            {
                @Override
                public Double getKey(IEntityPropertiesHolder well)
                {
                    return tryFindSubgroup(well);
                }
            });
    }

    /** @return true if this well has information about which biological replicate it is. */
    public boolean isBiologicalReplicate(IEntityPropertiesHolder well)
    {
        return tryFindSubgroup(well) != null;
    }

    /** Subgroup sequence, the same for all technical replicates of one biological replicate. */
    public Integer tryGetBiologicalReplicateSequence(IEntityPropertiesHolder well)
    {
        Double subgroupKey = tryFindSubgroup(well);
        if (subgroupKey == null)
        {
            return null;
        }
        Integer seqNum = biologicalReplicateSeqMap.get(subgroupKey);
        assert seqNum != null : "no biological replicate found for " + subgroupKey;
        return seqNum;
    }

    /**
     * Technical Replicate Sequence (unique in one biological replicate)
     */
    public int getTechnicalReplicateSequence(IEntityPropertiesHolder well)
    {
        return technicalReplicateSeqMap.get(well.getId());
    }

    private static <T extends Comparable<T>> Collection<T> createSortedCopy(Collection<T> keys)
    {
        ArrayList<T> sortedKeys = new ArrayList<T>(keys);
        Collections.sort(sortedKeys, new NullComparator<T>(new Comparator<T>()
            {
                @Override
                public int compare(T o1, T o2)
                {
                    return o1.compareTo(o2);
                }
            }));
        return sortedKeys;
    }

    public String tryGetBiologicalReplicateLabel(IEntityPropertiesHolder well)
    {
        Integer biologicalReplicateSeq = tryGetBiologicalReplicateSequence(well);
        if (biologicalReplicateSeq == null)
        {
            return null;
        }
        IEntityProperty subgroupProperty = tryFindSubgroupProperty(well);
        assert subgroupProperty != null : "cannot fnd the subgroup property";
        boolean isMaterialProperty = (subgroupProperty.getMaterial() != null);

        String propertyLabel = subgroupProperty.getPropertyType().getLabel();
        if (isMaterialProperty)
        {
            return propertyLabel + " " + biologicalReplicateSeq;
        } else
        {
            return propertyLabel + " " + subgroupProperty.tryGetAsString();
        }
    }

    private Double tryFindSubgroup(IEntityPropertiesHolder well)
    {
        IEntityProperty subgroupProperty = tryFindSubgroupProperty(well);
        if (subgroupProperty == null)
        {
            return null;
        }
        return tryExtractSubgroupValue(subgroupProperty);
    }

    private IEntityProperty tryFindSubgroupProperty(IEntityPropertiesHolder well)
    {
        if (biologicalReplicatePropertyTypeCodesOrNull == null)
        {
            return null;
        }
        return tryFindProperty(well.getProperties(), biologicalReplicatePropertyTypeCodesOrNull);
    }

    private static Double tryExtractSubgroupValue(IEntityProperty subgroupProperty)
    {
        Material subgroupMaterial = subgroupProperty.getMaterial();
        if (subgroupMaterial != null)
        {
            return Double.valueOf(subgroupMaterial.getId());
        }
        try
        {
            return Double.valueOf(subgroupProperty.tryGetAsString());
        } catch (NumberFormatException ex)
        {
            return null;
        }
    }

    private static IEntityProperty tryFindProperty(List<IEntityProperty> properties,
            List<String> subgroupPropertyTypeCodes)
    {
        for (IEntityProperty property : properties)
        {
            if (subgroupPropertyTypeCodes.contains(property.getPropertyType().getCode()))
            {
                return property;
            }
        }
        return null;
    }

}