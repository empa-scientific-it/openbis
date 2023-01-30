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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeFieldWithGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntitiesListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntitiesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MetaprojectArea;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MetaprojectChooserButton;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ButtonWithConfirmations;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ButtonWithConfirmations.IConfirmation;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ButtonWithConfirmations.IConfirmationChain;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * Super class for entity (sample, material, experiment, data set) registration and edition.
 * 
 * @author Izabela Adamczyk
 */
public abstract class AbstractGenericEntityRegistrationForm<T extends EntityType, S extends EntityTypePropertyType<T>>
        extends AbstractRegistrationForm implements IDatabaseModificationObserver
{
    public static final String ID_PREFIX = GenericConstants.ID_PREFIX;

    public static final String ID_SUFFIX_CODE = "code";

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    protected final EntityKind entityKind;

    protected final IViewContext<IGenericClientServiceAsync> viewContext;

    protected final TechId techIdOrNull;

    protected CodeFieldWithGenerator codeField;

    protected PropertiesEditor<T, S> propertiesEditor;

    protected MetaprojectArea metaprojectArea;

    protected MetaprojectChooserButton metaprojectChooserButton;

    private final Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions;

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------
    /**
     * For editing chosen entity.
     */
    protected AbstractGenericEntityRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptionsOrNull,
            IIdAndCodeHolder identifiableOrNull, EntityKind entityKind)
    {
        super(viewContext, createId(identifiableOrNull, entityKind), DEFAULT_LABEL_WIDTH + 90,
                DEFAULT_FIELD_WIDTH);
        this.viewContext = viewContext;
        this.inputWidgetDescriptions =
                inputWidgetDescriptionsOrNull == null ? Collections
                        .<String, List<IManagedInputWidgetDescription>> emptyMap()
                        : inputWidgetDescriptionsOrNull;
        this.entityKind = entityKind;
        this.techIdOrNull = TechId.create(identifiableOrNull);
    }

    /**
     * For registering new entity.
     */
    protected AbstractGenericEntityRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            EntityKind entityKind)
    {
        this(viewContext, inputWidgetDescriptions, null, entityKind);
    }

    // ---------------------------------------------------------------------------------------------
    // Main Part
    // ---------------------------------------------------------------------------------------------
    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        setLoading(true);
        loadForm();
    }

    // ---------------------------------------------------------------------------------------------
    // ID generation
    // ---------------------------------------------------------------------------------------------
    /**
     * Creates unique id based on {@link #createSimpleId(IIdAndCodeHolder, EntityKind)} and application specific ID prefix.
     */
    public static final String createId(IIdAndCodeHolder identifiable, EntityKind entityKind)
    {
        return ID_PREFIX + createSimpleId(identifiable, entityKind);
    }

    /**
     * Creates unique id based on {@link #createSimpleId(TechId, EntityKind)} and application specific ID prefix.
     */
    public static final String createId(TechId techId, EntityKind entityKind)
    {
        return ID_PREFIX + createSimpleId(techId, entityKind);
    }

    /**
     * Creates unique form id for given entity.
     */
    protected static final String createSimpleId(IIdAndCodeHolder identifiable,
            EntityKind entityKind)
    {
        return createSimpleId(TechId.create(identifiable), entityKind);
    }

    /**
     * Creates unique form id for given entity.
     */
    protected static final String createSimpleId(TechId techId, EntityKind entityKind)
    {
        String editOrRegister = (techId == null) ? "register" : ("edit_" + techId);
        return "generic-" + entityKind.name().toLowerCase() + "-" + editOrRegister + "_form";
    }

    // ---------------------------------------------------------------------------------------------
    // IDatabaseModificationObserver
    // ---------------------------------------------------------------------------------------------
    @Override
    public final void update(Set<DatabaseModificationKind> observedModifications)
    {
        createDatabaseModificationObserver().update(observedModifications);
    }

    @Override
    public final DatabaseModificationKind[] getRelevantModifications()
    {
        return createDatabaseModificationObserver().getRelevantModifications();
    }

    // the db modification observer is composed from all the fields' observers
    private IDatabaseModificationObserver createDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver compositeObserver =
                new CompositeDatabaseModificationObserver();
        compositeObserver.addObservers(getFormFieldsWithoutPropertyFields());
        compositeObserver.addObservers(getFormPropertyFields());
        return compositeObserver;
    }

    // ---------------------------------------------------------------------------------------------
    // Common Fields
    // ---------------------------------------------------------------------------------------------

    private final void createCommonFormFields()
    {
        propertiesEditor =
                createPropertiesEditor(createId(techIdOrNull, entityKind), inputWidgetDescriptions,
                        viewContext.getCommonViewContext());
        codeField =
                new CodeFieldWithGenerator(viewContext, viewContext.getMessage(Dict.CODE),
                        getGeneratedCodePrefix(), entityKind, isAutoGenerateCode());
        codeField.setId(getId() + ID_SUFFIX_CODE);
        boolean codeReadonly = techIdOrNull != null || isAutoGenerateCode();
        codeField.setReadOnly(codeReadonly);
        codeField.setHideTrigger(codeReadonly);
        if (techIdOrNull != null)
        {
            // we don't want to validate code during edition
            // (contained sample code has ':' inside and it is invalid)
            codeField.disable();
        }

        if (isAutoGenerateCode())
        {
            formPanel.addDirtyCheckIgnoredField(codeField);
        }

        metaprojectArea = new MetaprojectArea(viewContext, getId());
        metaprojectChooserButton =
                new MetaprojectChooserButton(viewContext, getId(),
                        new IChosenEntitiesProvider<String>()
                            {
                                @Override
                                public List<String> getEntities()
                                {
                                    String[] metaprojects = metaprojectArea.tryGetMetaprojects();
                                    return metaprojects != null ? Arrays.asList(metaprojects)
                                            : null;
                                }

                                @Override
                                public boolean isBlackList()
                                {
                                    return true;
                                }
                            });
        metaprojectChooserButton
                .addChosenEntityListener(new IChosenEntitiesListener<TableModelRowWithObject<Metaproject>>()
                    {
                        @Override
                        public void entitiesChosen(
                                List<TableModelRowWithObject<Metaproject>> entities)
                        {
                            for (TableModelRowWithObject<Metaproject> entity : entities)
                            {
                                metaprojectArea.appendItem(entity.getObjectOrNull().getName());
                            }
                        }
                    });
    }

    @Override
    protected void addSaveButtonConfirmationListener(ButtonWithConfirmations button)
    {
        super.addSaveButtonConfirmationListener(button);

        button.addConfirmation(new IConfirmation()
            {
                @Override
                public void confirm(final IConfirmationChain confirmationChain)
                {
                    String[] requestedMetaprojects = metaprojectArea.tryGetMetaprojects();

                    if (formPanel.isValid() && requestedMetaprojects != null
                            && requestedMetaprojects.length > 0)
                    {
                        viewContext.getCommonService().listMetaprojects(
                                new CreateNonExistingMetaprojectsConfirmationCallback(viewContext,
                                        confirmationChain, requestedMetaprojects));
                    } else
                    {
                        confirmationChain.next();
                    }
                }
            });
    }

    private static class CreateNonExistingMetaprojectsConfirmationCallback extends
            AbstractAsyncCallback<List<Metaproject>>
    {

        private IConfirmationChain confirmationChain;

        private String[] requestedMetaprojects;

        public CreateNonExistingMetaprojectsConfirmationCallback(IViewContext<?> viewContext,
                IConfirmationChain confirmationChain, String[] requestedMetaprojects)
        {
            super(viewContext);
            this.confirmationChain = confirmationChain;
            this.requestedMetaprojects = requestedMetaprojects;
        }

        @Override
        protected void process(List<Metaproject> existingMetaprojectsList)
        {
            Set<String> existingMetaprojectsSet = new HashSet<String>();
            Set<String> notExistingMetaprojects = new LinkedHashSet<String>();

            if (existingMetaprojectsList != null)
            {
                for (Metaproject existingMetaproject : existingMetaprojectsList)
                {
                    existingMetaprojectsSet.add(existingMetaproject.getName().toLowerCase());
                }
            }

            if (requestedMetaprojects != null)
            {
                for (String requestedMetaproject : requestedMetaprojects)
                {
                    if (existingMetaprojectsSet.contains(requestedMetaproject.toLowerCase()) == false)
                    {
                        notExistingMetaprojects.add(requestedMetaproject);
                    }
                }
            }

            if (notExistingMetaprojects.isEmpty())
            {
                confirmationChain.next();
            } else
            {
                new ConfirmationDialog(
                        viewContext
                                .getMessage(Dict.CREATE_NOT_EXISTING_METAPROJECTS_CONFIRMATION_TITLE),
                        viewContext.getMessage(
                                Dict.CREATE_NOT_EXISTING_METAPROJECTS_CONFIRMATION_MSG,
                                notExistingMetaprojects))
                    {
                        @Override
                        protected void onYes()
                        {
                            confirmationChain.next();
                        }
                    }.show();
            }
        }

    }

    /**
     * Specifies if the code should be automatically generated.
     */
    protected boolean isAutoGenerateCode()
    {
        return false;
    }

    /**
     * Returns prefix that will be used by code generator.
     */
    protected String getGeneratedCodePrefix()
    {
        return entityKind.name().substring(0, 1);
    }

    protected void updatePropertyFieldsOriginalValues()
    {
        for (DatabaseModificationAwareField<?> f : propertiesEditor.getPropertyFields())
        {
            updateFieldOriginalValue(f.get());
        }
    }

    protected <D> void updateFieldOriginalValue(Field<D> field)
    {
        field.updateOriginalValue(field.getValue());
    }

    /**
     * Returns form fields without fields for properties.
     */
    private final List<DatabaseModificationAwareField<?>> getFormFieldsWithoutPropertyFields()
    {
        List<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(DatabaseModificationAwareField.wrapUnaware(codeField));
        for (DatabaseModificationAwareField<?> specificField : getEntitySpecificFormFields())
        {
            fields.add(specificField);
        }
        fields.add(DatabaseModificationAwareField.wrapUnaware(metaprojectArea));
        fields.add(DatabaseModificationAwareField.wrapUnaware(metaprojectChooserButton.getField()));
        return fields;
    }

    /**
     * Returns all form fields for properties.
     */
    private final List<DatabaseModificationAwareField<?>> getFormPropertyFields()
    {
        return propertiesEditor.getPropertyFields();
    }

    /**
     * Creates, initializes and adds the fields to the form. To be used by the subclasses.
     */
    protected void initGUI()
    {
        createCommonFormFields();
        createEntitySpecificFormFields();
        initializeFormFields();
        addFormFieldsToPanel(formPanel);
        layout();
        postRenderingTask();
        setLoading(false);
    }

    protected void postRenderingTask()
    {
    }

    /**
     * Adds previously created and initialized fields to the form.
     */
    protected void addFormFieldsToPanel(FormPanel panel)
    {
        for (DatabaseModificationAwareField<?> fieldHolder : getFormFieldsWithoutPropertyFields())
        {
            panel.add(fieldHolder.get());
        }
        propertiesEditor.addPropertyFieldsWithFieldsetToPanel(panel);
    }

    /**
     * @see PropertiesEditor#extractProperties()
     */
    protected final List<IEntityProperty> extractProperties()
    {
        return propertiesEditor.extractProperties();
    }

    // ---------------------------------------------------------------------------------------------
    // Abstract methods
    // ---------------------------------------------------------------------------------------------

    /**
     * Creates fields specific to given entity kind.
     */
    abstract protected void createEntitySpecificFormFields();

    /**
     * Returns previously created fields specific to given entity kind, to be added to the form.
     */
    abstract protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields();

    /**
     * Initializes previously created form fields, before adding them to the form.
     */
    abstract protected void initializeFormFields();

    /**
     * Loads necessary data from the server and creates the form.
     */
    abstract protected void loadForm();

    /**
     * Returns the {@link PropertiesEditor} to be used for .
     */
    abstract protected PropertiesEditor<T, S> createPropertiesEditor(String string,
            Map<String, List<IManagedInputWidgetDescription>> widgetDescriptions,
            IViewContext<ICommonClientServiceAsync> context);

}
