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

package ch.systemsx.cisd.openbis.generic.shared.dto;

/**
 * Describes one dataset which should be processed by the plugin task.
 * 
 * @author Tomasz Pylak
 */
public class DatasetDescription
{
    private final String datasetCode;

    private final String dataSetLocation;

    public DatasetDescription(String datasetCode, String dataSetLocation)
    {
        this.dataSetLocation = dataSetLocation;
        this.datasetCode = datasetCode;
    }

    public String getDataSetLocation()
    {
        return dataSetLocation;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    @Override
    public String toString()
    {
        return String.format("Dataset '%s'", datasetCode);
    }

}
