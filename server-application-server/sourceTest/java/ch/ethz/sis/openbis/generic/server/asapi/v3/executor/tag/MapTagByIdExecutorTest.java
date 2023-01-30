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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.AbstractExecutorTest;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IGetTagIdentifierExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.MapTagByIdExecutor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMetaprojectDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class MapTagByIdExecutorTest extends AbstractExecutorTest
{

    private IMetaprojectDAO metaprojectDao;

    private IGetTagIdentifierExecutor getTagIdentifierExecutor;

    private ITagAuthorizationExecutor authorizationExecutor;

    @Override
    protected void init()
    {
        getTagIdentifierExecutor = context.mock(IGetTagIdentifierExecutor.class);
        metaprojectDao = context.mock(IMetaprojectDAO.class);
        authorizationExecutor = context.mock(ITagAuthorizationExecutor.class);
    }

    @Test
    public void testWithNull()
    {
        context.checking(new Expectations()
            {
                {
                    one(authorizationExecutor).canGet(operationContext);
                }
            });

        Map<ITagId, MetaprojectPE> map = execute(null);
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void testWithEmptyList()
    {
        context.checking(new Expectations()
            {
                {
                    one(authorizationExecutor).canGet(operationContext);
                }
            });

        Map<ITagId, MetaprojectPE> map = execute(Collections.<ITagId> emptyList());
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void testWithNonEmptyList()
    {
        final Session session = createSession();

        final ITagId tagId1 = new TagCode("TEST_TAG_1");
        final ITagId tagId2 = new TagPermId("/TEST_USER/TEST_TAG_2");
        final ITagId tagId3 = new TagPermId("/OTHER_USER/TEST_TAG_3");

        final MetaprojectPE tag1 = new MetaprojectPE();
        tag1.setName("TEST_TAG_1");
        tag1.setOwner(session.tryGetPerson());

        final MetaprojectPE tag2 = new MetaprojectPE();
        tag2.setName("TEST_TAG_2");
        tag2.setOwner(session.tryGetPerson());

        PersonPE otherUser = new PersonPE();
        otherUser.setUserId("OTHER_USER");

        final MetaprojectPE tag3 = new MetaprojectPE();
        tag3.setName("TEST_TAG_3");
        tag3.setOwner(otherUser);

        Collection<ITagId> tagIds = new LinkedList<ITagId>();
        tagIds.add(tagId1);
        tagIds.add(tagId2);
        tagIds.add(tagId3);

        context.checking(new Expectations()
            {
                {
                    one(authorizationExecutor).canGet(operationContext);

                    allowing(operationContext).getSession();
                    will(returnValue(session));

                    allowing(daoFactory).getMetaprojectDAO();
                    will(returnValue(metaprojectDao));

                    one(getTagIdentifierExecutor).getIdentifier(operationContext, tagId1);
                    will(returnValue(new MetaprojectIdentifier(session.tryGetPerson().getUserId(), "TEST_TAG_1")));

                    one(getTagIdentifierExecutor).getIdentifier(operationContext, tagId2);
                    will(returnValue(new MetaprojectIdentifier(session.tryGetPerson().getUserId(), "TEST_TAG_2")));

                    one(getTagIdentifierExecutor).getIdentifier(operationContext, tagId3);
                    will(returnValue(new MetaprojectIdentifier("OTHER_USER", "TEST_TAG_3")));

                    one(metaprojectDao).tryFindByOwnerAndName(session.tryGetPerson().getUserId(), "TEST_TAG_1");
                    will(returnValue(tag1));

                    one(metaprojectDao).tryFindByOwnerAndName(session.tryGetPerson().getUserId(), "TEST_TAG_2");
                    will(returnValue(tag2));

                    one(metaprojectDao).tryFindByOwnerAndName("OTHER_USER", "TEST_TAG_3");
                    will(returnValue(tag3));
                }
            });

        Map<ITagId, MetaprojectPE> map = execute(tagIds);

        Assert.assertEquals(3, map.size());
        Assert.assertEquals(tag1, map.get(tagId1));
        Assert.assertEquals(tag2, map.get(tagId2));
        Assert.assertEquals(tag3, map.get(tagId3));
    }

    private Map<ITagId, MetaprojectPE> execute(Collection<? extends ITagId> tagIds)
    {
        MapTagByIdExecutor executor = new MapTagByIdExecutor(daoFactory, getTagIdentifierExecutor, authorizationExecutor);
        return executor.map(operationContext, tagIds);
    }

}
