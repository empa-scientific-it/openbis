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

package ch.systemsx.cisd.openbis.uitest.webdriver;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Coordinates;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.internal.WrapsElement;

import ch.systemsx.cisd.openbis.uitest.screenshot.ScreenShotter;

/**
 * @author anttil
 */
class WidgetContext implements WebElement, Locatable, WrapsElement
{
    private WebElement element;

    private ScreenShotter shotter;

    public WidgetContext(WebElement element, ScreenShotter shotter)
    {
        this.element = element;
        this.shotter = shotter;
    }

    @Override
    public void click()
    {
        shotter.screenshot();
        element.click();
    }

    @Override
    public String getAttribute(String key)
    {
        return element.getAttribute(key);
    }

    @Override
    public String getTagName()
    {
        return element.getTagName();
    }

    @Override
    public void clear()
    {
        shotter.screenshot();
        element.clear();
    }

    @Override
    public WebElement findElement(By arg0)
    {
        return new WidgetContext(element.findElement(arg0), shotter);
    }

    @Override
    public List<WebElement> findElements(By arg0)
    {
        List<WebElement> result = new ArrayList<WebElement>();
        for (WebElement e : element.findElements(arg0))
        {
            result.add(new WidgetContext(e, shotter));
        }
        return result;
    }

    @Override
    public String getCssValue(String arg0)
    {
        return element.getCssValue(arg0);
    }

    @Override
    public Point getLocation()
    {
        return element.getLocation();
    }

    @Override
    public Dimension getSize()
    {
        return element.getSize();
    }

    @Override
    public Rectangle getRect() {
        return element.getRect();
    }

    @Override
    public String getText()
    {
        return element.getText();
    }

    @Override
    public boolean isDisplayed()
    {
        return element.isDisplayed();
    }

    @Override
    public boolean isEnabled()
    {
        return element.isEnabled();
    }

    @Override
    public boolean isSelected()
    {
        return element.isSelected();
    }

    @Override
    public void sendKeys(CharSequence... arg0)
    {
        shotter.screenshot();
        element.sendKeys(arg0);
    }

    @Override
    public void submit()
    {
        shotter.screenshot();
        element.submit();
    }

    @Override
    public Coordinates getCoordinates()
    {
        return ((Locatable) element).getCoordinates();
    }

    @Override
    public WebElement getWrappedElement()
    {
        if (element instanceof WrapsElement)
        {
            return ((WrapsElement) element).getWrappedElement();
        }
        return element;
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        throw new WebDriverException("Method not implemented");
    }
}
