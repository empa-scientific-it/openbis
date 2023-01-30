package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;

public abstract class AttachmentRegistrationHelper
{
    private static final long MAX_ATTACHMENT_SIZE = 256 * FileUtils.ONE_MB;

    abstract public void register(Collection<NewAttachment> attachmentPEs);

    final public void process(String sessionKey, HttpSession httpSession,
            List<NewAttachment> attachmentsOrNull)
    {
        UploadedFilesBean uploadedFiles = null;
        try
        {
            uploadedFiles = (UploadedFilesBean) httpSession.getAttribute(sessionKey);
            abortIfMaxSizeExceeded(uploadedFiles);
            final Map<String, NewAttachment> attachmentMap = createAttachmentMap(attachmentsOrNull);
            fillContent(uploadedFiles, attachmentMap);
            register(attachmentMap.values());
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } finally
        {
            if (uploadedFiles != null)
            {
                uploadedFiles.deleteTransferredFiles();
            }
            if (httpSession != null)
            {
                httpSession.removeAttribute(sessionKey);
            }
        }
    }

    private Map<String, NewAttachment> createAttachmentMap(
            final List<NewAttachment> attachmentsOrNull)
    {
        // fileName -> attachment
        Map<String, NewAttachment> result = new HashMap<String, NewAttachment>();
        if (attachmentsOrNull != null)
        {
            for (NewAttachment attachment : attachmentsOrNull)
            {
                result.put(attachment.getFileName(), attachment);
            }
        }
        return result;
    }

    private void abortIfMaxSizeExceeded(UploadedFilesBean uploadedFiles)
    {
        if (uploadedFiles != null)
        {
            for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
            {
                long fileSize = multipartFile.getSize();
                if (fileSize > MAX_ATTACHMENT_SIZE)
                {
                    String maxSizeString =
                            FileUtilities.byteCountToDisplaySize(MAX_ATTACHMENT_SIZE);
                    String fileSizeString = FileUtilities.byteCountToDisplaySize(fileSize);
                    String errorMessage =
                            String.format(
                                    "The file %s(%s) is larger than the configured maximum (%s).",
                                    multipartFile.getOriginalFilename(), fileSizeString,
                                    maxSizeString);
                    throw new UserFailureException(errorMessage);
                }
            }
        }

    }

    private void fillContent(UploadedFilesBean uploadedFiles, Map<String, NewAttachment> attachments)
    {
        if (uploadedFiles != null)
        {
            List<IUncheckedMultipartFile> unmatchedFiles = new ArrayList<IUncheckedMultipartFile>();
            List<NewAttachment> matchedAttachments = new ArrayList<NewAttachment>();
            for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
            {
                final String fileName = multipartFile.getOriginalFilename();
                final NewAttachment attachmentOrNull = attachments.get(fileName);
                if (attachmentOrNull != null)
                {
                    // NOTE: this will load the entire attachments in memory
                    final byte[] content = multipartFile.getBytes();
                    attachmentOrNull.setContent(content);
                    matchedAttachments.add(attachmentOrNull);
                } else
                {
                    // this can happen because RFC 2388 says:
                    // The sending application MAY supply a file name; if the file name of the sender's
                    // operating system is not in US-ASCII, the file name might be approximated, or encoded
                    // using the method of RFC 2231.
                    unmatchedFiles.add(multipartFile);
                }
            }
            List<NewAttachment> unmatchedAttachments = new ArrayList<NewAttachment>(attachments.values());
            unmatchedAttachments.removeAll(matchedAttachments);
            if (unmatchedAttachments.size() == 1 && unmatchedFiles.size() == 1)
            {
                // If only one file name doesn't match we ignore the file name and set the content anyway.
                unmatchedAttachments.get(0).setContent(unmatchedFiles.get(0).getBytes());
            }
        }
    }
}
