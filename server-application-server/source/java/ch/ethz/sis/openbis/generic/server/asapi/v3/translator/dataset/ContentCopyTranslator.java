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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.LinkedDataFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ContentCopyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

@Component
public class ContentCopyTranslator extends AbstractCachingTranslator<Long, ContentCopy, LinkedDataFetchOptions> implements IContentCopyTranslator
{

    @Autowired
    private IContentCopyBaseTranslator baseTranslator;

    @Autowired
    private IContentCopyExternalDmsTranslator externalDmsTranslator;

    @Override
    protected ContentCopy createObject(TranslationContext context, Long input, LinkedDataFetchOptions fetchOptions)
    {
        return new ContentCopy();
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> copyIds, LinkedDataFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IContentCopyBaseTranslator.class, baseTranslator.translate(context, copyIds, null));

        if (fetchOptions.hasExternalDms())
        {
            relations.put(IContentCopyExternalDmsTranslator.class, externalDmsTranslator.translate(context, copyIds, null));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long input, ContentCopy output, Object objectRelations,
            LinkedDataFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        ContentCopyRecord baseRecord = relations.get(IContentCopyBaseTranslator.class, input);
        output.setExternalCode(baseRecord.externalCode);
        output.setPath(baseRecord.path);
        output.setGitCommitHash(baseRecord.gitCommitHash);
        output.setGitRepositoryId(baseRecord.gitRepositoryId);
        output.setId(new ContentCopyPermId(baseRecord.id.toString()));
        if (fetchOptions.hasExternalDms())
        {
            ExternalDms externalDms = relations.get(IContentCopyExternalDmsTranslator.class, input);
            output.setExternalDms(externalDms);
        }
    }

}
