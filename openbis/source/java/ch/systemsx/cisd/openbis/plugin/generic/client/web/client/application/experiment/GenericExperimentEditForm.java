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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EditableExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityEditForm;

/**
 * The <i>generic</i> experiment edit form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericExperimentEditForm
        extends
        AbstractGenericEntityEditForm<ExperimentType, ExperimentTypePropertyType, ExperimentProperty, EditableExperiment>
{

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    public GenericExperimentEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            EditableExperiment entity, boolean editMode)
    {
        super(viewContext, entity, editMode);
        this.viewContext = viewContext;
    }

    public static final String ID_PREFIX = createId(EntityKind.EXPERIMENT, "");

    @Override
    public final void submitValidForm()
    {
        final List<ExperimentProperty> properties = extractProperties();
        viewContext.getCommonService().updateExperiment(entity.getIdentifier(), properties,
                new RegisterExperimentCallback(viewContext));
    }

    public final class RegisterExperimentCallback extends AbstractAsyncCallback<Void>
    {

        RegisterExperimentCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
        }

        private final String createSuccessfullRegistrationInfo()
        {
            return "Experiment successfully updated";
        }

        @Override
        protected final void process(final Void result)
        {
            infoBox.displayInfo(createSuccessfullRegistrationInfo());
            showCheckPage();
        }
    }

    @Override
    protected PropertiesEditor<ExperimentType, ExperimentTypePropertyType, ExperimentProperty> createPropertiesEditor(
            List<ExperimentTypePropertyType> entityTypesPropertyTypes,
            List<ExperimentProperty> properties, String id)
    {
        return new ExperimentPropertyEditor<ExperimentType, ExperimentTypePropertyType, ExperimentProperty>(
                entityTypesPropertyTypes, properties, id);
    }

    @Override
    protected List<Field<?>> getEntitySpecificFormFields()
    {
        return new ArrayList<Field<?>>();
    }

    @Override
    protected List<Widget> getEntitySpecificDisplayComponents()
    {
        return new ArrayList<Widget>();
    }

}
