/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.LinkedDataFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectToManyRelationTranslator;

public abstract class ObjectToContentCopiesTranslator extends ObjectToManyRelationTranslator<ContentCopy, LinkedDataFetchOptions>
        implements IObjectToContentCopiesTranslator
{

    @Autowired
    private IContentCopyTranslator contentCopyTranslator;

    @Override
    protected Map<Long, ContentCopy> translateRelated(TranslationContext context, Collection<Long> relatedIds,
            LinkedDataFetchOptions relatedFetchOptions)
    {

        return contentCopyTranslator.translate(context, relatedIds, relatedFetchOptions);
    }

    @Override
    protected Collection<ContentCopy> createCollection()
    {
        return new LinkedHashSet<ContentCopy>();
    }
}
