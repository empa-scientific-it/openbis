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

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * A field for selecting an experiment in a fixed group from a list or by specifying code and project.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentChooserField extends ChosenEntitySetter<TableModelRowWithObject<Experiment>>
{

    public interface ExperimentChooserFieldAdaptor
    {
        Field<?> getField();

        ExperimentChooserField getChooserField();

        /** @return the experiment identifier which is set as a field value */
        ExperimentIdentifier tryToGetValue();

        void updateOriginalValue();

        void updateValue(ExperimentIdentifier identifierOrNull);
    }

    /**
     * Creates a text field with the additional browse button which allow to choose an experiment from the list.
     */
    public static ExperimentChooserFieldAdaptor create(final String labelField,
            final boolean mandatory, final ExperimentIdentifier initialValueOrNull,
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final ExperimentChooserField chooserField =
                new ExperimentChooserField(mandatory, initialValueOrNull, viewContext)
                    {
                        @Override
                        protected void onTriggerClick(ComponentEvent ce)
                        {
                            super.onTriggerClick(ce);
                            browse(viewContext, this);
                        }
                    };

        chooserField.setFieldLabel(labelField);
        return new ExperimentChooserFieldAdaptor()
            {
                @Override
                public Field<?> getField()
                {
                    return chooserField;
                }

                @Override
                public ExperimentChooserField getChooserField()
                {
                    return chooserField;
                }

                @Override
                public ExperimentIdentifier tryToGetValue()
                {
                    return chooserField.tryGetIdentifier();
                }

                @Override
                public void updateOriginalValue()
                {
                    ExperimentIdentifier valueOrNull = tryToGetValue();
                    String textValue = (valueOrNull == null ? "" : valueOrNull.getIdentifier());
                    chooserField.setOriginalValue(textValue);
                }

                @Override
                public void updateValue(ExperimentIdentifier identifierOrNull)
                {
                    chooserField.updateValue(identifierOrNull);
                }
            };
    }

    private static void browse(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ChosenEntitySetter<TableModelRowWithObject<Experiment>> chosenEntityField)
    {
        DisposableEntityChooser<TableModelRowWithObject<Experiment>> browser =
                ExperimentBrowserGrid.createChooser(viewContext);
        String title = viewContext.getMessage(Dict.TITLE_CHOOSE_EXPERIMENT);
        new EntityChooserDialog<TableModelRowWithObject<Experiment>>(browser, chosenEntityField,
                title, viewContext).show();
    }

    // ------------------

    // the pattern used to validate experiment pointer expression
    private final static String EXPERIMENT_IDENTIFIER_WITHOUT_GROUP_PATTERN =
            "/" + CodeField.CODE_CHARS + "/" + CodeField.CODE_CHARS + "/" + CodeField.CODE_CHARS;

    private final static String EXPERIMENT_IDENTIFIER_WITH_GROUP_PATTERN =
            CodeField.CODE_CHARS + "/" + CodeField.CODE_CHARS;

    // @Private, only for tests
    public final static String EXPERIMENT_IDENTIFIER_PATTERN =
            "(" + EXPERIMENT_IDENTIFIER_WITH_GROUP_PATTERN + ")|("
                    + EXPERIMENT_IDENTIFIER_WITHOUT_GROUP_PATTERN + ")";

    @Override
    public String renderEntity(TableModelRowWithObject<Experiment> entity)
    {
        return print(ExperimentIdentifier.createIdentifier(entity.getObjectOrNull()));
    }

    private ExperimentIdentifier tryGetIdentifier()
    {
        String ident = getValue();
        if (StringUtils.isBlank(ident))
        {
            return null;
        } else
        {
            return new ExperimentIdentifier(ident);
        }
    }

    private String print(ExperimentIdentifier chosenEntity)
    {
        return chosenEntity.getIdentifier();
    }

    private ExperimentChooserField(boolean mandatory, ExperimentIdentifier initialValueOrNull,
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        FieldUtil.setMandatoryFlag(this, mandatory);
        setValidateOnBlur(true);
        setAutoValidate(true);

        setRegex(EXPERIMENT_IDENTIFIER_PATTERN);
        getMessages().setRegexText(viewContext.getMessage(Dict.INCORRECT_EXPERIMENT_SYNTAX));
        updateValue(initialValueOrNull);
    }

    public void updateValue(ExperimentIdentifier initialValueOrNull)
    {
        if (initialValueOrNull != null)
        {
            super.setValue(print(initialValueOrNull));
        }
    }

}
