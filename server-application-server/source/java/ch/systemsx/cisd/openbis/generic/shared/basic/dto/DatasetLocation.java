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

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;

/**
 * Encapsulates location and code of a data set in a store.
 * 
 * @author Piotr Buczek
 */
public class DatasetLocation implements IDatasetLocation, Serializable, ICodeHolder
{
    private static final long serialVersionUID = 1L;

    private String datasetCode;

    private String dataSetLocation;

    private String dataStoreCode;

    private String dataStoreUrl;

    private Integer orderInContainer;

    public DatasetLocation()
    {
    }

    public DatasetLocation(String datasetCode, String dataSetLocation, String dataStoreCode,
            String dataStoreUrl)
    {
        this(datasetCode, dataSetLocation, dataStoreCode, dataStoreUrl, null);
    }

    public DatasetLocation(String datasetCode, String dataSetLocation, String dataStoreCode,
            String dataStoreUrl, Integer orderInContainer)
    {
        this.datasetCode = datasetCode;
        this.dataSetLocation = dataSetLocation;
        this.dataStoreCode = dataStoreCode;
        this.dataStoreUrl = dataStoreUrl;
        this.orderInContainer = orderInContainer;
    }

    @Override
    public Integer getOrderInContainer(String containerDataSetCode)
    {
        return orderInContainer;
    }

    public void setOrderInContainer(Integer orderInContainer)
    {
        this.orderInContainer = orderInContainer;
    }

    @Override
    public String getDataSetLocation()
    {
        return dataSetLocation;
    }

    public void setDataSetLocation(String dataSetLocation)
    {
        this.dataSetLocation = dataSetLocation;
    }

    @Override
    public String getDataSetCode()
    {
        return datasetCode;
    }

    public void setDatasetCode(String datasetCode)
    {
        this.datasetCode = datasetCode;
    }

    @Override
    public String toString()
    {
        return "Dataset[" + datasetCode + "], location[" + dataSetLocation + "]";
    }

    @Override
    public String getCode()
    {
        return getDataSetCode();
    }

    @Override
    public String getDataStoreUrl()
    {
        return this.dataStoreUrl;
    }

    @Override
    public String getDataStoreCode()
    {
        return this.dataStoreCode;
    }

    public void setDataStoreCode(String dataStoreCode)
    {
        this.dataStoreCode = dataStoreCode;
    }

    public void setDataStoreUrl(String dataStoreUrl)
    {
        this.dataStoreUrl = dataStoreUrl;
    }

    @Override
    public Long getDataSetSize()
    {
        return null;
    }

}
