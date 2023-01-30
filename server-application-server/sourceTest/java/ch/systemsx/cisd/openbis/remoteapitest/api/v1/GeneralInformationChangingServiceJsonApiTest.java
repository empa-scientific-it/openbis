/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.remoteapitest.api.v1;

import static org.testng.AssertJUnit.assertEquals;

import java.net.MalformedURLException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.remoteapitest.RemoteApiTestCase;

/**
 * Verifies that an instance of {@link IGeneralInformationService} is published via JSON-RPC and that it is correctly functioning with external
 * clients.
 * 
 * @author Kaloyan Enimanev
 */
@Test(groups = { "remote api" }, dependsOnGroups = { "before remote api" })
public class GeneralInformationChangingServiceJsonApiTest extends RemoteApiTestCase
{
    protected IGeneralInformationService generalInfoService;

    protected IGeneralInformationChangingService generalInfoChangingService;

    protected String sessionToken;

    @BeforeMethod
    public void beforeMethod() throws MalformedURLException
    {
        generalInfoService = TestJsonServiceFactory.createGeneralInfoService();
        generalInfoChangingService = TestJsonServiceFactory.createGeneralInfoChangingService();
        sessionToken = generalInfoService.tryToAuthenticateForAllServices("test", "a");
    }

    @AfterMethod
    public void afterMethod() throws MalformedURLException
    {
        generalInfoService.logout(sessionToken);
    }

    @Test
    public void testUpdateSampleProperties()
    {
    }

    @Test
    public void testAddUnofficialTerm()
    {
        String vocabularyCode = "ORGANISM";
        Vocabulary vocabulary = fetchVocabularyFromServer(vocabularyCode);

        assertEquals("Vocabulary[ORGANISM,{VocabularyTerm[RAT,RAT],VocabularyTerm[DOG,DOG],"
                + "VocabularyTerm[HUMAN,HUMAN],VocabularyTerm[GORILLA,GORILLA],VocabularyTerm[FLY,FLY]}]",
                vocabulary.toString());

        NewVocabularyTerm newTerm = new NewVocabularyTerm();
        newTerm.setCode("ALIEN");
        newTerm.setLabel("Alien species");
        newTerm.setDescription("Extraterrestrial form of life.");
        newTerm.setPreviousTermOrdinal(0L);

        generalInfoChangingService.addUnofficialVocabularyTerm(sessionToken, vocabulary.getId(),
                newTerm);

        Vocabulary updatedVocabulary = fetchVocabularyFromServer(vocabularyCode);

        assertEquals("Vocabulary[ORGANISM,{VocabularyTerm[RAT,RAT],VocabularyTerm[DOG,DOG],"
                + "VocabularyTerm[HUMAN,HUMAN],VocabularyTerm[GORILLA,GORILLA],"
                + "VocabularyTerm[FLY,FLY],VocabularyTerm[ALIEN,Alien species]}]",
                updatedVocabulary.toString());
    }

    private Vocabulary fetchVocabularyFromServer(String vocabularyCode)
    {
        for (Vocabulary vocabulary : generalInfoService.listVocabularies(sessionToken))
        {
            if (vocabulary.getCode().equals(vocabularyCode))
            {
                return vocabulary;
            }
        }
        return null;
    }

}
