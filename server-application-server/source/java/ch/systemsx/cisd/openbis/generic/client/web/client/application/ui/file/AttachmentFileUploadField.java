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

import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;

/**
 * {@link FileUploadField} extension for {@link NewAttachment}s registration.
 * 
 * @author Piotr Buczek
 */
public class AttachmentFileUploadField extends FileUploadField
{
    private final AttachmentsFileSet fileSet;

    public AttachmentFileUploadField(final IMessageProvider messageProvider)
    {
        super();
        setFireChangeEventOnSetValue(true);
        this.fileSet = new AttachmentsFileSet(messageProvider, this);
    }

    public FieldSet getFieldSet()
    {
        return fileSet;
    }

    public void addFieldsTo(FormPanel form, String sessionKey, IMessageProvider messageProvider)
    {
        FileUploadField fileUploadField = fileSet.getFileUploadField();
        fileUploadField.setFieldLabel(messageProvider.getMessage(Dict.FILE));
        fileUploadField.setName(sessionKey);
        form.add(fileUploadField);
        form.add(fileSet.getDescriptionField());
        form.add(fileSet.getTitleField());
    }

    public NewAttachment tryExtractAttachment()
    {
        if (StringUtils.isBlank(getFilePathValue()))
        {
            return null;
        } else
        {
            final NewAttachment attachment = new NewAttachment();
            attachment.setFilePath(getFilePathValue());
            attachment.setTitle(getTitleValue());
            attachment.setDescription(getDescriptionValue());
            return attachment;
        }
    }

    @Override
    public void reset()
    {
        super.reset();
        fileSet.reset();
    }

    private String getFilePathValue()
    {
        return getFileInput().getValue(); // its not only file name, but relative file path
    }

    private String getDescriptionValue()
    {
        return fileSet.getDescriptionField().getValue();
    }

    private String getTitleValue()
    {
        return fileSet.getTitleField().getValue();
    }

    private static final class AttachmentsFileSet extends FieldSet
    {

        private final FileUploadField fileUploadField;

        private VarcharField titleField;

        private DescriptionField descriptionField;

        public AttachmentsFileSet(final IMessageProvider messageProvider,
                FileUploadField fileUploadField)
        {
            this.fileUploadField = fileUploadField;
            createForm(messageProvider);
        }

        /** resets all field set fields except {@link FileUploadField} */
        public void reset()
        {
            titleField.reset();
            descriptionField.reset();
        }

        private void createForm(final IMessageProvider messageProvider)
        {
            setHeading(messageProvider.getMessage(Dict.ATTACHMENT));
            setLayout(createFormLayout());
            setWidth(AbstractRegistrationForm.SECTION_WIDTH);
            add(getFileUploadField());
            add(descriptionField = new DescriptionField(messageProvider, false));
            add(titleField = createTitleField(messageProvider.getMessage(Dict.TITLE)));
        }

        private final FormLayout createFormLayout()
        {
            final FormLayout formLayout = new FormLayout();
            formLayout.setLabelWidth(AbstractRegistrationForm.SECTION_LABEL_WIDTH);
            formLayout.setDefaultWidth(AbstractRegistrationForm.SECTION_DEFAULT_FIELD_WIDTH);
            return formLayout;
        }

        private final VarcharField createTitleField(final String label)
        {
            final VarcharField varcharField = new VarcharField(label, false);
            varcharField.setMaxLength(100);
            return varcharField;
        }

        public DescriptionField getDescriptionField()
        {
            return descriptionField;
        }

        public final VarcharField getTitleField()
        {
            return titleField;
        }

        public FileUploadField getFileUploadField()
        {
            return fileUploadField;
        }
    }

}
