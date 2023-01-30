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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.form;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;

/**
 * {@link AbstractMetaprojectEditRegisterForm} extension for editing metaprojects.
 * 
 * @author pkupczyk
 */
public class MetaprojectEditForm extends AbstractMetaprojectEditRegisterForm
{

    private Metaproject originalMetaproject;

    private final Long metaprojectId;

    public static DatabaseModificationAwareComponent create(final IViewContext<?> viewContext,
            Long metaprojectId)
    {
        MetaprojectEditForm form = new MetaprojectEditForm(viewContext, metaprojectId);
        return DatabaseModificationAwareComponent.wrapUnaware(form);
    }

    protected MetaprojectEditForm(IViewContext<?> viewContext, Long metaprojectId)
    {
        super(viewContext, metaprojectId);
        setRevertButtonVisible(true);
        this.metaprojectId = metaprojectId;
    }

    @Override
    protected void saveMetaproject()
    {
        Metaproject updates = new Metaproject();
        updates.setName(metaprojectNameField.getValue());
        updates.setDescription(metaprojectDescriptionField.getValue());
        viewContext.getCommonService().updateMetaproject(metaprojectId, updates,
                new MetaprojectEditCallback(viewContext));
    }

    private final class MetaprojectEditCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Metaproject>
    {

        MetaprojectEditCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final Metaproject result)
        {
            setOriginalMetaproject(result);
            updateOriginalValues();
            super.process(result);
        }

        @Override
        protected List<HtmlMessageElement> createSuccessfullRegistrationInfo(Metaproject result)
        {
            return Arrays.asList(new HtmlMessageElement("Metaproject <b>" + originalMetaproject.getName() + "</b> successfully updated."));
        }
    }

    @Override
    protected void setValues()
    {
        FieldUtil.setValueWithUnescaping(metaprojectNameField, originalMetaproject.getName());
        FieldUtil.setValueWithUnescaping(metaprojectDescriptionField,
                originalMetaproject.getDescription());
    }

    public void updateOriginalValues()
    {
        metaprojectNameField.setOriginalValue(metaprojectNameField.getValue());
        metaprojectDescriptionField.setOriginalValue(metaprojectDescriptionField.getValue());
    }

    void setOriginalMetaproject(Metaproject metaproject)
    {
        this.originalMetaproject = metaproject;
    }

    @Override
    protected void loadForm()
    {
        viewContext.getCommonService().getMetaproject(metaprojectId,
                new MetaprojectLoadCallback(viewContext));
    }

    private final class MetaprojectLoadCallback extends AbstractAsyncCallback<Metaproject>
    {

        private MetaprojectLoadCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final Metaproject result)
        {
            setOriginalMetaproject(result);
            initGUI();
        }
    }

}
