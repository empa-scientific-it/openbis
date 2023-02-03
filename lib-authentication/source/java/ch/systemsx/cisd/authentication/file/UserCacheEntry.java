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

package ch.systemsx.cisd.authentication.file;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.security.PasswordHasher;

/**
 * A class representing a user entry in the password cache file.
 * 
 * @author Bernd Rinn
 */
public class UserCacheEntry extends UserEntry
{
    // For unit tests.
    static final int NUMBER_OF_COLUMNS_IN_PASSWORD_CACHE_FILE = 6;

    private static final int CACHED_AT_IDX = 5;

    private final long cachedAt;

    UserCacheEntry(String passwordFileLine) throws IllegalArgumentException
    {
        super(split(passwordFileLine, NUMBER_OF_COLUMNS_IN_PASSWORD_CACHE_FILE));
        this.cachedAt = Long.parseLong(getElement(CACHED_AT_IDX));
    }

    UserCacheEntry(Principal p, long cachedAt)
    {
        this(p, null, cachedAt);
    }

    UserCacheEntry(Principal p, String passwordOrNull, long cachedAt)
    {
        super(createPasswordFileEntry(p, passwordOrNull, cachedAt));
        this.cachedAt = cachedAt;
    }

    private static String[] createPasswordFileEntry(Principal p, String passwordOrNull,
            long cachedAt)
    {
        assert p != null;

        return new String[]
        {
                p.getUserId(),
                p.getEmail(),
                p.getFirstName(),
                p.getLastName(),
                (p.isAuthenticated() && StringUtils.isNotEmpty(passwordOrNull)) ? PasswordHasher
                        .computeSaltedHash(passwordOrNull)
                        : "",
                Long.toString(cachedAt),
        };
    }

    /**
     * Returns the time stamp when this cache entry was put into the cache.
     */
    long getCachedAt()
    {
        return cachedAt;
    }

    /**
     * @throw {@link UnsupportedOperationException}
     */
    @Override
    void setEmail(String email) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throw {@link UnsupportedOperationException}
     */
    @Override
    void setFirstName(String firstName) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throw {@link UnsupportedOperationException}
     */
    @Override
    void setLastName(String lastName) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throw {@link UnsupportedOperationException}
     */
    @Override
    void setPassword(String plainPassword) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

}
