/*
 * Copyright 2008 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;

/**
 * Component of the gui displayed when some functionality has not been implemented yet.
 * 
 * @author Izabela Adamczyk
 */
public final class DummyComponent extends LayoutContainer
{
    public static final String ID = GenericConstants.ID_PREFIX + "dummy-component";

    public DummyComponent()
    {
        this("This feature will be implemented later...");
    }

    public DummyComponent(String message)
    {
        setId(ID);
        setLayout(new CenterLayout());
        addText("<div class='dummy-component'>" + message + "</div>");
    }
}
