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

package ch.systemsx.cisd.openbis.uitest.layout;

import ch.systemsx.cisd.openbis.uitest.menu.BrowseMenu;
import ch.systemsx.cisd.openbis.uitest.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.page.SampleBrowser;
import ch.systemsx.cisd.openbis.uitest.webdriver.Pages;

/**
 * @author anttil
 */
public class SampleBrowserLocation implements Location<SampleBrowser>
{

    @Override
    public void moveTo(Pages pages)
    {
        pages.load(TopBar.class).browse();
        pages.load(BrowseMenu.class).samples();
        pages.load(SampleBrowser.class).allSpaces();
    }

    @Override
    public String getTabName()
    {
        return "Sample Browser";
    }

    @Override
    public Class<SampleBrowser> getPage()
    {
        return SampleBrowser.class;
    }
}