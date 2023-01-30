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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityEditorTabClickListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * Resolver for editing an entity.
 * 
 * @author Franz-Josef Elmer
 */
public class PermlinkEditingLocatorResolver extends PermlinkLocatorResolver
{

    private static final String EDIT_ACTION = "EDITING";

    /**
     * @param viewContext
     */
    public PermlinkEditingLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(EDIT_ACTION, viewContext);
    }

    @Override
    protected void openInitialEntityViewer(String entityKindValue, String permIdValue) throws UserFailureException
    {
        EntityKind entityKind = getEntityKind(entityKindValue);

        viewContext.getCommonService().getEntityInformationHolder(entityKind, permIdValue,
                new AbstractAsyncCallback<IEntityInformationHolderWithPermId>(viewContext)
                    {

                        @Override
                        protected void process(IEntityInformationHolderWithPermId result)
                        {
                            OpenEntityEditorTabClickListener.showEntityEditor(viewContext, result, false);

                        }
                    });
    }

}
