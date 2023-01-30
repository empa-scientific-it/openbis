/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.page;

import java.util.HashMap;
import java.util.Map;

/**
 * @author anttil
 */
public class BrowserRow
{

    private Map<String, BrowserCell> row;

    private boolean exists;

    public BrowserRow()
    {
        this.exists = false;
        this.row = new HashMap<String, BrowserCell>();
    }

    public BrowserRow(Map<String, BrowserCell> row)
    {
        this.row = row;
        this.exists = true;
    }

    public boolean exists()
    {
        return exists;
    }

    public BrowserCell get(String columnName)
    {
        return this.row.get(columnName);
    }

    @Override
    public String toString()
    {
        if (exists)
        {
            return "A browser row with the following data: " + row;
        } else
        {
            return "A row that does not exist";
        }
    }

}
