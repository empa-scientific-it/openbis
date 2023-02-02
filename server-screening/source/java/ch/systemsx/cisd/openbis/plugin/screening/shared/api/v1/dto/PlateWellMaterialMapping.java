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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Bernd Rinn
 */
@SuppressWarnings("unused")
@JsonObject("PlateWellMaterialMapping")
public class PlateWellMaterialMapping implements Serializable
{
    private static final long serialVersionUID = 1L;

    private PlateIdentifier plateIdentifier;

    private Geometry plateGeometry;

    @JsonProperty
    private List<MaterialIdentifier>[] mapping;

    public PlateWellMaterialMapping(PlateIdentifier plateIdentifier, Geometry plateGeometry,
            List<MaterialIdentifier>[] mapping)
    {
        this.plateIdentifier = plateIdentifier;
        this.plateGeometry = plateGeometry;
        this.mapping = mapping;
    }

    public PlateWellMaterialMapping(PlateIdentifier plateIdentifier, Geometry plateGeometry,
            int defaultSize)
    {
        this.plateIdentifier = plateIdentifier;
        final int size = plateGeometry.getNumberOfRows() * plateGeometry.getNumberOfColumns();
        this.plateGeometry = plateGeometry;
        this.mapping = createList(size);
        for (int i = 0; i < size; ++i)
        {
            mapping[i] = new ArrayList<MaterialIdentifier>(defaultSize);
        }
    }

    @SuppressWarnings("unchecked")
    private List<MaterialIdentifier>[] createList(final int size)
    {
        return new List[size];
    }

    /**
     * Returns the plate identifier that this plate is for.
     */
    public PlateIdentifier getPlateIdentifier()
    {
        return plateIdentifier;
    }

    /**
     * Returns the plate geometry of the plate that this mapping is for.
     */
    public Geometry getPlateGeometry()
    {
        return plateGeometry;
    }

    /**
     * Returns the list of materials present in well <var>(row,col)</var>.
     * 
     * @param row The row of the well to get the materials for
     * @param col The column of the well to get the materials for
     * @return The list of materials in the specified well.
     */
    public List<MaterialIdentifier> getMaterialsForWell(int row, int col)
    {
        return mapping[(row - 1) * plateGeometry.getNumberOfColumns() + (col - 1)];
    }

    //
    // JSON-RPC
    //

    private PlateWellMaterialMapping()
    {
    }

    private void setPlateIdentifier(PlateIdentifier plateIdentifier)
    {
        this.plateIdentifier = plateIdentifier;
    }

    private void setPlateGeometry(Geometry plateGeometry)
    {
        this.plateGeometry = plateGeometry;
    }

    private List<MaterialIdentifier>[] getMapping()
    {
        return mapping;
    }

    private void setMapping(List<MaterialIdentifier>[] mapping)
    {
        this.mapping = mapping;
    }

}
