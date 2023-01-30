/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import junit.framework.TestCase;

import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class VocabularyTest extends TestCase
{
    @Test
    public void testAddTermsWithOrdinals()
    {
        Vocabulary vocabulary = new Vocabulary("v");
        VocabularyTerm term1 = new VocabularyTerm("a");
        term1.setOrdinal(5L);
        vocabulary.addTerm(term1);
        VocabularyTerm term2 = new VocabularyTerm("b");
        term2.setOrdinal(3L);
        vocabulary.addTerm(term2);

        assertEquals("[b, a]", vocabulary.getVocabulary().getTerms().toString());
    }

    @Test
    public void testAddTermsWithSameOrdinals()
    {
        Vocabulary vocabulary = new Vocabulary("v");
        VocabularyTerm term1 = new VocabularyTerm("a");
        term1.setOrdinal(5L);
        vocabulary.addTerm(term1);
        VocabularyTerm term2 = new VocabularyTerm("b");
        term2.setOrdinal(5L);
        vocabulary.addTerm(term2);

        assertEquals("[b, a]", vocabulary.getVocabulary().getTerms().toString());
    }

    @Test
    public void testAddTermsWithNoOrdinals()
    {
        Vocabulary vocabulary = new Vocabulary("v");
        VocabularyTerm term1 = new VocabularyTerm("a");
        vocabulary.addTerm(term1);
        VocabularyTerm term2 = new VocabularyTerm("b");
        vocabulary.addTerm(term2);

        assertEquals("[a, b]", vocabulary.getVocabulary().getTerms().toString());
    }

    @Test
    public void testAddTermsWithAndWithoutOrdinals()
    {
        Vocabulary vocabulary = new Vocabulary("v");
        VocabularyTerm term1 = new VocabularyTerm("a");
        vocabulary.addTerm(term1);
        VocabularyTerm term2 = new VocabularyTerm("b");
        term2.setOrdinal(3L);
        vocabulary.addTerm(term2);
        VocabularyTerm term3 = new VocabularyTerm("c");
        vocabulary.addTerm(term3);
        VocabularyTerm term4 = new VocabularyTerm("d");
        term4.setOrdinal(1L);
        vocabulary.addTerm(term4);

        assertEquals("[d, b, a, c]", vocabulary.getVocabulary().getTerms().toString());
    }
}
