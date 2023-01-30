/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.managed_property;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.AbstractBOTest;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityLinkElement;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.structured.ElementFactory;

/**
 * @author Piotr Buczek
 */
public class EntityInformationProviderTest extends AbstractBOTest
{

    private EntityInformationProvider provider;

    private ElementFactory elementFactory;

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
        provider = new EntityInformationProvider(daoFactory);
        elementFactory = new ElementFactory();
    }

    @Test
    public void testGetIdentifier()
    {
        final String mCode = "mCode";
        final String mTypeCode = "mTypeCode";
        final String sPermId = "sPermId";
        final String ePermId = "ePermId";
        final String dPermId = "dPermId";
        IEntityLinkElement sampleLink = elementFactory.createSampleLink(sPermId);
        IEntityLinkElement experimentLink = elementFactory.createExperimentLink(ePermId);
        IEntityLinkElement materialLink = elementFactory.createMaterialLink(mCode, mTypeCode);
        IEntityLinkElement datasetLink = elementFactory.createDataSetLink(dPermId);

        final SamplePE sample = CommonTestUtils.createSample();
        final ExperimentPE experiment = CommonTestUtils.createExperiment();
        final DataPE dataSet = CommonTestUtils.createDataSet();
        final MaterialPE material = CommonTestUtils.createMaterial(mCode, mTypeCode);
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryToFindByPermID(sPermId);
                    will(returnValue(sample));
                    one(experimentDAO).tryGetByPermID(ePermId);
                    will(returnValue(experiment));
                    one(dataDAO).tryToFindDataSetByCode(dPermId);
                    will(returnValue(dataSet));
                    one(materialDAO).tryFindMaterial(new MaterialIdentifier(mCode, mTypeCode));
                    will(returnValue(material));
                }
            });

        String sIdentifier = provider.getIdentifier(sampleLink);
        assertEquals(sample.getIdentifier(), sIdentifier);

        String eIdentifier = provider.getIdentifier(experimentLink);
        assertEquals(experiment.getIdentifier(), eIdentifier);

        String dIdentifier = provider.getIdentifier(datasetLink);
        assertEquals(dataSet.getIdentifier(), dIdentifier);

        String mIdentifier = provider.getIdentifier(materialLink);
        assertEquals(MaterialIdentifier.print(mCode, mTypeCode), mIdentifier);

        // get identifiers of missing entities
        final String fakePermId = "fakePermId";
        final String fakeMCode = "fakeMCode";
        final String fakeMTypeCode = "fakeMTypeCode";
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryToFindByPermID(fakePermId);
                    will(returnValue(null));
                    one(experimentDAO).tryGetByPermID(fakePermId);
                    will(returnValue(null));
                    one(dataDAO).tryToFindDataSetByCode(fakePermId);
                    will(returnValue(null));
                    one(materialDAO).tryFindMaterial(
                            new MaterialIdentifier(fakeMCode, fakeMTypeCode));
                    will(returnValue(null));
                }
            });
        assertNull(provider.getIdentifier(elementFactory.createSampleLink(fakePermId)));
        assertNull(provider.getIdentifier(elementFactory.createExperimentLink(fakePermId)));
        assertNull(provider.getIdentifier(elementFactory.createDataSetLink(fakePermId)));
        assertNull(provider.getIdentifier(elementFactory.createMaterialLink(fakeMCode,
                fakeMTypeCode)));

        context.assertIsSatisfied();
    }

    @Test
    public void testGetProjectSamplePermId()
    {
        final SamplePE sample = new SamplePE();
        final ProjectPE project = new ProjectPE();
        SpacePE space = new SpacePE();
        space.setCode("S1");
        project.setSpace(space);
        project.setCode("P1");
        sample.setProject(project);
        sample.setCode("SAMP1");
        sample.setPermId("123");
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject("S1", "P1");
                    will(returnValue(project));

                    one(sampleDAO).tryfindByCodeAndProject("SAMP1", project);
                    will(returnValue(sample));
                }
            });

        String permId = provider.getProjectSamplePermId("S1", "P1", "SAMP1");

        assertEquals("123", permId);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetProjectSampleByIdentifier()
    {
        final SamplePE sample = new SamplePE();
        final ProjectPE project = new ProjectPE();
        SpacePE space = new SpacePE();
        space.setCode("S1");
        project.setSpace(space);
        project.setCode("P1");
        sample.setProject(project);
        sample.setCode("SAMP1");
        sample.setPermId("123");
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject("S1", "P1");
                    will(returnValue(project));

                    one(sampleDAO).tryfindByCodeAndProject("SAMP1", project);
                    will(returnValue(sample));
                }
            });

        String permId = provider.getSamplePermId("/S1/P1/SAMP1");

        assertEquals("123", permId);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetSpaceSampleByIdentifier()
    {
        final SamplePE sample = new SamplePE();
        final SpacePE space = new SpacePE();
        space.setCode("S1");
        sample.setSpace(space);
        sample.setCode("SAMP1");
        sample.setPermId("123");
        context.checking(new Expectations()
            {
                {
                    one(spaceDAO).tryFindSpaceByCode("S1");
                    will(returnValue(space));

                    one(sampleDAO).tryFindByCodeAndSpace("SAMP1", space);
                    will(returnValue(sample));
                }
            });

        String permId = provider.getSamplePermId("/S1/SAMP1");

        assertEquals("123", permId);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetSharedSampleByIdentifier()
    {
        final SamplePE sample = new SamplePE();
        sample.setCode("SAMP1");
        sample.setPermId("123");
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryFindByCodeAndDatabaseInstance("SAMP1");
                    will(returnValue(sample));
                }
            });

        String permId = provider.getSamplePermId("/SAMP1");
        
        assertEquals("123", permId);
        context.assertIsSatisfied();
    }
}
