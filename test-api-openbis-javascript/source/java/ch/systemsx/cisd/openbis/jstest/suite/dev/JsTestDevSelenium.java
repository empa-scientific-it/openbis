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

package ch.systemsx.cisd.openbis.jstest.suite.dev;

import ch.systemsx.cisd.openbis.jstest.page.OpenbisJsCommonWebapp;
import ch.systemsx.cisd.openbis.jstest.suite.common.JsTestCommonSelenium;
import ch.systemsx.cisd.openbis.uitest.layout.Location;

/**
 * @author pkupczyk
 */
public class JsTestDevSelenium extends JsTestCommonSelenium
{
    @Override
    protected void runTests(String method, Location<OpenbisJsCommonWebapp> location)
    {
        try
        {
            Thread.sleep(1000 * 60 * 60 * 24);
        } catch (Exception e)
        {
            new RuntimeException(e);
        }
    }

    @Override
    protected void doBefore()
    {
    }

    @Override
    protected void doAfter()
    {
    }

}
