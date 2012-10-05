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

package ch.systemsx.cisd.openbis.uitest.menu;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.webdriver.Context;
import ch.systemsx.cisd.openbis.uitest.webdriver.WidgetContext;

public class TabBar
{
    @Context
    private WidgetContext context;

    public boolean selectTab(String label)
    {
        if (label == null)
        {
            return false;
        }

        for (WebElement link : getTabLinks())
        {
            if (link.getText().equals(label))
            {
                link.click();
                return true;
            }
        }
        return false;
    }

    public Collection<String> getTabs()
    {
        Collection<String> tabs = new HashSet<String>();
        for (WebElement link : getTabLinks())
        {
            tabs.add(link.getText());
        }
        return tabs;
    }

    private List<WebElement> getTabLinks()
    {
        return context.findAll("//li[starts-with(@id, 'openbis_main-tab-panel__')]");
    }
}
