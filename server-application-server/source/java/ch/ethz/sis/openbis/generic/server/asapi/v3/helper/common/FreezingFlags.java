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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common;

import java.util.Set;
import java.util.TreeSet;

import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class FreezingFlags
{
    private final Set<String> freezingFlags = new TreeSet<>();
    
    public boolean noFlags()
    {
        return freezingFlags.isEmpty();
    }

    public FreezingFlags freeze()
    {
        freezingFlags.add("freeze");
        return this;
    }

    public FreezingFlags freezeForChildren()
    {
        freezingFlags.add("freezeForChildren");
        return this;
    }

    public FreezingFlags freezeForParents()
    {
        freezingFlags.add("freezeForParents");
        return this;
    }

    public FreezingFlags freezeForComponents()
    {
        freezingFlags.add("freezeForComponents");
        return this;
    }

    public FreezingFlags freezeForContainers()
    {
        freezingFlags.add("freezeForContainers");
        return this;
    }

    public FreezingFlags freezeForProjects()
    {
        freezingFlags.add("freezeForProjects");
        return this;
    }

    public FreezingFlags freezeForExperiments()
    {
        freezingFlags.add("freezeForExperiments");
        return this;
    }

    public FreezingFlags freezeForSamples()
    {
        freezingFlags.add("freezeForSamples");
        return this;
    }

    public FreezingFlags freezeForDataSets()
    {
        freezingFlags.add("freezeForDataSets");
        return this;
    }

    public String asJson()
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        for (String freezingFlag : freezingFlags)
        {
            builder.append("\"" + freezingFlag + "\"");
        }
        return "[" + builder.toString() + "]";
    }

    @Override
    public String toString()
    {
        return freezingFlags.toString();
    }

}
