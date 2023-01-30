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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.form;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.field.MetaprojectNameField;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;

/**
 * A {@link LayoutContainer} extension for registering and editing metaprojects.
 * 
 * @author pkupczyk
 */
abstract class AbstractMetaprojectEditRegisterForm extends AbstractRegistrationForm
{

    protected final IViewContext<?> viewContext;

    protected MetaprojectNameField metaprojectNameField;

    protected MultilineVarcharField metaprojectDescriptionField;

    abstract protected void saveMetaproject();

    abstract protected void setValues();

    protected AbstractMetaprojectEditRegisterForm(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this(viewContext, null);
    }

    protected AbstractMetaprojectEditRegisterForm(final IViewContext<?> viewContext,
            Long metaprojectIdOrNull)
    {
        super(viewContext, createId(metaprojectIdOrNull), DEFAULT_LABEL_WIDTH + 20,
                DEFAULT_FIELD_WIDTH);
        this.viewContext = viewContext;
        metaprojectNameField = createNameField();
        metaprojectDescriptionField = createDescriptionField();
    }

    public static String createId(Long id)
    {
        String editOrRegister = (id == null) ? "register" : ("edit_" + id);
        return GenericConstants.ID_PREFIX + "metaproject-" + editOrRegister + "_form";
    }

    private final MetaprojectNameField createNameField()
    {
        final MetaprojectNameField field = new MetaprojectNameField(viewContext);
        field.setId(getId() + "_name");
        return field;
    }

    private final MultilineVarcharField createDescriptionField()
    {
        return new DescriptionField(viewContext, false, getId());
    }

    private final void addFormFields()
    {
        formPanel.add(metaprojectNameField);
        formPanel.add(metaprojectDescriptionField);
    }

    @Override
    protected final void submitValidForm()
    {
        saveMetaproject();
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

}
