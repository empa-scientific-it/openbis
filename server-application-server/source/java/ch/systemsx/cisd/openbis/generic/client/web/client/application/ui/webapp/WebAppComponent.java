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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.webapp;

import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.google.gwt.user.client.ui.Frame;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;

/**
 * Component that displays a web application in an IFRAME.
 * 
 * @author pkupczyk
 */
public class WebAppComponent extends WidgetComponent
{

    private static final String ID_PREFIX = GenericConstants.ID_PREFIX + "webapp_";

    public WebAppComponent(WebAppUrl url)
    {
        super(new Frame(url.toString()));
    }

    public static String getId(String webAppCode)
    {
        return ID_PREFIX + webAppCode;
    }
}
