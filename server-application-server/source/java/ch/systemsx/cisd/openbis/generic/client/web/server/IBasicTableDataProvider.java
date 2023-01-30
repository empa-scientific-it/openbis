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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.List;

/**
 * @author Franz-Josef Elmer
 */
public interface IBasicTableDataProvider
{

    /**
     * Returns all rows.
     */
    public List<List<? extends Comparable<?>>> getRows();

    /**
     * Returns a list of column titles. The order is the same as the order of row values returned by {@link #getRows()}.
     */
    public List<String> getAllColumnTitles();

}