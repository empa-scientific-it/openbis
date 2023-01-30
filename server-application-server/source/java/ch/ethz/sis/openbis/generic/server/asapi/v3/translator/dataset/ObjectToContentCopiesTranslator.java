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
