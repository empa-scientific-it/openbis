/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.entity;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBrowserGrid;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * @author pkupczyk
 */
public class MetaprojectMaterialsSection extends MetaprojectEntitySection
{
    public MetaprojectMaterialsSection(IViewContext<?> viewContext, TechId metaprojectId)
    {
        super(viewContext, metaprojectId);
        setIds(DisplayTypeIDGenerator.MATERIALS_SECTION);
        setHeading(viewContext.getMessage(Dict.METAPROJECT_ENTITIES_MATERIALS));
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        MaterialBrowserGrid grid =
                MaterialBrowserGrid.createForMetaproject(viewContext.getCommonViewContext(),
                        metaprojectId);
        return createDisposableBrowser(grid);
    }

}
