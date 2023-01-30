/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import com.google.gwt.debugpanel.client.DefaultCookieDebugPanelComponent;
import com.google.gwt.debugpanel.client.DefaultDebugStatisticsDebugPanelComponent;
import com.google.gwt.debugpanel.client.DefaultExceptionDebugPanelComponent;
import com.google.gwt.debugpanel.client.DefaultRawLogDebugPanelComponent;
import com.google.gwt.debugpanel.client.DefaultStatisticsModelRpcEventHandler;
import com.google.gwt.debugpanel.client.DefaultStatisticsModelStartupEventHandler;
import com.google.gwt.debugpanel.client.DelayedDebugPanelComponent;
import com.google.gwt.debugpanel.common.GwtStatisticsEventSystem;
import com.google.gwt.debugpanel.models.GwtDebugStatisticsModel;
import com.google.gwt.debugpanel.models.GwtExceptionModel;
import com.google.gwt.debugpanel.widgets.DebugPanelWidget;
import com.google.gwt.user.client.ui.Widget;

public class DebugPanelManager implements DebugPanelWidget.Listener
{
    private GwtStatisticsEventSystem sys;

    private DefaultDebugStatisticsDebugPanelComponent panelComponent;

    private DelayedDebugPanelComponent xmlComponent;

    private DefaultRawLogDebugPanelComponent logComponent;

    private GwtDebugStatisticsModel sm;

    private GwtExceptionModel em;

    public static Widget createDebugPanel()
    {
        DebugPanelManager manager = new DebugPanelManager();
        return manager.createWidget();
    }

    DebugPanelManager()
    {
        sys = new GwtStatisticsEventSystem();
        panelComponent = new DefaultDebugStatisticsDebugPanelComponent(null);
        xmlComponent = panelComponent.xmlComponent();
        logComponent = new DefaultRawLogDebugPanelComponent(sys);
        em = new GwtExceptionModel();
    }

    private Widget createWidget()
    {
        return new DebugPanelWidget(this, true, new DebugPanelWidget.Component[]
        { panelComponent, new DefaultExceptionDebugPanelComponent(em),
                new DefaultCookieDebugPanelComponent(), logComponent, xmlComponent });
    }

    // @Override
    @Override
    public void onShow()
    {
        panelComponent.reset(sm =
                new GwtDebugStatisticsModel(new DefaultStatisticsModelStartupEventHandler(),
                        new DefaultStatisticsModelRpcEventHandler()));
        xmlComponent.reset();
        logComponent.reset();

        sys.addListener(sm, false);
        sys.addListener(em, false);
        sys.enable(true);
    }

    // @Override
    @Override
    public void onReset()
    {
        sys.removeListener(sm);
        sys.removeListener(em);

        panelComponent.reset(null);
        sys.clearEventHistory();
    }
}