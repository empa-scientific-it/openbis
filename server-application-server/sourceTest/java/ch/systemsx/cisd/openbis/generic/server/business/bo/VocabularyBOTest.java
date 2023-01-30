/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link VocabularyBO} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = VocabularyBO.class)
public final class VocabularyBOTest extends AbstractBOTest
{
    private static final String DESC_D = "desc_d";

    private static final String DESC_C = "desc_c";

    private static final String DESC_B = "desc_b";

    private static final String DESC_A = "desc_a";

    private static final String LABEL_C = "label_c";

    private static final String LABEL_D = "label_d";

    private static final String LABEL_B = "label_B";

    private static final String LABEL_A = "label_A";

    private static final String WHITE = "WHITE";

    private static final String YELLOW = "YELLOW";

    private static final String RED = "RED";

    static final String VOCABULARY_CODE = "USER.COLOR";

    static final String VOCABULARY_DESCRIPTION = "Some predefined colors";

    private final VocabularyBO createVocabularyBO()
    {
        return new VocabularyBO(daoFactory, EXAMPLE_SESSION, managedPropertyEvaluatorFactory, null, null);
    }

    private final VocabularyBO createVocabularyBO(VocabularyPE vocabulary)
    {
        return new VocabularyBO(daoFactory, EXAMPLE_SESSION, vocabulary,
                managedPropertyEvaluatorFactory, null, null);
    }

    static final NewVocabulary createVocabulary()
    {
        final NewVocabulary vocabulary = new NewVocabulary();
        vocabulary.setCode(VOCABULARY_CODE);
        vocabulary.setDescription(VOCABULARY_DESCRIPTION);
        vocabulary.setTerms(createTerms());
        return vocabulary;
    }

    final static List<VocabularyTerm> createTerms()
    {
        final List<VocabularyTerm> terms = new ArrayList<VocabularyTerm>();
        terms.add(createVocabularyTerm(RED));
        terms.add(createVocabularyTerm(YELLOW));
        terms.add(createVocabularyTerm(WHITE));
        return terms;
    }

    final static VocabularyTerm createVocabularyTerm(final String code)
    {
        final VocabularyTerm term = new VocabularyTerm();
        term.setCode(code);
        return term;
    }

    static void assertVocabularyEquals(final NewVocabulary vocabulary,
            final VocabularyPE vocabularyPE)
    {
        assertEquals(vocabulary.getCode(), vocabularyPE.getCode());
        assertEquals(vocabulary.getDescription(), vocabularyPE.getDescription());
        final List<VocabularyTerm> terms = vocabulary.getTerms();
        assertNotNull(terms);
        assertEquals(terms.size(), vocabularyPE.getTerms().size());
    }

    @Test
    public final void testWithNull()
    {
        boolean fail = true;
        try
        {
            createVocabularyBO().define(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            createVocabularyBO().save();
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            createVocabularyBO().getVocabulary();
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testDefine()
    {
        final VocabularyBO vocabularyBO = createVocabularyBO();
        final NewVocabulary vocabulary = createVocabulary();
        vocabularyBO.define(vocabulary);
        final VocabularyPE vocabularyPE = vocabularyBO.getVocabulary();
        assertVocabularyEquals(vocabulary, vocabularyPE);
        assertFalse(vocabularyPE.isManagedInternally());
        context.assertIsSatisfied();
    }

    @Test
    public final void testSave()
    {
        final VocabularyBO vocabularyBO = createVocabularyBO();
        final NewVocabulary vocabulary = createVocabulary();
        context.checking(new Expectations()
            {
                {
                    exactly(vocabulary.getTerms().size()).of(vocabularyTermDAO).validate(
                            with(aNonNull(VocabularyTermPE.class)));

                    one(vocabularyDAO).createOrUpdateVocabulary(with(aNonNull(VocabularyPE.class)));
                }
            });
        vocabularyBO.define(vocabulary);
        vocabularyBO.save();
        context.assertIsSatisfied();
    }

    @Test
    public final void testSaveWithException()
    {
        final VocabularyBO vocabularyBO = createVocabularyBO();
        final NewVocabulary vocabulary = createVocabulary();
        context.checking(new Expectations()
            {
                {
                    exactly(vocabulary.getTerms().size()).of(vocabularyTermDAO).validate(
                            with(aNonNull(VocabularyTermPE.class)));

                    one(vocabularyDAO).createOrUpdateVocabulary(with(aNonNull(VocabularyPE.class)));
                    will(throwException(new DataIntegrityViolationException(null)));
                }
            });
        vocabularyBO.define(vocabulary);
        try
        {
            vocabularyBO.save();
            fail(String.format("'%s' expected.", UserFailureException.class.getSimpleName()));
        } catch (final UserFailureException ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testSaveWithTermValidationException()
    {
        final VocabularyBO vocabularyBO = createVocabularyBO();
        final NewVocabulary vocabulary = createVocabulary();
        context.checking(new Expectations()
            {
                {
                    exactly(3).of(vocabularyTermDAO).validate(
                            with(aNonNull(VocabularyTermPE.class)));
                    will(throwException(new DataIntegrityViolationException(null)));
                }
            });
        vocabularyBO.define(vocabulary);
        try
        {
            vocabularyBO.save();
            fail(String.format("'%s' expected.", UserFailureException.class.getSimpleName()));
        } catch (final UserFailureException ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testLoad()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));
                }
            });

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");

        assertSame(vocabulary, vocabularyBO.getVocabulary());
        context.assertIsSatisfied();
    }

    @Test
    public void testAddNewTerms()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        final VocabularyBO vocabularyBO = createVocabularyBO();
        VocabularyTerm t1 = new VocabularyTerm();
        t1.setCode("a");
        VocabularyTerm t2 = new VocabularyTerm();
        t2.setCode("b");
        final List<VocabularyTerm> newTerms = Arrays.asList(t1, t2);
        final Long previousTermPosition = 5L;

        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));

                    one(vocabularyTermDAO).increaseVocabularyTermOrdinals(vocabulary,
                            previousTermPosition + 1, newTerms.size());
                }
            });

        vocabularyBO.load("voc-code");
        vocabularyBO.addNewTerms(newTerms, previousTermPosition);

        List<VocabularyTermPE> terms =
                new ArrayList<VocabularyTermPE>(vocabularyBO.getVocabulary().getTerms());
        Collections.sort(terms);
        assertEquals(2, terms.size());
        VocabularyTermPE term1 = terms.get(0);
        assertEquals("A", term1.getCode());
        assertEquals(Long.valueOf(previousTermPosition + 1), term1.getOrdinal());
        assertSame(EXAMPLE_SESSION.tryGetPerson(), term1.getRegistrator());
        VocabularyTermPE term2 = terms.get(1);
        assertEquals("B", term2.getCode());
        assertEquals(Long.valueOf(previousTermPosition + 2), term2.getOrdinal());
        assertSame(EXAMPLE_SESSION.tryGetPerson(), term2.getRegistrator());
        context.assertIsSatisfied();
    }

    @Test
    public void testAddNewTermsToAnInternallyManaggedVocabulary()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.setManagedInternally(true);
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));

                    one(vocabularyTermDAO).increaseVocabularyTermOrdinals(vocabulary, 1L, 1);
                }
            });

        VocabularyTerm newTerm = new VocabularyTerm();
        newTerm.setCode("test-term");

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        vocabularyBO.addNewTerms(Arrays.asList(newTerm), 0L);

        Set<VocabularyTermPE> terms = vocabularyBO.getVocabulary().getTerms();
        assertEquals(1, terms.size());
        assertEquals("TEST-TERM", terms.iterator().next().getCode());

        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteTermsFromAnInternallyManaggedVocabulary()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.setManagedInternally(true);
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));
                }
            });

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        try
        {
            vocabularyBO.delete(Collections.<VocabularyTerm> emptyList(),
                    Collections.<VocabularyTermReplacement> emptyList());
        } catch (UserFailureException e)
        {
            assertEquals("Not allowed to delete terms from an internally managed vocabulary.",
                    e.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteAllTerms()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        VocabularyTerm term1 = createTerm("1");
        vocabulary.addTerm(translate(term1));
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));
                }
            });

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");

        vocabularyBO.delete(Arrays.asList(term1),
                Collections.<VocabularyTermReplacement> emptyList());

        assertEquals(0, vocabulary.getTerms().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteTermsWithDeletedReplacement()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        VocabularyTerm term1 = createTerm("1");
        VocabularyTerm term2 = createTerm("2");
        VocabularyTerm term3 = createTerm("3");
        vocabulary.addTerm(translate(term1));
        vocabulary.addTerm(translate(term2));
        vocabulary.addTerm(translate(term3));
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));
                }
            });

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        try
        {
            vocabularyBO.delete(Arrays.asList(term1),
                    Arrays.asList(createTermWithReplacement(term2, term1)));
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Invalid vocabulary replacement because of unknown replacement: 2 -> [1]",
                    e.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteTermsWithUnkownReplacement()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        VocabularyTerm term1 = createTerm("1");
        VocabularyTerm term2 = createTerm("2");
        VocabularyTerm term3 = createTerm("3");
        vocabulary.addTerm(translate(term1));
        vocabulary.addTerm(translate(term2));
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));
                }
            });

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        try
        {
            vocabularyBO.delete(Collections.<VocabularyTerm> emptyList(),
                    Arrays.asList(createTermWithReplacement(term1, term3)));
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Invalid vocabulary replacement because of unknown replacement: 1 -> [3]",
                    e.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteTerms()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        VocabularyTerm term1 = createTerm("1");
        VocabularyTerm term2 = createTerm("2");
        vocabulary.addTerm(translate(term1));
        vocabulary.addTerm(translate(term2));
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));
                }
            });

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        vocabularyBO.delete(Arrays.asList(term1),
                Collections.<VocabularyTermReplacement> emptyList());

        assertEquals(1, vocabulary.getTerms().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteAndReplaceTerms()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        VocabularyTerm term1 = createTerm("1");
        final VocabularyTerm term2 = createTerm("2");
        term2.setId(2l);
        VocabularyTerm term3 = createTerm("3");
        vocabulary.addTerm(translate(term1));
        vocabulary.addTerm(translate(term2));
        vocabulary.addTerm(translate(term3));
        final MaterialPropertyPE entityPropertyPE = new MaterialPropertyPE();
        final MaterialPE entity = new MaterialPE();
        entity.addProperty(entityPropertyPE);
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));

                    for (EntityKind entityKind : EntityKind.values())
                    {
                        one(daoFactory).getEntityPropertyTypeDAO(entityKind);
                        will(returnValue(entityPropertyTypeDAO));

                        one(entityPropertyTypeDAO).listPropertiesByVocabularyTerm(term2.getId());
                        List<EntityPropertyPE> properties =
                                Arrays.<EntityPropertyPE> asList(entityPropertyPE);
                        will(returnValue(properties));

                        one(entityPropertyTypeDAO).updateProperties(properties);
                    }
                }
            });

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        vocabularyBO.delete(Arrays.asList(term1),
                Arrays.asList(createTermWithReplacement(term2, term3)));

        assertEquals(term3.getCode(), entityPropertyPE.getVocabularyTerm().getCode());
        assertEquals(1, vocabulary.getTerms().size());
        assertEquals(term3.getCode(), vocabulary.getTerms().iterator().next().getCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateTermsMissing() throws Exception
    {
        VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.setManagedInternally(false);
        String commonCode = YELLOW;
        vocabulary.setTerms(Arrays.asList(createTermPE(RED), createTermPE(commonCode),
                createTermPE(WHITE)));
        VocabularyBO bo = createVocabularyBO(vocabulary);
        VocabularyTermBatchUpdateDetails details = new VocabularyTermBatchUpdateDetails(true, true);
        boolean exceptionThrown = false;
        try
        {
            bo.updateTerms(convertToUpdatedTerms(Arrays.asList(createTerm(commonCode)), details));
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertTrue(ex.getMessage().contains("Missing vocabulary terms"));
            assertTrue(ex.getMessage().contains(RED));
            assertTrue(ex.getMessage().contains(WHITE));
        }

        assertTrue(exceptionThrown);
    }

    @Test
    public void testUpdateTermsInternalVocabulary() throws Exception
    {
        PersonPE systemUser = new PersonPE();
        systemUser.setUserId(PersonPE.SYSTEM_USER_ID);

        VocabularyTermPE term = new VocabularyTermPE();
        term.setRegistrator(systemUser);
        term.setCode("TEST-TERM");

        VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.setManagedInternally(true);
        vocabulary.setTerms(Arrays.asList(term));

        VocabularyBO bo = createVocabularyBO(vocabulary);

        VocabularyTerm termUpdate = new VocabularyTerm();
        termUpdate.setCode(term.getCode());

        try
        {
            bo.updateTerms(Arrays.asList(new UpdatedVocabularyTerm(termUpdate, null)));
            fail();
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "Authorization failure: Terms created by the system user that belong to internal vocabularies can be managed only by the system user.",
                    ex.getMessage());
        }
    }

    @Test
    public void testUpdateTermsAddNew() throws Exception
    {
        VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.setManagedInternally(false);
        vocabulary.setTerms(Arrays.asList(createTermPE(RED, 1)));
        VocabularyBO bo = createVocabularyBO(vocabulary);
        VocabularyTermBatchUpdateDetails details = new VocabularyTermBatchUpdateDetails(true, true);
        bo.updateTerms(convertToUpdatedTerms(
                Arrays.asList(createTerm(RED, 1), createTerm(WHITE, 2), createTerm(YELLOW, 3)),
                details));
        List<VocabularyTermPE> sorted = sortByOrdinal(bo.getVocabulary().getTerms());

        assertEquals(RED, sorted.get(0).getCode());
        assertEquals(WHITE, sorted.get(1).getCode());
        assertEquals(YELLOW, sorted.get(2).getCode());
    }

    @Test
    public void testUpdateTermsChangeOrder() throws Exception
    {
        VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.setManagedInternally(false);
        vocabulary.setTerms(Arrays.asList(createTermPE(RED, 1), createTermPE(WHITE, 2),
                createTermPE(YELLOW, 3)));
        VocabularyBO bo = createVocabularyBO(vocabulary);
        VocabularyTermBatchUpdateDetails details = new VocabularyTermBatchUpdateDetails(true, true);
        bo.updateTerms(convertToUpdatedTerms(
                Arrays.asList(createTerm(WHITE, 1), createTerm(YELLOW, 2), createTerm(RED, 3)),
                details));
        List<VocabularyTermPE> sorted = sortByOrdinal(bo.getVocabulary().getTerms());

        assertEquals(WHITE, sorted.get(0).getCode());
        assertEquals(YELLOW, sorted.get(1).getCode());
        assertEquals(RED, sorted.get(2).getCode());
    }

    @Test
    public void testUpdateTermsChangeLabel() throws Exception
    {
        // change label but leave description untouched
        VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.setManagedInternally(false);
        vocabulary.setTerms(Arrays.asList(
                createTermPEWithLabelAndDescription(RED, LABEL_A, DESC_A, 1),
                createTermPEWithLabelAndDescription(WHITE, LABEL_B, DESC_B, 2)));
        VocabularyBO bo = createVocabularyBO(vocabulary);
        VocabularyTermBatchUpdateDetails details =
                new VocabularyTermBatchUpdateDetails(true, false);
        bo.updateTerms(convertToUpdatedTerms(
                Arrays.asList(createTermWithLabel(RED, LABEL_C, 1),
                        createTermWithLabel(WHITE, LABEL_D, 2)),
                details));
        List<VocabularyTermPE> sorted = sortByOrdinal(bo.getVocabulary().getTerms());

        assertEquals(RED, sorted.get(0).getCode());
        assertEquals(WHITE, sorted.get(1).getCode());

        assertEquals(LABEL_C, sorted.get(0).getLabel());
        assertEquals(LABEL_D, sorted.get(1).getLabel());
        assertEquals(DESC_A, sorted.get(0).getDescription());
        assertEquals(DESC_B, sorted.get(1).getDescription());
    }

    @Test
    public void testUpdateTermsChangeDescription() throws Exception
    {
        // change description but leave label untouched
        VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.setManagedInternally(false);
        vocabulary.setTerms(Arrays.asList(
                createTermPEWithLabelAndDescription(RED, LABEL_A, DESC_A, 1),
                createTermPEWithLabelAndDescription(WHITE, LABEL_B, DESC_B, 2)));
        VocabularyBO bo = createVocabularyBO(vocabulary);
        VocabularyTermBatchUpdateDetails details =
                new VocabularyTermBatchUpdateDetails(false, true);
        bo.updateTerms(convertToUpdatedTerms(
                Arrays.asList(createTermWithDescription(RED, DESC_C, 1),
                        createTermWithDescription(WHITE, DESC_D, 2)),
                details));
        List<VocabularyTermPE> sorted = sortByOrdinal(bo.getVocabulary().getTerms());

        assertEquals(RED, sorted.get(0).getCode());
        assertEquals(WHITE, sorted.get(1).getCode());

        assertEquals(DESC_C, sorted.get(0).getDescription());
        assertEquals(DESC_D, sorted.get(1).getDescription());
        assertEquals(LABEL_A, sorted.get(0).getLabel());
        assertEquals(LABEL_B, sorted.get(1).getLabel());
    }

    private VocabularyTerm createTermWithDescription(String code, String desc, int ordinal)
    {
        VocabularyTerm term = createTerm(code, ordinal);
        term.setDescription(desc);
        return term;
    }

    private VocabularyTerm createTermWithLabel(String code, String label, int ordinal)
    {
        VocabularyTerm term = createTerm(code, ordinal);
        term.setLabel(label);
        return term;
    }

    private VocabularyTermPE createTermPEWithLabelAndDescription(String code, String label,
            String description, int ordinal)
    {
        VocabularyTermPE term = createTermPE(code, ordinal);
        term.setLabel(label);
        term.setDescription(description);
        return term;
    }

    private List<VocabularyTermPE> sortByOrdinal(Set<VocabularyTermPE> terms)
    {
        List<VocabularyTermPE> sorted = new ArrayList<VocabularyTermPE>(terms);
        Collections.sort(sorted, new Comparator<VocabularyTermPE>()
            {
                @Override
                public int compare(VocabularyTermPE o1, VocabularyTermPE o2)
                {
                    return o1.getOrdinal().compareTo(o2.getOrdinal());
                }
            });
        return sorted;
    }

    private VocabularyTermPE createTermPE(String code, int ordinal)
    {
        VocabularyTermPE term = createTermPE(code);
        term.setOrdinal((long) ordinal);
        return term;
    }

    private VocabularyTermPE createTermPE(String code)
    {
        VocabularyTermPE term = new VocabularyTermPE();
        term.setCode(code);
        return term;
    }

    private VocabularyTerm createTerm(String code)
    {
        VocabularyTerm vocabularyTerm = new VocabularyTerm();
        vocabularyTerm.setCode(code);
        return vocabularyTerm;
    }

    private List<VocabularyTerm> convertToUpdatedTerms(List<VocabularyTerm> terms,
            VocabularyTermBatchUpdateDetails batchUpdateDetails)
    {
        List<VocabularyTerm> converted = new ArrayList<VocabularyTerm>();
        for (VocabularyTerm term : terms)
        {
            converted.add(new UpdatedVocabularyTerm(term, batchUpdateDetails));
        }
        return converted;
    }

    private VocabularyTerm createTerm(String code, int ordinal)
    {
        VocabularyTerm vocabularyTerm = createTerm(code);
        vocabularyTerm.setOrdinal((long) ordinal);
        return vocabularyTerm;
    }

    private VocabularyTermReplacement createTermWithReplacement(VocabularyTerm term,
            VocabularyTerm replacement)
    {
        VocabularyTermReplacement vocabularyTermReplacement = new VocabularyTermReplacement();
        vocabularyTermReplacement.setTerm(term);
        vocabularyTermReplacement.setReplacementCode(replacement.getCode());
        return vocabularyTermReplacement;
    }

    private VocabularyTermPE translate(VocabularyTerm term)
    {
        VocabularyTermPE vocabularyTermPE = new VocabularyTermPE();
        vocabularyTermPE.setCode(term.getCode());
        return vocabularyTermPE;
    }
}
