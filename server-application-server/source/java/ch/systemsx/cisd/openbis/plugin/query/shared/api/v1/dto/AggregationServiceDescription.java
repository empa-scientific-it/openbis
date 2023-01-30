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

package ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Bean with information about aggregation services that provide data.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@JsonObject("AggregationServiceDescription")
public class AggregationServiceDescription implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String dataStoreCode;

    private String dataStoreBaseUrl;

    private String serviceKey;

    private String label;

    private AggregationServiceType type;

    /**
     * The code of the data store that provides this service. Non-null.
     */
    public String getDataStoreCode()
    {
        return dataStoreCode;
    }

    /**
     * Returns the base URL of the data store that provides this service. Non-null.
     * 
     * @since 1.4
     */
    public String getDataStoreBaseUrl()
    {
        return dataStoreBaseUrl;
    }

    /**
     * The key that identifies this particular service. Non-null.
     */
    public String getServiceKey()
    {
        return serviceKey;
    }

    /**
     * Returns the label of the service.
     * 
     * @since 1.5
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * The type of the service. Non-null.
     */
    public AggregationServiceType getType()
    {
        return type;
    }

    // The setters should just be used internally

    public void setDataStoreCode(String dataStoreCode)
    {
        this.dataStoreCode = dataStoreCode;
    }

    public void setDataStoreBaseUrl(String dataStoreBaseUrl)
    {
        this.dataStoreBaseUrl = dataStoreBaseUrl;
    }

    public void setServiceKey(String key)
    {
        this.serviceKey = key;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public void setType(AggregationServiceType type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "AggregationServiceDescription [dataStoreCode=" + dataStoreCode + ", serviceKey="
                + serviceKey + ", label=" + label + ", type=" + type + "]";
    }
}
