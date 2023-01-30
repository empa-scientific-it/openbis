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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ITableModel;

/**
 * Builder of simple table models. All column titles should be unique.
 * 
 * @author Piotr Buczek
 */
// NOTE: All methods of this interface are part of the Managed Properties API.
public interface ISimpleTableModelBuilderAdaptor
{

    /**
     * Returns the defined table model. It should be used to set an output of managed property.
     */
    ITableModel getTableModel();

    /**
     * Adds an empty row and returns a row builder for setting values of this row.
     */
    IRowBuilderAdaptor addRow();

    /**
     * Adds header column with specified title and default column width 150.
     * 
     * @throws UserFailureException if header with the same title has already been added.
     */
    void addHeader(String title);

    /**
     * Adds header column with specified title and specified default column width.
     * 
     * @throws UserFailureException if header with the same title has already been added.
     */
    void addHeader(String title, int defaultColumnWidth);

    /**
     * Adds header column with specified title, specified code and default column width 150.
     * 
     * @throws UserFailureException if header with the same title has already been added.
     */
    void addHeader(String title, String code);

    /**
     * A convenience method for adding complete header with columns with specified titles and default column width 150.
     * 
     * @throws UserFailureException if header titles are not unique.
     */
    void addFullHeader(String... titles);

    /**
     * A convenience method for adding a row with specified values.
     */
    void addFullRow(String... values);

}
