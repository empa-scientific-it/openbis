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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.form.Field;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * A field for selecting a sample from a list or by specifying sample identifier.
 * 
 * @author Piotr Buczek
 */
public class SampleChooserField extends ChosenEntitySetter<TableModelRowWithObject<Sample>>
{
    public interface SampleChooserFieldAdaptor
    {
        Field<?> getField();

        SampleChooserField getChooserField();

        /** @return the sample identifier (as a string) which is set as a field value */
        String getValue();

        void updateOriginalValue();

        void updateValue(String identifierOrNull);
    }

    /**
     * Creates a text field with the additional browse button which allow to choose a sample from the list.
     */
    public static SampleChooserFieldAdaptor create(final String labelField,
            final boolean mandatory, final String initialValueOrNull, final boolean addShared,
            boolean addAll, final boolean excludeWithoutExperiment,
            final IViewContext<ICommonClientServiceAsync> viewContext,
            SampleTypeDisplayID sampleTypeDisplayID, boolean multipleSelection)
    {
        return create(labelField, mandatory, initialValueOrNull, addShared, addAll,
                excludeWithoutExperiment, viewContext, null, sampleTypeDisplayID, multipleSelection);

    }

    public static SampleChooserFieldAdaptor create(final String labelField,
            final boolean mandatory, final String initialValueOrNull, final boolean addShared,
            final boolean addAll, final boolean excludeWithoutExperiment,
            final IViewContext<ICommonClientServiceAsync> viewContext, String idOrNull,
            final SampleTypeDisplayID sampleTypeDisplayID, final boolean multipleSelection)
    {
        final SampleChooserField chooserField =
                new SampleChooserField(mandatory, initialValueOrNull, viewContext)
                    {
                        @Override
                        protected void onTriggerClick(ComponentEvent ce)
                        {
                            super.onTriggerClick(ce);
                            browse(viewContext, this, addShared, addAll, excludeWithoutExperiment,
                                    sampleTypeDisplayID, multipleSelection);
                        }
                    };
        if (idOrNull != null)
        {
            chooserField.setId(idOrNull);
        }
        chooserField.setFieldLabel(labelField);
        return asSampleChooserFieldAdaptor(chooserField);
    }

    private static SampleChooserFieldAdaptor asSampleChooserFieldAdaptor(
            final SampleChooserField chooserField)
    {
        return new SampleChooserFieldAdaptor()
            {
                @Override
                public Field<?> getField()
                {
                    return chooserField;
                }

                @Override
                public SampleChooserField getChooserField()
                {
                    return chooserField;
                }

                @Override
                public String getValue()
                {
                    return chooserField.getValue();
                }

                @Override
                public void updateOriginalValue()
                {
                    String valueOrNull = getValue();
                    String textValue = (valueOrNull == null ? "" : valueOrNull);
                    chooserField.setOriginalValue(textValue);
                }

                @Override
                public void updateValue(String identifierOrNull)
                {
                    chooserField.updateValue(identifierOrNull);
                }
            };
    }

    private static void browse(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ChosenEntitySetter<TableModelRowWithObject<Sample>> chosenSampleField,
            final boolean addShared, boolean addAll, final boolean excludeWithoutExperiment,
            SampleTypeDisplayID sampleTypeDisplayID, boolean multipleSelection)
    {
        DisposableEntityChooser<TableModelRowWithObject<Sample>> browser =
                SampleBrowserGrid.createChooser(viewContext, addShared, addAll,
                        excludeWithoutExperiment, sampleTypeDisplayID, multipleSelection);
        String title = viewContext.getMessage(Dict.TITLE_CHOOSE_SAMPLE);
        new EntityChooserDialog<TableModelRowWithObject<Sample>>(browser, chosenSampleField, title,
                viewContext).show();
    }

    // ------------------

    @Override
    public String renderEntity(TableModelRowWithObject<Sample> entityOrNull)
    {
        return entityOrNull.getObjectOrNull().getIdentifier();
    }

    private SampleChooserField(boolean mandatory, String initialValueOrNull,
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        FieldUtil.setMandatoryFlag(this, mandatory);
        setValidateOnBlur(true);
        setAutoValidate(true);

        // no regexp validation is done
        // we use plain string identifiers which currently can be parsed only on the server side

        updateValue(initialValueOrNull);
    }

    public void updateValue(String valueOrNull)
    {
        if (valueOrNull != null)
        {
            setValue(valueOrNull);
        }
    }

}
