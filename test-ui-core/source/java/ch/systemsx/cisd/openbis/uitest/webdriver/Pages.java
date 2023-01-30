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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.layout.Location;
import ch.systemsx.cisd.openbis.uitest.menu.TabBar;
import ch.systemsx.cisd.openbis.uitest.screenshot.ScreenShotter;
import ch.systemsx.cisd.openbis.uitest.widget.Widget;

/**
 * @author anttil
 */
public class Pages
{
    private ScreenShotter shotter;

    public <T extends SeleniumTest> Pages()
    {
        this.shotter = new ScreenShotter()
            {
                @Override
                public void screenshot()
                {
                }
            };
    }

    @SuppressWarnings("unchecked")
    public <T> T load(final Class<T> clazz)
    {

        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(clazz);

        MethodHandler handler = new MethodHandler()
            {
                @Override
                public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args)
                        throws Throwable
                {
                    try
                    {
                        return proceed.invoke(self, args);
                    } catch (InvocationTargetException e)
                    {
                        if (e.getTargetException() instanceof StaleElementReferenceException)
                        {
                            System.out.println("STALE REFERENCE - RELOADING "
                                    + self.getClass().getSimpleName());
                            T t = load(clazz);
                            return thisMethod.invoke(t, args);
                        } else
                        {
                            throw e.getTargetException();
                        }
                    }
                }
            };

        T t;
        try
        {
            t = (T) factory.create(new Class<?>[0], new Object[0], handler);
        } catch (IllegalArgumentException ex1)
        {
            throw new RuntimeException(ex1);
        } catch (NoSuchMethodException ex1)
        {
            throw new RuntimeException(ex1);
        } catch (InstantiationException ex1)
        {
            throw new RuntimeException(ex1);
        } catch (IllegalAccessException ex1)
        {
            throw new RuntimeException(ex1);
        } catch (InvocationTargetException ex1)
        {
            throw new RuntimeException(ex1);
        }

        return init(clazz, t);
    }

    public <U extends Widget> U initializeWidget(Class<U> widgetClass,
            WebElement context, boolean lazy)
    {
        try
        {
            U widget = widgetClass.newInstance();
            for (Field field : widgetClass.getDeclaredFields())
            {
                Contextual c = field.getAnnotation(Contextual.class);
                if (c != null)
                {
                    field.setAccessible(true);
                    WebElement element;

                    if (lazy)
                    {
                        element =
                                (WebElement) LazyLoader.newInstance(c.value(), context);
                    } else
                    {
                        element = context.findElement(By.xpath(c.value()));
                    }

                    if (Widget.class.isAssignableFrom(field.getType()))
                    {
                        @SuppressWarnings("unchecked")
                        Widget w =
                                initializeWidget((Class<? extends Widget>) field.getType(),
                                        element, lazy);
                        field.set(widget, w);
                    } else if (WebElement.class.isAssignableFrom(field.getType()))
                    {
                        field.set(widget, element);
                    } else
                    {
                        throw new RuntimeException("Cannot annotate field of type "
                                + field.getType() + " with @Contextual");
                    }
                }
            }
            return widget;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private <T> T init(final Class<T> clazz, T t)
    {
        try
        {
            Class<?> pageClass = clazz;
            while (pageClass != null)
            {
                for (Field field : pageClass.getDeclaredFields())
                {
                    Locate locate = field.getAnnotation(Locate.class);
                    if (locate != null)
                    {
                        field.setAccessible(true);

                        if (Widget.class.isAssignableFrom(field.getType()))
                        {

                            boolean lazy = field.getAnnotation(Lazy.class) != null;

                            WebElement element;
                            if (lazy)
                            {
                                element =
                                        (WebElement) LazyLoader.newInstance(locate.value());
                            } else
                            {
                                element = SeleniumTest.driver.findElement(By.id(locate.value()));
                            }

                            @SuppressWarnings("unchecked")
                            Widget widget =
                                    initializeWidget((Class<? extends Widget>) field.getType(),
                                            new WidgetContext(element,
                                                    shotter), lazy);
                            field.set(t, widget);
                        } else if (WebElement.class.isAssignableFrom(field.getType()))
                        {
                            WebElement element =
                                    new WidgetContext(SeleniumTest.driver.findElement(By.id(locate
                                            .value())), shotter);
                            field.set(t, element);
                        } else
                        {
                            throw new RuntimeException("Cannot annotate field of type "
                                    + field.getType() + " with @Locate");
                        }
                    }
                }

                pageClass = pageClass.getSuperclass();
            }

            return t;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public <T> T tryLoad(Class<T> clazz)
    {
        return tryLoad(clazz, SeleniumTest.IMPLICIT_WAIT, TimeUnit.SECONDS);
    }

    public <T> T tryLoad(Class<T> clazz, long timeout, TimeUnit unit)
    {
        SeleniumTest.setImplicitWait(timeout, unit);
        try
        {
            return load(clazz);
        } catch (NoSuchElementException e)
        {
            e.printStackTrace();
            return null;
        } finally
        {
            SeleniumTest.setImplicitWaitToDefault();
        }
    }

    public <T> T goTo(Location<T> location)
    {
        if (load(TabBar.class).selectTab(location.getTabName()) == false)
        {
            location.moveTo(this);
        }
        return load(location.getPage());
    }

    public void setScreenShotter(ScreenShotter shotter)
    {
        this.shotter = shotter;
    }

    public void screenshot()
    {
        shotter.screenshot();
    }
}
