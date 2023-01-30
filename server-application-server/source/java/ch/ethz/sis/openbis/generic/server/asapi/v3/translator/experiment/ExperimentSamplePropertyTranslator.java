package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.SamplePropertyRecord;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

@Component
public class ExperimentSamplePropertyTranslator
        extends ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.SamplePropertyTranslator
        implements IExperimentSamplePropertyTranslator
{

    @Override
    protected List<SamplePropertyRecord> loadSampleProperties(Collection<Long> objectIds)
    {
        ExperimentQuery query = QueryTool.getManagedQuery(ExperimentQuery.class);
        return query.getSampleProperties(new LongOpenHashSet(objectIds));
    }

}
