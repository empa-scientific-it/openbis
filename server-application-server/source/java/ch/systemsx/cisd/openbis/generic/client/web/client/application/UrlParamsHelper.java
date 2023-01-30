/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.google.gwt.user.client.History;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.OpenViewAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorResolverRegistry;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * A class with helper methods for URL parameters handling and opening initial tab.
 * 
 * @author Piotr Buczek
 */
public final class UrlParamsHelper
{
    public static final String createTemplateURL(EntityKind kind, EntityType type,
            boolean withCodes, boolean withExperiments, BatchOperationKind operationKind)
    {
        return createTemplateURL(kind, type, withCodes, withExperiments, true, operationKind);
    }

    public static final String createTemplateURL(EntityKind kind, EntityType type,
            boolean withCodes, boolean withExperiments, boolean withSapce,
            BatchOperationKind operationKind)
    {
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(GenericConstants.TEMPLATE_SERVLET_NAME);
        methodWithParameters.addParameter(GenericConstants.ENTITY_KIND_KEY_PARAMETER, kind.name());
        methodWithParameters.addParameter(GenericConstants.ENTITY_TYPE_KEY_PARAMETER,
                type.getCode());
        methodWithParameters.addParameter(GenericConstants.AUTO_GENERATE, withCodes);
        methodWithParameters.addParameter(GenericConstants.WITH_EXPERIMENTS, withExperiments);
        methodWithParameters.addParameter(GenericConstants.WITH_SPACE, withSapce);
        methodWithParameters.addParameter(GenericConstants.BATCH_OPERATION_KIND,
                operationKind.name());
        methodWithParameters.addParameter(GenericConstants.TIMESTAMP_PARAMETER, Long.toString(System.currentTimeMillis()));
        return methodWithParameters.toString();
    }

    /** Creates an action which opens a page pointed by the current URL. */
    public static OpenViewAction createNavigateToCurrentUrlAction(IViewContext<?> viewContext)
    {
        ViewLocatorResolverRegistry resolver = viewContext.getLocatorResolverRegistry();
        ViewLocator viewLocator = new ViewLocator(History.getToken());
        OpenViewAction openViewAction = new OpenViewAction(resolver, viewLocator);
        return openViewAction;
    }
}
