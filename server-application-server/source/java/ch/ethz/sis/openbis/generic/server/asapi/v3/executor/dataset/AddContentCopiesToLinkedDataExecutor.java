package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.externaldms.IMapExternalDmsByIdExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ContentCopyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocationType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

@Component
public class AddContentCopiesToLinkedDataExecutor implements IAddContentCopiesToLinkedDataExecutor
{

    @Autowired
    private IMapExternalDmsByIdExecutor mapExternalDmsByIdExecutor;

    @Autowired
    protected IDAOFactory daoFactory;

    @Override
    public void add(IOperationContext context, LinkDataPE entity, Collection<ContentCopyCreation> added)
    {
        PersonPE person = context.getSession().tryGetPerson();

        Set<IExternalDmsId> emdsIds = new HashSet<>();
        for (ContentCopyCreation ccc : added)
        {
            IExternalDmsId edmsId = ccc.getExternalDmsId();
            if (edmsId == null)
            {
                throw new UserFailureException("External data management system id cannot be null");
            }
            emdsIds.add(edmsId);
        }

        Map<IExternalDmsId, ExternalDataManagementSystemPE> edmsPEs =
                mapExternalDmsByIdExecutor.map(context, emdsIds);
        Set<ContentCopyPE> contentCopies = new HashSet<>();

        for (ContentCopyCreation ccc : added)
        {
            ContentCopyPE copy = new ContentCopyPE();
            copy.setDataSet(entity);

            ExternalDataManagementSystemPE edms = edmsPEs.get(ccc.getExternalDmsId());
            if (edms == null)
            {
                throw new UserFailureException("No external data management system found with id " + ccc.getExternalDmsId());
            }
            copy.setExternalDataManagementSystem(edms);

            copy.setLocationType(getLocationType(ccc, edms));

            switch (copy.getLocationType())
            {
                case OPENBIS:
                case URL:
                    copy.setExternalCode(ccc.getExternalId());
                    break;
                case FILE_SYSTEM_GIT:
                    copy.setGitCommitHash(ccc.getGitCommitHash());
                    copy.setGitRepositoryId(ccc.getGitRepositoryId());
                case FILE_SYSTEM_PLAIN:
                    String path = ccc.getPath();
                    if (path.startsWith("/") == false)
                    {
                        path = "/" + path;
                    }
                    copy.setPath(path);
            }
            copy.setRegistrator(person);
            contentCopies.add(copy);
        }
        if (entity.getContentCopies() == null)
        {
            entity.setContentCopies(new HashSet<ContentCopyPE>());
        }
        entity.getContentCopies().addAll(contentCopies);

        Date timeStamp = daoFactory.getTransactionTimestamp();
        RelationshipUtils.updateModificationDateAndModifier(entity, person, timeStamp);
    }

    private LocationType getLocationType(ContentCopyCreation ccc, ExternalDataManagementSystemPE edms)
    {
        String externalId = ccc.getExternalId();
        String path = ccc.getPath();
        String gitCommitHash = ccc.getGitCommitHash();
        String gitRepositoryId = ccc.getGitRepositoryId();

        if (ExternalDataManagementSystemType.OPENBIS.equals(edms.getAddressType()) && externalId != null && path == null && 
        		gitCommitHash == null && gitRepositoryId == null)
        {
            return LocationType.OPENBIS;
        }
        if (ExternalDataManagementSystemType.URL.equals(edms.getAddressType()) && externalId != null && path == null && 
        		gitCommitHash == null && gitRepositoryId == null)
        {
            return LocationType.URL;
        }
        if (ExternalDataManagementSystemType.FILE_SYSTEM.equals(edms.getAddressType()) && externalId == null && path != null && 
        		gitCommitHash == null && gitRepositoryId == null)
        {
            return LocationType.FILE_SYSTEM_PLAIN;
        }
        if (ExternalDataManagementSystemType.FILE_SYSTEM.equals(edms.getAddressType()) && externalId == null && path != null && 
        		gitCommitHash != null && gitRepositoryId != null)
        {
            return LocationType.FILE_SYSTEM_GIT;
        }
        throw new UserFailureException("Invalid arguments: external data management system type: " + edms.getAddressType()
                + ", externalId: " + externalId + " , path: " + path + ", gitCommitHash: " + gitCommitHash 
                + ", gitRepositoryId: " + gitRepositoryId);
    }
}
