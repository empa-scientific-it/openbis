/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.hibernate.Hibernate;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwnerFinder.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.SampleTypeCode;

/**
 * Test cases for corresponding {@link SampleDAO} class.
 * 
 * @author Tomasz Pylak
 */
@Test(groups =
    { "db", "sample" })
public final class SampleDAOTest extends AbstractDAOTest
{
    @Test
    public final void testListGroupSamples()
    {
        SampleTypePE sampleType = getSampleType(SampleTypeCode.MASTER_PLATE);
        GroupPE group = createGroup("xxx");
        daoFactory.getGroupDAO().createGroup(group);

        SamplePE sample = createSample(sampleType, "code", null, SampleOwner.createGroup(group));
        save(sample);

        List<SamplePE> samples =
                daoFactory.getSampleDAO().listSamplesByTypeAndGroup(sampleType, group);
        assertEquals(1, samples.size());
        assertEquals(sample, samples.get(0));
    }

    @Test
    public final void testListSamplesFetchRelations()
    {
        SampleTypePE type1 = getSampleType(SampleTypeCode.MASTER_PLATE);
        SampleTypePE type2 = getSampleType(SampleTypeCode.DILUTION_PLATE);
        SampleTypePE type3 = getSampleType(SampleTypeCode.CELL_PLATE);

        type3.setContainerHierarchyDepth(1);
        type3.setGeneratedFromHierarchyDepth(1);

        SamplePE sampleA = createSample(type1, "grandParent", null);
        SamplePE sampleB = createSample(type2, "parent", sampleA);
        SamplePE sampleC = createSample(type3, "child", sampleB);
        save(sampleA, sampleB, sampleC);

        SamplePE well = createSample(type3, "well", null);
        SamplePE container = createSample(type2, "container", null);
        SamplePE superContainer = createSample(type2, "superContainer", null);
        well.setContainer(container);
        container.setContainer(superContainer);
        save(superContainer, container, well);

        // clear session to avoid using samples from first level cache
        sessionFactory.getCurrentSession().clear();
        List<SamplePE> samples = listSamplesFromHomeDatabase(type3);

        SamplePE foundWell = findSample(well, samples);
        AssertJUnit.assertTrue(Hibernate.isInitialized(foundWell.getContainer()));
        SamplePE foundContainer = foundWell.getContainer();
        AssertJUnit.assertFalse(Hibernate.isInitialized(foundContainer.getContainer()));

        sampleC = findSample(sampleC, samples);
        AssertJUnit.assertTrue(Hibernate.isInitialized(sampleC.getGeneratedFrom()));
        SamplePE parent = sampleC.getGeneratedFrom();
        AssertJUnit.assertFalse(Hibernate.isInitialized(parent.getGeneratedFrom()));
    }

    private List<SamplePE> listSamplesFromHomeDatabase(SampleTypePE sampleType)
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        return sampleDAO.listSamplesByTypeAndDatabaseInstance(sampleType, daoFactory
                .getHomeDatabaseInstance());
    }

    private SamplePE findSample(SamplePE sample, List<SamplePE> samples)
    {
        int sampleIx = samples.indexOf(sample);
        assert sampleIx != -1 : "sample not found " + sample;
        return samples.get(sampleIx);
    }

    private void save(SamplePE... samples)
    {
        ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        for (SamplePE samplePE : samples)
        {
            sampleDAO.createSample(samplePE);
        }
    }

    private SampleTypePE getSampleType(SampleTypeCode sampleTypeCode)
    {
        SampleTypePE sampleType =
                daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode.getCode());
        assert sampleType != null;
        return sampleType;
    }

    final SamplePE createSample(final SampleTypePE type, final String code, SamplePE generatorOrNull)
    {
        SampleOwner owner =
                SampleOwner.createDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        return createSample(type, code, generatorOrNull, owner);
    }

    final SamplePE createSample(final SampleTypePE type, final String code,
            SamplePE generatorOrNull, SampleOwner sampleOwner)
    {
        final SamplePE sample = new SamplePE();
        sample.setRegistrator(getSystemPerson());
        sample.setCode(code);
        sample.setSampleType(type);
        sample.setDatabaseInstance(sampleOwner.tryGetDatabaseInstance());
        sample.setGroup(sampleOwner.tryGetGroup());
        sample.setGeneratedFrom(generatorOrNull);
        return sample;
    }
}
