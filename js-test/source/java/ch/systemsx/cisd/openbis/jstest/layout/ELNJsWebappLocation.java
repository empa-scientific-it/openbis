package ch.systemsx.cisd.openbis.jstest.layout;

import ch.systemsx.cisd.openbis.jstest.page.OpenbisJsCommonWebapp;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.layout.Location;
import ch.systemsx.cisd.openbis.uitest.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.menu.UtilitiesMenu;
import ch.systemsx.cisd.openbis.uitest.webdriver.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ELNJsWebappLocation implements Location<OpenbisJsCommonWebapp> {

    @Override
    public void moveTo(Pages pages)
    {
        pages.load(TopBar.class).utilitiesMenu();
        pages.load(UtilitiesMenu.class).elnJSWebapp();
    }

    @Override
    public String getTabName()
    {
        return "eln-test.js";
    }

    @Override
    public Class<OpenbisJsCommonWebapp> getPage()
    {
        WebElement tabElement = SeleniumTest.driver.findElement(By.id("openbis_webapp_eln-test_tab"));
        WebElement iframeElement = tabElement.findElement(By.tagName("iframe"));
        SeleniumTest.driver.switchTo().frame(iframeElement);
        return OpenbisJsCommonWebapp.class;
    }
}
