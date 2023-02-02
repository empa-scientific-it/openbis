/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Holds the complete metadata of a plate and its wells.
 * 
 * @since 1.8
 * @author Kaloyan Enimanev
 */
@SuppressWarnings("unused")
@JsonObject("PlateMetadata")
public class PlateMetadata extends PlateIdentifier
{

    private static final long serialVersionUID = 1L;

    private Geometry plateGeometry;

    private Map<String, String> properties;

    private List<WellMetadata> wells;

    public PlateMetadata(PlateIdentifier identifier, Geometry plateGeometry, Map<String, String> properties,
            List<WellMetadata> unsortedWells)
    {
        super(identifier.getPlateCode(), identifier.tryGetSpaceCode(), identifier.getPermId());
        this.plateGeometry = plateGeometry;
        this.properties = new HashMap<String, String>(properties);
        this.wells = sortWells(unsortedWells);
    }

    public Geometry getPlateGeometry()
    {
        return plateGeometry;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public List<WellMetadata> getWells()
    {
        return Collections.unmodifiableList(wells);
    }

    public WellMetadata getWell(int row, int col)
    {
        int idx = getWellIndexForRowAndCol(row, col);
        return wells.get(idx);
    }

    private int getWellIndexForRowAndCol(int row, int col)
    {
        return (row - 1) * plateGeometry.getNumberOfColumns() + (col - 1);
    }

    private List<WellMetadata> sortWells(List<WellMetadata> unsortedWells)
    {
        WellMetadata[] wellsArray =
                new WellMetadata[plateGeometry.getNumberOfRows() * plateGeometry.getNumberOfColumns()];
        for (WellMetadata well : unsortedWells)
        {
            int row = well.getWellPosition().getWellRow();
            int col = well.getWellPosition().getWellColumn();
            wellsArray[getWellIndexForRowAndCol(row, col)] = well;
        }
        return Arrays.asList(wellsArray);
    }

    //
    // JSON-RPC
    //

    private PlateMetadata()
    {
        super(null, null);
    }

    private void setPlateGeometry(Geometry plateGeometry)
    {
        this.plateGeometry = plateGeometry;
    }

    private void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    private void setWells(List<WellMetadata> wells)
    {
        this.wells = wells;
    }

}
