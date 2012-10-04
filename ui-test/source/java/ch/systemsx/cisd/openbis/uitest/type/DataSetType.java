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

package ch.systemsx.cisd.openbis.uitest.type;

import java.util.Arrays;
import java.util.Collection;

import ch.systemsx.cisd.openbis.uitest.layout.DataSetTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.Location;
import ch.systemsx.cisd.openbis.uitest.page.DataSetTypeBrowser;

/**
 * @author anttil
 */
public class DataSetType implements Browsable<DataSetTypeBrowser>
{
    private final String code;

    private String description;

    public DataSetType(String code, String description)
    {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    public String getDescription()
    {
        return description;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public Collection<String> getColumns()
    {
        return Arrays.asList("Code", "Description");
    }

    @Override
    public int hashCode()
    {
        return code.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof DataSetType)
        {
            return ((DataSetType) o).getCode().equals(code);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "DataSetType " + this.code;
    }

    @Override
    public Location<? extends DataSetTypeBrowser> getBrowserLocation()
    {
        return new DataSetTypeBrowserLocation();
    }
}
