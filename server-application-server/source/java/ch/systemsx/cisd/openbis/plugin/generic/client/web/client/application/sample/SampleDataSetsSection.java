/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDirectlyConnectedController;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractEntityDataSetsSection;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleDataSetsSection extends AbstractEntityDataSetsSection
{
    public SampleDataSetsSection(final IViewContext<?> viewContext, TechId entityId,
            BasicEntityType entityType)
    {
        super(viewContext, entityId, entityType);
    }

    @Override
    protected IDisposableComponent createDataSetBrowser(BasicEntityType type, TechId entityID,
            IDirectlyConnectedController directlyConnectedController)
    {
        return SampleDataSetBrowser
                .create(viewContext, entityID, type, directlyConnectedController);
    }

}
