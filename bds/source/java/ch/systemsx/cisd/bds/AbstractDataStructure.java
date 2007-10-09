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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDataStructure implements IHasVersion
{
    protected final IStorage storage;
    protected final IDirectory root;

    AbstractDataStructure(IStorage storage)
    {
        assert storage != null: "Unspecified storage.";
        this.storage = storage;
        root = storage.getRoot();
    }

    public void load()
    {
        storage.load();
        Version loadedVersion = Version.loadFrom(root);
        if (getVersion().isBackwardsCompatibleWith(loadedVersion) == false)
        {
            throw new UserFailureException("Version of loaded data structure is " + loadedVersion
                    + " which is not backward compatible with " + getVersion());
        }
    }
    
    public void save()
    {
        getVersion().saveTo(root);
        storage.save();
    }
}
