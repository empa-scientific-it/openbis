/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.WebAppSettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A provider for user display settings, independent of the session.
 * 
 * @author Bernd Rinn
 */
public class DisplaySettingsProvider
{
    // user id to display settings map
    private final Map<String, DisplaySettings> displaySettingsMap =
            Collections.synchronizedMap(new HashMap<String, DisplaySettings>());

    // user id to lock map
    private final Map<String, Lock> locksMap = Collections.synchronizedMap(new HashMap<String, Lock>());

    public void addDisplaySettingsForPerson(final PersonPE person)
    {
        executeActionWithPersonLock(person, new IDelegatedActionWithResult<Void>()
            {
                @Override
                public Void execute(boolean didOperationSucceed)
                {
                    DisplaySettings settings = displaySettingsMap.get(person.getUserId());
                    if (settings == null)
                    {
                        settings = person.getDisplaySettings();
                        displaySettingsMap.put(person.getUserId(), settings);
                    }
                    return null;
                }
            });
    }

    public DisplaySettings getCurrentDisplaySettings(final PersonPE person)
    {
        return executeActionWithPersonLock(person, new IDelegatedActionWithResult<DisplaySettings>()
            {
                @Override
                public DisplaySettings execute(boolean didOperationSucceed)
                {
                    DisplaySettings settings = displaySettingsMap.get(person.getUserId());
                    if (settings == null)
                    {
                        settings = person.getDisplaySettings();
                        displaySettingsMap.put(person.getUserId(), settings);
                    }
                    settings = new DisplaySettings(settings);
                    return settings;
                }
            });
    }

    @SuppressWarnings("deprecation")
    public DisplaySettings getRegularDisplaySettings(final PersonPE person)
    {
        return executeActionWithPersonLock(person, new IDelegatedActionWithResult<DisplaySettings>()
            {
                @Override
                public DisplaySettings execute(boolean didOperationSucceed)
                {
                    DisplaySettings settings = displaySettingsMap.get(person.getUserId());
                    if (settings == null)
                    {
                        settings = person.getDisplaySettings();
                        displaySettingsMap.put(person.getUserId(), settings);
                    }
                    settings = new DisplaySettings(settings);
                    settings.clearCustomWebAppSettings();
                    return settings;
                }
            });
    }

    public DisplaySettings replaceCurrentDisplaySettings(final PersonPE person,
            final DisplaySettings settings)
    {
        return executeActionWithPersonLock(person, new IDelegatedActionWithResult<DisplaySettings>()
            {
                @Override
                public DisplaySettings execute(boolean didOperationSucceed)
                {
                    displaySettingsMap.put(person.getUserId(), settings);
                    person.setDisplaySettings(settings);
                    return settings;
                }
            });
    }

    @SuppressWarnings("deprecation")
    public DisplaySettings replaceRegularDisplaySettings(final PersonPE person,
            final DisplaySettings settings)
    {
        return executeActionWithPersonLock(person, new IDelegatedActionWithResult<DisplaySettings>()
            {
                @Override
                public DisplaySettings execute(boolean didOperationSucceed)
                {
                    final DisplaySettings oldSettings = displaySettingsMap.get(person.getUserId());
                    if (oldSettings != null)
                    {
                        settings.overwriteCustomWebAppSettings(oldSettings);
                        settings.overwriteColumnSettings(oldSettings);
                    }
                    displaySettingsMap.put(person.getUserId(), settings);
                    person.setDisplaySettings(settings);
                    return settings;
                }
            });
    }

    public Collection<String> getWebAppIds(final PersonPE person)
    {
        return executeActionWithPersonLock(person, new IDelegatedActionWithResult<Collection<String>>()
            {
                @Override
                public Collection<String> execute(boolean didOperationSucceed)
                {
                    DisplaySettings settings = displaySettingsMap.get(person.getUserId());
                    if (settings == null)
                    {
                        settings = person.getDisplaySettings();
                        displaySettingsMap.put(person.getUserId(), settings);
                    }
                    return settings.getCustomWebAppIds();
                }
            });
    }

    @SuppressWarnings("deprecation")
    public WebAppSettings getWebAppSettings(final PersonPE person, final String webAppId)
    {
        return executeActionWithPersonLock(person, new IDelegatedActionWithResult<WebAppSettings>()
            {
                @Override
                public WebAppSettings execute(boolean didOperationSucceed)
                {
                    DisplaySettings settings = displaySettingsMap.get(person.getUserId());
                    if (settings == null)
                    {
                        settings = person.getDisplaySettings();
                        displaySettingsMap.put(person.getUserId(), settings);
                    }
                    return new WebAppSettings(webAppId, settings.getCustomWebAppSettings(webAppId));
                }
            });
    }

    @SuppressWarnings("deprecation")
    public DisplaySettings replaceWebAppSettings(final PersonPE person,
            final WebAppSettings webAppSettings)
    {
        return executeActionWithPersonLock(person, new IDelegatedActionWithResult<DisplaySettings>()
            {
                @Override
                public DisplaySettings execute(boolean didOperationSucceed)
                {
                    DisplaySettings settings = displaySettingsMap.get(person.getUserId());
                    if (settings == null)
                    {
                        settings = person.getDisplaySettings();
                        displaySettingsMap.put(person.getUserId(), settings);
                    }
                    settings.setCustomWebAppSettings(webAppSettings.getWebAppId(), webAppSettings.getSettings());
                    person.setDisplaySettings(settings);
                    return settings;
                }
            });
    }

    public <T> T executeActionWithPersonLock(String userId, IDelegatedActionWithResult<T> action){
        Lock lock = getPersonLock(userId);
        lock.lock();
        try
        {
            return action.execute(true);
        } finally
        {
            lock.unlock();
        }
    }

    public <T> T executeActionWithPersonLock(PersonPE person, IDelegatedActionWithResult<T> action)
    {
        return executeActionWithPersonLock(person.getUserId(), action);
    }

    private synchronized Lock getPersonLock(String userId)
    {
        Lock lock = locksMap.get(userId);
        if (lock == null)
        {
            lock = new ReentrantLock();
            locksMap.put(userId, lock);
        }
        return lock;
    }

}
