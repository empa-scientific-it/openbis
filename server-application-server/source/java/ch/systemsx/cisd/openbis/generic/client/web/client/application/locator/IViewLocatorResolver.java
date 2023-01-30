/*
 * Copyright 2010 ETH Zuerich, CISD
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

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

public interface IViewLocatorResolver
{
    /**
     * Test if this handler can handle the action and parameters specified in the locator.
     */
    public boolean canHandleLocator(ViewLocator locator);

    /**
     * Test whether the locator exists (e.g. data to be displayed hasn't been deleted).
     */
    public void locatorExists(ViewLocator locator, AsyncCallback<Void> callback);

    /**
     * Invoke the code to open the view specified by the locator. Implementations should try to interpret the parameters to the best of their
     * abilities and not fail unless absolutely necessary (e.g., unnecessary parameters should be ignored).
     * 
     * @exception UserFailureException May be thrown if it is determined that some mandatory information is not specified.
     */
    public void resolve(ViewLocator locator) throws UserFailureException;

}