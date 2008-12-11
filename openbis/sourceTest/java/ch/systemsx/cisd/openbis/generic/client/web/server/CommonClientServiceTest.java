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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.DefaultResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;

/**
 * Test cases for corresponding {@link CommonClientService} class.
 * 
 * @author Christian Ribeaud
 */
public final class CommonClientServiceTest extends AbstractClientServiceTest
{
    private CommonClientService commonClientService;

    private ICommonServer commonServer;

    private final static ListSampleCriteria createListCriteria()
    {
        final ListSampleCriteria criteria = new ListSampleCriteria();
        final SampleType sampleType = createSampleType("MASTER_PLATE", "DB1");
        criteria.setSampleType(sampleType);
        return criteria;
    }

    private final static SampleType createSampleType(final String code, final String dbCode)
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode(code);
        final DatabaseInstance databaseInstance = new DatabaseInstance();
        databaseInstance.setCode(dbCode);
        sampleType.setDatabaseInstance(databaseInstance);
        return sampleType;
    }

    private final static List<Sample> createSampleList()
    {
        return Collections.emptyList();
    }

    //
    // AbstractClientServiceTest
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        commonClientService = new CommonClientService(commonServer, requestContextProvider);
        commonServer = context.mock(ICommonServer.class);
    }

    @Test
    public final void testListSamples()
    {
        final String resultSetKey = "1";
        final DefaultResultSet<String, Sample> defaultResultSet =
                new DefaultResultSet<String, Sample>(resultSetKey, createSampleList(), 0);
        final ListSampleCriteria listCriteria = createListCriteria();
        context.checking(new Expectations()
            {
                {
                    prepareGetSession(this);
                    prepareGetSessionToken(this);
                    prepareGetResultSetManager(this);

                    one(resultSetManager).getResultSet(with(listCriteria),
                            getOriginalDataProvider());
                    will(returnValue(defaultResultSet));
                }

                @SuppressWarnings("unchecked")
                private final IOriginalDataProvider<Sample> getOriginalDataProvider()
                {
                    return with(any(IOriginalDataProvider.class));
                }

            });
        final ResultSet<Sample> resultSet = commonClientService.listSamples(listCriteria);
        assertEquals(0, resultSet.getList().size());
        assertEquals(resultSetKey, resultSet.getResultSetKey());
        assertEquals(0, resultSet.getTotalLength());
        context.assertIsSatisfied();
    }

}
