/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.gui.model;

import java.util.List;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

/**
 * Data class bundling samples and data sets.
 *
 * @author Franz-Josef Elmer
 */
public class SamplesDataSets
{
    private final List<Sample> samples;

    private final List<DataSet> dataSets;

    public SamplesDataSets(List<Sample> samples, List<DataSet> dataSets)
    {
        this.samples = samples;
        this.dataSets = dataSets;
    }

    public List<Sample> getSamples()
    {
        return samples;
    }

    public List<DataSet> getDataSets()
    {
        return dataSets;
    }

}
