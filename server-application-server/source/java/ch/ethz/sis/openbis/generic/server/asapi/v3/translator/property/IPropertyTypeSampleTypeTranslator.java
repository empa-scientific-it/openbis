package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.IObjectToOneRelationTranslator;

public interface IPropertyTypeSampleTypeTranslator extends IObjectToOneRelationTranslator<SampleType, SampleTypeFetchOptions>
{

}
