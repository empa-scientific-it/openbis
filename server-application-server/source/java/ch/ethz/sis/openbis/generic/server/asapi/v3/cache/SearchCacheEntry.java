/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.cache;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author pkupczyk
 */
public class SearchCacheEntry<OBJECT> implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Collection<OBJECT> objects;

    public Collection<OBJECT> getObjects()
    {
        return objects;
    }

    public void setObjects(Collection<OBJECT> objects)
    {
        this.objects = objects;
    }

    @Override
    public String toString()
    {
        int objectsSize = objects != null ? objects.size() : 0;
        return objectsSize == 1 ? "1 object" : objectsSize + " objects";
    }

}
