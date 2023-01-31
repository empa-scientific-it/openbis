package ch.systemsx.cisd.etlserver.plugins.grouping;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * Key provider for grouping archival candidate data sets
 * 
 * @author Sascha Fedorenko
 */
public interface IGroupKeyProvider
{
    String getGroupKey(AbstractExternalData dataset);
}
