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

package ch.systemsx.cisd.openbis.dss.client.api.gui.model;

import java.util.List;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

/**
 * Methods to sort public API objects by identifier/code.
 * 
 * @author Kaloyan Enimanev
 */
public class UploadClientSortingUtils
{
    public static void sortSamplesByIdentifier(List<Sample> samples)
    {
        CollectionUtils.sort(samples, new IKeyExtractor<String, Sample>()
            {
                @Override
                public String getKey(Sample sample)
                {
                    return sample.getIdentifier();
                }
            });
    }

    public static void sortExperimentsByIdentifier(List<Experiment> samples)
    {
        CollectionUtils.sort(samples, new IKeyExtractor<String, Experiment>()
            {
                @Override
                public String getKey(Experiment experiment)
                {
                    return experiment.getIdentifier();
                }
            });
    }

    public static void sortDataSetsByCode(List<DataSet> dataSets)
    {
        CollectionUtils.sort(dataSets, new IKeyExtractor<String, DataSet>()
            {
                @Override
                public String getKey(DataSet dataSet)
                {
                    return dataSet.getCode();
                }
            });
    }

}
