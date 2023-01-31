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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.Arrays;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Test cases for corresponding {@link EncapsulatedOpenBISService} class.
 * 
 * @author Basil Neff
 */
public class EncapsulatedOpenBISServiceTest
{
    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private IServiceForDataStoreServer limsService;

    private EncapsulatedOpenBISService encapsulatedLimsService;

    private OpenBISSessionHolder session;

    private IShareIdManager shareIdManager;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        limsService = context.mock(IServiceForDataStoreServer.class);
        shareIdManager = context.mock(IShareIdManager.class);
        session = new OpenBISSessionHolder();
        session.setSessionToken(SESSION_TOKEN);
        encapsulatedLimsService = new EncapsulatedOpenBISService(limsService, session, "", shareIdManager);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetBaseExperiment()
    {
        final DataSetInformation dataSetInformation = createDataSetInformation();
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetSampleWithExperiment(SESSION_TOKEN,
                            dataSetInformation.getSampleIdentifier());
                }
            });
        encapsulatedLimsService
                .tryGetSampleWithExperiment(dataSetInformation.getSampleIdentifier());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterDataSet()
    {
        final DataSetInformation dataSetInfo = createDataSetInformation();
        final NewExternalData data = new NewExternalData();
        data.setCode("ds1");
        data.setShareId("42");
        context.checking(new Expectations()
            {
                {
                    one(limsService).registerDataSet(SESSION_TOKEN,
                            dataSetInfo.getSampleIdentifier(), data);
                    one(shareIdManager).setShareId("ds1", "42");
                }
            });
        encapsulatedLimsService.registerDataSet(dataSetInfo, data);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSampleAndDataSet()
    {
        final NewSample sample = new NewSample();
        final NewExternalData data = new NewExternalData();
        data.setCode("ds1");
        data.setShareId("42");
        context.checking(new Expectations()
            {
                {
                    one(limsService).registerSampleAndDataSet(SESSION_TOKEN, sample, data, "user-id");
                    one(shareIdManager).setShareId("ds1", "42");
                }
            });
        encapsulatedLimsService.registerSampleAndDataSet(sample, data, "user-id");
        context.assertIsSatisfied();
    }

    @Test
    public final void testUpdateSampleAndRegisterDataSet()
    {
        final SampleUpdatesDTO sample =
                new SampleUpdatesDTO(new TechId(1), Arrays.<IEntityProperty> asList(), null, null,
                        Arrays.<NewAttachment> asList(), 0, null, null, null);
        final NewExternalData data = new NewExternalData();
        data.setCode("ds1");
        data.setShareId("42");
        context.checking(new Expectations()
            {
                {
                    one(limsService).updateSampleAndRegisterDataSet(SESSION_TOKEN, sample, data);
                    one(shareIdManager).setShareId("ds1", "42");
                }
            });
        encapsulatedLimsService.updateSampleAndRegisterDataSet(sample, data);
        context.assertIsSatisfied();
    }

    @Test
    public final void testIsSampleRegisteredForDataSet()
    {
        final SampleIdentifier sampleIdentifier = SampleIdentifier.createHomeGroup("");
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryGetPropertiesOfSample(SESSION_TOKEN,
                            sampleIdentifier);
                }
            });
        encapsulatedLimsService.tryGetPropertiesOfSample(sampleIdentifier);
        context.assertIsSatisfied();
    }

    private final DataSetInformation createDataSetInformation()
    {
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setSampleCode("S1");
        return dataSetInformation;
    }
}
