/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.lang.reflect.Method;

import org.jmock.Expectations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.IDssServiceRpcScreeningMultiplexer;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SamplePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SampleTypePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SpacePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = ScreeningServer.class)
public class ScreeningServerTest extends AbstractServerTestCase
{
    private static final String SAMPLE_IDENTIFIER = "/S1/P1";

    private static final String PERM_ID = "1234-56";

    private IScreeningBusinessObjectFactory screeningBOFactory;

    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;

    private IDssServiceRpcScreeningMultiplexer dssMultiplexer;

    private ScreeningServer server;

    private SamplePE exampleSample;

    @BeforeMethod
    public void beforeMethod()
    {
        screeningBOFactory = context.mock(IScreeningBusinessObjectFactory.class);
        sampleTypeSlaveServerPlugin = context.mock(ISampleTypeSlaveServerPlugin.class);
        dataSetTypeSlaveServerPlugin = context.mock(IDataSetTypeSlaveServerPlugin.class);
        dssMultiplexer = context.mock(IDssServiceRpcScreeningMultiplexer.class);
        server =
                new ScreeningServer(sessionManager, daoFactory, propertiesBatchManager,
                        screeningBOFactory, sampleTypeSlaveServerPlugin,
                        dataSetTypeSlaveServerPlugin, dssMultiplexer);
        PropertyTypePE propertyType =
                CommonTestUtils.createPropertyType("A", DataTypeCode.VARCHAR, null, null);
        SampleTypePEBuilder typeBuilder = new SampleTypePEBuilder().id(1).code("ABC");
        EntityTypePropertyTypePE etpt =
                typeBuilder.assign(propertyType).getEntityTypePropertyType();
        SamplePEBuilder builder =
                new SamplePEBuilder(137, true)
                        .space(new SpacePEBuilder().code("S1").getSpace())
                        .code("P1").permID(PERM_ID).type(typeBuilder.getSampleType())
                        .property(etpt, "hello");
        exampleSample = builder.getSample();
    }

    @AfterMethod
    public void tearDown(Method method)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(method.getName() + "() : ", t);
        }
    }

    @Test
    public void testGetPlateSampleViaPermID()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).getDAOFactory();
                    one(screeningBOFactory).createSampleBO(session);
                    will(returnValue(sampleBO));

                    one(sampleBO).loadBySamplePermId(PERM_ID);
                    one(sampleBO).getSample();
                    will(returnValue(exampleSample));
                }
            });

        Sample sample =
                server.getPlateSample(SESSION_TOKEN, PlateIdentifier.createFromPermId(PERM_ID));

        assertEquals("P1", sample.getCode());
        assertEquals(PERM_ID, sample.getPermId());
        assertEquals("/S1/P1", sample.getIdentifier());
        assertEquals("ABC", sample.getSampleTypeCode());
        assertEquals(Long.valueOf(1), sample.getSampleTypeId());
        assertEquals(null, sample.getExperimentIdentifierOrNull());
        assertEquals("{A=hello}", sample.getProperties().toString());
    }

    @Test(groups = "slow")
    public void testGetPlateSampleViaAugmentedCode()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).getDAOFactory();
                    one(screeningBOFactory).createSampleBO(session);
                    will(returnValue(sampleBO));

                    one(sampleBO).loadBySampleIdentifier(
                            SampleIdentifierFactory.parse(SAMPLE_IDENTIFIER));
                    one(sampleBO).getSample();
                    will(returnValue(exampleSample));
                }
            });

        Sample sample =
                server.getPlateSample(SESSION_TOKEN,
                        PlateIdentifier.createFromAugmentedCode(SAMPLE_IDENTIFIER));

        assertEquals("P1", sample.getCode());
        assertEquals(PERM_ID, sample.getPermId());
        assertEquals("/S1/P1", sample.getIdentifier());
        assertEquals("ABC", sample.getSampleTypeCode());
        assertEquals(Long.valueOf(1), sample.getSampleTypeId());
        assertEquals(null, sample.getExperimentIdentifierOrNull());
        assertEquals("{A=hello}", sample.getProperties().toString());
    }
}
