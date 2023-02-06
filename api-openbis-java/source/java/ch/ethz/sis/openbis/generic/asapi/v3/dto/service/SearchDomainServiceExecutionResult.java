/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.service;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.DssServicePermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.service.SearchDomainServiceExecutionResult")
public class SearchDomainServiceExecutionResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DssServicePermId servicePermId;

    @JsonProperty
    private String searchDomainName;

    @JsonProperty
    private String searchDomainLabel;

    @JsonProperty
    private EntityKind entityKind;

    @JsonProperty
    private String entityType;

    @JsonProperty
    private String entityIdentifier;

    @JsonProperty
    private String entityPermId;

    @JsonProperty
    private Map<String, String> resultDetails;

    @JsonIgnore
    public DssServicePermId getServicePermId()
    {
        return servicePermId;
    }

    public void setServicePermId(DssServicePermId servicePermId)
    {
        this.servicePermId = servicePermId;
    }

    @JsonIgnore
    public String getSearchDomainName()
    {
        return searchDomainName;
    }

    public void setSearchDomainName(String searchDomainName)
    {
        this.searchDomainName = searchDomainName;
    }

    @JsonIgnore
    public String getSearchDomainLabel()
    {
        return searchDomainLabel;
    }

    public void setSearchDomainLabel(String label)
    {
        this.searchDomainLabel = label;
    }

    @JsonIgnore
    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public void setEntityKind(EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    @JsonIgnore
    public String getEntityType()
    {
        return entityType;
    }

    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
    }

    @JsonIgnore
    public String getEntityIdentifier()
    {
        return entityIdentifier;
    }

    public void setEntityIdentifier(String entityIdentifier)
    {
        this.entityIdentifier = entityIdentifier;
    }

    @JsonIgnore
    public String getEntityPermId()
    {
        return entityPermId;
    }

    public void setEntityPermId(String entityPermId)
    {
        this.entityPermId = entityPermId;
    }

    @JsonIgnore
    public Map<String, String> getResultDetails()
    {
        return resultDetails;
    }

    public void setResultDetails(Map<String, String> resultDetails)
    {
        this.resultDetails = resultDetails;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("entityKind", entityKind).append("entityPermId", entityPermId).toString();
    }

}
