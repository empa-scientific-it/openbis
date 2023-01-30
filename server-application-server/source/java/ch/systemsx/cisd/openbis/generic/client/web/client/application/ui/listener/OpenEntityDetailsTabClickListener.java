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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;

/**
 * A {@link ClickHandler} that opens entity details tab on click.
 * 
 * @author Piotr Buczek
 */
public class OpenEntityDetailsTabClickListener implements ClickHandler
{
    private final IEntityInformationHolderWithPermId entity;

    private final IViewContext<?> viewContext;

    public OpenEntityDetailsTabClickListener(IEntityInformationHolderWithPermId entity,
            final IViewContext<?> viewContext)
    {
        super();
        this.entity = entity;
        this.viewContext = viewContext;
    }

    protected IEntityInformationHolderWithPermId getEntity()
    {
        return entity;
    }

    @Override
    public void onClick(ClickEvent event)
    {
        new OpenEntityDetailsTabAction(getEntity(), viewContext,
                WidgetUtils.ifSpecialKeyPressed(event.getNativeEvent())).execute();
    }
}
