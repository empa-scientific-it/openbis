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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;

/**
 * Fixed column IDs for well search grid.
 * 
 * @author Franz-Josef Elmer
 */
public class WellSearchGridColumnIds
{
    public static final String EXPERIMENT = "experiment";

    public static final String PLATE = "PLATE";

    public static final String WELL = "WELL";

    public static final String IMAGE_DATA_SET = "IMAGE_DATA_SET";

    public static final String IMAGE_ANALYSIS_DATA_SET = "IMAGE_ANALYSIS_DATA_SET";

    public static final String FILE_FORMAT_TYPE = "file_format_type";

    public static final String WELL_IMAGES = "WELL_IMAGES";

    public static final String ANALYSIS_PROCEDURE = "ANALYSIS_PROCEDURE";

    private static final String MATERIAL_PROPERTY_GROUP = "MATERIAL_PROPERTY-";

    private static final String PROPERTY_CODE_MARKER = "$$$";

    public static String tryExtractWellMaterialPropertyCode(String columnId)
    {
        // TODO 2011-04-13, Tomasz Pylak: unify with TypedTableModelBuilder.getColumnId()
        String userPropertyPrefix = "USER-";
        String propertyPrefix = PROPERTY_CODE_MARKER + userPropertyPrefix;
        int ix = columnId.indexOf(propertyPrefix);
        if (ix != -1)
        {
            return columnId.substring(ix + propertyPrefix.length());
        } else
        {
            return null;
        }
    }

    /** id of the well column of material type */
    public static String getWellMaterialColumnGroupPrefix(IEntityProperty wellMaterialProperty)
    {
        return MATERIAL_PROPERTY_GROUP + wellMaterialProperty.getPropertyType().getSimpleCode()
                + "-" + PROPERTY_CODE_MARKER;
    }

    /**
     * id of the column which contains properties of the material (which itself is a property of the well)
     */
    public static String getWellMaterialPropertyColumnGroupPrefix(IEntityProperty materialProperty)
    {
        return MATERIAL_PROPERTY_GROUP + "PROP-" + PROPERTY_CODE_MARKER
                + materialProperty.getPropertyType().getSimpleCode() + "-";
    }

}
