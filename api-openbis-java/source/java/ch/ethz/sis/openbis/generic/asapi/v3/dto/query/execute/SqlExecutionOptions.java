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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.query.execute.SqlExecutionOptions")
public class SqlExecutionOptions implements Serializable
{
    private static final long serialVersionUID = 1L;

    private IQueryDatabaseId databaseId;

    private Map<String, String> parameters = new HashMap<String, String>();

    public SqlExecutionOptions withDatabaseId(IQueryDatabaseId databaseId)
    {
        this.databaseId = databaseId;
        return this;
    }

    public IQueryDatabaseId getDatabaseId()
    {
        return databaseId;
    }

    public SqlExecutionOptions withParameter(String parameterName, String value)
    {
        parameters.put(parameterName, value);
        return this;
    }

    public SqlExecutionOptions withParameters(Map<String, String> parameters)
    {
        this.parameters = parameters;
        return this;
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + ": databaseId=" + databaseId + ", parameterKeys=" + (parameters != null ? parameters.keySet() : "[]");
    }

}
