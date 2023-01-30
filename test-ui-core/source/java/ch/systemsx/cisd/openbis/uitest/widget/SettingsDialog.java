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

package ch.systemsx.cisd.openbis.uitest.widget;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.page.Browsable;
import ch.systemsx.cisd.openbis.uitest.webdriver.Contextual;

/**
 * @author anttil
 */
public class SettingsDialog implements Widget
{

    @Contextual
    private WebElement context;

    @Contextual("//*[@class='x-window-bl']//button[text()='OK']")
    private Button okButton;

    public void showColumns(String... columns)
    {
        context.findElement(By.xpath(".//*[text()='No Columns']")).click();

        for (String name : columns)
        {
            List<WebElement> l =
                    context.findElements(By.xpath(".//div[text()='"
                            + name
                            + "']/../..//div[contains(@class, 'IS_VISIBLE') and not(*)]"));

            if (l.size() > 0)
            {
                l.get(0).click();
            } else
            {
                throw new IllegalStateException("Could not find column " + name
                        + " from settings dialog!");
            }
        }

        okButton.click();
    }

    public void showFilters(String... filters)
    {
        context.findElement(By.xpath(".//*[text()='No Filters']")).click();

        for (String name : filters)
        {
            List<WebElement> l =
                    context.findElements(By.xpath(".//div[text()='"
                            + name
                            + "']/../..//div[contains(@class, 'HAS_FILTER') and not(*)]"));

            if (l.size() > 0)
            {
                l.get(0).click();
            } else
            {
                throw new IllegalStateException("Could not find filter " + name
                        + " from settings dialog!");
            }
        }

        okButton.click();
    }

    public void showColumnsOf(Browsable browsable)
    {
        showColumns(browsable.getColumns().toArray(new String[0]));
    }

}
