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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ProjectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * {@link AbstractProjectEditRegisterForm} extension for editing projects.
 * 
 * @author Izabela Adamczyk
 */
public class ProjectEditForm extends AbstractProjectEditRegisterForm
{

    private Project originalProject;

    private final TechId projectId;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, TechId projectId)
    {
        ProjectEditForm form = new ProjectEditForm(viewContext, projectId);
        return new DatabaseModificationAwareComponent(form, form.getGroupField());
    }

    protected ProjectEditForm(IViewContext<ICommonClientServiceAsync> viewContext, TechId projectId)
    {
        super(viewContext, projectId);
        setRevertButtonVisible(true);
        this.projectId = projectId;
        projectDescriptionField.setMaxLength(GenericConstants.DESCRIPTION_500000);
    }

    @Override
    protected void saveProject()
    {
        ProjectUpdates updates = new ProjectUpdates();
        updates.setAttachments(extractAttachments());
        updates.setAttachmentSessionKey(sessionKey);
        updates.setDescription(projectDescriptionField.getValue());
        updates.setTechId(projectId);
        updates.setVersion(originalProject.getVersion());
        Space space = spaceField.tryGetSelected();
        updates.setSpaceCode(space == null ? null : space.getCode());

        viewContext.getCommonService().updateProject(updates, new ProjectEditCallback(viewContext));
    }

    private final class ProjectEditCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Integer>
    {

        ProjectEditCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final Integer result)
        {
            originalProject.setVersion(result);
            updateOriginalValues();
            super.process(result);
        }

        @Override
        protected List<HtmlMessageElement> createSuccessfullRegistrationInfo(Integer result)
        {
            return Arrays.asList(new HtmlMessageElement("Project <b>" + originalProject.getCode() + "</b> successfully updated."));
        }
    }

    @Override
    protected void setValues()
    {
        FieldUtil.setValueWithUnescaping(projectDescriptionField, originalProject.getDescription());
        projectCodeField.setValue(originalProject.getCode());
        projectCodeField.setEnabled(false);
        spaceField.selectSpaceAndUpdateOriginal(originalProject.getSpace().getCode());
    }

    public void updateOriginalValues()
    {
        projectDescriptionField.setOriginalValue(projectDescriptionField.getValue());
        projectCodeField.setOriginalValue(projectCodeField.getValue());
        spaceField.setOriginalValue(spaceField.getValue());
    }

    void setOriginalProject(Project project)
    {
        this.originalProject = project;
    }

    @Override
    protected void loadForm()
    {
        viewContext.getService().getProjectInfo(projectId, new ProjectInfoCallback(viewContext));
    }

    private final class ProjectInfoCallback extends AbstractAsyncCallback<Project>
    {

        private ProjectInfoCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final Project result)
        {
            setOriginalProject(result);
            initGUI();
        }
    }

}
