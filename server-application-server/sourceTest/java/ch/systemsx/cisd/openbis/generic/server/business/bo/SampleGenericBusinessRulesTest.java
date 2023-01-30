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

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Test cases for corresponding {@link SampleGenericBusinessRules} class.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleGenericBusinessRulesTest extends AssertJUnit
{
    private static final String DB = "db";

    private static final String GROUP_1 = "group-1";

    private static final String GROUP_2 = "group-2";

    private static final String SAMPLE_1 = "sample-1";

    private static final String SAMPLE_2 = "sample-2";

    private static final String SAMPLE_3 = "sample-3";

    private static SpacePE createGroup(String code)
    {
        SpacePE g = new SpacePE();
        g.setCode(code);
        return g;
    }

    private static SamplePE createGroupSample(SpacePE g, String code)
    {
        SamplePE s = new SamplePE();
        s.setCode(code);
        s.setSpace(g);
        return s;
    }

    private static SamplePE createSharedSample(String code)
    {
        SamplePE s = new SamplePE();
        s.setCode(code);
        return s;
    }

    @Test
    public void testGroupSampleCanHaveParentFromTheSameGroup() throws Exception
    {
        SamplePE child = createGroupSample(createGroup(GROUP_1), SAMPLE_1);
        SamplePE generator = createGroupSample(createGroup(GROUP_1), SAMPLE_2);
        SamplePE container = createGroupSample(createGroup(GROUP_1), SAMPLE_3);

        setBidirectionalRelationships(child, generator, container);
        checkBusinessRules(child);
    }

    @Test
    public void testGroupSampleCanHaveSharedParent() throws Exception
    {
        SamplePE child = createGroupSample(createGroup(GROUP_1), SAMPLE_1);
        SamplePE generator = createSharedSample(SAMPLE_2);
        SamplePE container = createSharedSample(SAMPLE_3);

        setBidirectionalRelationships(child, generator, container);
        checkBusinessRules(child);
    }

    @Test
    public void testGroupSampleCanBeContainedByParentFromDifferentGroup() throws Exception
    {
        SamplePE child = createGroupSample(createGroup(GROUP_1), SAMPLE_1);
        SamplePE container = createGroupSample(createGroup(GROUP_2), SAMPLE_2);

        setBidirectionalRelationships(child, null, container);
        checkBusinessRules(child);
    }

    @Test
    public void testGroupSampleCanBeDerivedFromParentFromDifferentGroup() throws Exception
    {
        SamplePE child = createGroupSample(createGroup(GROUP_1), SAMPLE_1);
        SamplePE generator = createGroupSample(createGroup(GROUP_2), SAMPLE_2);

        setBidirectionalRelationships(child, generator, null);
        checkBusinessRules(child);
    }

    @Test
    public void testInstanceSampleCanHaveInstanceSampleParents() throws Exception
    {
        SamplePE child = createSharedSample(SAMPLE_1);
        SamplePE generator = createSharedSample(SAMPLE_2);
        SamplePE container = createSharedSample(SAMPLE_3);

        setBidirectionalRelationships(child, generator, container);
        checkBusinessRules(child);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testInstanceSampleCannotBeContainedByGroupSample() throws Exception
    {
        SamplePE child = createSharedSample(SAMPLE_1);
        SamplePE container = createGroupSample(createGroup(GROUP_1), SAMPLE_2);

        setBidirectionalRelationships(child, null, container);
        // two asserts need to fail
        try
        {
            SampleGenericBusinessRules.assertValidContainer(child);
        } catch (UserFailureException e)
        {
            SampleGenericBusinessRules.assertValidComponents(container);
        }
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testInstanceSampleCannotBeDerivedFromGroupSample() throws Exception
    {
        SamplePE child = createSharedSample(SAMPLE_1);
        SamplePE generator = createGroupSample(createGroup(GROUP_1), SAMPLE_2);

        setBidirectionalRelationships(child, generator, null);
        // two asserts need to fail
        try
        {
            SampleGenericBusinessRules.assertValidParents(child);
        } catch (UserFailureException e)
        {
            SampleGenericBusinessRules.assertValidChildren(generator);
        }
    }

    private void setBidirectionalRelationships(SamplePE child, SamplePE generatorOrNull,
            SamplePE containerOrNull)
    {
        child.setContainer(containerOrNull);
        if (generatorOrNull != null)
        {
            SampleRelationshipPE relationship =
                    new SampleRelationshipPE(generatorOrNull, child, createRelationshipType(), null);
            child.addParentRelationship(relationship);
            generatorOrNull.addChildRelationship(relationship);
        }
        if (containerOrNull != null)
        {
            containerOrNull.getContained().add(child);
        }
    }

    private RelationshipTypePE createRelationshipType()
    {
        RelationshipTypePE type = new RelationshipTypePE();
        type.setCode(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
        return type;
    }

    private void checkBusinessRules(SamplePE child)
    {
        SampleGenericBusinessRules.assertValidParents(child);
        SampleGenericBusinessRules.assertValidChildren(child.getContainer());
        SampleGenericBusinessRules.assertValidChildren(child.getGeneratedFrom());
    }
}