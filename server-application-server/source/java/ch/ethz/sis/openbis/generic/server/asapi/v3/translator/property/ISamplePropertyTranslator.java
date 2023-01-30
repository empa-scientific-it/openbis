package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property;

import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;

public interface ISamplePropertyTranslator extends ITranslator<Long, ObjectHolder<Map<String, Sample>>, SampleFetchOptions>
{

}
