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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;

/**
 * @author Pawel Glyzewski
 */
public class ExternalDataManagementSystemTranslatorTest extends AssertJUnit
{

    @Test
    public void testTranslatePEtoDTO()
    {
        ExternalDataManagementSystemPE edmsPE = new ExternalDataManagementSystemPE();
        edmsPE.setId(Math.round(10000.0d * Math.random()));
        edmsPE.setCode("TEST_EDMS");
        edmsPE.setLabel("This is only a test");
        edmsPE.setAddress("http://www.facebook.com/${code}");
        edmsPE.setAddressType(ExternalDataManagementSystemType.OPENBIS);

        ExternalDataManagementSystem edms =
                ExternalDataManagementSystemTranslator.translate(edmsPE);

        assertEquals(edmsPE.getId(), edms.getId());
        assertEquals(edmsPE.getCode(), edms.getCode());
        assertEquals(edmsPE.getLabel(), edms.getLabel());
        assertEquals(edmsPE.getAddress(), edms.getAddress());
        assertTrue(edms.isOpenBIS());
    }

    @Test
    public void testTranslateDTOtoPE()
    {
        ExternalDataManagementSystemPE edmsPE = new ExternalDataManagementSystemPE();

        ExternalDataManagementSystem edms = new ExternalDataManagementSystem();
        DatabaseInstance dbin = new DatabaseInstance();
        dbin.setCode("fake instance");
        edms.setDatabaseInstance(dbin);
        edms.setId(111L);
        edms.setCode("TEST_EDMS");
        edms.setLabel("This is only a test");
        edms.setUrlTemplate("http://www.facebook.com/${code}");
        edms.setOpenBIS(false);

        ExternalDataManagementSystemTranslator.translate(edms, edmsPE);

        assertNull(edmsPE.getId());
        assertEquals(edms.getCode(), edmsPE.getCode());
        assertEquals(edms.getLabel(), edmsPE.getLabel());
        assertEquals(edms.getUrlTemplate(), edmsPE.getAddress());
        assertFalse(edmsPE.isOpenBIS());
    }
}
