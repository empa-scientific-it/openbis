/*
 * Copyright 2008 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DataSetSearchTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FileFormatTypeGridTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VocabularyBrowserTest;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetEditorTest;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetViewerTest;

/**
 * @author Franz-Josef Elmer
 */
public class SystemTestSuite extends GWTTestSuite
{
    public static Test suite()
    {
        final TestSuite testSuite = new TestSuite("System Tests");
        testSuite.addTestSuite(AuthenticationTest.class);
        testSuite.addTestSuite(AuthorizationGroupsTest.class);
        testSuite.addTestSuite(VocabularyBrowserTest.class);
        testSuite.addTestSuite(FileFormatTypeGridTest.class);
        testSuite.addTestSuite(DataSetSearchTest.class);
        testSuite.addTestSuite(GenericDataSetViewerTest.class);
        testSuite.addTestSuite(GenericDataSetEditorTest.class);
        testSuite.addTestSuite(AuthorizationManagementConsolTest.class);
 /*      
  * TODO 2010-01-11 Commented out tests until we can figure out what the problem is with
  * running the new tests under GWT 2.0. 
        testSuite.addTestSuite(SampleBrowserTest.class);
        testSuite.addTestSuite(GenericSampleViewerTest.class);
        testSuite.addTestSuite(SearchTest.class);
        testSuite.addTestSuite(GenericSampleRegistrationTest.class);

        testSuite.addTestSuite(ExperimentBrowserTest.class);
        testSuite.addTestSuite(MaterialBrowserTest.class);

        // TODO 2009-01-21, IA: Uncomment after it's clear how to deal with
        // (NS_ERROR_DOM_SECURITY_ERR): Security error on CC
        // testSuite.addTestSuite(GenericExperimentRegistrationTest.class);

        testSuite.addTestSuite(GenericExperimentViewerTest.class);
        testSuite.addTestSuite(GenericExperimentEditorTest.class);
        testSuite.addTestSuite(PropertyTypeBrowserTest.class);
        testSuite.addTestSuite(PropertyTypeAssignmentBrowserTest.class);
        testSuite.addTestSuite(EntityTypePropertyTypeAssignmentTest.class);
        testSuite.addTestSuite(VocabularyRegistrationTest.class);
        testSuite.addTestSuite(PropertyTypeRegistrationTest.class);
        testSuite.addTestSuite(GenericExperimentAttachmentDownloadTest.class);
        testSuite.addTestSuite(ProjectRegistrationTest.class);
        testSuite.addTestSuite(EntityTypeBrowserTest.class);
*/        
        return testSuite;
    }
}