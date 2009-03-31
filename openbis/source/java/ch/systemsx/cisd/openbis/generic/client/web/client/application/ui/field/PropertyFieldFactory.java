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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import java.util.Date;

import com.extjs.gxt.ui.client.widget.form.Field;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyTermModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

public class PropertyFieldFactory
{
    /**
     * Creates a field for given data type.
     */
    public static DatabaseModificationAwareField<?> createField(final PropertyType pt,
            boolean isMandatory, String label, String fieldId, String originalRawValue,
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final DatabaseModificationAwareField<?> fieldHolder =
                doCreateField(pt, isMandatory, label, fieldId, originalRawValue, viewContext);
        Field<?> field = fieldHolder.get();
        field.setId(fieldId);
        if (originalRawValue != null)
        {
            setValue(field, originalRawValue);
        }
        return fieldHolder;
    }

    private static DatabaseModificationAwareField<?> doCreateField(final PropertyType pt,
            boolean isMandatory, String label, String fieldId, String originalRawValue,
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final DataTypeCode dataType = pt.getDataType().getCode();
        switch (dataType)
        {
            case BOOLEAN:
                return wrapUnaware(new CheckBoxField(label, isMandatory));
            case VARCHAR:
                return wrapUnaware(new VarcharField(label, isMandatory));
            case TIMESTAMP:
                return wrapUnaware(new DateFormField(label, isMandatory));
            case CONTROLLEDVOCABULARY:
                return VocabularyTermSelectionWidget.create(fieldId, label, pt.getVocabulary(),
                        isMandatory, viewContext);
            case INTEGER:
                return wrapUnaware(new IntegerField(label, isMandatory));
            case REAL:
                return wrapUnaware(new RealField(label, isMandatory));
            case MATERIAL:
                return wrapUnaware(MaterialChooserField.create(label, isMandatory, pt
                        .getMaterialType(), originalRawValue, viewContext));
        }
        throw new IllegalStateException("unknown enum " + dataType);
    }

    private static DatabaseModificationAwareField<?> wrapUnaware(Field<?> field)
    {
        return DatabaseModificationAwareField.wrapUnaware(field);
    }

    private static <T> void setValue(final Field<T> field, String originalRawValue)
    {
        field.setValue(field.getPropertyEditor().convertStringValue(originalRawValue));
    }

    public static final String valueToString(final Object value)
    {
        if (value == null)
        {
            return null;
        } else if (value instanceof Date)
        {
            return DateRenderer.renderDate((Date) value);
        } else if (value instanceof VocabularyTermModel)
        {
            return ((VocabularyTermModel) value).getTerm();
        } else
        {
            return value.toString();
        }
    }
}