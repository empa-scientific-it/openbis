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

package ch.systemsx.cisd.authentication.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * A class to read and write {@link UserEntry}.
 * 
 * @author Bernd Rinn
 */
final class LineBasedUserStore<T extends UserEntry> implements IUserStore<T>
{
    private final ILineStore lineStore;

    private final IUserEntryFactory<T> userEntryFactory;

    private Map<String, T> idToEntryMap;

    private Map<String, T> emailToEntryMap;

    interface IUserEntryFactory<T extends UserEntry>
    {
        /** Creates a new user entry from a line in the line-based store. */
        T create(String line);
    }

    LineBasedUserStore(final ILineStore lineStore, final IUserEntryFactory<T> userEntryFactory)
    {
        this.lineStore = lineStore;
        this.userEntryFactory = userEntryFactory;
        this.idToEntryMap = new LinkedHashMap<String, T>();
        this.emailToEntryMap = new LinkedHashMap<String, T>();
    }

    private synchronized Map<String, T> getIdToEntryMap()
    {
        return idToEntryMap;
    }

    private synchronized Map<String, T> getEmailToEntryMap()
    {
        return emailToEntryMap;
    }

    synchronized void setEntryMaps(Map<String, T> idToEntryMap,
            Map<String, T> emailToEntryMap)
    {
        this.idToEntryMap = idToEntryMap;
        this.emailToEntryMap = emailToEntryMap;
    }

    private void updateMaps()
    {
        if (lineStore.hasChanged())
        {
            final Map<String, T> newIdToEntryMap = new LinkedHashMap<String, T>();
            final Map<String, T> newEmailToEntryMap =
                    new LinkedHashMap<String, T>();
            for (String line : lineStore.readLines())
            {
                final T entry = userEntryFactory.create(line);
                newIdToEntryMap.put(entry.getUserId(), entry);
                if (StringUtils.isNotBlank(entry.getEmail()))
                {
                    if (newEmailToEntryMap.put(entry.getEmail().toLowerCase(), entry) != null)
                    {
                        // Multiple users with the same email
                        emailToEntryMap.remove(entry.getEmail().toLowerCase());
                    }
                }
            }
            setEntryMaps(newIdToEntryMap, newEmailToEntryMap);
        }
    }

    private List<String> asPasswordLines()
    {
        final Collection<T> users = getIdToEntryMap().values();
        final List<String> lines = new ArrayList<String>(users.size());
        for (T user : users)
        {
            lines.add(user.asPasswordLine());
        }
        return lines;
    }

    @Override
    public String getId()
    {
        return lineStore.getId();
    }

    @Override
    public T tryGetUserById(String user)
    {
        updateMaps();
        return getIdToEntryMap().get(user);
    }

    @Override
    public T tryGetUserByEmail(String email) throws EnvironmentFailureException
    {
        updateMaps();
        return getEmailToEntryMap().get(email.toLowerCase());
    }

    @Override
    public UserEntryAuthenticationState<T> tryGetAndAuthenticateUserById(String userId,
            String password)
            throws EnvironmentFailureException
    {
        final T entry = tryGetUserById(userId);
        if (entry == null)
        {
            return null;
        }
        final boolean authenticated = isPasswordCorrect(userId, password);
        return new UserEntryAuthenticationState<T>(entry, authenticated);
    }

    @Override
    public UserEntryAuthenticationState<T> tryGetAndAuthenticateUserByEmail(String email,
            String password) throws EnvironmentFailureException
    {
        final T entry = tryGetUserByEmail(email);
        if (entry == null)
        {
            return null;
        }
        final boolean authenticated = isPasswordCorrect(entry.getUserId(), password);
        return new UserEntryAuthenticationState<T>(entry, authenticated);
    }

    @Override
    public synchronized void addOrUpdateUser(T user)
    {
        assert user != null;

        updateMaps();
        final T oldUserOrNull = idToEntryMap.put(user.getUserId(), user);
        if (oldUserOrNull != null && StringUtils.isNotBlank(oldUserOrNull.getEmail()))
        {
            emailToEntryMap.remove(oldUserOrNull.getEmail().toLowerCase());
        }
        if (StringUtils.isNotBlank(user.getEmail()))
        {
            if (emailToEntryMap.put(user.getEmail().toLowerCase(), user) != null)
            {
                // Multiple users with the same email
                emailToEntryMap.remove(user.getEmail().toLowerCase());
            }
        }
        lineStore.writeLines(asPasswordLines());
    }

    @Override
    public synchronized boolean removeUser(String userId)
    {
        assert userId != null;

        updateMaps();
        final UserEntry oldEntryOrNull = idToEntryMap.remove(userId);
        if (oldEntryOrNull != null)
        {
            if (StringUtils.isNotBlank(oldEntryOrNull.getEmail()))
            {
                emailToEntryMap.remove(oldEntryOrNull.getEmail().toLowerCase());
            }
            lineStore.writeLines(asPasswordLines());
            return true;
        }
        return false;
    }

    @Override
    public boolean isPasswordCorrect(String user, String password)
    {
        assert user != null;

        final UserEntry userEntryOrNull = tryGetUserById(user);
        if (userEntryOrNull == null)
        {
            return false;
        }
        return userEntryOrNull.isPasswordCorrect(password);
    }

    @Override
    public List<T> listUsers()
    {
        updateMaps();
        return new ArrayList<T>(getIdToEntryMap().values());
    }

    /**
     * Checks whether this store is operational.
     * 
     * @throws ConfigurationFailureException If the store is not operational.
     */
    @Override
    public void check() throws ConfigurationFailureException
    {
        lineStore.check();
    }

    @Override
    public boolean isRemote()
    {
        return false;
    }

}
