/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.fasta;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class FastaUtilitiesTest extends AssertJUnit
{

    @Test
    public void testLineWithInvalidCharacter()
    {
        try
        {
            FastaUtilities.determineSequenceType("ABC3DEF");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Invalid symbol '3' in line 'ABC3DEF'.", ex.getMessage());
        }
    }

    @Test
    public void testLineWithExclusiveAminoAcidCodes()
    {
        assertEquals(SequenceType.PROT, FastaUtilities.determineSequenceType("abij"));
    }

    @Test
    public void testLineWithMoreThanFiveDifferentCodes()
    {
        assertEquals(SequenceType.PROT, FastaUtilities.determineSequenceType("ABCDEFG"));
    }

    @Test
    public void testLineWithMoreUAndT()
    {
        assertEquals(SequenceType.PROT, FastaUtilities.determineSequenceType("UT"));
    }

    @Test
    public void testLineWithOnlyNuclCodesWithT()
    {
        assertEquals(SequenceType.NUCL, FastaUtilities.determineSequenceType("AAGGCCTTN"));
    }

    @Test
    public void testLineWithOnlyNuclCodesWithU()
    {
        assertEquals(SequenceType.NUCL, FastaUtilities.determineSequenceType("aagcunnau"));
    }

    @Test
    public void testLineWithSpacesInNuclCodes()
    {
        assertEquals(SequenceType.NUCL, FastaUtilities.determineSequenceType("3'-aag ttg cca-5'"));
    }

    @Test
    public void testLineWithSpacesInProtCodes()
    {
        assertEquals(SequenceType.PROT, FastaUtilities.determineSequenceType("TLI IGGBC"));
    }
    
    @Test
    public void testDetermineSequenceTypeOrNullForPureU()
    {
        assertEquals(SequenceType.NUCL, FastaUtilities.determineSequenceTypeOrNull("UUU"));
    }
    
    @Test
    public void testDetermineSequenceTypeOrNullForPureNuclSequence()
    {
        assertEquals(SequenceType.NUCL, FastaUtilities.determineSequenceTypeOrNull("GATTACA"));
    }
    
    @Test
    public void testDetermineSequenceTypeOrNullForProtSequence()
    {
        assertEquals(SequenceType.PROT, FastaUtilities.determineSequenceTypeOrNull("ACE"));
    }
    
    @Test
    public void testDetermineSequenceTypeOrNullForMixOfPureNuclCharactersAndPureAminoCharacters()
    {
        assertEquals(null, FastaUtilities.determineSequenceTypeOrNull("UV"));
    }
    
    @Test
    public void testDetermineSequenceTypeOrNullForUnknownCharacter()
    {
        assertEquals(null, FastaUtilities.determineSequenceTypeOrNull("ABCZ"));
    }

}
