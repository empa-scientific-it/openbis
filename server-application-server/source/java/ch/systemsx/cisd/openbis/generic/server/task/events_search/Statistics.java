/*
 * Copyright ETH 2021 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Statistics
{

    private int loadedSpaces;

    private int loadedProjects;

    private int loadedExperiments;

    private int loadedSamples;

    private int loadedEvents;

    private int createdEvents;

    public void increaseLoadedSpaces(int count)
    {
        loadedSpaces += count;
    }

    public void increaseLoadedProjects(int count)
    {
        loadedProjects += count;
    }

    public void increaseLoadedExperiments(int count)
    {
        loadedExperiments += count;
    }

    public void increaseLoadedSamples(int count)
    {
        loadedSamples += count;
    }

    public void increaseLoadedEvents(int count)
    {
        loadedEvents += count;
    }

    public void increaseCreatedEvents(int count)
    {
        createdEvents += count;
    }

    @Override
    public String toString()
    {
        return new ReflectionToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE).toString();
    }
}
