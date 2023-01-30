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

package ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application.module;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.IDemoClientServiceAsync;

/**
 * Customized menu widget.
 * 
 * @author Izabela Adamczyk
 */
public class CustomizedWidgetDemoModuleMenu extends HorizontalPanel
{

    public CustomizedWidgetDemoModuleMenu(IViewContext<IDemoClientServiceAsync> viewContext)
    {
        add(new Button("Agree")
            {
                @Override
                protected void onClick(ComponentEvent ce)
                {
                    super.onClick(ce);
                    GWTUtils.alert("Response", "You are right!");
                }
            });
        add(new Button("Disagree")
            {
                @Override
                protected void onClick(ComponentEvent ce)
                {
                    super.onClick(ce);
                    GWTUtils.alert("Response", "You are wrong!");
                }
            });
    }

}
