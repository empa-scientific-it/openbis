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

package ch.systemsx.cisd.etlserver.registrator.api.impl;

import net.lemnik.eodsql.DynamicTransactionQuery;

/**
 * A simple bean for capturing the contextual information about a failure in a secondary transaction
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SecondaryTransactionFailure
{
    private final DynamicTransactionQuery query;

    private final Throwable error;

    public SecondaryTransactionFailure(DynamicTransactionQuery query, Throwable error)
    {
        this.query = query;
        this.error = error;
    }

    public DynamicTransactionQuery getQuery()
    {
        return query;
    }

    public Throwable getError()
    {
        return error;
    }

    @Override
    public String toString()
    {
        return "SecondaryTransactionFailure [query=" + query + ", error=" + error + "]";
    }

}
