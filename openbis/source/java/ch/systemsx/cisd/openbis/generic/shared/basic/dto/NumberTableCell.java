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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * Table cell wrapping an integer.
 *
 * @author Franz-Josef Elmer
 */
public class NumberTableCell implements ISerializableComparable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;
    
    private Number integer;
    
    public NumberTableCell(Number integer)
    {
        this.integer = integer;
    }

    public int compareTo(ISerializableComparable o)
    {
        if (o instanceof NumberTableCell)
        {
            double v1 = integer.doubleValue();
            NumberTableCell numberTableCell = (NumberTableCell) o;
            double v2 = numberTableCell.integer.doubleValue();
            return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
        }
        return integer.toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj
                || (obj instanceof NumberTableCell && integer.equals(((NumberTableCell) obj).integer));
   }
    
    @Override
    public int hashCode()
    {
        return integer.hashCode();
    }
    
    @Override
    public String toString()
    {
        return integer.toString();
    }
    
    // ---------------------------

    // GWT only
    @SuppressWarnings("unused")
    private NumberTableCell()
    {
    }

    // GWT only
    @SuppressWarnings("unused")
    private void setNumber(Number number)
    {
        this.integer = number;
    }

    
}
