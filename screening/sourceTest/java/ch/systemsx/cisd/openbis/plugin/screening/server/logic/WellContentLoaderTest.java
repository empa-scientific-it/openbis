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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.bds.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.AbstractScreeningDAOTest;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCriteria;

/**
 * @author Kaloyan Enimanev
 */
@Test(groups =
    { "db", "screening" })
public class WellContentLoaderTest extends AbstractScreeningDAOTest
{

    @Autowired
    private IScreeningBusinessObjectFactory businessObjectFactory;

    @Autowired
    IGenericServer server;

    @Test(enabled = false)
    public void testLoadWellContentsWithProperties()
    {

        String[] materialCodes = new String[]
            { "*" };
        String[] materialTypeCodes =
            { "SIRNA" };

        WellSearchCriteria searchCriteria =
                new WellSearchCriteria(ExperimentSearchCriteria.createAllExperiments(),
                        MaterialSearchCriteria.createCodesCriteria(materialCodes,
                                materialTypeCodes, false));

        Session session = createSession(getSystemPerson());
        List<WellContent> wellContents =
                WellContentLoader.load(session, businessObjectFactory,
                        daoFactory, searchCriteria);

        // the test database contains two matching wells
        assertEquals(2, wellContents.size());
        for (WellContent wc : wellContents)
        {
            assertPropertiesPresent(wc);
        }
    }

    private void assertPropertiesPresent(WellContent wellContent)
    {
        List<IEntityProperty> props = wellContent.getWellProperties();
        assertTrue("At least one property per well is expected", props.size() > 0);
        for (IEntityProperty prop : props)
        {
            MaterialEntityProperty wellProp = (MaterialEntityProperty) prop;
            assertNotNull(wellProp.getPropertyType().getCode());
            assertNotNull(wellProp.getMaterial());
            // assert the string representation actually has contents
            String stringValue = wellProp.tryGetAsString();
            assertEquals(false, StringUtils.isBlank(stringValue));
        }
    }

    /**
     * Create session for the specified person.
     */
    public final static Session createSession(final PersonPE person)
    {
        final String userId = person.getUserId();
        final Principal principal =
                new Principal(userId, person.getFirstName(), person.getLastName(),
                        person.getEmail());
        final Session session =
                new Session(userId, "xyz", principal, "localhost", System.currentTimeMillis());
        session.setPerson(person);
        return session;
    }

}
