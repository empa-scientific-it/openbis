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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.IEditableEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.EntityPropertyGrid;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * The <i>generic</i> entity edit form. Two modes are available: edit and check.
 * 
 * @author Izabela Adamczyk
 */
abstract public class AbstractGenericEntityEditForm<T extends EntityType, S extends EntityTypePropertyType<T>, P extends EntityProperty<T, S>, V extends IEditableEntity<T, S, P>>
        extends AbstractRegistrationForm implements IDatabaseModificationObserver
{

    private final PropertiesEditor<T, S, P> editor;

    private final EntityPropertyGrid<T, S, P> grid;

    private boolean editMode;

    protected final V entity;

    private final List<Widget> checkComponents;

    abstract protected List<Widget> getEntitySpecificCheckPageWidgets();

    abstract protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields();

    abstract protected void updateCheckPageWidgets();

    abstract protected PropertiesEditor<T, S, P> createPropertiesEditor(
            List<S> entityTypesPropertyTypes, List<P> properties, String string,
            IViewContext<ICommonClientServiceAsync> context);

    public AbstractGenericEntityEditForm(final IViewContext<?> viewContext, V entity,
            boolean editMode)
    {
        super(viewContext, createId(entity.getEntityKind(), entity.getIdentifier()));
        this.checkComponents = new ArrayList<Widget>();
        this.entity = entity;
        this.editMode = editMode;
        editor =
                createPropertiesEditor(entity.getEntityTypePropertyTypes(), entity.getProperties(),
                        createId(entity.getEntityKind(), entity.getIdentifier()), viewContext
                                .getCommonViewContext());
        grid = new EntityPropertyGrid<T, S, P>(viewContext, entity.getProperties());
    }

    protected void initializeComponents(final IViewContext<?> viewContext)
    {
        checkComponents.add(grid.getWidget());
        for (Widget w : getEntitySpecificCheckPageWidgets())
        {
            checkComponents.add(w);
        }
        for (Widget w : checkComponents)
        {
            add(w, new RowData(1, -1, new Margins(5)));
        }
    }

    protected static String createId(EntityKind entityKind, String identifier)
    {
        return GenericConstants.ID_PREFIX + createSimpleId(entityKind, identifier);
    }

    protected static String createSimpleId(EntityKind entityKind, String identifier)
    {
        return "generic-" + entityKind.name().toLowerCase() + "-edit_form_" + identifier;
    }

    private final List<DatabaseModificationAwareField<?>> getAllFields()
    {
        List<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        for (DatabaseModificationAwareField<?> specificField : editor.getPropertyFields())
        {
            fields.add(specificField);
        }
        for (DatabaseModificationAwareField<?> propertyField : getEntitySpecificFormFields())
        {
            fields.add(propertyField);
        }
        return fields;
    }

    private final void addFormFields()
    {
        for (DatabaseModificationAwareField<?> field : getAllFields())
        {
            formPanel.add(field.get());
        }
    }

    protected List<P> extractProperties()
    {
        return editor.extractProperties();
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        setEditMode(editMode);
        addFormFields();
    }

    private void setEditMode(boolean edit)
    {
        this.editMode = edit;
        formPanel.setVisible(edit);
        for (Widget w : checkComponents)
        {
            w.setVisible(edit == false);
        }
    }

    protected void showCheckPage()
    {
        updateState();
        setEditMode(false);
    }

    protected void updateState()
    {
        for (DatabaseModificationAwareField<?> f : editor.getPropertyFields())
        {
            updateOriginalValue(f.get());
        }
        entity.setProperties(editor.extractProperties());
        grid.setProperties(entity.getProperties());
        updateCheckPageWidgets();
    }

    @SuppressWarnings("unchecked")
    private void updateOriginalValue(Field field)
    {
        field.updateOriginalValue(field.getValue());
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        createDatabaseModificationObserver().update(observedModifications);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return createDatabaseModificationObserver().getRelevantModifications();
    }

    // the db modification observer is composed from all the fields' observers
    private IDatabaseModificationObserver createDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver compositeObserver =
                new CompositeDatabaseModificationObserver();
        compositeObserver.addObservers(getAllFields());
        return compositeObserver;
    }

}
