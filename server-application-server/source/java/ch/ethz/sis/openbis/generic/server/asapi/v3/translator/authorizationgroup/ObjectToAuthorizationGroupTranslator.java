/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.authorizationgroup;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.authorizationgroup.IAuthorizationGroupTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectToOneRelationTranslator;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public abstract class ObjectToAuthorizationGroupTranslator
        extends ObjectToOneRelationTranslator<AuthorizationGroup, AuthorizationGroupFetchOptions>
        implements IObjectToAuthorizationGroupTranslator
{
    @Autowired
    private IAuthorizationGroupTranslator translator;

    @Override
    protected Map<Long, AuthorizationGroup> translateRelated(TranslationContext context, Collection<Long> relatedIds,
            AuthorizationGroupFetchOptions relatedFetchOptions)
    {
        return translator.translate(context, relatedIds, relatedFetchOptions);
    }
}
