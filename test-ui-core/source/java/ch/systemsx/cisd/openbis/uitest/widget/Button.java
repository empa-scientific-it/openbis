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

import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.webdriver.Contextual;

/**
 * @author anttil
 */
public class Button implements Widget
{

    @Contextual("./descendant-or-self::button")
    private WebElement button;

    public void click()
    {
        button.click();
    }

    public boolean isPressed()
    {
        return "true".equalsIgnoreCase(button.getAttribute("aria-pressed"));
    }

    public String getText()
    {
        return button.getText();
    }
}
