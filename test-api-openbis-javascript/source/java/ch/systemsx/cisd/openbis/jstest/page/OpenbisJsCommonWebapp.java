/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.jstest.page;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;

/**
 * @author pkupczyk
 */
public class OpenbisJsCommonWebapp
{

    @Locate("qunit-testresult")
    private WebElement infoBox;

    @Locate("qunit-junit-report")
    private WebElement junitReport;

    public int getFailedCount()
    {
        SeleniumTest.setImplicitWait(300, TimeUnit.SECONDS);
        try
        {
            WebElement failedElement = infoBox.findElement(By.xpath("span[3]"));
            return Integer.valueOf(failedElement.getText());
        } finally
        {
            SeleniumTest.setImplicitWaitToDefault();
        }
    }

    public String getJunitReport()
    {
        return junitReport.getText();
    }
}
