/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Test cases for corresponding {@link VocabularyDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
{ "db" })
@Friend(toClasses = VocabularyPE.class)
public final class VocabularyDAOTest extends AbstractDAOTest
{
    private final VocabularyTermPE createVocabularyTerm(final String code, final int ordinal)
    {
        final VocabularyTermPE vocabularyTermPE = new VocabularyTermPE();
        vocabularyTermPE.setRegistrator(getSystemPerson());
        vocabularyTermPE.setCode(code);
        vocabularyTermPE.setOrdinal(Long.valueOf(ordinal));
        vocabularyTermPE.setOfficial(true);
        return vocabularyTermPE;
    }

    @Test
    public final void testTryFindVocabularyByCode()
    {
        final IVocabularyDAO vocabularyDAO = daoFactory.getVocabularyDAO();
        boolean fail = true;
        try
        {
            vocabularyDAO.tryFindVocabularyByCode(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertNull(vocabularyDAO.tryFindVocabularyByCode("DOES_NOT_EXIST"));
        final VocabularyPE vocabulary = vocabularyDAO.tryFindVocabularyByCode("$PLATE_GEOMETRY");
        assertNotNull(vocabulary);
        assertEquals(3, vocabulary.getTerms().size());
    }

    @Test
    public final void testCreateVocabulary()
    {
        final IVocabularyDAO vocabularyDAO = daoFactory.getVocabularyDAO();
        boolean fail = true;
        try
        {
            vocabularyDAO.createOrUpdateVocabulary(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        final PersonPE registrator = getSystemPerson();
        final VocabularyPE vocabularyPE = new VocabularyPE();
        final String vocabularyCode = "FORMAT";
        vocabularyPE.setCode(vocabularyCode);
        vocabularyPE.setDescription("The format description");
        vocabularyPE.setRegistrator(registrator);
        vocabularyPE.addTerm(createVocabularyTerm("SMALL", 1));
        vocabularyPE.addTerm(createVocabularyTerm("MEDIUM", 2));
        vocabularyPE.addTerm(createVocabularyTerm("BIG:.-_-", 3));
        vocabularyDAO.createOrUpdateVocabulary(vocabularyPE);
        // Check saved vocabulary.
        final VocabularyPE savedVocabulary = vocabularyDAO.tryFindVocabularyByCode(vocabularyCode);
        assertNotNull(savedVocabulary);
        assertNotNull(savedVocabulary.getDescription());
        assertEquals(3, savedVocabulary.getTerms().size());
    }

    @Test
    public final void testFindVocabularyTermByCode()
    {
        final IVocabularyDAO vocabularyDAO = daoFactory.getVocabularyDAO();
        final String vocabularyCode = "HUMAN";
        final String realTermCode = "MAN";
        final String fakeTermCode = "DOG";
        final VocabularyPE vocabularyPE = vocabularyDAO.tryFindVocabularyByCode(vocabularyCode);

        testFindVocabularyTermByCodeAssertions(vocabularyDAO, null, realTermCode);
        testFindVocabularyTermByCodeAssertions(vocabularyDAO, vocabularyPE, null);

        assertNull(vocabularyDAO.tryFindVocabularyTermByCode(vocabularyPE, fakeTermCode));

        VocabularyTermPE termPE =
                vocabularyDAO.tryFindVocabularyTermByCode(vocabularyPE, realTermCode);
        assertNotNull(termPE);
        VocabularyTermPE realTermPE = createVocabularyTerm(realTermCode, 8);
        realTermPE.setVocabulary(vocabularyPE);
        assertEquals(termPE, realTermPE);
    }

    private final void testFindVocabularyTermByCodeAssertions(IVocabularyDAO vocabularyDAO,
            VocabularyPE vocabulary, String code)
    {
        boolean fail = true;
        try
        {
            vocabularyDAO.tryFindVocabularyTermByCode(vocabulary, code);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testListVocabularies()
    {
        final IVocabularyDAO vocabularyDAO = daoFactory.getVocabularyDAO();
        final List<VocabularyPE> vocabularies = vocabularyDAO.listVocabularies(true);
        assertEquals(4, vocabularies.size());
        final VocabularyPE vocabularyPE = vocabularies.get(0);
        assertFalse(HibernateUtils.isInitialized(vocabularyPE.getTerms()));
    }

    @Test
    public final void testListAllVocabularies()
    {
        final IVocabularyDAO vocabularyDAO = daoFactory.getVocabularyDAO();
        final List<VocabularyPE> vocabularies = vocabularyDAO.listVocabularies(false);
        assertEquals(6, vocabularies.size());
        final VocabularyPE vocabularyPE = vocabularies.get(0);
        assertFalse(HibernateUtils.isInitialized(vocabularyPE.getTerms()));
    }

    @Test
    public final void testDeleteWithVocabularyTerms()
    {
        final IVocabularyDAO vocabularyDAO = daoFactory.getVocabularyDAO();
        final VocabularyPE deletedVocabulary = findVocabulary("HUMAN");

        // Deleted vocabulary should have all collections which prevent it from deletion empty.
        assertTrue(deletedVocabulary.getPropertyTypes().isEmpty());

        // delete
        vocabularyDAO.delete(deletedVocabulary);

        // test successful deletion of vocabulary
        assertNull(vocabularyDAO.tryGetByTechId(TechId.create(deletedVocabulary)));

        // test successful deletion of vocabulary terms
        assertFalse(deletedVocabulary.getTerms().isEmpty());
        for (VocabularyTermPE term : deletedVocabulary.getTerms())
        {
            assertNull(vocabularyDAO.tryFindVocabularyTermByCode(deletedVocabulary, term.getCode()));
        }
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFail()
    {
        final IVocabularyDAO vocabularyDAO = daoFactory.getVocabularyDAO();
        final VocabularyPE deletedVocabulary = findVocabulary("ORGANISM");

        // Deleted project should have property types which prevent it from deletion.
        assertFalse(deletedVocabulary.getPropertyTypes().isEmpty());

        // delete
        vocabularyDAO.delete(deletedVocabulary);
    }

    private final VocabularyPE findVocabulary(String code)
    {
        final IVocabularyDAO vocablaryDAO = daoFactory.getVocabularyDAO();
        final VocabularyPE vocabulary = vocablaryDAO.tryFindVocabularyByCode(code);
        assertNotNull(vocabulary);

        return vocabulary;
    }
}
