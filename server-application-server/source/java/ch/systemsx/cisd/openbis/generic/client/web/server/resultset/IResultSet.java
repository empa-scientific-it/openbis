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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;

/**
 * A result set that is returned to the client.
 * 
 * @author Christian Ribeaud
 */
public interface IResultSet<K, T>
{
    /**
     * Uniquely identifies a result set on the server side.
     */
    public K getResultSetKey();

    /**
     * Returns the list produced by a given {@link IResultSetConfig}.
     */
    public GridRowModels<T> getList();

    /**
     * Returns the total count.
     * <p>
     * This value will usually not equal the number returned by {@link #getList()}.
     * </p>
     * 
     * @return the total count
     */
    public int getTotalLength();

    /**
     * Returns <code>true</code> if this result set is not complete. Note, that {@link #getTotalLength()} is meaningless if this is the case.
     */
    public boolean isPartial();

}
