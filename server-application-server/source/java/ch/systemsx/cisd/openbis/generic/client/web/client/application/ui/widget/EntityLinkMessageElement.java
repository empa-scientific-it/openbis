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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * Message element for a link to an entity specified by perm ID.
 * 
 * @author anttil
 */
public class EntityLinkMessageElement implements IMessageElement
{
    private final IViewContext<?> viewContext;

    private final String label;

    private final EntityKind entityKind;

    private final String permId;

    public EntityLinkMessageElement(IViewContext<?> viewContext, String label, EntityKind entityKind, String permId)
    {
        this.viewContext = viewContext;
        this.label = label;
        this.entityKind = entityKind;
        this.permId = permId;
    }

    @Override
    public int length()
    {
        return 0;
    }

    @Override
    public Widget render(int maxLength)
    {
        return render();
    }

    @Override
    public Widget render()
    {
        Anchor linkElement = new Anchor(label);
        linkElement.addClickHandler(new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    OpenEntityDetailsTabHelper.open(viewContext, entityKind, permId, false);
                }
            });
        return linkElement;
    }

    @Override
    public String toString()
    {
        return label;
    }

}
