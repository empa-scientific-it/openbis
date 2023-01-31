package ch.systemsx.cisd.etlserver.plugins;

import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.etlserver.IArchiveCandidateDiscoverer;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;

/**
 * Default archive candidate data set discoverer that simply finds all old data sets as specified by criteria
 * 
 * @author Sascha Fedorenko
 */
public class AgeArchiveCandidateDiscoverer implements IArchiveCandidateDiscoverer
{
    public AgeArchiveCandidateDiscoverer(Properties properties)
    {

    }

    @Override
    public List<AbstractExternalData> findDatasetsForArchiving(IEncapsulatedOpenBISService openBISService, ArchiverDataSetCriteria criteria)
    {
        return openBISService.listAvailableDataSets(criteria);
    }

}
