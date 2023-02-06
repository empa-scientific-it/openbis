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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.filter;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;

/**
 * Filter which will be passed only by data sets with a data set type code matching a certain regular expression.
 * 
 * @author Franz-Josef Elmer
 */
public class TypeBasedDataSetFilter implements IDataSetFilter
{
    private final String datasetTypeCodePattern;

    /**
     * Creates an instance for the specified regular expression.
     */
    public TypeBasedDataSetFilter(String datasetTypeCodePattern)
    {
        this.datasetTypeCodePattern = datasetTypeCodePattern;
    }

    /**
     * Return <code>true</code> if the data set type code of the specified data set matches the regular expression provided as constructor argument.
     */
    @Override
    public boolean pass(DataSet dataSet)
    {
        return dataSet.getDataSetTypeCode().matches(datasetTypeCodePattern);
    }

    @Override
    public String toString()
    {
        return "Type:" + datasetTypeCodePattern;
    }
}
