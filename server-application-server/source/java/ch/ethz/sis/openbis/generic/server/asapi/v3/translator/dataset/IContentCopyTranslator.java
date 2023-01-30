package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.LinkedDataFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;

public interface IContentCopyTranslator extends ITranslator<Long, ContentCopy, LinkedDataFetchOptions>
{

}
