package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

@Component
public class LinkedDataContentCopiesTranslator extends ObjectToContentCopiesTranslator implements ILinkedDataContentCopiesTranslator
{

    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet dataSetIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        return query.getContentCopyIds(dataSetIds);
    }
}
