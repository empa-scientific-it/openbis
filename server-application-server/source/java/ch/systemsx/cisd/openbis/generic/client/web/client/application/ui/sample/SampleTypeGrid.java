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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AbstractEditEntityTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AbstractEntityTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AddEntityTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AddTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractSaveDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityTypeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid displaying sample types.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class SampleTypeGrid extends AbstractEntityTypeGrid<SampleType>
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "sample-type-browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    private static final Boolean DEFAULT_LISTABLE_VALUE = true;

    private static final Boolean DEFAULT_AUTO_GENERATE_CODES_VALUE = false;

    private static final Boolean DEFAULT_SHOW_PARENT_METADATA_VALUE = false;

    private static final Boolean DEFAULT_UNIQUE_SUBCODES_VALUE = false;

    private static final String DEFAULT_GENERATED_CODE_PREFIX_VALUE = "S";

    private static final Boolean DEFAULT_SHOW_CONTAINER_VALUE = false;

    private static final Boolean DEFAULT_SHOW_PARENTS_VALUE = true;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, ComponentProvider componentProvider)
    {
        final SampleTypeGrid grid = new SampleTypeGrid(viewContext, componentProvider);
        return grid.asDisposableWithoutToolbar();
    }

    private SampleTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext, ComponentProvider componentProvider)
    {
        super(viewContext, componentProvider, BROWSER_ID, GRID_ID);
    }

    @Override
    public AddEntityTypeDialog<SampleType> getNewDialog(SampleType newType)
    {
        return (AddEntityTypeDialog<SampleType>) createRegisterEntityTypeDialog("New " + viewContext.getMessage(Dict.SAMPLE),
                newType, newType.getEntityKind());
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<SampleType>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<SampleType>> callback)
    {
        viewContext.getService().listSampleTypes(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<SampleType>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportSampleTypes(exportCriteria, callback);
    }

    @Override
    protected EntityKind getEntityKindOrNull()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    protected void register(SampleType sampleType, AsyncCallback<Void> registrationCallback)
    {
        viewContext.getService().registerSampleType(sampleType, registrationCallback);
    }

    @Override
    protected SampleType createNewEntityType()
    {
        return new SampleType();
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<SampleType>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<SampleType>> schema =
                super.createColumnsDefinition();

        new SemanticAnnotationGridColumns().setRenderers(schema);

        return schema;
    }

    @Override
    protected Window createEditEntityTypeDialog(final EntityKind entityKind,
            final SampleType sampleType)
    {
        final String code = sampleType.getCode();
        String title =
                viewContext.getMessage(Dict.EDIT_TYPE_TITLE_TEMPLATE,
                        EntityTypeUtils.translatedEntityKindForUI(viewContext, entityKind), code);
        return new AbstractEditEntityTypeDialog<SampleType>(viewContext, title,
                postRegistrationCallback,
                EntityKind.SAMPLE, sampleType)
            {
                private final SampleTypeDialogFieldHelper helper;

                {
                    helper =
                            new SampleTypeDialogFieldHelper(viewContext, this, sampleType,
                                    createHelpPageIdentifier());
                }

                @Override
                protected void setSpecificAttributes(SampleType entityType)
                {
                    helper.setAttributes(entityType);
                }

                private HelpPageIdentifier createHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.SAMPLE_TYPE,
                            HelpPageIdentifier.HelpPageAction.EDIT);
                }
            };
    }

    @Override
    protected Window createRegisterEntityTypeDialog(String title, SampleType newEntityType,
            EntityKind entityKind)
    {
        return new AddEntityTypeDialog<SampleType>(viewContext, title, postRegistrationCallback,
                newEntityType, entityKind)
            {
                private final SampleTypeDialogFieldHelper helper;

                {
                    SampleType sampleType = new SampleType();
                    sampleType.setListable(DEFAULT_LISTABLE_VALUE);
                    sampleType.setShowContainer(DEFAULT_SHOW_CONTAINER_VALUE);
                    sampleType.setShowParents(DEFAULT_SHOW_PARENTS_VALUE);
                    sampleType.setSubcodeUnique(DEFAULT_UNIQUE_SUBCODES_VALUE);
                    sampleType.setAutoGeneratedCode(DEFAULT_AUTO_GENERATE_CODES_VALUE);
                    sampleType.setShowParentMetadata(DEFAULT_SHOW_PARENT_METADATA_VALUE);
                    sampleType.setGeneratedCodePrefix(DEFAULT_GENERATED_CODE_PREFIX_VALUE);
                    helper =
                            new SampleTypeDialogFieldHelper(viewContext, this, sampleType,
                                    createHelpPageIdentifier());
                }

                @Override
                protected void register(SampleType sampleType,
                        AsyncCallback<Void> registrationCallback)
                {
                    helper.setAttributes(sampleType);
                    SampleTypeGrid.this.register(sampleType, registrationCallback);
                }

                private HelpPageIdentifier createHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.SAMPLE_TYPE,
                            HelpPageIdentifier.HelpPageAction.REGISTER);
                }
            };
    }

    //
    // Helpers
    //

    private static final class SampleTypeDialogFieldHelper
    {
        private static CheckBoxField createCheckBoxField(final String title, Boolean value)
        {
            final CheckBoxField field = new CheckBoxField(title, false);
            field.setValue(value);
            return field;
        }

        private final CheckBoxField showContainerField;

        private final CheckBoxField showParentsField;

        private final CheckBoxField listableField;

        private final CheckBoxField subcodeUniqueField;

        private final CheckBoxField autoGeneratedCodeField;

        private final CheckBoxField showParentMetadataField;

        private final TextField<String> generatedCodePrefixField;

        public SampleTypeDialogFieldHelper(IViewContext<ICommonClientServiceAsync> viewContext,
                AbstractSaveDialog dialog, SampleType sampleType,
                HelpPageIdentifier helpPageIdentifier)
        {
            listableField =
                    SampleTypeDialogFieldHelper.createCheckBoxField(
                            viewContext.getMessage(Dict.LISTABLE), sampleType.isListable());
            listableField.setId(AddTypeDialog.DIALOG_ID + "-listable");
            dialog.addField(listableField);

            showContainerField =
                    SampleTypeDialogFieldHelper.createCheckBoxField(
                            viewContext.getMessage(Dict.SHOW_CONTAINER),
                            sampleType.isShowContainer());
            showContainerField.setId(AddTypeDialog.DIALOG_ID + "-show-container");
            dialog.addField(showContainerField);

            showParentsField =
                    SampleTypeDialogFieldHelper.createCheckBoxField(
                            viewContext.getMessage(Dict.SHOW_PARENTS), sampleType.isShowParents());
            showParentsField.setId(AddTypeDialog.DIALOG_ID + "-show-parents");
            dialog.addField(showParentsField);

            subcodeUniqueField =
                    SampleTypeDialogFieldHelper.createCheckBoxField(
                            viewContext.getMessage(Dict.SUBCODE_UNIQUE_LABEL),
                            sampleType.isSubcodeUnique());
            subcodeUniqueField.setId(AddTypeDialog.DIALOG_ID + "-subcode-unique");
            dialog.addField(subcodeUniqueField);

            autoGeneratedCodeField =
                    SampleTypeDialogFieldHelper.createCheckBoxField(
                            viewContext.getMessage(Dict.AUTO_GENERATE_CODES_LABEL),
                            sampleType.isAutoGeneratedCode());
            autoGeneratedCodeField.setId(AddTypeDialog.DIALOG_ID + "-autogenerated-code");
            dialog.addField(autoGeneratedCodeField);

            showParentMetadataField =
                    SampleTypeDialogFieldHelper.createCheckBoxField(
                            viewContext.getMessage(Dict.SHOW_PARENT_METADATA_LABEL),
                            sampleType.isShowParentMetadata());
            showParentMetadataField.setId(AddTypeDialog.DIALOG_ID + "-show-parent-metadata");
            dialog.addField(showParentMetadataField);

            generatedCodePrefixField =
                    new CodeField(viewContext, viewContext.getMessage(Dict.GENERATED_CODE_PREFIX));
            generatedCodePrefixField.setValue(sampleType.getGeneratedCodePrefix());
            generatedCodePrefixField.setId(AddTypeDialog.DIALOG_ID + "-generated-code-prefix");
            dialog.addField(generatedCodePrefixField);

            DialogWithOnlineHelpUtils.addHelpButton(viewContext, dialog, helpPageIdentifier);

        }

        public void setAttributes(SampleType sampleType)
        {
            sampleType.setShowParents(showParentsField.getValue());
            sampleType.setShowContainer(showContainerField.getValue());
            sampleType.setListable(listableField.getValue());
            sampleType.setAutoGeneratedCode(autoGeneratedCodeField.getValue());
            sampleType.setShowParentMetadata(showParentMetadataField.getValue());
            sampleType.setSubcodeUnique(subcodeUniqueField.getValue());
            sampleType.setGeneratedCodePrefix(generatedCodePrefixField.getValue());

        }
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] { createOrDelete(ObjectKind.SAMPLE_TYPE),
                edit(ObjectKind.SAMPLE_TYPE),
                createOrDelete(ObjectKind.PROPERTY_TYPE),
                edit(ObjectKind.PROPERTY_TYPE),
                createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT)
        };
    }

}
