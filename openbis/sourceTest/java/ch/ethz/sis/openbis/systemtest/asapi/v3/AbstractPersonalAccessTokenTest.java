/*
 * Copyright 2016 ETH Zuerich, CISD
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.update.PersonalAccessTokenUpdate;

/**
 * @author pkupczyk
 */
@Test
public class AbstractPersonalAccessTokenTest extends AbstractTest
{

    protected PersonalAccessTokenCreation testCreation()
    {
        PersonalAccessTokenCreation creation = new PersonalAccessTokenCreation();
        creation.setSessionName("test session");
        creation.setValidFromDate(new Date(System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY));
        creation.setValidToDate(new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY));
        return creation;
    }

    protected PersonalAccessToken createToken(String user, String password, PersonalAccessTokenCreation creation)
    {
        String sessionToken = v3api.login(user, password);

        List<PersonalAccessTokenPermId> ids = v3api.createPersonalAccessTokens(sessionToken, Arrays.asList(creation));
        assertEquals(ids.size(), 1);

        return getToken(user, password, ids.get(0));
    }

    protected PersonalAccessToken updateToken(String user, String password, PersonalAccessTokenUpdate update)
    {
        String sessionToken = v3api.login(user, password);

        v3api.updatePersonalAccessTokens(sessionToken, Arrays.asList(update));

        return getToken(user, password, update.getPersonalAccessTokenId());
    }

    protected PersonalAccessToken getToken(String user, String password, IPersonalAccessTokenId tokenId)
    {
        String sessionToken = v3api.login(user, password);

        PersonalAccessTokenFetchOptions fetchOptions = new PersonalAccessTokenFetchOptions();
        fetchOptions.withOwner();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();

        Map<IPersonalAccessTokenId, PersonalAccessToken> map = v3api.getPersonalAccessTokens(sessionToken, Arrays.asList(tokenId), fetchOptions);

        return map.get(tokenId);
    }

}
