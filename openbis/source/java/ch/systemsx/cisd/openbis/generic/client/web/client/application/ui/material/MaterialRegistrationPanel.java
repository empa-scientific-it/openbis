/*
* Copyright 2008 ETH Zuerich, CISD
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MaterialTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.EntityRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * The {@link EntityRegistrationPanel} extension for registering an material.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialRegistrationPanel extends
        EntityRegistrationPanel<MaterialTypeModel, MaterialTypeSelectionWidget>
{

    public static final String ID = EntityRegistrationPanel.createId(EntityKind.MATERIAL);

    public MaterialRegistrationPanel(final CommonViewContext viewContext)
    {
        super(viewContext, EntityKind.MATERIAL, new MaterialTypeSelectionWidget(viewContext,
                EntityRegistrationPanel.createId(EntityKind.MATERIAL)));
    }

}
