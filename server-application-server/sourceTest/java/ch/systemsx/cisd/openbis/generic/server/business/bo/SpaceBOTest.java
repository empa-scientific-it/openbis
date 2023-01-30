/*
 * Copyright 2008 ETH Zuerich, CISD
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

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Test cases for corresponding {@link SpaceBO} class.
 * 
 * @author Christian Ribeaud
 */
public final class SpaceBOTest extends AbstractBOTest
{
    private final SpaceBO createSpaceBO()
    {
        return new SpaceBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION,
                managedPropertyEvaluatorFactory, null, null);
    }

    @Test
    public final void testSaveWithNullGroup()
    {
        boolean fail = true;
        try
        {
            createSpaceBO().save();
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testDefineWithNullCode()
    {
        final SpaceBO spaceBO = createSpaceBO();
        boolean fail = true;
        try
        {
            spaceBO.define(null, null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testDefineAndSave()
    {
        final SpaceBO spaceBO = createSpaceBO();
        final SpacePE groupDTO = new SpacePE();
        groupDTO.setCode("MY_CODE");
        context.checking(new Expectations()
            {
                {
                    one(spaceDAO).createSpace(groupDTO);
                }
            });
        spaceBO.define(groupDTO.getCode(), null);
        spaceBO.save();
        context.assertIsSatisfied();
    }
}