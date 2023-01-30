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

package ch.systemsx.cisd.openbis.generic.client.web.server.calculator;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.IBasicTableDataProvider;

/**
 * Interface to access table data and meta data. Used by calculator classes.
 *
 * @author Franz-Josef Elmer
 */
public interface ITableDataProvider extends IBasicTableDataProvider
{
    /**
     * Returns the value of specified column in the list of specified row values.
     * 
     * @throws IllegalArgumentException if undefined columnID
     */
    public Comparable<?> getValue(String columnID, List<? extends Comparable<?>> rowValues);

    /**
     * Returns a collection of the identifiers of all columns.
     */
    public Collection<String> getAllColumnIDs();

    /**
     * Returns the property of specified key for specified column.
     */
    public String tryToGetProperty(String columnID, String key);

}
