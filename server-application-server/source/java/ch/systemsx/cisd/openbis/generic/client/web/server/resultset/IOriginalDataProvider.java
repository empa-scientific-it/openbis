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

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;

/**
 * Each implementation knows how to retrieve the original data used to produce a {@link IResultSet}.
 * 
 * @author Christian Ribeaud
 */
public interface IOriginalDataProvider<T>
{

    /**
     * Gets the original data limited by specified number of items. All data is returned if the argument is {@link Integer#MAX_VALUE}.
     */
    public List<T> getOriginalData(int maxSize) throws UserFailureException;

    /**
     * Returns headers if known, otherwise an empty list is returned.
     */
    public List<TableModelColumnHeader> getHeaders();
}
