/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;

/**
 * A default {@link IResultSet} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class DefaultResultSet<K, T> implements IResultSet<K, T>
{
    private final K resultSetKey;

    private final GridRowModels<T> list;

    private final int totalLength;

    private final boolean partial;

    public DefaultResultSet(final K resultSetKey, final GridRowModels<T> list, final int totalLength, boolean partial)
    {
        this.partial = partial;
        assert resultSetKey != null : "Unspecified result set key";
        assert list != null : "Unspecified list.";
        assert totalLength > -1 : "Total length must be >= 0.";
        this.resultSetKey = resultSetKey;
        this.list = list;
        this.totalLength = totalLength;
    }

    //
    // IResult
    //

    @Override
    public final K getResultSetKey()
    {
        return resultSetKey;
    }

    @Override
    public final GridRowModels<T> getList()
    {
        return list;
    }

    @Override
    public final int getTotalLength()
    {
        return totalLength;
    }

    @Override
    public boolean isPartial()
    {
        return partial;
    }

}
