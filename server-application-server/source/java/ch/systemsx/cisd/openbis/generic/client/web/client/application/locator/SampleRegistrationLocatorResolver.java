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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleRegistrationTypeFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * @author pkupczyk
 */
public class SampleRegistrationLocatorResolver extends AbstractViewLocatorResolver
{
    public final static String ACTION = "SAMPLE_REGISTRATION";

    public final static String PATTERN = "sampleTypePattern";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public SampleRegistrationLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(final ViewLocator locator) throws UserFailureException
    {
        String pattern = locator.getParameters().get(PATTERN);
        SampleRegistrationTypeFilter filter = new SampleRegistrationTypeFilter(pattern, true);
        DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext)
                .getSampleRegistration(new ActionContext(), filter));
    }
}
