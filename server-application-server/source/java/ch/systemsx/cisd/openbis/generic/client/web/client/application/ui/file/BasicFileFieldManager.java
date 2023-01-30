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

import com.extjs.gxt.ui.client.widget.form.FileUploadField;

/**
 * Stores and manages basic {@link FileUploadField} fields.
 * 
 * @author Piotr Buczek
 */
public class BasicFileFieldManager extends FileFieldManager<FileUploadField>
{

    public BasicFileFieldManager(final String sessionKey, final int initialNumberOfFields,
            final String fieldLabel)
    {
        super(sessionKey, initialNumberOfFields, fieldLabel);
    }

    @Override
    protected FileUploadField createFileUploadField()
    {
        FileUploadField field = new FileUploadField()
            {
                @Override
                public void setEnabled(boolean enabled)
                {
                    // WORKAROUND to keep the button enabled after field reset
                    // and change of visibility
                    super.setEnabled(enabled);
                    setReadOnly(!enabled);
                }
            };
        field.setFireChangeEventOnSetValue(true);
        return field;
    }

}
