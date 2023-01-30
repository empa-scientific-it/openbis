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

package ch.systemsx.cisd.openbis.generic.shared.util;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode.BOOLEAN;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode.INTEGER;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode.REAL;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode.TIMESTAMP;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode.VARCHAR;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class DataTypeUtilsTest extends AssertJUnit
{
    @Test
    public void testGetCompatibleDataType()
    {
        assertEquals(null, DataTypeUtils.getCompatibleDataType(null, null));
        assertEquals(REAL, DataTypeUtils.getCompatibleDataType(null, REAL));
        assertEquals(REAL, DataTypeUtils.getCompatibleDataType(REAL, null));
        assertEquals(REAL, DataTypeUtils.getCompatibleDataType(REAL, INTEGER));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(REAL, BOOLEAN));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(REAL, TIMESTAMP));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(REAL, VARCHAR));
        assertEquals(INTEGER, DataTypeUtils.getCompatibleDataType(null, INTEGER));
        assertEquals(INTEGER, DataTypeUtils.getCompatibleDataType(INTEGER, null));
        assertEquals(INTEGER, DataTypeUtils.getCompatibleDataType(INTEGER, INTEGER));
        assertEquals(REAL, DataTypeUtils.getCompatibleDataType(INTEGER, REAL));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(INTEGER, BOOLEAN));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(INTEGER, TIMESTAMP));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(INTEGER, VARCHAR));
        assertEquals(BOOLEAN, DataTypeUtils.getCompatibleDataType(null, BOOLEAN));
        assertEquals(BOOLEAN, DataTypeUtils.getCompatibleDataType(BOOLEAN, null));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(BOOLEAN, REAL));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(BOOLEAN, INTEGER));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(BOOLEAN, TIMESTAMP));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(BOOLEAN, VARCHAR));
        assertEquals(TIMESTAMP, DataTypeUtils.getCompatibleDataType(null, TIMESTAMP));
        assertEquals(TIMESTAMP, DataTypeUtils.getCompatibleDataType(TIMESTAMP, null));
        assertEquals(TIMESTAMP, DataTypeUtils.getCompatibleDataType(TIMESTAMP, TIMESTAMP));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(TIMESTAMP, REAL));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(TIMESTAMP, INTEGER));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(TIMESTAMP, BOOLEAN));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(TIMESTAMP, VARCHAR));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(null, VARCHAR));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(VARCHAR, null));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(VARCHAR, REAL));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(VARCHAR, INTEGER));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(VARCHAR, BOOLEAN));
        assertEquals(VARCHAR, DataTypeUtils.getCompatibleDataType(VARCHAR, TIMESTAMP));
    }
}
