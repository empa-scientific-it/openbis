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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;

/**
 * @author pkupczyk
 */
public class TileLocation implements Serializable
{

    private static final long serialVersionUID = 1L;

    private int row;

    private int column;

    // GWT only
    @SuppressWarnings("unused")
    private TileLocation()
    {
    }

    public TileLocation(int row, int column)
    {
        if (row <= 0)
        {
            throw new IllegalArgumentException("Row must be > 0");
        }
        if (column <= 0)
        {
            throw new IllegalArgumentException("Column must be > 0");
        }
        this.row = row;
        this.column = column;
    }

    public int getRow()
    {
        return row;
    }

    public int getColumn()
    {
        return column;
    }

}
