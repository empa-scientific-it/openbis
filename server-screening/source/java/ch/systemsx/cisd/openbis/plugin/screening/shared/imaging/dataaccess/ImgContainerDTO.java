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
 * DTO holding information about the container in the imaging database.
 * 
 * @author Tomasz Pylak
 */
public class ImgContainerDTO extends AbstractImgIdentifiable
{
    @ResultColumn("PERM_ID")
    private String permId;

    @ResultColumn("SPOTS_WIDTH")
    private Integer numberOfColumns;

    @ResultColumn("SPOTS_HEIGHT")
    private Integer numberOfRows;

    @ResultColumn("EXPE_ID")
    private long experimentId;

    @SuppressWarnings("unused")
    private ImgContainerDTO()
    {
    }

    public ImgContainerDTO(String permId, Integer numberOfRows, Integer numberOfColumns,
            long experimentId)
    {
        this.permId = permId;
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        this.experimentId = experimentId;
    }

    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    public Integer getNumberOfColumns()
    {
        return numberOfColumns;
    }

    public void setNumberOfColumns(Integer spotNumberOfColumns)
    {
        this.numberOfColumns = spotNumberOfColumns;
    }

    public Integer getNumberOfRows()
    {
        return numberOfRows;
    }

    public void setNumberOfRows(Integer spotNumberOfRows)
    {
        this.numberOfRows = spotNumberOfRows;
    }

    public long getExperimentId()
    {
        return experimentId;
    }

    public void setExperimentId(long experimentId)
    {
        this.experimentId = experimentId;
    }

}
