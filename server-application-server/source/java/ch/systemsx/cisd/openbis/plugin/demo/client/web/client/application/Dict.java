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

package ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application;

/**
 * An {@link ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict} extension for <i>demo</i> specific message keys.
 * 
 * @author Christian Ribeaud
 */
public final class Dict extends ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict
{
    public static final String MODULE_MENU_TITLE = "module_menu_title";

    public static final String STATISTICS_DEMO_TAB_HEADER = "statistics_tab_header";

    private Dict()
    {
        // Can not be instantiated.
    }
}
