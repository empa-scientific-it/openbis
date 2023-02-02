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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.plugin.screening.client.web.server.resultset.PlateMetadataProvider.CONTENT_PROPERTY_PREFIX;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.server.resultset.PlateMetadataProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ObjectCreationUtilForTests;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.PlateMetadataGridColumnIDs;

/**
 * @author Franz-Josef Elmer
 */
public class PlateMetadataProviderTest extends AbstractServerTestCase
{
    private IScreeningServer service;

    private TechId plateId;

    private PlateMetadataProvider provider;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        service = context.mock(IScreeningServer.class);
        plateId = new TechId(42);
        provider = new PlateMetadataProvider(service, SESSION_TOKEN, plateId);
    }

    @Test(groups = "slow")
    public void test()
    {
        final WellMetadata wellMetadata = new WellMetadata();
        Sample wellSample = new Sample();
        wellSample.setCode("my-code");
        SampleType sampleType = new SampleType();
        sampleType.setCode("my-type");
        wellSample.setSampleType(sampleType);
        EntityProperty property = ObjectCreationUtilForTests.createIntProperty("answer", 42);
        wellSample.setProperties(Arrays.<IEntityProperty> asList(property));
        wellMetadata.setWellSample(wellSample, null);
        context.checking(new Expectations()
            {
                {
                    one(service).getPlateContent(SESSION_TOKEN, plateId);
                    Sample plate = new Sample();
                    will(returnValue(new PlateContent(new PlateMetadata(plate, Arrays
                            .asList(wellMetadata), 0, 0), null, null, null)));
                }
            });

        TypedTableModel<WellMetadata> tableModel = provider.getTableModel(Integer.MAX_VALUE);

        List<TableModelColumnHeader> headers = tableModel.getHeader();
        assertEquals(null, headers.get(0).getTitle());
        assertEquals(PlateMetadataGridColumnIDs.CODE, headers.get(0).getId());
        assertEquals(0, headers.get(0).getIndex());
        assertEquals(DataTypeCode.VARCHAR, headers.get(0).getDataType());
        assertEquals(null, headers.get(1).getTitle());
        assertEquals(PlateMetadataGridColumnIDs.TYPE, headers.get(1).getId());
        assertEquals(1, headers.get(1).getIndex());
        assertEquals(DataTypeCode.VARCHAR, headers.get(1).getDataType());
        assertEquals("answer", headers.get(2).getTitle());
        assertEquals(CONTENT_PROPERTY_PREFIX + "USER-ANSWER", headers.get(2).getId());
        assertEquals(2, headers.get(2).getIndex());
        assertEquals(DataTypeCode.INTEGER, headers.get(2).getDataType());
        assertEquals(3, headers.size());

        List<TableModelRowWithObject<WellMetadata>> rows = tableModel.getRows();
        assertSame(wellMetadata, rows.get(0).getObjectOrNull());
        assertEquals("[my-code, my-type, 42]", rows.get(0).getValues().toString());
        assertEquals(1, rows.size());

        context.assertIsSatisfied();
    }

}
