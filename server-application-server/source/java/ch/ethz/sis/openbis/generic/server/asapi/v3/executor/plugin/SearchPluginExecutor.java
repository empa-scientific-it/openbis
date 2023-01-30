/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.plugin;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginKindSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractIdMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractIdsMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SearchPluginExecutor
        extends AbstractSearchObjectManuallyExecutor<PluginSearchCriteria, ScriptPE>
        implements ISearchPluginExecutor
{
    @Autowired
    private IPluginAuthorizationExecutor authorizationExcutor;

    @Override
    public List<ScriptPE> search(IOperationContext context, PluginSearchCriteria criteria)
    {
        authorizationExcutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<ScriptPE> listAll()
    {
        return daoFactory.getScriptDAO().listAllEntities();
    }

    @Override
    protected Matcher<ScriptPE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof IdsSearchCriteria)
        {
            return new IdsMatcher();
        } else if (criteria instanceof NameSearchCriteria)
        {
            return new NameMatcher();
        } else if (criteria instanceof PluginKindSearchCriteria)
        {
            return new PluginKindMatcher();
        } else if (criteria instanceof PluginTypeSearchCriteria)
        {
            return new PluginTypeMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends AbstractIdMatcher<ScriptPE>
    {
        @Override
        protected AbstractIdsMatcher<ScriptPE> createIdsMatcher()
        {
            return new IdsMatcher();
        }
    }
    
    private class IdsMatcher extends AbstractIdsMatcher<ScriptPE>
    {
        @Override
        protected boolean addPermIdIfPossible(Collection<String> permIds, IObjectId id)
        {
            if (id instanceof PluginPermId == false)
            {
                return false;
            }
            permIds.add(((PluginPermId) id).getPermId());
            return true;
        }
    }
    
    private static final class NameMatcher extends StringFieldMatcher<ScriptPE>
    {
        @Override
        protected String getFieldValue(ScriptPE plugin)
        {
            return plugin.getName();
        }
    }
    
    private static final class PluginKindMatcher extends SimpleFieldMatcher<ScriptPE>
    {
        @Override
        protected boolean isMatching(IOperationContext context, ScriptPE script, ISearchCriteria criteria)
        {
            PluginKind fieldValue = ((PluginKindSearchCriteria) criteria).getFieldValue();
            return fieldValue == null ? true : script.getPluginType().name().equals(fieldValue.name());
        }
    }
    
    private static final class PluginTypeMatcher extends SimpleFieldMatcher<ScriptPE>
    {
        @Override
        protected boolean isMatching(IOperationContext context, ScriptPE script, ISearchCriteria criteria)
        {
            PluginType fieldValue = ((PluginTypeSearchCriteria) criteria).getFieldValue();
            return fieldValue == null ? true : script.getScriptType().name().equals(fieldValue.name());
        }
    }
}
