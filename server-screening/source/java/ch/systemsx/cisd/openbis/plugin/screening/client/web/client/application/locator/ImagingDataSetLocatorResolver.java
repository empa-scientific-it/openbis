/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.PermlinkLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * @author Pawel Glyzewski
 */
public class ImagingDataSetLocatorResolver extends PermlinkLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    public ImagingDataSetLocatorResolver(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext());
        this.viewContext = viewContext;
    }

    @Override
    public boolean canHandleLocator(ViewLocator locator)
    {
        String entityKindValueOrNull = tryGetEntityKind(locator);
        String permIdValueOrNull = tryGetPermId(locator);

        return super.canHandleLocator(locator)
                && (EntityKind.DATA_SET.name().equals(entityKindValueOrNull) || EntityKind.SAMPLE
                        .name().equals(entityKindValueOrNull)) && permIdValueOrNull != null
                && permIdValueOrNull.contains(":");
    }

    @Override
    public void locatorExists(ViewLocator locator, AsyncCallback<Void> callback)
    {
        EntityKind entityKindValueOrNull = tryGetEntityKindEnum(locator);
        String permIdValueOrNull = tryGetPermId(locator);

        if (entityKindValueOrNull != null && permIdValueOrNull != null)
        {
            int idx = permIdValueOrNull.indexOf(':');
            if (idx != -1)
            {
                viewContext.getCommonService().getEntityInformationHolder(entityKindValueOrNull,
                        permIdValueOrNull.substring(0, idx),
                        new LocatorExistsCallback<IEntityInformationHolderWithPermId>(callback));
                return;
            }
        }

        callback.onFailure(null);
    }

    /**
     * Open the entity details tab for the specified entity kind and permId.
     */
    @Override
    protected void openInitialEntityViewer(String entityKindValue, String permIdValue)
            throws UserFailureException
    {
        EntityKind entityKind = getEntityKind(entityKindValue);
        int idx = permIdValue.indexOf(':');

        viewContext.getCommonService().getEntityInformationHolder(entityKind,
                permIdValue.substring(0, idx),
                new OpenEntityDetailsTabCallback(viewContext, permIdValue.substring(idx + 1)));
    }

    private static class OpenEntityDetailsTabCallback extends
            AbstractAsyncCallback<IEntityInformationHolderWithPermId>
    {
        private final String wellLocation;

        private OpenEntityDetailsTabCallback(final IViewContext<?> viewContext,
                final String wellLocation)
        {
            super(viewContext);
            this.wellLocation = wellLocation;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Opens the tab with <var>result</var> entity details.
         */
        @Override
        protected final void process(final IEntityInformationHolderWithPermId result)
        {
            new OpenEntityDetailsTabAction(new IEntityInformationHolderWithPermId()
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public BasicEntityType getEntityType()
                    {
                        return result.getEntityType();
                    }

                    @Override
                    public EntityKind getEntityKind()
                    {
                        return result.getEntityKind();
                    }

                    @Override
                    public Long getId()
                    {
                        return result.getId();
                    }

                    @Override
                    public String getCode()
                    {
                        return result.getCode();
                    }

                    @Override
                    public String getPermId()
                    {
                        return result.getPermId() + ":" + wellLocation;
                    }

                }, viewContext, false).execute();
        }
    }

}
