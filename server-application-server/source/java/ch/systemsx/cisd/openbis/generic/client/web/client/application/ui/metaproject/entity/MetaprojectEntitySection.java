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

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * @author pkupczyk
 */
public abstract class MetaprojectEntitySection extends DisposableTabContent
{

    public static final EventType SECTION_CHANGED_EVENT = new EventType();

    protected final TechId metaprojectId;

    public MetaprojectEntitySection(IViewContext<?> viewContext, TechId metaprojectId)
    {
        super(null, viewContext, metaprojectId);
        this.metaprojectId = metaprojectId;
    }

    protected String createBrowserId(String sectionName)
    {
        return GenericConstants.ID_PREFIX + "metaproject-" + sectionName + "-section_"
                + metaprojectId + "-browser";
    }

    protected IDisposableComponent createDisposableBrowser(AbstractEntityGrid<?> grid)
    {
        grid.addListener(AbstractEntityGrid.ENTITY_TAGGED_EVENT, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    fireEvent(SECTION_CHANGED_EVENT);
                }
            });

        grid.addListener(AbstractEntityGrid.ENTITY_UNTAGGED_EVENT, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    fireEvent(SECTION_CHANGED_EVENT);
                }
            });

        return grid.asDisposableWithoutToolbar();
    }

}
