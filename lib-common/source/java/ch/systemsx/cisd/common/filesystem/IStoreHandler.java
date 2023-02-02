/*
 * Copyright 2007 ETH Zuerich, CISD
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
 * Handles items in the file store.
 * <p>
 * Note that this interface is a higher abstraction of {@link IPathHandler} which works with {@link java.io.File}.
 * </p>
 * 
 * @see IPathHandler
 * @author Tomasz Pylak
 */
public interface IStoreHandler extends IStopSignaler
{
    /**
     * Handles given <var>item</var>.
     * 
     * @return <code>true</code> if the handling of the item was successful.
     */
    boolean handle(StoreItem item);
}
