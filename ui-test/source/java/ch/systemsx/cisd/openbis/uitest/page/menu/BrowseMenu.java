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

package ch.systemsx.cisd.openbis.uitest.page.menu;

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Link;

public class BrowseMenu
{

    @Locate("openbis_top-menu_SAMPLE_MENU_BROWSE")
    private Link samples;

    @Locate("openbis_top-menu_PROJECT_MENU_BROWSE")
    private Link projects;

    @Locate("openbis_top-menu_EXPERIMENT_MENU_BROWSE")
    private Link experiments;

    public void samples()
    {
        samples.click();
    }

    public void projects()
    {
        projects.click();
    }

    public void experiments()
    {
        experiments.click();
    }
}
