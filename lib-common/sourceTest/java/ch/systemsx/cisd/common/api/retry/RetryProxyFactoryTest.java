/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.api.retry;

import java.lang.reflect.InvocationTargetException;

import org.springframework.remoting.RemoteConnectFailureException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.api.retry.config.DefaultRetryConfiguration;

/**
 * @author pkupczyk
 */
public class RetryProxyFactoryTest
{

    @BeforeMethod
    public void beforeMethod()
    {
        DefaultRetryConfiguration.getInstance().setWaitingTimeBetweenRetries(100);
    }

    @AfterMethod
    public void afterMethod()
    {
        DefaultRetryConfiguration.getInstance().reset();
    }

    @Test(expectedExceptions = RemoteConnectFailureException.class)
    public void testCreateProxyForClassWithoutAnyInterfaceAndCallCommunicationFailingRetryMethod()
            throws Throwable
    {
        // retry proxies are not created for classes without any interfaces (see RetryProxyFactory)
        testWithRetryAnnotation(new RetryClassWithoutAnyInterface(),
                new RetryClassCommunicationFailingMethod(), null);
    }

    @Test(expectedExceptions = RemoteConnectFailureException.class)
    public void testCreateProxyForClassWithoutAnyInterfaceAndCallCommunicationFailingNotRetryMethod()
            throws Throwable
    {
        testWithoutRetryAnnotation(new RetryClassWithoutAnyInterface(),
                new RetryClassCommunicationFailingMethod(), null);
    }

    @Test
    public void testCreateProxyForClassWithoutAnyInterfaceAndCallNotFailingRetryMethod()
            throws Throwable
    {
        testWithRetryAnnotation(new RetryClassWithoutAnyInterface(),
                new RetryClassNotFailingMethod(), 1);
    }

    @Test
    public void testCreateProxyForClassWithoutAnyInterfaceAndCallNotFailingNotRetryMethod()
            throws Throwable
    {
        testWithoutRetryAnnotation(new RetryClassWithoutAnyInterface(),
                new RetryClassNotFailingMethod(), 1);
    }

    @Test
    public void testCreateProxyForClassWithOneInterfaceAndCallCommunicationFailingRetryMethod()
            throws Throwable
    {
        testWithRetryAnnotation(new RetryClassWithOneInterface(),
                new RetryClassCommunicationFailingMethod(), 2);
    }

    @Test(expectedExceptions = RemoteConnectFailureException.class)
    public void testCreateProxyForClassWithOneInterfaceAndCallCommunicationFailingNotRetryMethod()
            throws Throwable
    {
        testWithoutRetryAnnotation(new RetryClassWithOneInterface(),
                new RetryClassCommunicationFailingMethod(), null);
    }

    @Test
    public void testCreateProxyForClassWithOneInterfaceAndCallNotFailingRetryMethod()
            throws Throwable
    {
        testWithRetryAnnotation(new RetryClassWithOneInterface(), new RetryClassNotFailingMethod(),
                1);
    }

    @Test
    public void testCreateProxyForClassWithOneInterfaceAndCallNotFailingNotRetryMethod()
            throws Throwable
    {
        testWithoutRetryAnnotation(new RetryClassWithOneInterface(),
                new RetryClassNotFailingMethod(), 1);
    }

    @Test
    public void testCreateProxyForClassWithManyInterfacesAndCallCommunicationFailingRetryMethod()
            throws Throwable
    {
        testWithRetryAnnotation(new RetryClassWithManyInterfaces(),
                new RetryClassCommunicationFailingMethod(), 2);
    }

    @Test(expectedExceptions = RemoteConnectFailureException.class)
    public void testCreateProxyForClassWithManyInterfacesAndCallCommunicationFailingNotRetryMethod()
            throws Throwable
    {
        testWithoutRetryAnnotation(new RetryClassWithManyInterfaces(),
                new RetryClassCommunicationFailingMethod(), null);
    }

    @Test
    public void testCreateProxyForClassWithManyInterfacesAndCallNotFailingRetryMethod()
            throws Throwable
    {
        testWithRetryAnnotation(new RetryClassWithManyInterfaces(),
                new RetryClassNotFailingMethod(), 1);
    }

    @Test
    public void testCreateProxyForClassWithManyInterfacesAndCallNotFailingNotRetryMethod()
            throws Throwable
    {
        testWithoutRetryAnnotation(new RetryClassWithManyInterfaces(),
                new RetryClassNotFailingMethod(), 1);
    }

    private void testWithRetryAnnotation(Object object, RetryClassMethod testMethodImpl,
            Integer expectedCount) throws Throwable
    {
        test(object, "testWithRetryAnnotation", testMethodImpl, expectedCount);
    }

    private void testWithoutRetryAnnotation(Object object, RetryClassMethod testMethodImpl,
            Integer expectedCount) throws Throwable
    {
        test(object, "testWithoutRetryAnnotation", testMethodImpl, expectedCount);
    }

    private void test(Object object, String testMethodName, RetryClassMethod testMethodImpl,
            Integer expectedCount) throws Throwable
    {
        try
        {
            Object proxy = RetryProxyFactory.createProxy(object);
            proxy.getClass().getMethod("setMethod", RetryClassMethod.class)
                    .invoke(proxy, testMethodImpl);
            proxy.getClass().getMethod(testMethodName).invoke(proxy);
            Integer count =
                    (Integer) testMethodImpl.getClass().getField("count").get(testMethodImpl);
            Assert.assertEquals(count, expectedCount);
        } catch (InvocationTargetException e)
        {
            throw e.getTargetException();
        }
    }

}
