/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

/**
 * Usage information for a user and a group.
 */
class UsageInfo
{
    private int numberOfNewExperiments;

    private int numberOfNewSamples;

    private int numberOfNewDataSets;

    void addNewExperiment()
    {
        numberOfNewExperiments++;
    }

    public int getNumberOfNewExperiments()
    {
        return numberOfNewExperiments;
    }

    void addNewSample()
    {
        numberOfNewSamples++;
    }

    public int getNumberOfNewSamples()
    {
        return numberOfNewSamples;
    }

    void addNewDataSet()
    {
        numberOfNewDataSets++;
    }

    public int getNumberOfNewDataSets()
    {
        return numberOfNewDataSets;
    }

    public boolean isIdle()
    {
        return numberOfNewExperiments == 0 && numberOfNewSamples == 0 && numberOfNewDataSets == 0;
    }

}