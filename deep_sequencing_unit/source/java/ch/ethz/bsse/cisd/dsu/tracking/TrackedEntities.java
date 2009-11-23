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

package ch.ethz.bsse.cisd.dsu.tracking;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Simple encapsulation of list of entities that are tracked.
 * 
 * @author Piotr Buczek
 */
public class TrackedEntities
{
    private final List<Sample> sequencingSamples;

    private final List<Sample> flowLaneSamples;

    private final List<ExternalData> dataSets;

    public TrackedEntities(List<Sample> sequencingSamples, List<Sample> flowLaneSamples,
            List<ExternalData> dataSets)
    {
        this.sequencingSamples = sequencingSamples;
        this.flowLaneSamples = flowLaneSamples;
        this.dataSets = dataSets;
    }

    public List<Sample> getSequencingSamples()
    {
        return sequencingSamples;
    }

    public List<Sample> getFlowLaneSamples()
    {
        return flowLaneSamples;
    }

    public List<ExternalData> getDataSets()
    {
        return dataSets;
    }
}
