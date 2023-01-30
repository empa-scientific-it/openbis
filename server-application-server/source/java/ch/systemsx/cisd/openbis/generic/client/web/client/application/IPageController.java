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

import com.extjs.gxt.ui.client.mvc.Dispatcher;

/**
 * The main page controller.
 * <p>
 * This controller lies on top of the ones specified in {@link Dispatcher}.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public interface IPageController
{
    /**
     * Reloads the application.
     * <p>
     * Depending whether you are logged in or not, this determines the page you will be landing.
     * </p>
     * 
     * @param logout whether the reload happens after a logout (meaning that the {@link Dispatcher} will be cleared).
     */
    public void reload(final boolean logout);
}
