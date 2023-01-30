/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.google.gwt.event.dom.client.KeyCodes;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyTermModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DateDateFormField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DateFormField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.HyperlinkField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntitiesListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IntegerField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MaterialChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.RealField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VocabularyTermSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Utility methods for columns.
 * 
 * @author Franz-Josef Elmer
 */
public class ColumnUtils
{
    /**
     * Creates a {@link CellEditor} based on specified data type.
     */
    public static <T> CellEditor createCellEditor(IColumnDefinitionUI<T> column,
            IViewContext<?> viewContext)
    {
        final CellEditor editor;
        DataTypeCode dataType = column.tryToGetDataType();
        assert dataType != null;
        switch (dataType)
        {
            case INTEGER:
                editor = new StringBasedCellEditor(new IntegerField("", false));
                break;
            case REAL:
                editor = new StringBasedCellEditor(new RealField("", false));
                break;
            case DATE:
                editor = new StringBasedCellEditor(new DateDateFormField("", false));
                break;
            case TIMESTAMP:
                editor = new StringBasedCellEditor(new DateFormField("", false));
                break;
            case VARCHAR:
                editor = new StringEscapingCellEditor(new VarcharField("", false));
                break;
            case HYPERLINK:
                editor = new StringEscapingCellEditor(new HyperlinkField("", false));
                break;
            case XML:
            case MULTILINE_VARCHAR:
                editor = new StringEscapingCellEditor(new MultilineVarcharField("", false))
                    {
                        // WORKAROUND to allow use enter key in table editing
                        @Override
                        protected void onSpecialKey(FieldEvent fe)
                        {
                            if (fe.getKeyCode() != KeyCodes.KEY_ENTER)
                            {
                                super.onSpecialKey(fe);
                            }
                        }
                    };
                break;
            case BOOLEAN:
                final SimpleComboBox<String> combo = new SimpleComboBox<String>();
                combo.setForceSelection(true);
                combo.setTriggerAction(TriggerAction.ALL);
                combo.add("true");
                combo.add("false");
                editor = new CellEditor(combo)
                    {
                        @Override
                        public Object preProcessValue(Object value)
                        {
                            if (value == null)
                            {
                                return value;
                            }
                            return combo.findModel(value.toString());
                        }

                        @Override
                        public Object postProcessValue(Object value)
                        {
                            if (value == null)
                            {
                                return value;
                            }
                            return ((ModelData) value).get("value");
                        }
                    };
                break;
            case CONTROLLEDVOCABULARY:
                final VocabularyTermSelectionWidget vocabularyTermSelectionWidget =
                        (VocabularyTermSelectionWidget) VocabularyTermSelectionWidget.create(
                                "_table_edit", "", column.tryGetVocabulary(), false, viewContext,
                                null, false).get();
                editor = new CellEditor(vocabularyTermSelectionWidget)
                    {
                        @Override
                        public Object preProcessValue(Object value)
                        {
                            if (value == null)
                            {
                                return value;
                            }

                            return vocabularyTermSelectionWidget.findModel(value.toString());
                        }

                        @Override
                        public Object postProcessValue(Object value)
                        {
                            if (value == null)
                            {
                                return value;
                            }
                            return ((VocabularyTermModel) value).getTerm();
                        }
                    };
                break;
            case MATERIAL:
                MaterialChooserField materialChooser =
                        MaterialChooserField.create("", false, null, null,
                                viewContext.getCommonViewContext());
                editor = new StringBasedCellEditor(materialChooser);
                materialChooser.addListener(Events.TriggerClick, new Listener<BaseEvent>()
                    {
                        @Override
                        public void handleEvent(BaseEvent be)
                        {
                            editor.setAllowBlur(true);
                        }
                    });
                materialChooser
                        .addChosenEntityListener(new IChosenEntitiesListener<TableModelRowWithObject<Material>>()
                            {
                                @Override
                                public void entitiesChosen(
                                        List<TableModelRowWithObject<Material>> entities)
                                {
                                    editor.completeEdit();
                                }
                            });
                break;
            case SAMPLE:
                editor = new StringEscapingCellEditor(new VarcharField("", false));
                break;
            default:
                throw new UserFailureException("Edition of properties of type '" + dataType
                        + "' is not supported.");
        }
        return editor;
    }

    /**
     * Extension of GXT {@link CellEditor} with automatic conversion from String values that we hold in tables to specific data types (like
     * {@link Date} or {@link Boolean}) handled by editor fields and vice versa.
     */
    private static class StringBasedCellEditor extends CellEditor
    {

        public StringBasedCellEditor(Field<? extends Object> field)
        {
            super(field);
        }

        @Override
        public Object preProcessValue(Object value)
        {
            if (StringUtils.isBlank(value))
            {
                return null;
            }
            return getField().getPropertyEditor().convertStringValue(value.toString());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object postProcessValue(Object value)
        {
            if (StringUtils.isBlank(value))
            {
                return null;
            }
            return getField().getPropertyEditor().getStringValue(value);
        }

    }

    /**
     * Extension of GXT {@link CellEditor} with escaping of String values.
     */
    private static class StringEscapingCellEditor extends CellEditor
    {

        public StringEscapingCellEditor(Field<? extends Object> field)
        {
            super(field);
        }

        @Override
        public Object preProcessValue(Object value)
        {
            if (value == null)
            {
                return value;
            }
            return StringEscapeUtils.unescapeHtml(value.toString());
        }

        @Override
        public Object postProcessValue(Object value)
        {
            if (value == null)
            {
                return null;
            }
            return StringEscapeUtils.escapeHtml(value.toString());
        }

    }
}
