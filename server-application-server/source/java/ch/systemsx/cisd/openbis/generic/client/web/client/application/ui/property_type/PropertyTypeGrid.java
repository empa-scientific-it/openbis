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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.XmlField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SemanticAnnotationGridColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyTypeGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid displaying property types.
 * 
 * @author Tomasz Pylak
 */
public class PropertyTypeGrid extends TypedTableGrid<PropertyType>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "property-type-browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        return new PropertyTypeGrid(viewContext).asDisposableWithoutToolbar();
    }

    private final IDelegatedAction postRegistrationCallback;

    private PropertyTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, true, DisplayTypeIDGenerator.PROPERTY_TYPE_BROWSER_GRID);
        extendBottomToolbar();
        postRegistrationCallback = createRefreshGridAction();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        final Button addButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD, "Property Type"),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    DispatcherHelper.dispatchNaviEvent(new ComponentProvider(
                                            viewContext).getPropertyTypeRegistration());
                                }
                            });
        addButton.setId(GRID_ID + "-add-button");
        addButton(addButton);

        addButton(createSelectedItemButton(
                viewContext.getMessage(Dict.BUTTON_EDIT),
                new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<PropertyType>>>()
                    {

                        @Override
                        public void invoke(
                                BaseEntityModel<TableModelRowWithObject<PropertyType>> selectedItem,
                                boolean keyPressed)
                        {
                            final PropertyType propertyType =
                                    selectedItem.getBaseObject().getObjectOrNull();
                            if (propertyType.isManagedInternally())
                            {
                                final String errorMsg =
                                        "Internally managed property types cannot be edited.";
                                GWTUtils.alert("Error", errorMsg);
                            } else
                            {
                                createEditDialog(propertyType).show();
                            }
                        }
                    }));

        Button deleteButton = createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                new AbstractCreateDialogListener()
                    {

                        @Override
                        protected Dialog createDialog(
                                List<TableModelRowWithObject<PropertyType>> propertyTypes,
                                IBrowserGridActionInvoker invoker)
                        {
                            return new PropertyTypeListDeletionConfirmationDialog(viewContext,
                                    propertyTypes, createRefreshCallback(invoker));
                        }

                        @Override
                        protected boolean validateSelectedData(
                                List<TableModelRowWithObject<PropertyType>> data)
                        {
                            String errorMsg =
                                    "Internally managed property types cannot be deleted.";
                            for (TableModelRowWithObject<PropertyType> propertyType : data)
                            {
                                if (propertyType.getObjectOrNull().isManagedInternally())
                                {
                                    GWTUtils.alert("Error", errorMsg);
                                    return false;
                                }
                            }
                            return true;
                        }
                    });
        deleteButton.setId(GRID_ID + "-delete-button");
        addButton(deleteButton);

        allowMultipleSelection(); // we allow deletion of multiple property types

        addEntityOperationsSeparator();
    }

    private Window createEditDialog(final PropertyType propertyType)
    {
        final String code = propertyType.getCode();
        final String description = propertyType.getDescription();
        final String label = propertyType.getLabel();
        final String title = viewContext.getMessage(Dict.EDIT_TITLE, "Property Type", code);
        final DataTypeCode dataTypeCode = propertyType.getDataType().getCode();

        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final DescriptionField descriptionField;

                private final TextField<String> labelField;

                private XmlField xmlSchemaField;

                private XmlField xslTransformationsField;

                {
                    boolean mandatory = true;

                    labelField = createTextField(viewContext.getMessage(Dict.LABEL), mandatory);
                    labelField.setMaxLength(GenericConstants.COLUMN_LABEL);
                    FieldUtil.setValueWithUnescaping(labelField, label);
                    addField(labelField);

                    descriptionField = createDescriptionField(viewContext, mandatory);
                    FieldUtil.setValueWithUnescaping(descriptionField, description);
                    addField(descriptionField);

                    if (dataTypeCode == DataTypeCode.XML)
                    {
                        xmlSchemaField = createXmlSchemaField();
                        FieldUtil.setValueWithUnescaping(xmlSchemaField, propertyType.getSchema());
                        addField(xmlSchemaField);

                        xslTransformationsField = createXslTransformationsField();
                        FieldUtil.setValueWithUnescaping(xslTransformationsField,
                                propertyType.getTransformation());
                        addField(xslTransformationsField);
                    }
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    propertyType.setDescription(descriptionField.getValue());
                    propertyType.setLabel(labelField.getValue());
                    if (dataTypeCode == DataTypeCode.XML)
                    {
                        propertyType.setSchema(xmlSchemaField.getValue());
                        propertyType.setTransformation(xslTransformationsField.getValue());
                    }

                    viewContext.getService().updatePropertyType(propertyType, registrationCallback);
                }

                // XML data type specific

                private final XmlField createXmlSchemaField()
                {
                    return new XmlField(viewContext.getMessage(Dict.XML_SCHEMA), false);
                }

                private final XmlField createXslTransformationsField()
                {
                    return new XmlField(viewContext.getMessage(Dict.XSLT), false);
                }
            };
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(PropertyTypeGridColumnIDs.LABEL, PropertyTypeGridColumnIDs.CODE,
                PropertyTypeGridColumnIDs.DATA_TYPE);
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<PropertyType>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<PropertyType>> schema =
                super.createColumnsDefinition();

        GridCellRenderer<BaseEntityModel<?>> multilineCellRenderer =
                createMultilineStringCellRenderer();
        schema.setGridCellRendererFor(PropertyTypeGridColumnIDs.DESCRIPTION, multilineCellRenderer);
        schema.setGridCellRendererFor(PropertyTypeGridColumnIDs.XML_SCHEMA, multilineCellRenderer);
        schema.setGridCellRendererFor(PropertyTypeGridColumnIDs.XSLT, multilineCellRenderer);

        new SemanticAnnotationGridColumns().setRenderers(schema);

        return schema;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<PropertyType>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<PropertyType>> callback)
    {
        viewContext.getService().listPropertyTypes(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<PropertyType>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportPropertyTypes(exportCriteria, callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] { createOrDelete(ObjectKind.PROPERTY_TYPE), edit(ObjectKind.PROPERTY_TYPE),
                createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }
}
