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

package ch.systemsx.cisd.openbis.jstest.suite.common;

import java.io.File;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.jstest.layout.OpenbisJsWebappLocation;
import ch.systemsx.cisd.openbis.jstest.layout.OpenbisV3JsWebappLocation;
import ch.systemsx.cisd.openbis.jstest.layout.ELNJsWebappLocation;
import ch.systemsx.cisd.openbis.jstest.page.OpenbisJsCommonWebapp;
import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.layout.Location;
import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class JsTestCommonSelenium extends SeleniumTest
{

    /** Duration of sleep in milliseconds before next check for JUnit report. */
    private static final int JUNIT_REPORT_SLEEP_DURATION = 2000;

    /** Total duration in milliseconds for JUnit report. */
    private static final int JUNIT_REPORT_TOTAL_DURATION = 1000000;

    /** How many checks for report should be performed. */
    private static final int CHECKS_COUNT = JUNIT_REPORT_TOTAL_DURATION / JUNIT_REPORT_SLEEP_DURATION;


    private void createWebappLink()
    {
        try
        {
            String jettyHome = new File(System.getProperty("jetty.home")).getAbsolutePath();
            new File(jettyHome + "/webapps").mkdirs();
            Unix.createSymbolicLink(jettyHome + "/webapps/openbis/webapp", jettyHome + "/webapps/webapp/webapp");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected String startApplicationServer() throws Exception
    {
        JsTestCommonApplicationServer as = new JsTestCommonApplicationServer();
        as.setDeamon(true);
        String result = as.start();
        createWebappLink();
        return result;
    }

    @Override
    protected String startDataStoreServer() throws Exception
    {
        JsTestCommonDataStoreServer1 dss = new JsTestCommonDataStoreServer1();
        dss.setDeamon(true);
        return dss.start();
    }

    @Override
    protected String startDataStoreServer2() throws Exception
    {
        JsTestCommonDataStoreServer2 dss = new JsTestCommonDataStoreServer2();
        dss.setDeamon(true);
        return dss.start();
    }

    @BeforeTest
    public void before()
    {
        doBefore();
    }

    protected void doBefore()
    {
        useGui();

        login(ADMIN_USER, ADMIN_PASSWORD);
    }

    @AfterTest
    public void after()
    {
        doAfter();
    }

    protected void doAfter()
    {
        logout();
    }

    @Test
    public void runOpenbisJsTests()
    {
        runTests("runOpenbisJsTests", new OpenbisJsWebappLocation());
    }

    @Test
    public void runOpenbisV3JsTests()
    {
        runTests("runOpenbisV3JsTests", new OpenbisV3JsWebappLocation());
    }

    /*
    @Test
    public void runELNTests()
    {
        runTests("runELNTests", new ELNJsWebappLocation());
    }
     */

    protected void runTests(String method, Location<OpenbisJsCommonWebapp> location)
    {
        try
        {
            OpenbisJsCommonWebapp webapp = browser().goTo(location);

            String junitReport = "";
            for (int x = 0; x < CHECKS_COUNT; x++)
            {
                junitReport = webapp.getJunitReport();
                if (junitReport.length() == 0)
                {
                    try
                    {
                        System.out.println("JUnit report is not there yet. Waiting...");
                        Thread.sleep(JUNIT_REPORT_SLEEP_DURATION);
                    } catch (InterruptedException e)
                    {
                    }
                } else
                {
                    System.out.println("JUnit report has arrived.");
                    break;
                }
            }

            Assert.assertTrue("JUnit test report is empty", junitReport.length() > 0);

            File report =
                    new File("targets/dist/" + this.getClass().getSimpleName() + "/" + method
                            + "/TEST-" + method + ".xml");
            FileUtilities.writeToFile(report, junitReport);

            Assert.assertEquals(0, webapp.getFailedCount());
        } catch (Throwable t)
        {
            throw new AssertionError("Test runner throws exception: " + t, t);
        } finally
        {
            SeleniumTest.driver.switchTo().defaultContent();
        }
    }
}
