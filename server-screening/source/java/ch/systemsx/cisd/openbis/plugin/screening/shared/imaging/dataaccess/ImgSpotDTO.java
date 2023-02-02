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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.ResultColumn;

/**
 * @author Tomasz Pylak
 */
public class ImgSpotDTO extends AbstractImgIdentifiable
{
    // position in the container, one-based (e.g. well column)
    @ResultColumn("X")
    private Integer column;

    // position in the container, one-based (e.g. well row, 1 is the first row)
    @ResultColumn("Y")
    private Integer row;

    @ResultColumn("CONT_ID")
    private long containerId;

    @SuppressWarnings("unused")
    private ImgSpotDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public ImgSpotDTO(Integer row, Integer column, long containerId)
    {
        this.column = column;
        this.row = row;
        this.containerId = containerId;
    }

    public Integer getColumn()
    {
        return column;
    }

    public void setColumn(Integer column)
    {
        this.column = column;
    }

    public Integer getRow()
    {
        return row;
    }

    public void setRow(Integer row)
    {
        this.row = row;
    }

    public long getContainerId()
    {
        return containerId;
    }

    public void setContainerId(long containerId)
    {
        this.containerId = containerId;
    }

}
