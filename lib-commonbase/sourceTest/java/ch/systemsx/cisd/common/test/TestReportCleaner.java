/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.test;

import java.util.HashSet;
import java.util.Set;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/*
 * TO-DO Clean Repeated success counts.
 * 
 * @author anttil & juanf
 */
public class TestReportCleaner extends TestListenerAdapter
{
    Set<ITestResult> failures = new HashSet<ITestResult>();

    @Override
    public void onTestFailure(ITestResult tr)
    {
        super.onTestFailure(tr);

        RetryTen testRetryAnalyzer = (RetryTen) tr.getMethod().getRetryAnalyzer();

        if (testRetryAnalyzer == null || testRetryAnalyzer.getCount() == 0)
        {
            failures.add(tr);
        }
    }

    @Override
    public void onFinish(ITestContext testContext)
    {
        // Deletes retries counted as failures at the suite level
        for (ITestResult failedResult : testContext.getFailedTests().getAllResults())
        {
            if (!failures.contains(failedResult))
            {
                testContext.getFailedTests().removeResult(failedResult.getMethod());
            }
        }
    }

}
