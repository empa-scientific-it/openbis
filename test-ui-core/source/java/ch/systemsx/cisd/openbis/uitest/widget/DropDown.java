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
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.webdriver.Contextual;

/**
 * @author anttil
 */
public class DropDown implements Widget, Fillable
{
    @Contextual(".//input")
    private Text textField;

    @Contextual(".//img")
    private WebElement opener;

    public void select(String text)
    {
        if (text.equals(textField.getValue()))
        {
            return;
        }

        Collection<String> found = new HashSet<String>();
        for (WebElement choice : getChoiceElements())
        {
            if (choice.getText().equalsIgnoreCase(text))
            {
                SeleniumTest.mouseOver(choice);
                choice.click();
                return;
            }
            found.add(choice.getText());
        }
        throw new IllegalArgumentException("Selection " + text + " not found, got " + found);
    }

    public String getValue()
    {
        return textField.getValue();
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

        SeleniumTest.setImplicitWait(0, TimeUnit.SECONDS);
        List<WebElement> wlist =
                SeleniumTest.driver.findElements(By.className("x-combo-list-item"));
        SeleniumTest.setImplicitWaitToDefault();

        opener.click();

        if (wlist.size() != 0)
        {
            WebDriverWait wait = new WebDriverWait(SeleniumTest.driver, SeleniumTest.IMPLICIT_WAIT);
            wait.until(ExpectedConditions.stalenessOf(wlist.get(0)));
        }

        wlist = SeleniumTest.driver.findElements(By.className("x-combo-list-item"));

        if (wlist.size() == 0)
        {
            System.out.println("dropdown retry");
            return getChoiceElements();
        }
        return wlist;
    }

    @Override
    public void fillWith(String string)
    {
        select(string);
    }
}
