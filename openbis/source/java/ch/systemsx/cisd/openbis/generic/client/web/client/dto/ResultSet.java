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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.annotation.DoNotEscape;

/**
 * A result set that has been returned by the server.
 * <p>
 * This is the <i>GWT</i> pendant to IResultSet.
 * </p>
 * 
 * @author Christian Ribeaud
 */
@DoNotEscape
public final class ResultSet<T> implements IsSerializable, IResultSetHolder<T>
{
    private GridRowModels<T> list;

    private int totalLength;

    private String resultSetKey;
    
    private boolean partial;

    public final void setList(final GridRowModels<T> result)
    {
        this.list = result;
    }

    public final void setTotalLength(final int totalLength)
    {
        this.totalLength = totalLength;
    }

    public final void setResultSetKey(final String resultSetKey)
    {
        this.resultSetKey = resultSetKey;
    }

    /**
     * Uniquely identifies a result set on the server side.
     */
    public String getResultSetKey()
    {
        return resultSetKey;
    }

    /**
     * Returns the list produced by a given {@link IResultSetConfig}.
     */
    public GridRowModels<T> getList()
    {
        return list;
    }

    /**
     * Returns the total count.
     * <p>
     * This value will usually not equal the number returned by {@link #getList()}.
     * </p>
     * 
     * @return the total count
     */
    public int getTotalLength()
    {
        return totalLength;
    }

    public ResultSet<T> getResultSet()
    {
        return this;
    }

    @Override
    public String toString()
    {
        return list.toString();
    }

    public void setPartial(boolean partial)
    {
        this.partial = partial;
    }

    public boolean isPartial()
    {
        return partial;
    }

}
