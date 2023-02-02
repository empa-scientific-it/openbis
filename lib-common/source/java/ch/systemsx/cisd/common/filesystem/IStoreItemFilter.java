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

package ch.systemsx.cisd.common.filesystem;

/**
 * Filter of {@link StoreItem} objects. Note, that a filter can be stateful. That is, invocations of {@link #accept(StoreItem)} with the same argument
 * might return different values.
 *
 * @author Franz-Josef Elmer
 */
public interface IStoreItemFilter
{
    /**
     * Accepts the specified item or not.
     * 
     * @return <code>true</code> if the item is accepted (i.e. passes the filter).
     */
    public boolean accept(StoreItem item);

}