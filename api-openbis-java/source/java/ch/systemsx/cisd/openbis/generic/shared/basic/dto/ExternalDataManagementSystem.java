package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.util.JsonPropertyUtil;

/**
 * @author Pawel Glyzewski
 */
@SuppressWarnings("unused")
@JsonObject("ExternalDataManagementSystem")
public class ExternalDataManagementSystem extends Code<ExternalDataManagementSystem> implements
        Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    private DatabaseInstance databaseInstance;

    private String label;

    private String urlTemplate;

    private String address;

    private boolean openBIS;

    private ExternalDataManagementSystemType addressType;

    @JsonIgnore
    public Long getId()
    {
        return id;
    }

    @JsonIgnore
    public void setId(Long id)
    {
        this.id = id;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getUrlTemplate()
    {
        return urlTemplate;
    }

    public void setUrlTemplate(String urlTemplate)
    {
        this.urlTemplate = urlTemplate;
    }

    public boolean isOpenBIS()
    {
        return openBIS;
    }

    public void setOpenBIS(boolean openBIS)
    {
        this.openBIS = openBIS;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public ExternalDataManagementSystemType getAddressType()
    {
        return addressType;
    }

    public void setAddressType(ExternalDataManagementSystemType addressType)
    {
        this.addressType = addressType;
    }

    @JsonProperty("id")
    private String getIdAsString()
    {
        return JsonPropertyUtil.toStringOrNull(id);
    }

    private void setIdAsString(String id)
    {
        this.id = JsonPropertyUtil.toLongOrNull(id);
    }

}
