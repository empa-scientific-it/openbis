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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;

/**
 * Stores and manages {@link AttachmentFileUploadField} fields.
 * 
 * @author Piotr Buczek
 */
public class AttachmentsFileFieldManager extends FileFieldManager<AttachmentFileUploadField>
{
    // by default there are no attachment field sets created
    private static final int DEFAULT_NUMBER_OF_ATTACHMENTS = 0;

    private final int initialNumberOfFields;

    private LabelField addAttachmentLink;

    public AttachmentsFileFieldManager(final String sessionKey,
            final IMessageProvider messageProvider)
    {
        this(sessionKey, DEFAULT_NUMBER_OF_ATTACHMENTS, messageProvider);
    }

    private AttachmentsFileFieldManager(final String sessionKey, final int initialNumberOfFields,
            final IMessageProvider messageProvider)
    {
        super(sessionKey, initialNumberOfFields, messageProvider.getMessage(Dict.FILE),
                messageProvider);
        this.initialNumberOfFields = initialNumberOfFields;
    }

    @Override
    protected AttachmentFileUploadField createFileUploadField()
    {
        return new AttachmentFileUploadField(messageProvider);
    }

    public void addAttachmentFieldSetsToPanel(FormPanel panel)
    {
        addAttachmentLink =
                createAddAttachmentLink(messageProvider.getMessage(Dict.ADD_ATTACHMENT), panel);
        panel.add(addAttachmentLink);
        for (AttachmentFileUploadField attachmentField : getFields())
        {
            addFileFieldsetToPanel(attachmentField, panel);
        }
    }

    public void resetAttachmentFieldSetsInPanel(FormPanel panel)
    {
        for (AttachmentFileUploadField attachmentField : getFields())
        {
            removeFileFieldsetFromPanel(attachmentField, panel);
        }
        getFields().clear();
        for (int i = 0; i < initialNumberOfFields; i++)
        {
            AttachmentFileUploadField newField = addField();
            addFileFieldsetToPanel(newField, panel);
        }
        panel.layout();
    }

    private void removeFileFieldsetFromPanel(AttachmentFileUploadField attachmentField,
            FormPanel panel)
    {
        panel.remove(attachmentField.getFieldSet());
    }

    private void addFileFieldsetToPanel(AttachmentFileUploadField attachmentField, FormPanel panel)
    {
        panel.add(attachmentField.getFieldSet());
    }

    public List<NewAttachment> extractAttachments()
    {
        final List<NewAttachment> result = new ArrayList<NewAttachment>();
        for (AttachmentFileUploadField field : getFields())
        {
            NewAttachment attachmentOrNull = field.tryExtractAttachment();
            if (attachmentOrNull != null)
            {
                result.add(attachmentOrNull);
            }
        }
        return result;
    }

    private LabelField createAddAttachmentLink(final String label, final FormPanel panel)
    {
        final String link = LinkRenderer.renderAsLink(label);
        final LabelField result = new LabelField(link);
        result.setOriginalValue(link);
        result.sinkEvents(Event.ONCLICK);
        result.addListener(Events.OnClick, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    AttachmentFileUploadField newField = addField();
                    addFileFieldsetToPanel(newField, panel);
                    panel.layout();
                }
            });
        return result;
    }

}
