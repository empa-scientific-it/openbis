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
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;

/**
 * @author Pawel Glyzewski
 */
public class ExternalDataManagementSystemDAOTest extends AbstractDAOTest
{
    private static final String EXAMPLE_CODE = "TRALALA";

    private static final String EXAMPLE_LABEL = "External openBIS server";

    @Test
    public void testCreate()
    {
        ExternalDataManagementSystemPE edms = new ExternalDataManagementSystemPE();
        edms.setCode(EXAMPLE_CODE);
        edms.setAddress("http://www.google.com/${code}");
        edms.setLabel(EXAMPLE_LABEL);
        edms.setAddressType(ExternalDataManagementSystemType.OPENBIS);
        daoFactory.getExternalDataManagementSystemDAO().createOrUpdateExternalDataManagementSystem(
                edms);
        assertNotNull(edms.getId());

        ExternalDataManagementSystemPE fromDB =
                daoFactory.getExternalDataManagementSystemDAO()
                        .tryToFindExternalDataManagementSystemByCode(EXAMPLE_CODE);

        assertEquals(edms, fromDB);

        assertEquals(4, daoFactory.getExternalDataManagementSystemDAO()
                .listExternalDataManagementSystems().size());
    }

    @Test
    public void testListExternalDataManagementSystems()
    {
        List<ExternalDataManagementSystemPE> edmses =
                daoFactory.getExternalDataManagementSystemDAO().listExternalDataManagementSystems();

        assertEquals(3, edmses.size());

        for (ExternalDataManagementSystemPE edms : edmses)
        {
            if (edms.getId() == 1L)
            {
                assertEquals("DMS_1", edms.getCode());
                assertEquals("Test EDMS", edms.getLabel());
                assertEquals("http://example.edms.pl/code=${code}", edms.getAddress());
                assertFalse(edms.isOpenBIS());
            } else if (edms.getId() == 2L)
            {
                assertEquals("DMS_2", edms.getCode());
                assertEquals("Test External openBIS instance", edms.getLabel());
                assertEquals("http://www.openbis.ch/perm_id=${code}", edms.getAddress());
                assertTrue(edms.isOpenBIS());
            }
        }
    }
}
