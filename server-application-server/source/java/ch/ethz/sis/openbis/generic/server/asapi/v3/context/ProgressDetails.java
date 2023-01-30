/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.context;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author pkupczyk
 */
public class ProgressDetails
{

    private Map<String, Object> map = new LinkedHashMap<String, Object>();

    public void set(String name, Object value)
    {
        if (value != null)
        {
            map.put(name, value);
        }
    }

    public void set(String name, ProgressDetails details)
    {
        if (details != null)
        {
            map.put(name, details.map);
        }
    }

    @Override
    public String toString()
    {
        return ProgressDetailsToStringBuilder.getInstance().toString(map);
    }

}
