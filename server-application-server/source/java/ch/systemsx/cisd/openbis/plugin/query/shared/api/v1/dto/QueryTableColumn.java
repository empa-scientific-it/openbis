/*
 * Copyright 2010 ETH Zuerich, CISD
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
 * Column of query data. Defines title and data type.
 * 
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("QueryTableColumn")
public class QueryTableColumn implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String title;

    private QueryTableColumnDataType dataType;

    /**
     * Creates an instance for specified title and data type.
     */
    public QueryTableColumn(String title, QueryTableColumnDataType dataType)
    {
        this.title = title;
        this.dataType = dataType;
    }

    /**
     * Returns the title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Returns the data type.
     */
    public QueryTableColumnDataType getDataType()
    {
        return dataType;
    }

    // JSON-RPC serialization
    private QueryTableColumn()
    {

    }

    private void setDataType(QueryTableColumnDataType dataType)
    {
        this.dataType = dataType;
    }

    private void setTitle(String title)
    {
        this.title = title;
    }
}
