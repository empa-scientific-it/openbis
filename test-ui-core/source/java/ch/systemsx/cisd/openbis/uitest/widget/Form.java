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

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.webdriver.Contextual;

/**
 * @author anttil
 */
public class Form implements Widget
{
    @Contextual
    private WebElement context;

    public Widget getWidget(PropertyType type)
    {
        List<WebElement> elements = context.findElements(By.xpath(".//form/div/label"));

        for (WebElement element : elements)
        {
            if (element.getText().toLowerCase().startsWith(type.getLabel().toLowerCase()))
            {
                WebElement e = element.findElement(By.xpath("../div/div"));
                return SeleniumTest.initializeWidget(type.getDataType().representedAs(), e);
            }
        }
        throw new IllegalArgumentException("Could not find " + type.getLabel());
    }

    public List<String> getLabels()
    {
        List<String> labels = new ArrayList<String>();
        List<WebElement> elements = context.findElements(By.xpath(".//form/div/label"));
        for (WebElement element : elements)
        {
            labels.add(element.getText());
        }
        return labels;
    }

}
