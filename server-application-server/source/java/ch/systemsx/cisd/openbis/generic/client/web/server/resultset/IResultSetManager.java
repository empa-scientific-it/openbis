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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;

/**
 * A result set manager.
 * 
 * @author Christian Ribeaud
 */
public interface IResultSetManager<K>
{

    /**
     * Produces a {@link IResultSet} from given <var>resultConfig</var> and given <var>dataProvider</var>.
     * <p>
     * In case of cached data, the {@link IOriginalDataProvider} implementation is only used in the first call, when the full data are not already
     * there.
     * </p>
     */
    public <T> IResultSet<K, T> getResultSet(final String sessionToken,
            final IResultSetConfig<K, T> resultConfig, final IOriginalDataProvider<T> dataProvider)
            throws UserFailureException;

    /**
     * Remove the data mapped to given <var>resultSetKey</var>.
     */
    public void removeResultSet(final K resultSetKey) throws UserFailureException;

    /**
     * Locks specified result set. Lock will be released after invocation of {@link #removeResultSet(Object)} and after reloading of the result set.
     * {@link #getResultSet(String, IResultSetConfig, IOriginalDataProvider)} waits until lock is released.
     */
    public void lockResultSet(K resultSetKey);

}