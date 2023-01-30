/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.shared.parser;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;

/**
 * @author Franz-Josef Elmer
 */
public class SampleUploadSectionsParserTest
{
    private static final class Generator implements SampleUploadSectionsParser.SampleCodeGenerator
    {
        private int counter;

        @Override
        public List<String> generateCodes(int size)
        {

            List<String> codes = new ArrayList<>(size);
            for (int i = 0; i < size; i++)
            {
                codes.add("S-" + counter++);
            }
            return codes;
        }

    }

    @Test
    public void testGenerateIdentifiersWithDefaultSpaceAndNoProjectSamples()
    {
        // Given
        NewSamplesWithTypes swt1 = new NewSamplesWithTypes();
        swt1.setNewEntities(Arrays.asList(s().defaultSpace("/MY_SPACE"),
                s().experiment("/A/B/C")));
        NewSamplesWithTypes swt2 = new NewSamplesWithTypes();
        swt2.setNewEntities(Arrays.asList(s().defaultSpace("/ABC").experiment("/D/E/F")));

        // When
        SampleUploadSectionsParser.generateIdentifiers(false, "/DEFAULT", new Generator(), true, Arrays.asList(swt1, swt2));

        // Then
        assertEquals(extractIdentifiers(swt1).toString(), "[/MY_SPACE/S-0, /DEFAULT/S-1]");
        assertEquals(extractIdentifiers(swt2).toString(), "[/ABC/S-2]");
    }

    @Test
    public void testGenerateIdentifiersWithNoDefaultSpaceAndNoProjectSamples()
    {
        // Given
        NewSamplesWithTypes swt1 = new NewSamplesWithTypes();
        swt1.setNewEntities(Arrays.asList(s().defaultSpace("/MY_SPACE"),
                s().defaultSpace("/A1").experiment("/A/B/C")));
        NewSamplesWithTypes swt2 = new NewSamplesWithTypes();
        swt2.setNewEntities(Arrays.asList(s().defaultSpace("/ABC").experiment("/D/E/F")));

        // When
        SampleUploadSectionsParser.generateIdentifiers(false, null, new Generator(), true, Arrays.asList(swt1, swt2));

        // Then
        assertEquals(extractIdentifiers(swt1).toString(), "[/MY_SPACE/S-0, /A1/S-1]");
        assertEquals(extractIdentifiers(swt2).toString(), "[/ABC/S-2]");
    }

    @Test
    public void testGenerateIdentifiersWithDefaultSpaceAndProjectSamples()
    {
        // Given
        NewSamplesWithTypes swt1 = new NewSamplesWithTypes();
        swt1.setNewEntities(Arrays.asList(s().defaultSpace("/MY_SPACE"),
                s().experiment("/A/B/C")));
        NewSamplesWithTypes swt2 = new NewSamplesWithTypes();
        swt2.setNewEntities(Arrays.asList(s().defaultSpace("/ABC").experiment("/D/E/F")));

        // When
        SampleUploadSectionsParser.generateIdentifiers(true, "/DEFAULT", new Generator(), true, Arrays.asList(swt1, swt2));

        // Then
        assertEquals(extractIdentifiers(swt1).toString(), "[/MY_SPACE/S-0, /A/B/S-1]");
        assertEquals(extractIdentifiers(swt2).toString(), "[/D/E/S-2]");
    }

    @Test
    public void testGenerateIdentifiersWithNoDefaultSpaceAndProjectSamples()
    {
        // Given
        NewSamplesWithTypes swt1 = new NewSamplesWithTypes();
        swt1.setNewEntities(Arrays.asList(s().defaultSpace("/MY_SPACE"), s().defaultSpace("/A1").experiment("/A/B/C")));
        NewSamplesWithTypes swt2 = new NewSamplesWithTypes();
        swt2.setNewEntities(Arrays.asList(s().defaultSpace("/ABC").experiment("/D/E/F"),
                s().defaultSpace("/S1").experiment("/C/D/E")));

        // When
        SampleUploadSectionsParser.generateIdentifiers(true, null, new Generator(), true, Arrays.asList(swt1, swt2));

        // Then
        assertEquals(extractIdentifiers(swt1).toString(), "[/MY_SPACE/S-0, /A/B/S-1]");
        assertEquals(extractIdentifiers(swt2).toString(), "[/D/E/S-2, /C/D/S-3]");
    }

    private List<String> extractIdentifiers(NewSamplesWithTypes samplesWithTypes)
    {
        return samplesWithTypes.getNewEntities().stream().map(NewSample::getIdentifier).collect(Collectors.toList());
    }

    private NewSampleBuilder s()
    {
        return new NewSampleBuilder();
    }

    private static final class NewSampleBuilder extends NewSample
    {
        private static final long serialVersionUID = 1L;

        public NewSampleBuilder defaultSpace(String defaultSpaceIdentifier)
        {
            setDefaultSpaceIdentifier(defaultSpaceIdentifier);
            return this;
        }

        public NewSampleBuilder experiment(String experimentIdentifier)
        {
            setExperimentIdentifier(experimentIdentifier);
            return this;
        }
    }

}
