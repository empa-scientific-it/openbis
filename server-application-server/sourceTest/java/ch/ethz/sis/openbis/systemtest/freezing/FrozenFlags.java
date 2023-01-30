/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.freezing;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;

/**
 * @author Franz-Josef Elmer
 */
class FrozenFlags
{
    private static final int FROZEN_INDEX = 0;

    private static final int FROZEN_FOR_CHILDREN_INDEX = 1;

    private static final int FROZEN_FOR_PARENTS_INDEX = 2;

    private static final int FROZEN_FOR_COMPONENTS_INDEX = 3;

    private static final int FROZEN_FOR_CONTAINERS_INDEX = 4;

    private static final int FROZEN_FOR_DATA_SET_INDEX = 5;

    private static final int FROZEN_FOR_SAMPLE_INDEX = 6;

    private static final int FROZEN_FOR_EXPERMENT_INDEX = 7;

    private static final int FROZEN_FOR_PROJECT_INDEX = 8;

    private static final int NUMBER_OF_FLAGS = 9;

    private boolean[] flags = new boolean[NUMBER_OF_FLAGS];

    FrozenFlags(boolean frozen)
    {
        flags[FROZEN_INDEX] = frozen;
    }

    private FrozenFlags(boolean[] flags)
    {
        this.flags = flags;
    }

    public FrozenFlags clone()
    {
        return new FrozenFlags(flags.clone());
    }

    List<FrozenFlags> createAllCombinations()
    {
        List<Integer> indicesOfUsedFlags = new ArrayList<>();
        int numberOfCombinations = 1;
        for (int i = flags.length - 1; i >= 0; i--)
        {
            if (flags[i])
            {
                indicesOfUsedFlags.add(i);
                numberOfCombinations <<= 1;
            }
        }

        List<FrozenFlags> combinations = new ArrayList<>();
        for (int i = 0; i < numberOfCombinations; i++)
        {
            boolean[] flags = new boolean[NUMBER_OF_FLAGS];
            int index = 0;
            for (int b = i; b > 0; b >>= 1, index++)
            {
                if ((b & 1) != 0)
                {
                    flags[indicesOfUsedFlags.get(index)] = true;
                }
            }
            combinations.add(new FrozenFlags(flags));
        }
        return combinations;
    }

    FrozenFlags melt()
    {
        flags[FROZEN_INDEX] = false;
        return this;
    }

    boolean isFrozen()
    {
        return flags[FROZEN_INDEX];
    }

    FrozenFlags freezeForChildren()
    {
        flags[FROZEN_FOR_CHILDREN_INDEX] = true;
        return this;
    }

    FrozenFlags meltForChildren()
    {
        flags[FROZEN_FOR_CHILDREN_INDEX] = false;
        return this;
    }

    boolean isFrozenForChildren()
    {
        return flags[FROZEN_FOR_CHILDREN_INDEX];
    }

    FrozenFlags freezeForParents()
    {
        flags[FROZEN_FOR_PARENTS_INDEX] = true;
        return this;
    }

    FrozenFlags meltForParents()
    {
        flags[FROZEN_FOR_PARENTS_INDEX] = false;
        return this;
    }

    boolean isFrozenForParents()
    {
        return flags[FROZEN_FOR_PARENTS_INDEX];
    }

    FrozenFlags freezeForComponents()
    {
        flags[FROZEN_FOR_COMPONENTS_INDEX] = true;
        return this;
    }

    FrozenFlags meltForComponents()
    {
        flags[FROZEN_FOR_COMPONENTS_INDEX] = false;
        return this;
    }

    boolean isFrozenForComponents()
    {
        return flags[FROZEN_FOR_COMPONENTS_INDEX];
    }

    FrozenFlags freezeForContainers()
    {
        flags[FROZEN_FOR_CONTAINERS_INDEX] = true;
        return this;
    }

    FrozenFlags meltForContainers()
    {
        flags[FROZEN_FOR_CONTAINERS_INDEX] = false;
        return this;
    }

    boolean isFrozenForContainers()
    {
        return flags[FROZEN_FOR_CONTAINERS_INDEX];
    }

    FrozenFlags freezeForDataSet()
    {
        flags[FROZEN_FOR_DATA_SET_INDEX] = true;
        return this;
    }

    FrozenFlags meltForDataSet()
    {
        flags[FROZEN_FOR_DATA_SET_INDEX] = false;
        return this;
    }

    boolean isFrozenForDataSet()
    {
        return flags[FROZEN_FOR_DATA_SET_INDEX];
    }

    FrozenFlags freezeForSample()
    {
        flags[FROZEN_FOR_SAMPLE_INDEX] = true;
        return this;
    }

    FrozenFlags meltForSample()
    {
        flags[FROZEN_FOR_SAMPLE_INDEX] = false;
        return this;
    }

    boolean isFrozenForSample()
    {
        return flags[FROZEN_FOR_SAMPLE_INDEX];
    }

    FrozenFlags freezeForExperiment()
    {
        flags[FROZEN_FOR_EXPERMENT_INDEX] = true;
        return this;
    }

    FrozenFlags meltForExperiment()
    {
        flags[FROZEN_FOR_EXPERMENT_INDEX] = false;
        return this;
    }

    boolean isFrozenForExperiment()
    {
        return flags[FROZEN_FOR_EXPERMENT_INDEX];
    }

    FrozenFlags freezeForProject()
    {
        flags[FROZEN_FOR_PROJECT_INDEX] = true;
        return this;
    }

    FrozenFlags meltForProject()
    {
        flags[FROZEN_FOR_PROJECT_INDEX] = false;
        return this;
    }

    boolean isFrozenForProject()
    {
        return flags[FROZEN_FOR_PROJECT_INDEX];
    }

    @Override
    public String toString()
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        addForFlag(builder, isFrozenForChildren(), "children");
        addForFlag(builder, isFrozenForParents(), "parents");
        addForFlag(builder, isFrozenForComponents(), "components");
        addForFlag(builder, isFrozenForContainers(), "containers");
        addForFlag(builder, isFrozenForDataSet(), "data set");
        addForFlag(builder, isFrozenForSample(), "sample");
        addForFlag(builder, isFrozenForExperiment(), "experiment");
        addForFlag(builder, isFrozenForProject(), "project");
        String forList = builder.toString();
        if (forList.length() == 0)
        {
            return isFrozen() ? "frozen" : "liquid";
        }
        return (isFrozen() ? "frozen for " : "liquid but frozen for ") + forList;
    }

    private void addForFlag(CommaSeparatedListBuilder builder, boolean frozenFor, String name)
    {
        if (frozenFor)
        {
            builder.append(name);
        }
    }

}
