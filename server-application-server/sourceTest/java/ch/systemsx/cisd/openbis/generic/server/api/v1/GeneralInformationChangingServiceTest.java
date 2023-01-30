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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.VocabularyTermBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;

/**
 * @author Franz-Josef Elmer
 */
// PLEASE, if you add here a new test add also a system test to
// ch.systemsx.cisd.openbis.systemtest.api.v1.GeneralInformationChangingServiceTest
public class GeneralInformationChangingServiceTest extends AbstractServerTestCase
{
    private static final long SAMPLE_ID = 137;

    private GeneralInformationChangingService service;

    private ICommonServer commonServer;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        commonServer = context.mock(ICommonServer.class);
        service =
                new GeneralInformationChangingService(sessionManager, daoFactory,
                        propertiesBatchManager, commonServer);
    }

    @Test
    public void testUpdateSampleProperties()
    {
        prepareGetSession();
        final RecordingMatcher<SampleUpdatesDTO> updateMatcher =
                new RecordingMatcher<SampleUpdatesDTO>();
        context.checking(new Expectations()
            {
                {
                    one(commonServer).getSampleInfo(SESSION_TOKEN, new TechId(SAMPLE_ID));
                    SampleBuilder sample =
                            new SampleBuilder("/P/S1:A03")
                                    .id(SAMPLE_ID)
                                    .experiment(
                                            new ExperimentBuilder().identifier("/S/P/E")
                                                    .getExperiment())
                                    .partOf(new SampleBuilder("/P/S1").getSample()).version(4711)
                                    .property("name", "Albert").property("age", "42");
                    sample.property("material").type(DataTypeCode.MATERIAL)
                            .value(new MaterialBuilder().code("A").type("Fluid"));
                    sample.property("level").type(DataTypeCode.CONTROLLEDVOCABULARY)
                            .value(new VocabularyTermBuilder("LOW").getTerm());
                    SampleParentWithDerived sampleParentWithDerived = new SampleParentWithDerived();
                    sampleParentWithDerived.setParent(sample.getSample());
                    will(returnValue(sampleParentWithDerived));

                    one(commonServer).updateSample(with(SESSION_TOKEN), with(updateMatcher));
                }
            });
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("age", "76");
        properties.put("greetings", "hello");
        properties.put("material", "B (Fluid)");

        service.updateSampleProperties(SESSION_TOKEN, SAMPLE_ID, properties);

        SampleUpdatesDTO updatesDTO = updateMatcher.recordedObject();
        assertEquals(SAMPLE_ID, updatesDTO.getSampleIdOrNull().getId().longValue());
        assertEquals(4711, updatesDTO.getVersion());
        List<IEntityProperty> props = updatesDTO.getProperties();
        Collections.sort(props, new Comparator<IEntityProperty>()
            {
                @Override
                public int compare(IEntityProperty o1, IEntityProperty o2)
                {
                    return o1.getPropertyType().getCode().compareTo(o2.getPropertyType().getCode());
                }
            });
        assertEquals("[age: 76, greetings: hello, material: B (Fluid)]", props.toString());
        assertEquals("/P/S1:A03", updatesDTO.getSampleIdentifier().toString());
        assertEquals("/S/P/E", updatesDTO.getExperimentIdentifierOrNull().toString());
        assertEquals("/P/S1", updatesDTO.getContainerIdentifierOrNull().toString());
        assertEquals(0, updatesDTO.getAttachments().size());
        context.assertIsSatisfied();
    }
}
