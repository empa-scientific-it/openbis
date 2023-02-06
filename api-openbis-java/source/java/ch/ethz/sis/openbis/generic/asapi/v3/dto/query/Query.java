/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.query;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDescriptionHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.query.Query")
public class Query implements Serializable, IDescriptionHolder, IModificationDateHolder, IRegistrationDateHolder, IRegistratorHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private QueryFetchOptions fetchOptions;

    @JsonProperty
    private IQueryId permId;

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private IQueryDatabaseId databaseId;

    @JsonProperty
    private String databaseLabel;

    @JsonProperty
    private QueryType queryType;

    @JsonProperty
    private String entityTypeCodePattern;

    @JsonProperty
    private String sql;

    @JsonProperty
    private boolean publicFlag;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Date modificationDate;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public QueryFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(QueryFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public IQueryId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(IQueryId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getName()
    {
        return name;
    }

    // Method automatically generated with DtoGenerator
    public void setName(String name)
    {
        this.name = name;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getDescription()
    {
        return description;
    }

    // Method automatically generated with DtoGenerator
    public void setDescription(String description)
    {
        this.description = description;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public IQueryDatabaseId getDatabaseId()
    {
        return databaseId;
    }

    // Method automatically generated with DtoGenerator
    public void setDatabaseId(IQueryDatabaseId databaseId)
    {
        this.databaseId = databaseId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getDatabaseLabel()
    {
        return databaseLabel;
    }

    // Method automatically generated with DtoGenerator
    public void setDatabaseLabel(String databaseLabel)
    {
        this.databaseLabel = databaseLabel;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public QueryType getQueryType()
    {
        return queryType;
    }

    // Method automatically generated with DtoGenerator
    public void setQueryType(QueryType queryType)
    {
        this.queryType = queryType;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getEntityTypeCodePattern()
    {
        return entityTypeCodePattern;
    }

    // Method automatically generated with DtoGenerator
    public void setEntityTypeCodePattern(String entityTypeCodePattern)
    {
        this.entityTypeCodePattern = entityTypeCodePattern;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getSql()
    {
        return sql;
    }

    // Method automatically generated with DtoGenerator
    public void setSql(String sql)
    {
        this.sql = sql;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public boolean isPublic()
    {
        return publicFlag;
    }

    // Method automatically generated with DtoGenerator
    public void setPublic(boolean publicFlag)
    {
        this.publicFlag = publicFlag;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions() != null && getFetchOptions().hasRegistrator())
        {
            return registrator;
        } else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "Query " + name;
    }

}
