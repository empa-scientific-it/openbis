/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.RpcMap;

/**
 * A Subclass of the GXT BaseModelData that simplifies default behavior ignoring special characters ('.', '[', ']').
 * <p>
 * As a result nested value support is turned off. NestedValues check for key strings with '.' in then treat them as paths for traversing multiple
 * HashMaps. We don't need this, and, in fact, it causes problems for us, since we occasionally use '.' as part of a normal column name for a table
 * without intending the nested-value semantics; thus we turn it off.
 * <p>
 * Also handling of property maps with syntax p1[key1] is switched off to allow '[' and ']' characters in normal column names.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Piotr Buczek
 */
public abstract class SimplifiedBaseModelData extends BaseModelData
{

    private static final long serialVersionUID = 1L;

    // no special handling of '.', '[' and ']'

    @Override
    @SuppressWarnings("unchecked")
    public <X> X get(String property)
    {
        if (map == null)
        {
            return null;
        }
        return (X) map.get(property);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> X set(String property, X value)
    {
        if (map == null)
        {
            map = new RpcMap();
        }
        return (X) map.put(property, value);
    }
}
