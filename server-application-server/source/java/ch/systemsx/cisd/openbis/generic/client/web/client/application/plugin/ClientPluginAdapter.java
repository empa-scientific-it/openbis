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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;

/**
 * A dummy {@link IClientPlugin} implementation which throws {@link UnsupportedOperationException} as default behavior.
 * 
 * @author Christian Ribeaud
 */
public class ClientPluginAdapter<E extends BasicEntityType, I extends IIdAndCodeHolder> implements
        IClientPlugin<E, I>
{

    //
    // IClientPlugin
    //

    @Override
    public Widget createBatchRegistrationForEntityType(final E entityType)
    {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Widget createBatchUpdateForEntityType(E entityType)
    {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public AbstractTabItemFactory createEntityViewer(final IEntityInformationHolderWithPermId entity)
    {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public DatabaseModificationAwareWidget createRegistrationForEntityType(final E entityType,
            Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            final ActionContext context)
    {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public AbstractTabItemFactory createEntityEditor(I identifiable)
    {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
