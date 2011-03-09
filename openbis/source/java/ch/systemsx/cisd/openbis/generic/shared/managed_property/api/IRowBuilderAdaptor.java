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

package ch.systemsx.cisd.openbis.generic.shared.managed_property.api;

import java.util.Date;

/**
 * Builder of a row of a simple table.
 * <p>
 * <b>All methods of this enum are part of the Managed Properties API.</b>
 * 
 * @see ISimpleTableModelBuilderAdaptor
 * @author Piotr Buczek
 */
public interface IRowBuilderAdaptor
{

    /**
     * Sets the value of the column specified by the header title to the specified string.
     */
    public void setCell(String headerTitle, String value);

    /**
     * Sets the value of the column specified by the header title to the specified long value.
     */
    public void setCell(String headerTitle, long value);

    /**
     * Sets the value of the column specified by the header title to the specified double value.
     */
    public void setCell(String headerTitle, double value);

    /**
     * Sets the value of the column specified by the header title to the specified date.
     */
    public void setCell(String headerTitle, Date value);

    /**
     * Sets the value of the column specified by the header title to the specified entity link
     * rendered using identifier.
     */
    public void setCell(String headerTitle, IEntityLinkElement value);

    /**
     * Sets the value of the column specified by the header title to the specified entity link
     * rendered using given text.
     */
    public void setCell(String headerTitle, IEntityLinkElement value, String linkText);

}
