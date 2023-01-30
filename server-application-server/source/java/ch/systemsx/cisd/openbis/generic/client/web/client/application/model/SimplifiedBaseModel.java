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

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.RpcMap;

/**
 * A Subclass of the GXT BaseModel that simplifies default behavior ignoring special characters (e.g. '.', '[', ']').
 * 
 * @see SimplifiedBaseModelData
 * @author Chandrasekhar Ramakrishnan
 * @author Piotr Buczek
 */
public abstract class SimplifiedBaseModel extends BaseModel
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
