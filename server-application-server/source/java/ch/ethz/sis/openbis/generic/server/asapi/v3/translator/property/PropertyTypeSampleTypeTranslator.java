package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectToOneRelationTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleTypeTranslator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

@Component
public class PropertyTypeSampleTypeTranslator extends ObjectToOneRelationTranslator<SampleType, SampleTypeFetchOptions> implements
        IPropertyTypeSampleTypeTranslator
{

    @Autowired
    private ISampleTypeTranslator sampleTypeTranslator;

    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet objectIds)
    {
        PropertyTypeQuery query = QueryTool.getManagedQuery(PropertyTypeQuery.class);
        return query.getSampleTypeIds(objectIds);
    }

    @Override
    protected Map<Long, SampleType> translateRelated(TranslationContext context, Collection<Long> relatedIds,
            SampleTypeFetchOptions relatedFetchOptions)
    {
        return sampleTypeTranslator.translate(context, relatedIds, relatedFetchOptions);
    }

}
