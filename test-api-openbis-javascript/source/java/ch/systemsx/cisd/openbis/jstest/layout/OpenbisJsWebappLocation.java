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

package ch.systemsx.cisd.openbis.jstest.layout;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.jstest.page.OpenbisJsCommonWebapp;
import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.layout.Location;
import ch.systemsx.cisd.openbis.uitest.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.menu.UtilitiesMenu;
import ch.systemsx.cisd.openbis.uitest.webdriver.Pages;

/**
 * @author pkupczyk
 */
public class OpenbisJsWebappLocation implements Location<OpenbisJsCommonWebapp>
{

    @Override
    public void moveTo(Pages pages)
    {
        pages.load(TopBar.class).utilitiesMenu();
        pages.load(UtilitiesMenu.class).openbisJsWebapp();
    }

    @Override
    public String getTabName()
    {
        return "openbis-test.js";
    }

    @Override
    public Class<OpenbisJsCommonWebapp> getPage()
    {
        WebElement tabElement = SeleniumTest.driver.findElement(By.id("openbis_webapp_openbis-test_tab"));
        WebElement iframeElement = tabElement.findElement(By.tagName("iframe"));
        SeleniumTest.driver.switchTo().frame(iframeElement);
        return OpenbisJsCommonWebapp.class;
    }
}