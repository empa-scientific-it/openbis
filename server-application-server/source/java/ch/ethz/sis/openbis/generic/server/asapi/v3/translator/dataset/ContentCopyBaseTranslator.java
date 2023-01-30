package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectBaseTranslator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

@Component
public class ContentCopyBaseTranslator extends ObjectBaseTranslator<ContentCopyRecord> implements IContentCopyBaseTranslator
{

    @Override
    protected List<ContentCopyRecord> loadRecords(LongOpenHashSet objectIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        return query.getContentCopies(new LongOpenHashSet(objectIds));
    }

}
