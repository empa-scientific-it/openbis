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

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.webdriver.Contextual;

/**
 * @author anttil
 */
public class FileUpload implements Widget
{
    @Contextual("./descendant-or-self::input[@type='file']")
    private WebElement input;

    public void setFile(String file)
    {
        ((JavascriptExecutor) SeleniumTest.driver).executeScript("arguments[0].readOnly=false",
                input);
        input.sendKeys(file);
        input.click();
    }
}
