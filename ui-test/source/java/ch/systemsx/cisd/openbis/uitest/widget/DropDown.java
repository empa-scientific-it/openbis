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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import ch.systemsx.cisd.openbis.uitest.suite.SeleniumTest;

/**
 * @author anttil
 */
public class DropDown extends Widget implements Fillable
{
    public void select(String text)
    {
        Collection<String> found = new HashSet<String>();
        for (WebElement choice : getChoiceElements())
        {
            if (choice.getText().equalsIgnoreCase(text))
            {
                Actions builder = new Actions(SeleniumTest.driver);
                builder.moveToElement(choice).click(choice).build().perform();
                return;
            }
            found.add(choice.getText());
        }
        throw new IllegalArgumentException("Selection " + text + " not found, got " + found);
    }

    public List<String> getChoices()
    {
        List<String> choices = new ArrayList<String>();
        for (WebElement choice : getChoiceElements())
        {
            choices.add(choice.getText());
        }
        return choices;
    }

    private List<WebElement> getChoiceElements()
    {
        WebElement opener = find(".//img");
        opener.click();
        return SeleniumTest.driver.findElements(By.className("x-combo-list-item"));
    }

    @Override
    public void fillWith(String string)
    {
        select(string);
    }

}
