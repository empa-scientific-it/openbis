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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.List;

/**
 * Contains a list of data sets and their type.
 * 
 * @author Izabela Adamczyk
 */
public class NewDataSetsWithTypes
{
    DataSetType dataSetType;

    List<NewDataSet> dataSets;

    public NewDataSetsWithTypes()
    {
    }

    public NewDataSetsWithTypes(DataSetType dataSetType, List<NewDataSet> dataSets)
    {
        setDataSetType(dataSetType);
        setNewDataSets(dataSets);
    }

    public DataSetType getDataSetType()
    {
        return dataSetType;
    }

    public void setDataSetType(DataSetType dataSetType)
    {
        this.dataSetType = dataSetType;
    }

    public List<NewDataSet> getNewDataSets()
    {
        return dataSets;
    }

    public void setNewDataSets(List<NewDataSet> newSamples)
    {
        this.dataSets = newSamples;
    }
}
