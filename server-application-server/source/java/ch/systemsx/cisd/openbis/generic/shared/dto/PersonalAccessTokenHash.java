/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.authentication.SessionTokenHash;

public class PersonalAccessTokenHash
{

    // The prefix needs to contain at least one character that is disallowed in a username.
    // Thanks to that we can distinguish a pat token from a regular session token.
    private static final String PREFIX = "$pat-";

    private final String hash;

    private PersonalAccessTokenHash(String hash)
    {
        this.hash = hash;
    }

    public static PersonalAccessTokenHash create(String user, long timestamp)
    {
        return new PersonalAccessTokenHash(PREFIX + SessionTokenHash.create(user, timestamp));
    }

    public static boolean isValid(String hashOrNull)
    {
        if (hashOrNull == null || !hashOrNull.startsWith(PREFIX))
        {
            return false;
        }

        return SessionTokenHash.isValid(hashOrNull.substring(PREFIX.length()));
    }

    @Override public String toString()
    {
        return hash;
    }
}
