/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionAdd;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionRemove;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.create.WebAppSettingCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.update.WebAppSettingsUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.UpdateRelationProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.roleassignment.RoleAssignmentUtils;
import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.DisplaySettingsProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.IDisplaySettingsUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateWebAppSettingsExecutor implements IUpdateWebAppSettingsExecutor
{

    @Autowired
    private DisplaySettingsProvider displaySettingsProvider;

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    public void update(IOperationContext context, MapBatch<PersonUpdate, PersonPE> batch)
    {
        new MapBatchProcessor<PersonUpdate, PersonPE>(context, batch)
            {
                @Override
                public void process(PersonUpdate update, PersonPE person)
                {
                    Map<String, WebAppSettingsUpdateValue> webAppSettingsMap = update.getWebAppSettings();

                    if (webAppSettingsMap != null)
                    {
                        for (Map.Entry<String, WebAppSettingsUpdateValue> entry : webAppSettingsMap.entrySet())
                        {
                            update(context, person, entry.getKey(), entry.getValue());
                        }
                    }
                }

                @Override
                public IProgress createProgress(PersonUpdate update, PersonPE person, int objectIndex, int totalObjectCount)
                {
                    return new UpdateRelationProgress(update, person, "person-web-app-settings", objectIndex, totalObjectCount);
                }
            };
    }

    private void update(IOperationContext context, PersonPE person, String webAppId, WebAppSettingsUpdateValue webAppSettingsUpdate)
    {
        if (webAppId == null)
        {
            throw new UserFailureException("Web app id cannot be null");
        }

        if (webAppSettingsUpdate == null)
        {
            throw new UserFailureException("Web app settings update cannot be null");
        }

        if (webAppSettingsUpdate.getActions() != null && false == webAppSettingsUpdate.getActions().isEmpty())
        {
            PersonPE loggedInPerson = context.getSession().tryGetPerson();

            if (false == person.equals(loggedInPerson) && false == RoleAssignmentUtils.isInstanceAdmin(loggedInPerson))
            {
                throw new UnauthorizedObjectAccessException(new PersonPermId(person.getUserId()));
            }

            updateDisplaySettings(person, new IDisplaySettingsUpdate()
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public DisplaySettings update(DisplaySettings displaySettings)
                    {
                        remove(context, displaySettings, webAppId, webAppSettingsUpdate);
                        add(context, displaySettings, webAppId, webAppSettingsUpdate);
                        set(context, displaySettings, webAppId, webAppSettingsUpdate);
                        return displaySettings;
                    }
                });
        }
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    private void remove(IOperationContext context, DisplaySettings displaySettings, String webAppId,
            WebAppSettingsUpdateValue webAppSettingsUpdate)
    {
        Map<String, String> webAppSettings = displaySettings.getCustomWebAppSettings(webAppId);

        for (ListUpdateAction<?> action : webAppSettingsUpdate.getActions())
        {
            if (action instanceof ListUpdateActionRemove<?>)
            {
                Collection<String> names = (Collection<String>) action.getItems();
                if (names != null)
                {
                    for (String name : names)
                    {
                        if (name == null)
                        {
                            throw new UserFailureException("Web app setting name cannot be null");
                        }
                        webAppSettings.remove(name);
                    }
                }
            }
        }

        if (webAppSettings.isEmpty())
        {
            displaySettings.removeCustomWebAppSettings(webAppId);
        }
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    private void add(IOperationContext context, DisplaySettings displaySettings, String webAppId, WebAppSettingsUpdateValue webAppSettingsUpdate)
    {
        Map<String, String> webAppSettings = displaySettings.getCustomWebAppSettings(webAppId);

        for (ListUpdateAction<?> action : webAppSettingsUpdate.getActions())
        {
            if (action instanceof ListUpdateActionAdd<?>)
            {
                Collection<WebAppSettingCreation> creations = (Collection<WebAppSettingCreation>) action.getItems();
                if (creations != null)
                {
                    for (WebAppSettingCreation creation : creations)
                    {
                        if (creation != null)
                        {
                            if (creation.getName() == null)
                            {
                                throw new UserFailureException("Web app setting name cannot be null");
                            }

                            webAppSettings.put(creation.getName(), creation.getValue());
                        }
                    }
                }
            }
        }

        if (webAppSettings.isEmpty())
        {
            displaySettings.removeCustomWebAppSettings(webAppId);
        }
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    private void set(IOperationContext context, DisplaySettings displaySettings, String webAppId, WebAppSettingsUpdateValue webAppSettingsUpdate)
    {
        ListUpdateActionSet<WebAppSettingCreation> lastSet = null;

        for (ListUpdateAction<?> action : webAppSettingsUpdate.getActions())
        {
            if (action instanceof ListUpdateActionSet<?>)
            {
                lastSet = (ListUpdateActionSet<WebAppSettingCreation>) action;
            }
        }

        if (lastSet != null)
        {
            Map<String, String> settings = new HashMap<String, String>();

            if (lastSet.getItems() != null)
            {
                for (WebAppSettingCreation creation : lastSet.getItems())
                {
                    if (creation != null)
                    {
                        if (creation.getName() == null)
                        {
                            throw new UserFailureException("Web app setting name cannot be null");
                        }

                        settings.put(creation.getName(), creation.getValue());
                    }
                }
            }

            if (settings.isEmpty())
            {
                displaySettings.removeCustomWebAppSettings(webAppId);
            } else
            {
                displaySettings.setCustomWebAppSettings(webAppId, settings);
            }
        }
    }

    private void updateDisplaySettings(final PersonPE person, final IDisplaySettingsUpdate displaySettingsUpdate)
    {
        org.hibernate.Session hibernateSession = daoFactory.getSessionFactory().getCurrentSession();
        PersonPE attachedPerson = (PersonPE) hibernateSession.get(PersonPE.class, person.getId());

        daoFactory.getPersonDAO().lock(attachedPerson);
        displaySettingsProvider.executeActionWithPersonLock(attachedPerson, new IDelegatedActionWithResult<Void>()
            {
                @Override
                public Void execute(boolean didOperationSucceed)
                {
                    DisplaySettings currentDisplaySettings = displaySettingsProvider.getCurrentDisplaySettings(attachedPerson);
                    DisplaySettings newDisplaySettings = displaySettingsUpdate.update(currentDisplaySettings);
                    displaySettingsProvider.replaceCurrentDisplaySettings(attachedPerson, newDisplaySettings);
                    daoFactory.getPersonDAO().updatePerson(attachedPerson);
                    return null;
                }
            });
    }

}
