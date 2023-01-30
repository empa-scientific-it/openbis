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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;

public class LazyLoader implements InvocationHandler
{

    private String reference;

    private WebElement element;

    private WebElement context;

    public static Object newInstance(String id)
    {
        return java.lang.reflect.Proxy.newProxyInstance(
                WebElement.class.getClassLoader(),
                new Class<?>[]
                { WebElement.class, WrapsElement.class },
                new LazyLoader(id, null));
    }

    public static Object newInstance(String xpath, WebElement context)
    {
        return java.lang.reflect.Proxy.newProxyInstance(
                WebElement.class.getClassLoader(),
                new Class<?>[]
                { WebElement.class, WrapsElement.class },
                new LazyLoader(xpath, context));
    }

    private LazyLoader(String reference, WebElement context)
    {
        this.reference = reference;
        this.context = context;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
    {
        try
        {
            if (element == null)
            {
                if (context == null)
                {
                    element = SeleniumTest.driver.findElement(By.id(reference));
                } else
                {
                    element = context.findElement(By.xpath(reference));
                }
            }
            return m.invoke(element, args);
        } catch (InvocationTargetException e)
        {
            throw e.getTargetException();
        }
    }
}
