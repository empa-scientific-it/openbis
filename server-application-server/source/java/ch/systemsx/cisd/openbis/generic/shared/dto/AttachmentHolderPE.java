/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Encapsulates collection of {@link AttachmentPE}s with useful logic.
 * 
 * @author Izabela Adamczyk
 */
@MappedSuperclass
public abstract class AttachmentHolderPE implements Serializable, IIdentifierHolder, IIdHolder, IPermIdHolder
{
    //
    // Version
    //
    private static final long serialVersionUID = IServer.VERSION;

    //
    // Constants
    //
    public static final char HIDDEN_EXPERIMENT_PROPERTY_PREFIX_CHARACTER = '$';

    public static final String HIDDEN_EXPERIMENT_PROPERTY_PREFIX = Character
            .toString(HIDDEN_EXPERIMENT_PROPERTY_PREFIX_CHARACTER);

    public static final String HIDDEN_EXPERIMENT_PROPERTY_PREFIX2 =
            HIDDEN_EXPERIMENT_PROPERTY_PREFIX + HIDDEN_EXPERIMENT_PROPERTY_PREFIX;

    //
    // Fields
    //
    protected Set<AttachmentPE> attachments = new HashSet<AttachmentPE>();

    private boolean attachmentsUnescaped = false;

    //
    // Abstract methods
    //
    @Transient
    abstract protected Set<AttachmentPE> getInternalAttachments();

    @Transient
    abstract public AttachmentHolderKind getAttachmentHolderKind();

    @Transient
    public final String getHolderName()
    {
        return getAttachmentHolderKind().name().toLowerCase();
    }

    //
    //
    //
    public void setInternalAttachments(final Set<AttachmentPE> attachments)
    // for Hibernate and bean conversion only
    {
        this.attachments = attachments;
    }

    @Transient
    public final Set<AttachmentPE> getAttachments()
    {
        final Set<AttachmentPE> set = new HashSet<AttachmentPE>(getInternalAttachments());
        if (attachmentsUnescaped == false)
        {
            for (final Iterator<AttachmentPE> iter = set.iterator(); iter.hasNext(); /**/)
            {
                final AttachmentPE property = iter.next();
                final boolean isHiddenFile = isHiddenFile(property.getFileName());
                if (isHiddenFile)
                {
                    iter.remove();
                }
                unescapeFileName(property);
            }
            attachmentsUnescaped = true;
        }
        return set;
    }

    final void setAttachments(final Set<AttachmentPE> attachments)
    // Package visibility to avoid bean conversion which will call an uninitialized field.
    {
        getInternalAttachments().clear();
        for (final AttachmentPE attachment : attachments)
        {
            addAttachment(attachment);
        }
    }

    public void addAttachment(final AttachmentPE child)
    {
        final AttachmentHolderPE parent = child.getParent();
        if (parent != null)
        {
            parent.getInternalAttachments().remove(child);
        }
        child.setParent(this);
        getInternalAttachments().add(child);
    }

    // Should be called ONLY before deletion of the attachment.
    // Doesn't clear connection with parent - it is handled by cascade delete.
    public void removeAttachment(final AttachmentPE child)
    {
        getInternalAttachments().remove(child);
    }

    public final static boolean isHiddenFile(final String fileName)
    {
        return fileName.startsWith(HIDDEN_EXPERIMENT_PROPERTY_PREFIX)
                && (fileName.length() == 1 || fileName.charAt(1) != HIDDEN_EXPERIMENT_PROPERTY_PREFIX_CHARACTER);
    }

    public final static String escapeFileName(final String fileName)
    {
        if (fileName != null && fileName.startsWith(HIDDEN_EXPERIMENT_PROPERTY_PREFIX))
        {
            return HIDDEN_EXPERIMENT_PROPERTY_PREFIX + fileName;
        }
        return fileName;
    }

    public final static void unescapeFileName(final AttachmentPE attachment)
    {
        if (attachment != null)
        {
            final String fileName = attachment.getFileName();
            if (fileName != null && fileName.startsWith(HIDDEN_EXPERIMENT_PROPERTY_PREFIX2))
            {
                attachment.setFileName(fileName.substring(1));
            }
        }
    }

    public void ensureAttachmentsLoaded()
    {
        HibernateUtils.initialize(getInternalAttachments());
    }

    public boolean attachmentsInitialized()
    {
        return HibernateUtils.isInitialized(getInternalAttachments());
    }

}
