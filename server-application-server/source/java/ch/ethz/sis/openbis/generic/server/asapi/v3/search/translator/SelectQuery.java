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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator;

import java.util.List;

public class SelectQuery
{

    private final String query;

    private final List<Object> args;

    public SelectQuery(String query, List<Object> args)
    {
        this.query = query;
        this.args = args;
    }

    public String getQuery()
    {
        return query;
    }

    public List<Object> getArgs()
    {
        return args;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        SelectQuery that = (SelectQuery) o;

        if (!query.equals(that.query))
        {
            return false;
        }
        return args.equals(that.args);
    }

    @Override
    public int hashCode()
    {
        int result = query.hashCode();
        result = 31 * result + args.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "SelectQuery{" +
                "query='" + query + '\'' +
                ", args=" + args +
                '}';
    }

}
