/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.AbstractExecutorTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class GetTagIdentifierExecutorTest extends AbstractExecutorTest
{

    @Test
    public void testWithPermId()
    {
        final Session session = createSession();

        context.checking(new Expectations()
            {
                {
                    allowing(operationContext).getSession();
                    will(returnValue(session));

                }
            });

        MetaprojectIdentifier identifier = execute(new TagPermId("/" + session.tryGetPerson().getUserId() + "/TEST_PERM_ID"));
        Assert.assertEquals("TEST_PERM_ID", identifier.getMetaprojectName());
        Assert.assertEquals(session.tryGetPerson().getUserId(), identifier.getMetaprojectOwnerId());
    }

    @Test
    public void testWithPermIdForDifferentUser()
    {
        final Session session = createSession();

        context.checking(new Expectations()
            {
                {
                    allowing(operationContext).getSession();
                    will(returnValue(session));

                }
            });

        MetaprojectIdentifier identifier = execute(new TagPermId("/OTHER_USER/TEST_PERM_ID"));
        Assert.assertEquals("TEST_PERM_ID", identifier.getMetaprojectName());
        Assert.assertEquals("OTHER_USER", identifier.getMetaprojectOwnerId());
    }

    @Test
    public void testWithNameId()
    {
        final Session session = createSession();

        context.checking(new Expectations()
            {
                {
                    allowing(operationContext).getSession();
                    will(returnValue(session));

                }
            });

        MetaprojectIdentifier identifier = execute(new TagCode("TEST_NAME_ID"));
        Assert.assertEquals("TEST_NAME_ID", identifier.getMetaprojectName());
        Assert.assertEquals(session.tryGetPerson().getUserId(), identifier.getMetaprojectOwnerId());
    }

    private MetaprojectIdentifier execute(ITagId tagId)
    {
        GetTagIdentifierExecutor executor = new GetTagIdentifierExecutor();
        return executor.getIdentifier(operationContext, tagId);
    }

}
