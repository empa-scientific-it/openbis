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

package ch.systemsx.cisd.openbis.generic.shared.dto.builders;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Builder for instances of {@link DatasetDescription}.
 * 
 * @author Franz-Josef Elmer
 */
public class DatasetDescriptionBuilder
{
    private final DatasetDescription datasetDescription;

    public DatasetDescriptionBuilder(String code)
    {
        datasetDescription = new DatasetDescription();
        datasetDescription.setDataSetCode(code);
    }

    public DatasetDescriptionBuilder type(String dataSetTypeCode)
    {
        datasetDescription.setDatasetTypeCode(dataSetTypeCode);
        return this;
    }

    public DatasetDescriptionBuilder location(String location)
    {
        datasetDescription.setDataSetLocation(location);
        return this;
    }

    public DatasetDescriptionBuilder size(long size)
    {
        datasetDescription.setDataSetSize(size);
        return this;
    }

    public DatasetDescriptionBuilder sample(String sampleCode)
    {
        datasetDescription.setSampleCode(sampleCode);
        return this;
    }

    public DatasetDescriptionBuilder space(String spaceCode)
    {
        datasetDescription.setSpaceCode(spaceCode);
        return this;
    }

    public DatasetDescriptionBuilder project(String projectCode)
    {
        datasetDescription.setProjectCode(projectCode);
        return this;
    }

    public DatasetDescriptionBuilder experiment(String experimentCode)
    {
        datasetDescription.setExperimentCode(experimentCode);
        return this;
    }

    public DatasetDescription getDatasetDescription()
    {
        return datasetDescription;
    }
}
