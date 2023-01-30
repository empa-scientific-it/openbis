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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;

/**
 * Abstract {@link IDataRefreshCallback} implementation for refreshing {@link TypedTableGrid}.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractGridDataRefreshCallback<T extends Serializable/* Entity */>
        implements IDataRefreshCallback
{
    private TypedTableGrid<T> grid;

    public TypedTableGrid<T> getGrid()
    {
        assert grid != null : "grid not set!";
        return grid;
    }

    public void setGrid(TypedTableGrid<T> grid)
    {
        this.grid = grid;
    }
}
