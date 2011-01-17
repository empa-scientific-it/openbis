/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Piotr Buczek
 */
public class QueryDatabase implements ISerializable, Comparable<QueryDatabase>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String key;

    private String label;

    public QueryDatabase()
    {
    }

    /** for tests */
    public QueryDatabase(String key)
    {
        this(key, key);
    }

    public QueryDatabase(String key, String label)
    {
        this.key = key;
        this.label = label;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return label + " [" + key + "]";
    }

    public int compareTo(QueryDatabase o)
    {
        return key.compareTo(o.key);
    }

}
