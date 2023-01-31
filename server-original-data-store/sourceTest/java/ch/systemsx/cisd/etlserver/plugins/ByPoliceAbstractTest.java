/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;

/**
 * @author Jakub Straszewski
 */
public abstract class ByPoliceAbstractTest extends AbstractAutoArchiverPolicyTestCase
{
    protected Mockery context;

    private IDataSetPathInfoProvider pathProviderMock;

    private ISingleDataSetPathInfoProvider singleDsProviderMock;

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        context = new Mockery();
        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
        pathProviderMock = ServiceProviderTestWrapper.mock(context, IDataSetPathInfoProvider.class);
        singleDsProviderMock = ServiceProviderTestWrapper.mock(context, ISingleDataSetPathInfoProvider.class);

        ServiceProviderTestWrapper.addMock(context, IDataSetPathInfoProvider.class, pathProviderMock);

    }

    /**
     * Expect that the data set with given code will have no size, and operation to retrieve it will return given size.
     */
    protected void prepareDataSetWithNoSize(final String code, final Long sizeInBytes)
    {
        context.checking(new Expectations()
            {
                {
                    one(pathProviderMock).tryGetSingleDataSetPathInfoProvider(code);
                    if (sizeInBytes != null)
                    {
                        will(returnValue(singleDsProviderMock));

                        one(singleDsProviderMock).getRootPathInfo();
                        will(returnValue(new DataSetPathInfo()
                            {
                                {
                                    setSizeInBytes(42);
                                }
                            }));
                    }
                    else
                    {
                        will(returnValue(null));
                    }
                }
            });
    }

    @AfterMethod
    public void checkMockExpectations(ITestResult result)
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        context.assertIsSatisfied();
    }

    // Some general tests for all policies

    protected abstract IAutoArchiverPolicy getPolicy(int min, int max);

    /**
     * Creates the policy with given min and max value. To improve the robusntess of the test this method shuffles the incoming dataset and sorts the
     * result.
     */
    protected List<AbstractExternalData> filter(int min, int max, List<AbstractExternalData> dataSets)
    {
        return filter(min, max, dataSets, true);
    }

    /**
     * Creates the policy with given min and max value. The input to the filtering is not shuffled.
     */
    protected List<AbstractExternalData> filterWithoutShuffling(int min, int max, List<AbstractExternalData> dataSets)
    {
        return filter(min, max, dataSets, false);
    }

    private List<AbstractExternalData> filter(int min, int max, List<AbstractExternalData> dataSets, boolean shuffle)
    {
        IAutoArchiverPolicy policy = getPolicy(min, max);

        if (shuffle)
        {
            Collections.shuffle(dataSets, new Random(445332075L));
        }

        List<AbstractExternalData> result = new ArrayList<AbstractExternalData>(policy.filter(dataSets));

        Collections.sort(result, new SimpleComparator<AbstractExternalData, String>()
            {
                @Override
                public String evaluate(AbstractExternalData item)
                {
                    return item.getCode();
                }
            });

        return result;
    }

    @Test
    public void testNothingFoundForNoInputAndNoConfig()
    {
        IAutoArchiverPolicy policy = getPolicy(40, 100);

        List<AbstractExternalData> filtered = policy.filter(new ArrayList<AbstractExternalData>());

        assertEquals(0, filtered.size());

        context.assertIsSatisfied();
    }

    @Test
    public void testDatasetSizeIsPatched()
    {
        IAutoArchiverPolicy policy = getPolicy(40, 100);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "234", "t1", "dsNoSizeButWillGetIt", null));

        prepareDataSetWithNoSize("dsNoSizeButWillGetIt", 42L);

        List<AbstractExternalData> filtered = policy.filter(dataSets);

        assertEquals(1, filtered.size());
        assertEquals(42, filtered.get(0).getSize().longValue());

        context.assertIsSatisfied();
    }

    @Test
    public void testDatasetSizeIsNotPatched()
    {
        IAutoArchiverPolicy policy = getPolicy(40, 100);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "234", "t1", "dsNoSize", null));

        prepareDataSetWithNoSize("dsNoSize", null);

        try
        {
            policy.filter(dataSets);
            fail("Expected failure of filtering in case of unknonwn data set size");
        } catch (EnvironmentFailureException ef)
        {
            // ok
        }

        context.assertIsSatisfied();
    }

    protected void assertAllDataSetsAreNotGenerated(List<AbstractExternalData> filtered)
    {
        if (Code.extractCodes(filtered).toString().contains("generated"))
        {
            fail(getErrorMessage(filtered));
        }
    }

    protected String getErrorMessage(List<AbstractExternalData> filtered)
    {
        return "Unexpected data sets in result of filtering data sets." + Code.extractCodes(filtered).toString();
    }

    protected void assertTotalDataSetsSize(long expectedSize, List<AbstractExternalData> dataSets)
    {
        long actualSize = 0;
        for (AbstractExternalData data : dataSets)
        {
            actualSize += data.getSize();
        }
        assertEquals(getErrorMessage(dataSets), expectedSize, actualSize);
    }

}
