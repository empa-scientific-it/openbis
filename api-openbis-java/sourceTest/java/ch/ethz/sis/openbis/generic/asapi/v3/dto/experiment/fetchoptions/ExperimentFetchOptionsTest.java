/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentFetchOptionsTest
{

    @Test
    public void testToString()
    {
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        assertEquals(fetchOptions.toString(), "Experiment\n");

        fetchOptions.withType();
        assertEquals(fetchOptions.toString(), "Experiment\n"
                + "    with Type\n");

        fetchOptions.withType().withPropertyAssignments();
        assertEquals(fetchOptions.toString(), "Experiment\n"
                + "    with Type\n"
                + "        with PropertyAssignments\n");

        fetchOptions.withType().withPropertyAssignments().withPropertyType().withVocabulary();
        assertEquals(fetchOptions.toString(), "Experiment\n"
                + "    with Type\n"
                + "        with PropertyAssignments\n"
                + "            with PropertyType\n"
                + "                with Vocabulary\n");
    }

}
