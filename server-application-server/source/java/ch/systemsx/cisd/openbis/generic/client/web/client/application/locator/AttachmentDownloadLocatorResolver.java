package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.attachment.AttachmentDownloadHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * ViewLocatorHandler used for downloading attachment. We use permIds to identify attachment holder and we don't have permIds for projects so these
 * locators work only for samples and experiments.
 * 
 * @author Piotr Buczek
 */
public class AttachmentDownloadLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public AttachmentDownloadLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(PermlinkUtilities.DOWNLOAD_ATTACHMENT_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        String entityKindValueOrNull = locator.tryGetEntity();
        checkRequiredParameter(entityKindValueOrNull, PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY);
        AttachmentHolderKind attachmentHolderKind = getAttachmentHolderKind(entityKindValueOrNull);

        String fileNameOrNull = locator.getParameters().get(PermlinkUtilities.FILE_NAME_KEY);
        String versionOrNull = locator.getParameters().get(PermlinkUtilities.VERSION_KEY);

        checkRequiredParameter(fileNameOrNull, PermlinkUtilities.FILE_NAME_KEY);
        try
        {
            Integer version = null;
            if (versionOrNull != null)
            {
                version = Integer.parseInt(versionOrNull);
            }

            if (attachmentHolderKind == AttachmentHolderKind.PROJECT)
            {
                BasicProjectIdentifier projectIdentifier =
                        ProjectLocatorResolver.extractProjectIdentifier(locator);
                downloadProjectAttachment(projectIdentifier, fileNameOrNull, version);
            } else
            {
                String permIdValueOrNull =
                        locator.getParameters().get(PermlinkUtilities.PERM_ID_PARAMETER_KEY);
                checkRequiredParameter(permIdValueOrNull, PermlinkUtilities.PERM_ID_PARAMETER_KEY);
                EntityKind entityKind = getEntityKind(locator);
                downloadAttachment(entityKind, attachmentHolderKind, permIdValueOrNull,
                        fileNameOrNull, version);
            }
        } catch (NumberFormatException e)
        {
            throw new UserFailureException("URL parameter '" + PermlinkUtilities.VERSION_KEY
                    + "' value is expected to be a number.");
        }
    }

    private void downloadAttachment(EntityKind entityKind,
            AttachmentHolderKind attachmentHolderKind, String permId, String fileName,
            Integer version)
    {
        viewContext.getService()
                .getEntityInformationHolder(
                        entityKind,
                        permId,
                        new AttachmentDownloadCallback(viewContext, attachmentHolderKind, fileName,
                                version));
    }

    private void downloadProjectAttachment(BasicProjectIdentifier identifier,
            final String fileName, final Integer version) throws UserFailureException
    {
        viewContext.getService().getProjectInfo(identifier,
                new ProjectAttachmentDownloadCallback(viewContext, fileName, version));
    }

    private AttachmentHolderKind getAttachmentHolderKind(String entityKind)
    {
        try
        {
            return AttachmentHolderKind.valueOf(entityKind);
        } catch (IllegalArgumentException exception)
        {
            throw new UserFailureException("Invalid '" + PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY
                    + "' URL parameter value.");
        }
    }

    private static class AttachmentDownloadCallback extends
            AbstractAsyncCallback<IEntityInformationHolderWithPermId>
    {

        private final AttachmentHolderKind attachmentHolderKind;

        private final String fileName;

        private final Integer version;

        private AttachmentDownloadCallback(final IViewContext<?> viewContext,
                AttachmentHolderKind attachmentHolderKind, final String fileName,
                final Integer version)
        {
            super(viewContext);
            this.attachmentHolderKind = attachmentHolderKind;
            this.fileName = fileName;
            this.version = version;
        }

        @Override
        public void process(final IEntityInformationHolderWithPermId result)
        {
            AttachmentDownloadHelper.download(fileName, version, createAttachmentHolder(result));
        }

        private IAttachmentHolder createAttachmentHolder(final IEntityInformationHolder result)
        {
            return new IAttachmentHolder()
                {

                    @Override
                    public AttachmentHolderKind getAttachmentHolderKind()
                    {
                        return attachmentHolderKind;
                    }

                    @Override
                    public Long getId()
                    {
                        return result.getId();
                    }

                    @Override
                    public String getCode()
                    {
                        return result.getCode();
                    }

                };
        }

    }

    private static class ProjectAttachmentDownloadCallback extends AbstractAsyncCallback<Project>
    {
        private final String fileName;

        private final Integer version;

        private ProjectAttachmentDownloadCallback(final IViewContext<?> viewContext,
                final String fileName, final Integer version)
        {
            super(viewContext);
            this.fileName = fileName;
            this.version = version;
        }

        @Override
        protected final void process(final Project result)
        {
            AttachmentDownloadHelper.download(fileName, version, result);
        }
    }

}