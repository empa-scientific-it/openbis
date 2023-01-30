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
 * The client plugin.
 * <p>
 * It specifies widgets for following operations:
 * <ul>
 * <li>Detailed view of a given entity identifier.</li>
 * <li>Registration of an entity of a given type.</li>
 * <li>Batch registration of an entity of a given type.</li>
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IClientPlugin<T extends BasicEntityType, I extends IIdAndCodeHolder>
{
    /**
     * Shows a detailed view of the entity specified by its <var>identifier</var>.
     */
    // NOTE: BasicEntityType is used here to allow viewing entities from MatchingEntitiesPanel
    public AbstractTabItemFactory createEntityViewer(final IEntityInformationHolderWithPermId entity);

    /**
     * Shows a registration form for entities of given <var>entityType</var>.
     */
    public DatabaseModificationAwareWidget createRegistrationForEntityType(final T entityType,
            Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            final ActionContext context);

    /**
     * Shows a batch registration form for entities of given <var>entityType</var>.
     */
    public Widget createBatchRegistrationForEntityType(final T entityType);

    /**
     * Shows a batch update form for entities of given <var>entityType</var>.
     */
    public Widget createBatchUpdateForEntityType(final T entityType);

    /**
     * Shows a editor of the specified entity.
     */
    public AbstractTabItemFactory createEntityEditor(final I identifiable);
}
