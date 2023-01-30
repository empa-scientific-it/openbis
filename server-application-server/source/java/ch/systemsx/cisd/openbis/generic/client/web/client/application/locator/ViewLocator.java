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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;

/**
 * A view locator represents the information necessary to open a view including populating it any parameters. The concept is similar to a URL
 * (Universal Resource Locator), but made specific to view in openBIS.
 * <p>
 * The view locator may be initialized from URL-encoded parameters. One parameter, ACTION, is required. ENTITY, though not required, is often used.
 * These two parameters are thus handled specially.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ViewLocator
{
    // Constants
    private static final String KEY_VALUE_SEPARATOR = "=";

    private static final String PARAMETER_SEPARATOR = "&";

    private static final String GWT_PARAMETER = "gwt.codesvr";

    // Instance Variables
    private String sessionIdOrNull;

    private String actionOrNull;

    private String entityOrNull;

    // A map of all parameters, excluding action and entity
    private final Map<String, String> parameters = new HashMap<String, String>();

    /** token used to initialize the locator */
    private final String historyToken;

    // Public API
    /**
     * Create a ViewLocator initialized from the history token
     */
    public ViewLocator(String historyToken)
    {
        this.historyToken = historyToken;
        initializeFromHistoryToken();
    }

    /** Returns the history token that can be used to initialize this locator. */
    public String getHistoryToken()
    {
        return historyToken;
    }

    /**
     * Returns the session id or <code>null</code> if not known.
     */
    public String getSessionId()
    {
        return sessionIdOrNull;
    }

    /**
     * The action parameter for the view locator. If the locator is valid, then action is non-null.
     */
    public String tryGetAction()
    {
        return actionOrNull;
    }

    /**
     * The entity view for this view locator
     */
    public String tryGetEntity()
    {
        return entityOrNull;
    }

    /**
     * A map of all parameters, excluding action and entity. Do not modify the returned map.
     * <p>
     * Keys are in lower case.
     */
    public Map<String, String> getParameters()
    {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Return true if this view locator meets the minimal criteria for validity. If the locator is valid, then action is non-null.
     */
    public boolean isValid()
    {
        return actionOrNull != null;
    }

    /**
     * Return true if this view locator does not meet the minimal criteria for validity.
     */
    public boolean isInvalid()
    {
        return isValid() == false;
    }

    // Private methods
    /**
     * Extract the information for locating a view from the history token
     */
    private void initializeFromHistoryToken()
    {
        if (StringUtils.isBlank(historyToken))
        {
            return;
        }
        final String[] params = historyToken.split(PARAMETER_SEPARATOR);
        for (int i = 0; i < params.length; i++)
        {
            final String[] paramPair = params[i].split(KEY_VALUE_SEPARATOR);
            String paramName = paramPair.length > 0 ? paramPair[0] : null;
            String paramValue = paramPair.length > 1 ? paramPair[1] : null;

            // TODO 2010-09-20, Piotr Buczek: use com.google.gwt.http.client.URL.decode, exchange
            // BasicURLEncoder with URL.encode
            if (paramValue != null)
            {
                paramValue = StringEscapeUtils.unescapeHtml(paramValue.replaceAll("%2F", "/"));
            }

            if (GWT_PARAMETER.equals(paramName))
            {
                // skip GWT parameters -- only relevant during testing
            } else if (GenericSharedConstants.SESSION_ID_PARAMETER.equalsIgnoreCase(paramName))
            {
                sessionIdOrNull = paramValue;
            } else if (BasicConstant.LOCATOR_ACTION_PARAMETER.equalsIgnoreCase(paramName))
            {
                actionOrNull = paramValue;
            } else if (PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY.equalsIgnoreCase(paramName))
            {
                entityOrNull = paramValue;
            } else if (PermlinkUtilities.PERM_ID_PARAMETER_KEY.equalsIgnoreCase(paramName))
            {
                // Permlink URLs have an implied action
                if (actionOrNull == null)
                {
                    actionOrNull = PermlinkUtilities.PERMLINK_ACTION;
                }
                parameters.put(paramName, paramValue);
            } else
            {
                parameters.put(paramName, paramValue);
            }
        }

        // If the entity is specified, but no action, default the action to Permlink
        if (entityOrNull != null && actionOrNull == null)
        {
            actionOrNull = PermlinkUtilities.PERMLINK_ACTION;
        }
    }
}
