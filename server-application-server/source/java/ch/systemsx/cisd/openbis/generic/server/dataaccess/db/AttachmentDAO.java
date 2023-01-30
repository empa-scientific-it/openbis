/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.AttachmentEntry;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityModification;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Implementation of {@link IAttachmentDAO} for data bases.
 * 
 * @author Franz-Josef Elmer
 * @author Tomasz Pylak
 */
final class AttachmentDAO extends AbstractGenericEntityDAO<AttachmentPE> implements IAttachmentDAO
{

    private final static Class<AttachmentPE> ATTACHMENT_CLASS = AttachmentPE.class;

    private final static String TABLE_NAME = ATTACHMENT_CLASS.getSimpleName();

    private final PersistencyResources persistencyResources;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AttachmentDAO.class);

    private final IEventDAO eventDAO;

    AttachmentDAO(final PersistencyResources persistencyResources,
            IEventDAO eventDAO, EntityHistoryCreator historyCreator)
    {
        super(persistencyResources.getSessionFactory(), ATTACHMENT_CLASS, historyCreator);
        this.persistencyResources = persistencyResources;
        this.eventDAO = eventDAO;
    }

    private final static String createFindLastVersionQuery(AttachmentHolderPE owner)
    {
        return createFindLastVersionQuery(owner, "?");
    }

    private final static String createFindLastVersionQuery(AttachmentHolderPE owner, String fileName)
    {
        String ownerAsParent = getParentName(owner);
        return String.format("select max(version) from %s where " + ownerAsParent
                + " = ? and fileName = %s", TABLE_NAME, fileName);
    }

    private static String getParentName(AttachmentHolderPE owner)
    {
        String ownerAsParent = owner.getHolderName() + "ParentInternal";
        return ownerAsParent;
    }

    //
    // IAttachmentDAO
    //

    @Override
    public Map<String, AttachmentEntry> deleteAttachments(final AttachmentHolderPE holder, final String reason,
            final List<String> fileNames, PersonPE registrator)
    {
        Map<String, AttachmentEntry> attachmentEntries = new TreeMap<>();
        for (String fileName : fileNames)
        {
            List<AttachmentPE> attachmentsToBeDeleted = new ArrayList<>();
            for (AttachmentPE att : holder.getAttachments())
            {
                if (fileName.equals(att.getFileName()))
                {
                    attachmentsToBeDeleted.add(att);
                }
            }
            deleteByOwnerAndFileName(holder, fileName);
            attachmentEntries.putAll(createDeletionEvents(attachmentsToBeDeleted, registrator, reason));
        }
        return attachmentEntries;
    }

    private Map<String, AttachmentEntry> createDeletionEvents(List<AttachmentPE> attachmentsToBeDeleted,
            PersonPE registrator, String reason)
    {
        Map<String, AttachmentEntry> attachmentEntries = new TreeMap<>();
        for (AttachmentPE attachmentToBeDeleted : attachmentsToBeDeleted)
        {
            EventPE event = new EventPE();
            AttachmentHolderPE holder = attachmentToBeDeleted.getParent();
            String holderName = holder.getHolderName();
            String holderIdentifier = holder.getPermId();
            String fileName = attachmentToBeDeleted.getFileName();
            int version = attachmentToBeDeleted.getVersion();
            String identifier = String.format("%s/%s/%s(%s)", holderName, holderIdentifier, fileName, version);
            event.setEventType(EventType.DELETION);
            event.setEntityType(EntityType.ATTACHMENT);
            event.setIdentifiers(Collections.singletonList(identifier));
            event.setDescription(identifier);
            event.setReason(reason);
            event.setRegistrator(registrator);
            event.setAttachmentContent(attachmentToBeDeleted.getAttachmentContent());
            Map<String, List<? extends EntityModification>> modifications = new HashMap<String, List<? extends EntityModification>>();
            AttachmentEntry attachmentEntry = new AttachmentEntry();
            attachmentEntry.fileName = fileName;
            attachmentEntry.version = version;
            attachmentEntry.description = attachmentToBeDeleted.getDescription();
            attachmentEntry.title = attachmentToBeDeleted.getTitle();
            attachmentEntry.relationType = "OWNED";
            attachmentEntry.entityType = holder.getAttachmentHolderKind().toString();
            attachmentEntry.relatedEntity = holder.getPermId();
            attachmentEntry.validFrom = attachmentToBeDeleted.getRegistrationDate();
            attachmentEntry.userId = attachmentToBeDeleted.getRegistrator().getUserId();
            modifications.put(identifier, Arrays.asList(attachmentEntry));
            attachmentEntries.put(identifier, attachmentEntry);
            event.setContent(historyCreator.jsonize(modifications));
            eventDAO.persist(event);
        }
        return attachmentEntries;
    }

    @Override
    public final AttachmentHolderPE createAttachment(final AttachmentPE attachment,
            final AttachmentHolderPE ownerParam) throws DataAccessException
    {
        AttachmentHolderPE result = internalCreateAttachment(attachment, ownerParam);
        getHibernateTemplate().flush();

        scheduleDynamicPropertiesEvaluation(ownerParam);

        return result;
    }

    @Override
    public void createAttachments(Collection<AttachmentPE> attachments) throws DataAccessException
    {
        if (attachments == null || attachments.isEmpty())
        {
            return;
        }

        for (AttachmentPE attachment : attachments)
        {
            internalCreateAttachment(attachment, attachment.getParent());
        }

        getHibernateTemplate().flush();
    }

    private AttachmentHolderPE internalCreateAttachment(final AttachmentPE attachment,
            final AttachmentHolderPE ownerParam) throws DataAccessException
    {
        assert attachment != null : "Unspecified attachment";
        assert attachment.getAttachmentContent() != null : "Unspecified attachment content.";
        AttachmentHolderPE owner = ownerParam;
        validatePE(attachment.getAttachmentContent());

        final AttachmentPE previousAttachmentVersionOrNull =
                tryFindAttachmentByOwnerAndFileName(owner, attachment.getFileName());
        fillAttachmentData(attachment, previousAttachmentVersionOrNull);

        final HibernateTemplate template = getHibernateTemplate();
        Session session = currentSession();
        if (session.contains(owner) == false)
        {
            owner = (AttachmentHolderPE) session.merge(owner);
        }
        owner.addAttachment(attachment);
        validatePE(attachment);

        template.save(attachment.getAttachmentContent());
        template.save(attachment);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: file attachment '%s'.", attachment));
        }
        return owner;
    }

    private void fillAttachmentData(AttachmentPE attachment,
            AttachmentPE previousAttachmentVersionOrNull)
    {
        int previousVersion = 0;
        if (previousAttachmentVersionOrNull != null)
        {
            previousVersion = previousAttachmentVersionOrNull.getVersion();
            if (StringUtils.isBlank(attachment.getTitle()))
            {
                attachment.setTitle(previousAttachmentVersionOrNull.getTitle());
            }
            if (StringUtils.isBlank(attachment.getDescription()))
            {
                attachment.setDescription(previousAttachmentVersionOrNull.getDescription());
            }
        }
        attachment.setVersion(previousVersion + 1);
    }

    @Override
    public final List<AttachmentPE> listAttachments(final AttachmentHolderPE owner)
            throws DataAccessException
    {
        assert owner != null : "Unspecified attachment holder.";

        final String query =
                String.format("from %s where " + getParentName(owner) + " = ?", TABLE_NAME);
        final List<AttachmentPE> result = cast(getHibernateTemplate().find(query, toArray(owner)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d attachment(s) found for " + owner.getHolderName()
                    + " '%s'.", result.size(), owner));
        }
        return result;
    }

    @Override
    public final AttachmentPE tryFindAttachmentByOwnerAndFileName(final AttachmentHolderPE owner,
            final String fileName) throws DataAccessException
    {
        assert fileName != null : "Unspecified file name.";
        assert owner != null : "Unspecified parent attachment holder.";

        final String query =
                String.format("from %s where " + getParentName(owner)
                        + " = ? and fileName = ? and version = ("
                        + createFindLastVersionQuery(owner) + ")", TABLE_NAME);
        final List<AttachmentPE> result =
                cast(getHibernateTemplate().find(query, toArray(owner, fileName, owner, fileName)));
        final AttachmentPE attachment = tryFindEntity(result, "attachment", owner, fileName);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s found for " + owner.getHolderName()
                    + " '%s' and file name '%s'.", attachment == null ? "No attachment"
                            : "Attachment '" + attachment + "'",
                    owner, fileName));
        }
        return attachment;
    }

    @Override
    public final AttachmentPE tryFindAttachmentByOwnerAndFileNameAndVersion(
            final AttachmentHolderPE owner, final String fileName, final int version)
            throws DataAccessException
    {
        assert owner != null : "Unspecified attachment holder.";
        assert fileName != null : "Unspecified file name.";
        assert version > 0 : "Version must be > 0.";

        final String query =
                String.format("from %s where " + getParentName(owner)
                        + " = ? and fileName = ? and version = ?", TABLE_NAME);
        final List<AttachmentPE> result =
                cast(getHibernateTemplate().find(query, toArray(owner, fileName, version)));
        final AttachmentPE attachment =
                tryFindEntity(result, "attachment", owner, fileName, version);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s found for " + owner.getHolderName()
                    + " '%s', file name '%s' and version %d.", attachment == null ? "No attachment"
                            : "Attachment '" + attachment + "'",
                    owner, fileName, version));
        }
        return attachment;
    }

    @Override
    public int deleteByOwnerAndFileName(final AttachmentHolderPE owner, final String fileName)
            throws DataAccessException
    {
        assert owner != null : "Unspecified attachment holder.";
        assert fileName != null : "Unspecified file name.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();

        int deletedRows = 0;
        for (AttachmentPE att : owner.getAttachments())
        {
            if (fileName.equals(att.getFileName()))
            {
                owner.removeAttachment(att);
                hibernateTemplate.delete(att);
                deletedRows++;
            }
        }

        hibernateTemplate.flush();

        scheduleDynamicPropertiesEvaluation(owner);

        if (operationLog.isInfoEnabled())
        {
            operationLog.debug(String.format(
                    "%s attachment(s) deleted for %s '%s' and file name '%s'.", deletedRows,
                    owner.getHolderName(), owner, fileName));
        }

        return deletedRows;
    }

    private void scheduleDynamicPropertiesEvaluation(final AttachmentHolderPE owner)
    {
        // updates the index if the attachment owner is a Sample or an Experiment
        if (IEntityInformationWithPropertiesHolder.class.isAssignableFrom(owner.getClass()))
        {
            IEntityInformationWithPropertiesHolder entity = (IEntityInformationWithPropertiesHolder) owner;
            persistencyResources.getDynamicPropertyEvaluationScheduler()
                    .scheduleUpdate(DynamicPropertyEvaluationOperation.evaluate(entity.getClass(), Arrays.asList(entity.getId())));
        }
    }
}
