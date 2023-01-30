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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * @author Piotr Buczek
 */
class DataSetParentsSection extends AbstractDataSetsSection
{
    private final AbstractExternalData dataset;

    DataSetParentsSection(IViewContext<?> viewContext, AbstractExternalData dataset)
    {
        super("Parents (Data Sets)", viewContext, dataset);
        this.dataset = dataset;
        setIds(DisplayTypeIDGenerator.DATA_SET_PARENTS_SECTION);
    }

    @Override
    protected IDisposableComponent createDatasetBrowserComponent()
    {
        return DataSetRelationshipBrowser.create(viewContext, TechId.create(dataset),
                DataSetRelationshipRole.CHILD, dataset.getDataSetType());
    }

}
