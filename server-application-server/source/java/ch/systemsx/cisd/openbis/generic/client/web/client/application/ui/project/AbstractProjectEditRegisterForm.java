/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.AttachmentsFileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;

/**
 * A {@link LayoutContainer} extension for registering and editing projects.
 * 
 * @author Izabela Adamczyk
 */
abstract class AbstractProjectEditRegisterForm extends AbstractRegistrationForm
{

    protected final IViewContext<ICommonClientServiceAsync> viewContext;

    protected CodeField projectCodeField;

    protected MultilineVarcharField projectDescriptionField;

    protected SpaceSelectionWidget spaceField;

    private AttachmentsFileFieldManager attachmentsManager;

    protected final String sessionKey;

    abstract protected void saveProject();

    abstract protected void setValues();

    protected AbstractProjectEditRegisterForm(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this(viewContext, null);
    }

    protected AbstractProjectEditRegisterForm(
            final IViewContext<ICommonClientServiceAsync> viewContext, TechId projectIdOrNull)
    {
        super(viewContext, createId(projectIdOrNull), DEFAULT_LABEL_WIDTH + 20, DEFAULT_FIELD_WIDTH);
        this.viewContext = viewContext;
        sessionKey = createId(projectIdOrNull);
        attachmentsManager = new AttachmentsFileFieldManager(sessionKey, viewContext);
        projectCodeField = createProjectCodeField();
        spaceField = createGroupField();
        projectDescriptionField = createProjectDescriptionField();
        addUploadFeatures(sessionKey);
    }

    public static String createId(TechId id)
    {
        String editOrRegister = (id == null) ? "register" : ("edit_" + id);
        return GenericConstants.ID_PREFIX + "project-" + editOrRegister + "_form";
    }

    SpaceSelectionWidget getGroupField()
    {
        return spaceField;
    }

    private final CodeField createProjectCodeField()
    {
        final CodeField codeField = new CodeField(viewContext, viewContext.getMessage(Dict.CODE));
        codeField.setId(getId() + "_code");
        return codeField;
    }

    private final MultilineVarcharField createProjectDescriptionField()
    {
        return new DescriptionField(viewContext, false, getId());
    }

    private final SpaceSelectionWidget createGroupField()
    {
        SpaceSelectionWidget field = new SpaceSelectionWidget(viewContext, getId(), false, false);
        FieldUtil.markAsMandatory(field);
        field.setFieldLabel(viewContext.getMessage(Dict.GROUP));
        return field;
    }

    @Override
    protected void resetPanel()
    {
        super.resetPanel();
        attachmentsManager.resetAttachmentFieldSetsInPanel(formPanel);
    }

    @Override
    protected void revertPanel()
    {
        super.revertPanel();
        attachmentsManager.resetAttachmentFieldSetsInPanel(formPanel);
    }

    private final void addFormFields()
    {
        formPanel.add(projectCodeField);
        formPanel.add(spaceField);
        formPanel.add(projectDescriptionField);
        attachmentsManager.addAttachmentFieldSetsToPanel(formPanel);
        formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
            {
                @Override
                protected void onSuccessfullUpload()
                {
                    saveProject();
                }

                @Override
                protected void setUploadEnabled()
                {
                    AbstractProjectEditRegisterForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
    }

    void redefineSaveListeners()
    {
        saveButton.removeAllListeners();
        addSaveButtonConfirmationListener();
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        if (attachmentsManager.filesDefined() > 0)
                        {
                            setUploadEnabled(false);
                            formPanel.submit();
                        } else
                        {
                            saveProject();
                        }
                    }
                }
            });
    }

    @Override
    protected final void submitValidForm()
    {
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        setLoading(true);
        loadForm();
    }

    protected abstract void loadForm();

    protected void initGUI()
    {
        setValues();
        addFormFields();
        setLoading(false);
        layout();
    }

    protected List<NewAttachment> extractAttachments()
    {
        return attachmentsManager.extractAttachments();
    }

}
