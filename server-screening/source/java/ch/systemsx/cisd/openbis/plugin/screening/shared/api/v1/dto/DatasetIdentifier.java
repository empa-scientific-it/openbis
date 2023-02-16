/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Contains data which uniquely define a dataset.
 * 
 * @author Tomasz Pylak
 */

@SuppressWarnings("unused")
@JsonObject("DatasetIdentifier")
public class DatasetIdentifier implements Serializable, IDatasetIdentifier
{
    private static final long serialVersionUID = 1L;

    private String datasetCode;

    // a.k.a. downloadURL
    private String datastoreServerUrl;

    public DatasetIdentifier(String datasetCode, String datastoreServerUrl)
    {
        this.datasetCode = datasetCode;
        this.datastoreServerUrl = datastoreServerUrl;
    }

    /**
     * The code of this dataset.
     */
    @Override
    public String getDatasetCode()
    {
        return datasetCode;
    }

    @Override
    public String getPermId()
    {
        return datasetCode;
    }

    @Override
    public String getDatastoreServerUrl()
    {
        return datastoreServerUrl;
    }

    @Override
    public String toString()
    {
        return datasetCode;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || obj instanceof DatasetIdentifier == false)
        {
            return false;
        }
        DatasetIdentifier that = (DatasetIdentifier) obj;
        return datasetCode.equals(that.datasetCode);
    }

    @Override
    public int hashCode()
    {
        return datasetCode.hashCode();
    }

    //
    // JSON-RPC
    //

    private DatasetIdentifier()
    {
    }

    private void setDatasetCode(String datasetCode)
    {
        this.datasetCode = datasetCode;
    }

    private void setPermId(String permId)
    {
        this.datasetCode = permId;
    }

    private void setDatastoreServerUrl(String datastoreServerUrl)
    {
        this.datastoreServerUrl = datastoreServerUrl;
    }

}