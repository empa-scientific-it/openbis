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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.query.update;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.query.update.QueryUpdate")
public class QueryUpdate implements IUpdate, IObjectUpdate<IQueryId>
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IQueryId queryId;

    @JsonProperty
    private FieldUpdateValue<String> name = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> description = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<IQueryDatabaseId> databaseId = new FieldUpdateValue<IQueryDatabaseId>();

    @JsonProperty
    private FieldUpdateValue<QueryType> queryType = new FieldUpdateValue<QueryType>();

    @JsonProperty
    private FieldUpdateValue<String> entityTypeCodePattern = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> sql = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<Boolean> publicFlag = new FieldUpdateValue<Boolean>();

    @Override
    @JsonIgnore
    public IQueryId getObjectId()
    {
        return getQueryId();
    }

    @JsonIgnore
    public IQueryId getQueryId()
    {
        return queryId;
    }

    @JsonIgnore
    public void setQueryId(IQueryId queryId)
    {
        this.queryId = queryId;
    }

    @JsonIgnore
    public void setName(String name)
    {
        this.name.setValue(name);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getName()
    {
        return name;
    }

    @JsonIgnore
    public void setDescription(String description)
    {
        this.description.setValue(description);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getDescription()
    {
        return description;
    }

    @JsonIgnore
    public void setDatabaseId(IQueryDatabaseId databaseId)
    {
        this.databaseId.setValue(databaseId);
    }

    @JsonIgnore
    public FieldUpdateValue<IQueryDatabaseId> getDatabaseId()
    {
        return databaseId;
    }

    @JsonIgnore
    public void setQueryType(QueryType queryType)
    {
        this.queryType.setValue(queryType);
    }

    @JsonIgnore
    public FieldUpdateValue<QueryType> getQueryType()
    {
        return queryType;
    }

    @JsonIgnore
    public void setEntityTypeCodePattern(String entityTypeCodePattern)
    {
        this.entityTypeCodePattern.setValue(entityTypeCodePattern);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getEntityTypeCodePattern()
    {
        return entityTypeCodePattern;
    }

    @JsonIgnore
    public void setSql(String sql)
    {
        this.sql.setValue(sql);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getSql()
    {
        return sql;
    }

    @JsonIgnore
    public void setPublic(boolean publicFlag)
    {
        this.publicFlag.setValue(publicFlag);
    }

    @JsonIgnore
    public FieldUpdateValue<Boolean> isPublic()
    {
        return publicFlag;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("queryId", queryId).toString();
    }

}
