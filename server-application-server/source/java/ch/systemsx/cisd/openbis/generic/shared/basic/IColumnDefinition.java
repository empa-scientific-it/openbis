/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * Describes table column's metadata. Has the ability to render cell values for the column given the row model.
 * 
 * @author Tomasz Pylak
 */
public interface IColumnDefinition<T> extends IsSerializable
{
    /** extracts value for the cell of the represented column */
    String getValue(GridRowModel<T> rowModel);

    /** tries to extract comparable value for the cell of the represented column */
    Comparable<?> tryGetComparableValue(GridRowModel<T> rowModel);

    /** column's header */
    String getHeader();

    /** unique identifier of the column */
    String getIdentifier();

    /**
     * Returns data type code or <code>null</code> if undefined.
     */
    DataTypeCode tryToGetDataType();

    /** Tries to get specified property or <code>null</code> if not found. */
    String tryToGetProperty(String key);

    /** Returns true if this is a custom column */
    boolean isCustom();

}
