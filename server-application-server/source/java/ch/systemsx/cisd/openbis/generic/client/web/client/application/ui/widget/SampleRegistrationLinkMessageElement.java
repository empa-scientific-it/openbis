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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleRegistrationTypeFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * A message element linking to sample registration tab.
 * 
 * @author anttil
 */
public class SampleRegistrationLinkMessageElement implements IMessageElement
{
    private static final String LABEL = "Register child";

    private final Sample sample;

    private final IViewContext<?> viewContext;

    public SampleRegistrationLinkMessageElement(IViewContext<?> viewContext, Sample sample)
    {
        this.viewContext = viewContext;
        this.sample = sample;
    }

    @Override
    public int length()
    {
        return 0;
    }

    @Override
    public Widget render()
    {
        Anchor linkElement = new Anchor(LABEL);
        linkElement.addClickHandler(new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    ComponentProvider componentProvider = new ComponentProvider(viewContext.getCommonViewContext());
                    ActionContext context = createActionContext();
                    SampleRegistrationTypeFilter filter = new SampleRegistrationTypeFilter(null, false);
                    AbstractTabItemFactory sampleRegistration = componentProvider.getSampleRegistration(context, filter);
                    DispatcherHelper.dispatchNaviEvent(sampleRegistration);
                }
            });
        return linkElement;
    }

    @Override
    public Widget render(int maxLength)
    {
        return render();
    }

    @Override
    public String toString()
    {
        return LABEL;
    }

    private ActionContext createActionContext()
    {
        ActionContext context = new ActionContext();
        context.setParent(sample);
        Experiment experiment = sample.getExperiment();
        if (experiment != null)
        {
            context.setExperiment(experiment);
        }
        Space space = sample.getSpace();
        if (space != null)
        {
            context.setSpaceCode(space.getCode());
        }
        return context;
    }

}
