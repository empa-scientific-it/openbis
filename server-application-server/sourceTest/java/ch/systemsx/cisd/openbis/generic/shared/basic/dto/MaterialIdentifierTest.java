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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialTypeBuilder;

/**
 * @author Kaloyan Enimanev
 */
public class MaterialIdentifierTest extends AssertJUnit
{

    @Test
    public void testParseNormalCodes()
    {
        assertCorrectlyParsed("CODE", "TYPE", "CODE (TYPE)");
        assertCorrectlyParsed("12_CODE", "12_TYPE", "12_CODE (12_TYPE)");
        assertCorrectlyParsed("0", "0", "0 (0)");
    }

    @Test
    public void testParseWithArbitraryCharacters()
    {
        assertCorrectlyParsed(":123(ABC).PQ//", "TYPE", ":123(ABC).PQ// (TYPE)");
        assertCorrectlyParsed("/.()$%", "CONTROL_WELL", "/.()$% (CONTROL_WELL)");

    }

    @Test
    public void testTryCreateWithFullIdentifier()
    {
        assertEquals("ABC (MY_MATERIAL)", MaterialIdentifier.tryCreate("ABC (MY_MATERIAL)", null)
                .toString());
    }

    @Test
    public void testTryCreateWithFullIdentifierAndType()
    {
        MaterialType materialType = new MaterialTypeBuilder().code("MY_MATERIAL").getMaterialType();
        assertEquals("ABC (MY_MATERIAL)",
                MaterialIdentifier.tryCreate("ABC (MY_MATERIAL)", materialType).toString());
    }

    @Test
    public void testTryCreateWithFullIdentifierAndInconsistentType()
    {
        MaterialType materialType = new MaterialTypeBuilder().code("MY_M").getMaterialType();
        try
        {
            MaterialIdentifier.tryCreate("ABC (MY_MATERIAL)", materialType);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Material identified by 'ABC (MY_MATERIAL)' has to be of type MY_M.",
                    ex.getMessage());
        }
    }

    @Test
    public void testTryCreateWithCodeAndType()
    {
        MaterialType materialType = new MaterialTypeBuilder().code("MY_M").getMaterialType();
        assertEquals("ABC (MY_M)", MaterialIdentifier.tryCreate("ABC", materialType).toString());
    }

    @Test
    public void testTryCreateWithTypeAndMissingCode()
    {
        MaterialType materialType = new MaterialTypeBuilder().code("MY_M").getMaterialType();
        assertEquals(null, MaterialIdentifier.tryCreate("", materialType));
    }

    @Test
    public void testTryCreateWithCodeAndMissingType()
    {
        assertEquals(null, MaterialIdentifier.tryCreate("ABC", null));
    }

    void assertCorrectlyParsed(String materialCode, String materialTypeCode, String identifierString)
    {
        MaterialIdentifier identifier = MaterialIdentifier.tryParseIdentifier(identifierString);
        assertEquals(materialCode, identifier.getCode());
        assertEquals(materialTypeCode, identifier.getTypeCode());
    }

}
