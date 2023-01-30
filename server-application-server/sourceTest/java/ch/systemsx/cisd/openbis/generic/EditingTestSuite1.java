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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AuthenticationTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AuthorizationGroupsTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AuthorizationManagementConsolTest;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetEditorTest;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleRegistrationTest;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class EditingTestSuite1 extends GWTTestSuite
{
    public static Test suite()
    {
        final TestSuite testSuite = new TestSuite("Editing Tests 1");
        testSuite.addTestSuite(AuthenticationTest.class);
        testSuite.addTestSuite(AuthorizationGroupsTest.class);
        testSuite.addTestSuite(GenericDataSetEditorTest.class);
        testSuite.addTestSuite(AuthorizationManagementConsolTest.class);
        testSuite.addTestSuite(GenericSampleRegistrationTest.class);

        // TODO 2009-01-21, IA: Uncomment after it's clear how to deal with
        // (NS_ERROR_DOM_SECURITY_ERR): Security error on CC
        // testSuite.addTestSuite(GenericExperimentRegistrationTest.class);
        return testSuite;
    }
}
