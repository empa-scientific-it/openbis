/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.Date;

/**
 * Table cell wrapping a {@link Date}.
 *
 * @author Franz-Josef Elmer
 */
public class DateTableCell implements ISerializableComparable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private long dateTime;

    public DateTableCell(long dateTime)
    {
        this.dateTime = dateTime;
    }

    public DateTableCell(Date date)
    {
        dateTime = date.getTime();
    }

    public Date getDateTime()
    {
        return new Date(dateTime);
    }

    @Override
    public int compareTo(ISerializableComparable o)
    {
        if (o instanceof DateTableCell)
        {
            DateTableCell cell = (DateTableCell) o;
            return dateTime < cell.dateTime ? -1 : (dateTime > cell.dateTime ? 1 : 0);
        }
        return toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj
                || (obj instanceof DateTableCell && dateTime == ((DateTableCell) obj).dateTime);
    }

    @Override
    public int hashCode()
    {
        return (int) dateTime;
    }

    @Override
    public String toString()
    {
        return new Date(dateTime).toString();
    }

    // ---------------------------

    // GWT only
    @SuppressWarnings("unused")
    private DateTableCell()
    {
    }
}
