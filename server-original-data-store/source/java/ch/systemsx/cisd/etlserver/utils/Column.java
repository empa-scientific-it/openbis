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

package ch.systemsx.cisd.etlserver.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Franz-Josef Elmer
 */
public final class Column
{
    private final String header;

    private final List<String> values;

    Column(String header)
    {
        this.header = header;
        values = new ArrayList<String>();
    }

    public final String getHeader()
    {
        return header;
    }

    public final List<String> getValues()
    {
        return Collections.unmodifiableList(values);
    }

    final void add(String value)
    {
        values.add(value);
    }
}
