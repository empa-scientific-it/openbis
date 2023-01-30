/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodeMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodesMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchTagExecutor extends AbstractSearchObjectManuallyExecutor<TagSearchCriteria, MetaprojectPE> implements
        ISearchTagExecutor
{

    @Autowired
    private ITagAuthorizationExecutor authorizationExecutor;

    @Override
    public List<MetaprojectPE> search(IOperationContext context, TagSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<MetaprojectPE> listAll()
    {
        return daoFactory.getMetaprojectDAO().listAllEntities();
    }

    @Override
    protected Matcher<MetaprojectPE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof PermIdSearchCriteria)
        {
            return new PermIdMatcher();
        } else if (criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher<MetaprojectPE>();
        } else if (criteria instanceof CodesSearchCriteria)
        {
            return new CodesMatcher<MetaprojectPE>();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher<MetaprojectPE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, MetaprojectPE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof TagPermId)
            {
                return id.equals(new TagPermId(object.getOwner().getUserId(), object.getCode()));
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }

    }

    private class PermIdMatcher extends StringFieldMatcher<MetaprojectPE>
    {

        @Override
        protected String getFieldValue(MetaprojectPE object)
        {
            return new TagPermId(object.getOwner().getUserId(), object.getCode()).toString();
        }

    }

}
