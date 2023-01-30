/*
 * Copyright 2022 ETH Zuerich, SIS
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

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.jstest.layout.ELNJsWebappLocation;

/**
 * @author Franz-Josef Elmer
 *
 */
public class JsTestElnSelenium extends JsTestCommonSelenium
{
    @Override
    protected String startApplicationServer() throws Exception
    {
        JsTestCommonApplicationServer as = new JsTestCommonApplicationServer();
        as.setDumpsPath("db/eln");
        as.setDeamon(true);
        String result = as.start();
        createWebappLink();
        return result;
    }

    @Override
    protected String startDataStoreServer2() throws Exception
    {
        return "";
    }

    @Test(groups = {"eln-test"})
    public void runELNTests()
    {
        runTests("runELNTests", new ELNJsWebappLocation());
    }

}
