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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Utility methods for plates.
 * 
 * @author Franz-Josef Elmer
 */
public class PlateUtils
{
    /**
     * Translates a row number into letter code. Thus, 1 -> A, 2 -> B, 26 -> Z, 27 -> AA, 28 -> AB, etc.
     */
    public static String translateRowNumberIntoLetterCode(int rowNumber)
    {
        // This code is duplicated in ch.systemsx.cisd.common.geometry.ConversionUtils.
        // But there is no way around this, since PlateUtils needs to be translatable to JavaScript,
        // whereas ConversionUtils does not. OTOH, ConversionUtils cannot depend on PlateUtils.
        // Alas....
        int rowIndex = rowNumber - 1;
        String code = "";
        while (rowIndex >= 0)
        {
            code = (char) (rowIndex % 26 + 'A') + code;
            rowIndex = rowIndex / 26 - 1;
        }
        return code;
    }

    /** Allows to generate well code from location, e.g. (1,1) produces "A1" */
    public static String translateLocationIntoWellCode(WellLocation wellLocation)
    {
        return translateRowNumberIntoLetterCode(wellLocation.getRow()) + wellLocation.getColumn();
    }

}
