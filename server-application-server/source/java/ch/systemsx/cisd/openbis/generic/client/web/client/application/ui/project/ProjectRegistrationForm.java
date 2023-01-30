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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * {@link AbstractProjectEditRegisterForm} extension for registering projects.
 * 
 * @author Izabela Adamczyk
 */
public class ProjectRegistrationForm extends AbstractProjectEditRegisterForm
{

    protected ProjectRegistrationForm(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext);
        setResetButtonVisible(true);
        projectDescriptionField.setMaxLength(GenericConstants.DESCRIPTION_500000);
    }

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        ProjectRegistrationForm form = new ProjectRegistrationForm(viewContext);
        return new DatabaseModificationAwareComponent(form, form.getGroupField());
    }

    public static String createId()
    {
        return AbstractProjectEditRegisterForm.createId(null);
    }

    private final Project createProject()
    {
        final Project project = new Project();
        project.setDescription(projectDescriptionField.getValue());
        project.setIdentifier(spaceField.tryGetSelectedSpace().getIdentifier() + "/"
                + projectCodeField.getValue());
        project.setNewAttachments(extractAttachments());
        return project;
    }

    @Override
    protected void saveProject()
    {
        final Project project = createProject();
        viewContext.getService().registerProject(sessionKey, project,
                new ProjectRegistrationCallback(viewContext, project));
    }

    private final class ProjectRegistrationCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Void>
    {
        private final Project project;

        ProjectRegistrationCallback(final IViewContext<?> viewContext, final Project project)
        {
            super(viewContext);
            this.project = project;
        }

        @Override
        protected List<HtmlMessageElement> createSuccessfullRegistrationInfo(Void result)
        {
            return Arrays.asList(new HtmlMessageElement("Project <b>" + project.getIdentifier().toUpperCase()
                    + "</b> successfully registered."));
        }
    }

    @Override
    protected void setValues()
    {
    }

    @Override
    protected void loadForm()
    {
        initGUI();
    }

}
