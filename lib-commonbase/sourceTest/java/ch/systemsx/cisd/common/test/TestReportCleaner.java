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
