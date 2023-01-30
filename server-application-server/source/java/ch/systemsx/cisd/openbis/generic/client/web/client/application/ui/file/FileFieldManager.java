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

import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Stores and manages fields that extend {@link FileUploadField}.
 * 
 * @author Izabela Adamczyk
 */
public abstract class FileFieldManager<T extends FileUploadField>
{
    protected IMessageProvider messageProvider;

    private final String fieldLabel;

    private static final String FIELD_NAME_TEMPLATE = "{0}_{1}";

    private final List<T> fileFields;

    private final String sessionKey;

    protected FileFieldManager(String sessionKey, int initialNumberOfFields, String fieldLabel,
            final IMessageProvider messageProvider)
    {
        this.messageProvider = messageProvider;
        this.sessionKey = sessionKey;
        this.fieldLabel = fieldLabel;
        fileFields = new ArrayList<T>();
        for (int i = 0; i < initialNumberOfFields; i++)
        {
            fileFields.add(createFileUploadField(i));
        }
    }

    protected FileFieldManager(String sessionKey, int initialNumberOfFields, String fieldLabel)
    {
        this(sessionKey, initialNumberOfFields, fieldLabel, null);
    }

    public void setMandatory()
    {
        assert fileFields.size() > 0;
        FileUploadField field = fileFields.get(0);
        field.setAllowBlank(false);
        FieldUtil.markAsMandatory(field);
    }

    public List<T> getFields()
    {
        return fileFields;
    }

    protected T addField()
    {
        int counter = fileFields.size();
        T field = createFileUploadField(counter);
        fileFields.add(field);
        return field;
    }

    public int filesDefined()
    {
        int i = 0;
        for (FileUploadField field : fileFields)
        {
            Object value = field.getValue();
            if (value != null && String.valueOf(value).length() > 0)
            {
                i++;
            }
        }
        return i;
    }

    protected abstract T createFileUploadField();

    private final T createFileUploadField(final int counter)
    {
        final T file = createFileUploadField();
        final int number = counter + 1;
        file.setFieldLabel(fieldLabel);
        file.setName(Format.substitute(FIELD_NAME_TEMPLATE, sessionKey, number));
        file.setId(Format.substitute(FIELD_NAME_TEMPLATE, sessionKey, number));
        return file;
    }

}
