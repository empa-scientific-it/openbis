/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.webdriver.Contextual;

/**
 * @author anttil
 */
public class TreeGrid implements Widget
{
    @Contextual
    private WebElement context;

    public boolean select(String label)
    {
        List<WebElement> elements =
                context.findElements(By.xpath(".//span[not(*) and @class='gwt-InlineHTML']"));

        Collection<String> found = new ArrayList<String>();
        for (WebElement element : elements)
        {
            String text = element.getText();
            if (label.equalsIgnoreCase(text))
            {
                element.click();
                return true;
            } else
            {
                found.add(text);
            }
        }

        return false;
    }
}
