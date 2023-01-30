/*
 * Copyright 2012 ETH Zuerich, CISD
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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Pawel Glyzewski
 */
public class MetaprojectDAOTest extends AbstractDAOTest
{

    @Test
    public void testFindByOwnerAndName()
    {
        MetaprojectPE metaproject =
                daoFactory.getMetaprojectDAO().tryFindByOwnerAndName("test", "TEST_METAPROJECTS");
        assertEquals(Long.valueOf(1), metaproject.getId());

        metaproject =
                daoFactory.getMetaprojectDAO().tryFindByOwnerAndName("test",
                        "ANOTHER_TEST_METAPROJECTS");
        assertEquals(Long.valueOf(3), metaproject.getId());
    }

    @Test
    public void testListMetaprojects()
    {
        List<MetaprojectPE> metaprojects =
                daoFactory.getMetaprojectDAO().listMetaprojects(getTestPerson());

        assertEquals(2, metaprojects.size());

        for (MetaprojectPE metaproject : metaprojects)
        {
            if (metaproject.getId() == 1)
            {
                assertEquals("TEST_METAPROJECTS", metaproject.getName());
                assertEquals("Example metaproject no. 1", metaproject.getDescription());
                assertEquals(getTestPerson(), metaproject.getOwner());
                assertTrue(metaproject.isPrivate());
                assertNotNull(metaproject.getCreationDate());
            } else if (metaproject.getId() == 3)
            {
                assertEquals("ANOTHER_TEST_METAPROJECTS", metaproject.getName());
                assertEquals("Another example metaproject", metaproject.getDescription());
                assertEquals(getTestPerson(), metaproject.getOwner());
                assertTrue(metaproject.isPrivate());
                assertNotNull(metaproject.getCreationDate());
            } else
            {
                fail();
            }
        }
    }

    @Test
    public void testDeleteMetaproject()
    {
        List<MetaprojectPE> metaprojects =
                daoFactory.getMetaprojectDAO().listMetaprojects(getTestPerson());
        assertEquals(2, metaprojects.size());

        daoFactory.getMetaprojectDAO().delete(metaprojects.get(0));

        metaprojects = daoFactory.getMetaprojectDAO().listMetaprojects(getTestPerson());
        assertEquals(1, metaprojects.size());
    }

    @Test
    public void testCreateMetaproject()
    {
        MetaprojectPE newMetaproject = new MetaprojectPE();
        newMetaproject.setOwner(getTestPerson());
        newMetaproject.setName("MY_METAPROJECT");
        newMetaproject.setDescription("This in a new metaproject");

        daoFactory.getMetaprojectDAO().createOrUpdateMetaproject(newMetaproject, getTestPerson());

        List<MetaprojectPE> metaprojects =
                daoFactory.getMetaprojectDAO().listMetaprojects(getTestPerson());
        assertEquals(3, metaprojects.size());

        for (MetaprojectPE metaproject : metaprojects)
        {
            if (metaproject.getId() == 1)
            {
                assertEquals("TEST_METAPROJECTS", metaproject.getName());
                assertEquals("Example metaproject no. 1", metaproject.getDescription());
                assertEquals(getTestPerson(), metaproject.getOwner());
                assertTrue(metaproject.isPrivate());
                assertNotNull(metaproject.getCreationDate());
            } else if (metaproject.getId() == 3)
            {
                assertEquals("ANOTHER_TEST_METAPROJECTS", metaproject.getName());
                assertEquals("Another example metaproject", metaproject.getDescription());
                assertEquals(getTestPerson(), metaproject.getOwner());
                assertTrue(metaproject.isPrivate());
                assertNotNull(metaproject.getCreationDate());
            } else
            {
                assertTrue(metaproject.getId() > 3);
                assertEquals("MY_METAPROJECT", metaproject.getName());
                assertEquals("This in a new metaproject", metaproject.getDescription());
                assertEquals(getTestPerson(), metaproject.getOwner());
                assertTrue(metaproject.isPrivate());
                assertNotNull(metaproject.getCreationDate());
            }
        }
    }

    @Test
    public void testUpdateMetaproject()
    {
        MetaprojectPE updatedMetaproject =
                daoFactory.getMetaprojectDAO().getByTechId(new TechId(1L));

        updatedMetaproject.setDescription("New description");
        daoFactory.getMetaprojectDAO().createOrUpdateMetaproject(updatedMetaproject,
                getTestPerson());

        List<MetaprojectPE> metaprojects =
                daoFactory.getMetaprojectDAO().listMetaprojects(getTestPerson());
        assertEquals(2, metaprojects.size());

        boolean checked = false;
        for (MetaprojectPE metaproject : metaprojects)
        {
            if (metaproject.getId() == 1)
            {
                assertEquals("TEST_METAPROJECTS", metaproject.getName());
                assertEquals("New description", metaproject.getDescription());
                assertEquals(getTestPerson(), metaproject.getOwner());
                assertTrue(metaproject.isPrivate());
                assertNotNull(metaproject.getCreationDate());
                checked = true;
            }
        }

        assertTrue(checked);
    }

    @Test
    public void testListMetaprojectsForEntity()
    {
        ExperimentPE experiment = daoFactory.getExperimentDAO().getByTechId(new TechId(4));
        Collection<MetaprojectPE> connectedMetaprojects =
                daoFactory.getMetaprojectDAO().listMetaprojectsForEntity(getTestPerson(),
                        experiment);

        assertEquals(2, connectedMetaprojects.size());
        for (MetaprojectPE metaproject : connectedMetaprojects)
        {
            assertTrue(metaproject.getId().longValue() == 1l
                    || metaproject.getId().longValue() == 3l);
        }
    }

    @Test
    public void testListMetaprojectsForEntities()
    {
        ExperimentPE experiment1 = daoFactory.getExperimentDAO().getByTechId(new TechId(4));
        ExperimentPE experiment2 = daoFactory.getExperimentDAO().getByTechId(new TechId(23));

        Collection<MetaprojectAssignmentPE> assignments =
                daoFactory.getMetaprojectDAO().listMetaprojectAssignmentsForEntities(
                        getTestPerson(), Arrays.asList(new IEntityInformationWithPropertiesHolder[]
                        { experiment1, experiment2 }), EntityKind.EXPERIMENT);

        assertEquals(3, assignments.size());
        for (MetaprojectAssignmentPE assignment : assignments)
        {
            assertTrue(assignment.getMetaproject().getId().longValue() == 1l
                    || assignment.getMetaproject().getId().longValue() == 3l);
        }
    }
}
