/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Jakub Straszewski
 */
@Test(groups = "slow")
public class VocabularyDropboxApiSystemTest extends SystemTestCase
{
    // for jython script locate
    // vocabularies-api.py

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-vocabularies-api-test");
    }

    @Test
    public void testControlledVocabulariesApi() throws Exception
    {
        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        runDropbox();

        Sample sample =
                openBISService.tryGetSampleWithExperiment(SampleIdentifier.create("VOC",
                        "CELL_PLATE"));

        IEntityProperty property = EntityHelper.tryFindProperty(sample.getProperties(), "ORGANISM");

        assertEquals("RAT", property.getVocabularyTerm().getCode());

        Vocabulary vocabulary = openBISService.tryGetVocabulary("TEST_VOCABULARY");

        assertNotNull(vocabulary);
        assertEquals("modified description", vocabulary.getDescription());
        assertEquals(false, vocabulary.isManagedInternally());
        assertEquals(false, vocabulary.isChosenFromList());
        assertEquals("localuri", vocabulary.getURLTemplate());

        assertSampleForVocabularyTermExists(openBISService, "TEST_TERM_A");
        assertSampleForVocabularyTermExists(openBISService, "TEST_TERM_B");

        VocabularyTerm term = assertSampleForVocabularyTermExists(openBISService, "NEW_TERM");

        assertEquals(Long.valueOf(3), term.getOrdinal());
        assertEquals("new description", term.getDescription());
        assertEquals("new label", term.getLabel());

        assertEquals(3, vocabulary.getTerms().size());

    }

    private VocabularyTerm assertSampleForVocabularyTermExists(
            IEncapsulatedOpenBISService openBISService, String term)
    {
        Sample sample =
                openBISService.tryGetSampleWithExperiment(SampleIdentifier.create("VOC", "NORMAL_"
                        + term));

        IEntityProperty property =
                EntityHelper.tryFindProperty(sample.getProperties(), "TEST_VOCABULARY");

        assertNotNull("The property of type TEST_VOCABULARY (" + term
                + ") is null. All properties : " + sample.getProperties(), property);

        assertNotNull("The property term (" + term + ")should not be null. Property: " + property,
                property.getVocabularyTerm());

        assertEquals(term, property.getVocabularyTerm().getCode());
        return property.getVocabularyTerm();
    }

    private void runDropbox() throws IOException, Exception
    {
        File exampleDataSet = new File(workingDirectory, "my-data");
        createExampleDataSet(exampleDataSet);
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported();
    }

    private void createExampleDataSet(File exampleDataSet)
    {
        exampleDataSet.mkdirs();
        FileUtilities.writeToFile(new File(exampleDataSet, "file.txt"), "hello world");
    }

}
