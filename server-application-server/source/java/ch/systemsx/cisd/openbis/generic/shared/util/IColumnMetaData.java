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

package ch.systemsx.cisd.openbis.generic.shared.util;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * Interface for setting meta data of columns defined by {@link TypedTableModelBuilder#addColumn(String)}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IColumnMetaData
{
    /**
     * Sets the title.
     */
    public IColumnMetaData withTitle(String title);

    /**
     * Sets the default column width.
     */
    public IColumnMetaData withDefaultWidth(int width);

    /**
     * Sets the data type.
     */
    public IColumnMetaData withDataType(DataTypeCode dataType);

    /**
     * Sets hidden flag to <code>true</code>.
     */
    public IColumnMetaData hideByDefault();

    /**
     * Sets editable falg to <code>true</code>.
     */
    public IColumnMetaData editable();

    /**
     * Sets linkEntitiesOnly flag to <code>true</code>
     */
    public IColumnMetaData linkEntitiesOnly();
}