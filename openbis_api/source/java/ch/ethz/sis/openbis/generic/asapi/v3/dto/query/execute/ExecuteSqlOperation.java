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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.query.execute.ExecuteSqlOperation")
public class ExecuteSqlOperation implements IOperation
{
    private static final long serialVersionUID = 1L;

    private String sql;

    private SqlExecutionOptions options;

    @SuppressWarnings("unused")
    private ExecuteSqlOperation()
    {
    }

    public ExecuteSqlOperation(String sql, SqlExecutionOptions options)
    {
        this.sql = sql;
        this.options = options;
    }

    public String getSql()
    {
        return sql;
    }

    public SqlExecutionOptions getOptions()
    {
        return options;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + (sql != null ? " " + sql : "");
    }

}
