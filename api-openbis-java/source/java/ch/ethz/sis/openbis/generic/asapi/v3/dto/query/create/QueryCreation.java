/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.IObjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.query.create.QueryCreation")
public class QueryCreation implements ICreation, IObjectCreation
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String name;

    @JsonProperty
    private IQueryDatabaseId databaseId;

    @JsonProperty
    private QueryType queryType;

    @JsonProperty
    private String entityTypeCodePattern;

    @JsonProperty
    private String description;

    @JsonProperty
    private String sql;

    @JsonProperty
    private boolean publicFlag;

    @JsonIgnore
    public String getName()
    {
        return name;
    }

    @JsonIgnore
    public void setName(String name)
    {
        this.name = name;
    }

    @JsonIgnore
    public IQueryDatabaseId getDatabaseId()
    {
        return databaseId;
    }

    @JsonIgnore
    public void setDatabaseId(IQueryDatabaseId databaseId)
    {
        this.databaseId = databaseId;
    }

    @JsonIgnore
    public QueryType getQueryType()
    {
        return queryType;
    }

    @JsonIgnore
    public void setQueryType(QueryType queryType)
    {
        this.queryType = queryType;
    }

    @JsonIgnore
    public String getEntityTypeCodePattern()
    {
        return entityTypeCodePattern;
    }

    @JsonIgnore
    public void setEntityTypeCodePattern(String entityTypeCodePattern)
    {
        this.entityTypeCodePattern = entityTypeCodePattern;
    }

    @JsonIgnore
    public String getDescription()
    {
        return description;
    }

    @JsonIgnore
    public void setDescription(String description)
    {
        this.description = description;
    }

    @JsonIgnore
    public String getSql()
    {
        return sql;
    }

    @JsonIgnore
    public void setSql(String sql)
    {
        this.sql = sql;
    }

    @JsonIgnore
    public boolean isPublic()
    {
        return publicFlag;
    }

    @JsonIgnore
    public void setPublic(boolean publicFlag)
    {
        this.publicFlag = publicFlag;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("databaseId", databaseId).append("name", name).toString();
    }

}
