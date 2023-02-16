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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;

/**
 * {@link IViewLocatorResolver} for metaproject.
 * 
 * @author pkupczyk
 */
public class MetaprojectLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public MetaprojectLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(PermlinkUtilities.PERMLINK_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public boolean canHandleLocator(ViewLocator locator)
    {
        String entityKindValueOrNull = locator.tryGetEntity();
        return super.canHandleLocator(locator) && PermlinkUtilities.METAPROJECT.equals(entityKindValueOrNull);
    }

    @Override
    public void locatorExists(ViewLocator locator, AsyncCallback<Void> callback)
    {
        try
        {
            MetaprojectIdentifier identifier = extractMetaprojectIdentifier(locator);
            viewContext.getService().getMetaproject(identifier.format(),
                    new LocatorExistsCallback<Metaproject>(callback));
        } catch (UserFailureException e)
        {
            callback.onFailure(null);
        }
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        MetaprojectIdentifier identifier = extractMetaprojectIdentifier(locator);
        viewContext.getService().getMetaproject(identifier.format(),
                new OpenMetaprojectDetailsTabCallback(viewContext));
    }

    private MetaprojectIdentifier extractMetaprojectIdentifier(ViewLocator locator)
    {
        String owner = viewContext.getModel().getLoggedInPerson().getUserId();
        String name = getMandatoryParameter(locator, PermlinkUtilities.NAME_PARAMETER_KEY);
        return new MetaprojectIdentifier(owner, name);
    }

    private static class OpenMetaprojectDetailsTabCallback extends
            AbstractAsyncCallback<Metaproject>
    {

        private OpenMetaprojectDetailsTabCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final Metaproject result)
        {
            OpenEntityDetailsTabHelper.openMetaproject(viewContext, result, false);
        }
    }

}