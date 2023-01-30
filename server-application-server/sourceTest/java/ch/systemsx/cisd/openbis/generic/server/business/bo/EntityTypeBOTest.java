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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.Arrays;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SampleTypePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Kaloyan Enimanev
 */
public class EntityTypeBOTest extends AbstractBOTest
{

    @Test
    public void testDeleteReferencedFromTrashCan()
    {
        final String sampleTypeCode = "SAMPLE_TYPE";
        final IEntityTypeBO entityTypeBO =
                new EntityTypeBO(daoFactory, EXAMPLE_SESSION, managedPropertyEvaluatorFactory, null, null);
        final EntityKind entityKind = EntityKind.SAMPLE;
        final SampleTypePE sampleType =
                new SampleTypePEBuilder().id(1).code(sampleTypeCode).getSampleType();

        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(entityKind);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(sampleType.getCode());
                    will(returnValue(sampleType));

                    one(deletionDAO).listDeletedEntitiesForType(entityKind,
                            new TechId(sampleType.getId()));
                    will(returnValue(Arrays.asList(1L, 2L)));
                }
            });

        entityTypeBO.load(entityKind, sampleTypeCode);
        try
        {
            entityTypeBO.delete();
            fail("UserFailureException expected");
        } catch (UserFailureException ufe)
        {
            assertEquals("'SAMPLE_TYPE' is referred from entities in the trash can. Please empty "
                    + "the trash can and try deleting it again.", ufe.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testAssertValidDataSetTypeMainPattern()
    {
        EntityTypeBO.assertValidDataSetTypeMainPattern(null);
        EntityTypeBO.assertValidDataSetTypeMainPattern(".*\\.csv");
        try
        {
            EntityTypeBO.assertValidDataSetTypeMainPattern("*.csv");
            fail("UserFailureException expected.");
        } catch (UserFailureException ex)
        {
            assertEquals("The pattern '*.csv' is invalid: "
                    + "Dangling meta character '*' at position 1.", ex.getMessage());
        }
    }
}
