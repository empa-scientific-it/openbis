/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.utilities;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 *
 */
public class VersionUtilsTest
{

    @Test
    public void test()
    {
        assertEquals(VersionUtils.isCompatible("12", "11", false), false);
        assertEquals(VersionUtils.isCompatible("12", "11", true), false);
        assertEquals(VersionUtils.isCompatible("12", "11.1", false), false);
        assertEquals(VersionUtils.isCompatible("12", "11.1", true), false);
        assertEquals(VersionUtils.isCompatible("12", "12", false), true);
        assertEquals(VersionUtils.isCompatible("12", "12", true), true);
        assertEquals(VersionUtils.isCompatible("12", "12.1", false), true);
        assertEquals(VersionUtils.isCompatible("12", "12.1", true), true);
        assertEquals(VersionUtils.isCompatible("12", "13", false), false);
        assertEquals(VersionUtils.isCompatible("12", "13", true), true);
        assertEquals(VersionUtils.isCompatible("12", "13.1", false), false);
        assertEquals(VersionUtils.isCompatible("12", "13.1", true), true);
        
        assertEquals(VersionUtils.isCompatible("12.2", "11", false), false);
        assertEquals(VersionUtils.isCompatible("12.2", "11", true), false);
        assertEquals(VersionUtils.isCompatible("12.2", "11.1", false), false);
        assertEquals(VersionUtils.isCompatible("12.2", "11.1", true), false);
        assertEquals(VersionUtils.isCompatible("12.2", "12", false), false);
        assertEquals(VersionUtils.isCompatible("12.2", "12", true), false);
        assertEquals(VersionUtils.isCompatible("12.2", "12.1", false), false);
        assertEquals(VersionUtils.isCompatible("12.2", "12.1", true), false);
        assertEquals(VersionUtils.isCompatible("12.2", "12.2", false), true);
        assertEquals(VersionUtils.isCompatible("12.2", "12.2", true), true);
        assertEquals(VersionUtils.isCompatible("12.2", "12.2.1", false), true);
        assertEquals(VersionUtils.isCompatible("12.2", "12.2.1", true), true);
        assertEquals(VersionUtils.isCompatible("12.2", "12.3", false), false);
        assertEquals(VersionUtils.isCompatible("12.2", "12.3", true), true);
        assertEquals(VersionUtils.isCompatible("12.2", "13", false), false);
        assertEquals(VersionUtils.isCompatible("12.2", "13", true), true);
        assertEquals(VersionUtils.isCompatible("12.2", "13.1", false), false);
        assertEquals(VersionUtils.isCompatible("12.2", "13.1", true), true);
    }

}
