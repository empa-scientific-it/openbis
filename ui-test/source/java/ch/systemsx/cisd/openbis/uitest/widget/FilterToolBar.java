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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.function.Function;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.webdriver.Contextual;

/**
 * @author anttil
 */
public class FilterToolBar implements Widget
{

    @Contextual
    private WebElement context;

    public void setFilter(String filter, final String text, final Refreshable refresher)
    {
        final Object state = refresher.getState();

        WebElement t =
                context.findElement(By.xpath(".//input[contains(@id, '" + filter + "-input')]"));
        t.clear();
        t.sendKeys(text);

        new FluentWait<Refreshable>(refresher)
                .withTimeout(30, TimeUnit.SECONDS)
                .pollingEvery(100, TimeUnit.MILLISECONDS)
                .until(
                        new Function<Refreshable, Boolean>()
                        {

                            @Override
                            public Boolean apply(Refreshable refreshable)
                            {
                                return refresher.hasStateBeenUpdatedSince(state);
                            }
                        });
    }

    public Collection<String> getVisibleFilters()
    {
        SeleniumTest.setImplicitWait(500, TimeUnit.MILLISECONDS);

        List<WebElement> elements =
                context.findElements(By.xpath(".//input[not(contains(@id, 'grid-input'))]/.."));

        SeleniumTest.setImplicitWaitToDefault();

        Set<String> filters = new HashSet<String>();
        for (WebElement element : elements)
        {
            String id = element.getAttribute("id");
            String[] split = id.split("-");
            filters.add(split[split.length - 1]);
        }
        return filters;
    }

    public void reset()
    {
        WebElement b = context.findElement(By.xpath(".//button[text()='Reset']"));
        b.click();
    }
}
