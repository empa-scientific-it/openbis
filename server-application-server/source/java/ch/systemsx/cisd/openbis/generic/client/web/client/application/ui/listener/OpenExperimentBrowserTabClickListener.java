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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;

/**
 * A {@link ClickHandler} that opens experiment browser tab on click.
 * 
 * @author Piotr Buczek
 */
public class OpenExperimentBrowserTabClickListener implements ClickHandler
{
    private final String spaceCodeOrNull;

    private final String projectIdentifierOrNull;

    private final IViewContext<?> viewContext;

    private final boolean forceReopen;

    public OpenExperimentBrowserTabClickListener(final IViewContext<?> viewContext,
            String spaceCodeOrNull, String projectIdentifierOrNull, boolean forceReopen)
    {
        super();
        this.spaceCodeOrNull = spaceCodeOrNull;
        this.projectIdentifierOrNull = projectIdentifierOrNull;
        this.viewContext = viewContext;
        this.forceReopen = forceReopen;
    }

    @Override
    public void onClick(ClickEvent event)
    {
        final AbstractTabItemFactory tabView =
                new ComponentProvider(viewContext.getCommonViewContext()).getExperimentBrowser(
                        spaceCodeOrNull, projectIdentifierOrNull, null);
        final boolean keyPressed = WidgetUtils.ifSpecialKeyPressed(event.getNativeEvent());
        tabView.setInBackground(keyPressed);
        tabView.setForceReopen(forceReopen);
        DispatcherHelper.dispatchNaviEvent(tabView);
    }
}
