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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Bean with information about aggregation services that provide data.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AggregationServiceDescription implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String dataStoreCode;

    private String moduleKey;

    private String serviceKey;

    private AggregationServiceType type;

    /**
     * The code of the data store the provides this service. Non-null.
     */
    public String getDataStoreCode()
    {
        return dataStoreCode;
    }

    /**
     * The module that implements this service. Non-null.
     */
    public String getModuleKey()
    {
        return moduleKey;
    }

    /**
     * The key that identifies this particular service within a module. Non-null.
     */
    public String getServiceKey()
    {
        return serviceKey;
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

    public void setModuleKey(String module)
    {
        this.moduleKey = module;
    }

    public void setServiceKey(String key)
    {
        this.serviceKey = key;
    }

    public void setType(AggregationServiceType type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(dataStoreCode);
        builder.append(moduleKey);
        builder.append(serviceKey);
        return builder.toString();
    }

}
