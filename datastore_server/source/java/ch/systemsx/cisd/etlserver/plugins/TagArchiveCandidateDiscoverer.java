package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.IArchiveCandidateDiscoverer;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectIdentifierId;

/**
 * Search for archival candidates by tags
 * 
 * @author Sascha Fedorenko
 */
public class TagArchiveCandidateDiscoverer implements IArchiveCandidateDiscoverer
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, TagArchiveCandidateDiscoverer.class);

    private static final String TAG_LIST = "tags";

    private final List<MetaprojectIdentifier> identifiers = new ArrayList<MetaprojectIdentifier>();

    public TagArchiveCandidateDiscoverer(Properties properties)
    {
        List<String> tags = PropertyUtils.getList(properties, TAG_LIST);
        if (tags.size() == 0)
        {
            operationLog.error("TagArchiveCandidateDiscoverer is configured with no tags. Nothing will be found.");
        }
        for (String tag : tags)
        {
            try
            {
                identifiers.add(MetaprojectIdentifier.parse(tag));
            } catch (Exception ex)
            {
                throw new ConfigurationFailureException("Invalid tag in property '" + TAG_LIST + "': " + ex.getMessage());
            }
        }
    }

    @Override
    public List<AbstractExternalData> findDatasetsForArchiving(IEncapsulatedOpenBISService openbis, ArchiverDataSetCriteria criteria)
    {
        if (identifiers.size() == 0)
        {
            return Collections.emptyList();
        }

        List<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
        for (MetaprojectIdentifier identifier : identifiers)
        {
            String name = identifier.getMetaprojectName();
            String user = identifier.getMetaprojectOwnerId();
            Metaproject metaproject = openbis.tryGetMetaproject(name, user);
            if (metaproject != null)
            {
                result.addAll(openbis.listNotArchivedDatasetsWithMetaproject(new MetaprojectIdentifierId(identifier)));
            }
        }
        return result;
    }
}
