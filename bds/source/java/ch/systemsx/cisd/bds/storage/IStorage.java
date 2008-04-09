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

package ch.systemsx.cisd.bds.storage;

import ch.systemsx.cisd.bds.exception.StorageException;

/**
 * Abstraction of a hierarchical storage.
 * 
 * @author Franz-Josef Elmer
 */
public interface IStorage
{
    /**
     * Mounts this storage. May perform some initializations. Should be called before calling
     * {@link #getRoot()}.
     */
    public void mount();

    /**
     * Returns root directory of this storage.
     * 
     * @throws StorageException if invoked before {@link #mount()} or after {@link #unmount()}.
     */
    public IDirectory getRoot();

    /**
     * Unmounts this storage. May perform some finalization (e.g. make cached data persistent).
     */
    public void unmount();
}
