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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class GenericViewContext implements IMessageProvider
{
    private final IGenericClientServiceAsync service;
    private final IMessageProvider messageProvider;
    private final IGenericImageBundle imageBundle;
    private final GenericViewModel viewModel;

    GenericViewContext(IGenericClientServiceAsync service, IMessageProvider messageProvider,
            IGenericImageBundle imageBundle)
    {
        this.service = service;
        this.messageProvider = messageProvider;
        this.imageBundle = imageBundle;
        viewModel = new GenericViewModel();
    }
    
    public final GenericViewModel getModel()
    {
        return viewModel;
    }

    public final IGenericClientServiceAsync getService()
    {
        return service;
    }

    public String getMessage(String key, Object... parameters)
    {
        return messageProvider.getMessage(key, parameters);
    }

    public final IGenericImageBundle getImageBundle()
    {
        return imageBundle;
    }
    
}
