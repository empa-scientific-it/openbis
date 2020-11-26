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

import java.util.StringTokenizer;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.webdriver.Contextual;

/**
 * @author anttil
 */
public class PagingToolBar implements Widget, Refreshable
{

    @Contextual
    private WebElement context;

    @Contextual(".//button[text()='Filters']")
    private Button filtersButton;

    @Contextual(".//button[text()='Settings']")
    private Button settingsButton;

    public void filters()
    {
        if (filtersButton.isPressed() == false)
        {
            filtersButton.click();
        }
    }

    public int rowCount()
    {
        String text = context.findElement(By.xpath(".//*[contains(text(), 'isplay')]")).getText();

        StringTokenizer tokens = new StringTokenizer(text, " ");

        if (tokens.nextToken().equals("No"))
        {
            return 0;
        }

        String from = tokens.nextToken();
        tokens.nextElement(); // -
        String to = tokens.nextToken();

        return Integer.parseInt(to) - Integer.parseInt(from) + 1;

    }

    public void settings()
    {
        settingsButton.click();
    }

    @Override
    public Object getState()
    {
        return context.findElement(By.xpath(".//div[contains(@class, 'my-paging-display')]"))
                .getText();
    }

    @Override
    public boolean hasStateBeenUpdatedSince(Object oldState)
    {
        String currentState =
                context.findElement(By.xpath(".//div[contains(@class, 'my-paging-display')]"))
                        .getText();

        System.out.println("comparing " + oldState + " with " + currentState);

        if (currentState.contains("Loading"))
        {
            return false;
        }

        return (currentState.equals(oldState) == false);
    }

    public boolean isEnabled()
    {
        return !context.getAttribute("class").contains("x-item-disabled");
    }
}
