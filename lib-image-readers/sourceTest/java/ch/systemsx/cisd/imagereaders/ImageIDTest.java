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

package ch.systemsx.cisd.imagereaders;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ImageIDTest extends AssertJUnit
{
    @Test
    public void testToString()
    {
        assertEquals("1-3-42-0", new ImageID(1, 3, 42, 0).toString());
    }
    
    @Test
    public void testParse()
    {
        assertEquals(new ImageID(1, 3, 9, 27), ImageID.parse("1-3-9-27"));
    }
}
