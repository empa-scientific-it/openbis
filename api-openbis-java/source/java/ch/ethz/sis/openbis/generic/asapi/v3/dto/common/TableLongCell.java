/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.common.TableLongCell")
public final class TableLongCell implements ITableCell
{
    private static final long serialVersionUID = 1L;

    private long value;

    public TableLongCell(long value)
    {
        this.value = value;
    }

    public long getValue()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        return (int) value;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (super.equals(obj) == false || getClass() != obj.getClass())
        {
            return false;
        }
        return value == ((TableLongCell) obj).value;
    }

    @Override
    public String toString()
    {
        return String.valueOf(value);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private TableLongCell()
    {
    }

    @SuppressWarnings("unused")
    private void setValue(long value)
    {
        this.value = value;
    }

}
