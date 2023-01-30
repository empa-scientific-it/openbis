/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.google.gwt.junit.tools.GWTTestSuite;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.DataSetSearchTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DummyAuthenticationTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FileFormatTypeGridTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VocabularyBrowserTest;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ViewingTestSuite1 extends GWTTestSuite
{
    public static Test suite()
    {
        final TestSuite testSuite = new TestSuite("Viewing Tests 1");
        testSuite.addTestSuite(DummyAuthenticationTest.class);
        testSuite.addTestSuite(VocabularyBrowserTest.class);
        testSuite.addTestSuite(FileFormatTypeGridTest.class);
        testSuite.addTestSuite(DataSetSearchTest.class);
        return testSuite;
    }
}
