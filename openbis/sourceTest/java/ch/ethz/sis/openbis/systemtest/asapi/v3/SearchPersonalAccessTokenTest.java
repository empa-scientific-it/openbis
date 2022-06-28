/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.PersonalAccessTokenSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.update.PersonalAccessTokenUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class SearchPersonalAccessTokenTest extends AbstractPersonalAccessTokenTest
{

    private PersonalAccessToken token1;

    private PersonalAccessToken token2;

    private PersonalAccessToken token3;

    @BeforeMethod
    private void createTokens()
    {
        PersonalAccessTokenCreation creation1 = testCreation();
        creation1.setSessionName("test session name 1");

        PersonalAccessTokenCreation creation2 = testCreation();
        creation2.setSessionName("test session name 2");

        PersonalAccessTokenCreation creation3 = testCreation();
        creation3.setSessionName("test session name 3");

        token1 = createToken(TEST_USER, PASSWORD, creation1);
        token2 = createToken(TEST_USER, PASSWORD, creation2);
        token3 = createToken(TEST_GROUP_OBSERVER, PASSWORD, creation3);
    }

    @Test
    public void testSearch()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SearchResult<PersonalAccessToken> result =
                v3api.searchPersonalAccessTokens(sessionToken, new PersonalAccessTokenSearchCriteria(), new PersonalAccessTokenFetchOptions());

        assertEquals(result.getObjects().size(), 3);
    }

}
