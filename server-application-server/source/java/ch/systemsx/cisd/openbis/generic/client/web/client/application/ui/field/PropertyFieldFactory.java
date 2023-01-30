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
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyTermModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

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
        String description = pt.getDescription();
        if (StringUtils.isBlank(description) == false)
        {
            AbstractImagePrototype infoIcon =
                    AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
            FieldUtil.addInfoIcon(field, description, infoIcon.createImage());
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
                return wrapUnaware(setValue(new CheckBoxField(label, isMandatory), originalRawValue));
            case VARCHAR:
                return wrapUnaware(setValue(new VarcharField(label, isMandatory), originalRawValue));
            case DATE:
                return wrapUnaware(setValue(new DateDateFormField(label, isMandatory), originalRawValue));
            case TIMESTAMP:
                return wrapUnaware(setValue(new DateFormField(label, isMandatory), originalRawValue));
            case CONTROLLEDVOCABULARY:
                return createControlledVocabularyField(fieldId, label, pt.getVocabulary(),
                        isMandatory, viewContext, originalRawValue);
            case INTEGER:
                return wrapUnaware(setValue(new IntegerField(label, isMandatory), originalRawValue));
            case REAL:
                return wrapUnaware(setValue(new RealField(label, isMandatory), originalRawValue));
            case MATERIAL:
                return wrapUnaware(MaterialChooserField.create(label, isMandatory, pt
                        .getMaterialType(), originalRawValue, viewContext));
            case SAMPLE:
                return wrapUnaware(setValue(new VarcharField(label, isMandatory), originalRawValue));
            case HYPERLINK:
                return wrapUnaware(setValue(new HyperlinkField(label, isMandatory),
                        originalRawValue));
            case MULTILINE_VARCHAR:
                return wrapUnaware(setValue(new MultilineVarcharField(label, isMandatory),
                        originalRawValue));
            case XML:
                return wrapUnaware(setValue(new XmlField(label, isMandatory), originalRawValue));
        }
        throw new IllegalStateException("unknown enum " + dataType);
    }

    private static DatabaseModificationAwareField<?> createControlledVocabularyField(
            String fieldId, String label, Vocabulary vocabulary, boolean isMandatory,
            IViewContext<ICommonClientServiceAsync> viewContext, String originalRawValue)
    {
        if (vocabulary.isChosenFromList())
        {
            return VocabularyTermSelectionWidget.create(fieldId, label, vocabulary, isMandatory,
                    viewContext, originalRawValue);
        } else
        {
            return wrapUnaware(setValue(new VocabularyTermField(viewContext, label, isMandatory),
                    originalRawValue));
        }
    }

    private static DatabaseModificationAwareField<?> wrapUnaware(Field<?> field)
    {
        return DatabaseModificationAwareField.wrapUnaware(field);
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
            final VocabularyTerm term = ((VocabularyTermModel) value).getTerm();
            return term.getCode();
        } else
        {
            return value.toString();
        }
    }

    private static <T> Field<T> setValue(Field<T> field, String originalRawValue)
    {
        if (originalRawValue != null)
        {
            field.setValue(field.getPropertyEditor().convertStringValue(originalRawValue));
        }
        return field;
    }
}
