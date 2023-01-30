package ch.systemsx.cisd.openbis.uitest.suite.main;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

public class VocabularyTest extends MainSuite
{

    @Test
    public void newVocabularyIsListedInVocabularyBrowser() throws Exception
    {
        Vocabulary vocabulary = create(aVocabulary());

        assertThat(browserEntryOf(vocabulary), exists());
    }

}
