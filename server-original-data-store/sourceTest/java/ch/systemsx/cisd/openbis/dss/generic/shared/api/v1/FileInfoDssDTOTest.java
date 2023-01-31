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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class FileInfoDssDTOTest extends AssertJUnit
{

    @Test
    public void testToString()
    {
        FileInfoDssDTO fi = new FileInfoDssDTO("path/in/data/set", "path/in/listing", false, 800);
        assertEquals("FileInfoDssDTO[path/in/data/set,path/in/listing,800]", fi.toString());

        fi = new FileInfoDssDTO("path/in/data/set", "path/in/listing", true, -1);
        assertEquals("FileInfoDssDTO[path/in/data/set,path/in/listing,-1]", fi.toString());
    }
}
