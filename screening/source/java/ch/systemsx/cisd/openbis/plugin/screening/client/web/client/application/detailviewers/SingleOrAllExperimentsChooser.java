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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntityListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteriaHolder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.SingleExperimentSearchCriteria;

/**
 * A {@link LayoutContainer} making it possible to choose between either all experiments and a
 * single experiment.
 */
class SingleOrAllExperimentsChooser extends LayoutContainer
{
    private static final String SINGLE_EXPERIMENT_TEXT = "Single experiment";

    private static final String ALL_EXPERIMENTS_TEXT = "All experiments";

    private static final String CHOOSE_ONE_EXPERIMENT_TEXT = "Choose one experiment...";

    private SingleExperimentSearchCriteria singleExperimentChooserStateOrNull;

    private final IViewContext<?> viewContext;

    private final IDelegatedAction refreshAction;

    private final ExperimentSearchCriteriaHolder experimentCriteriaHolder;

    private final boolean restrictGlobalScopeLinkToProject;

    public SingleOrAllExperimentsChooser(IViewContext<?> viewContext,
            ExperimentSearchCriteriaHolder experimentCriteriaHolder,
            boolean restrictGlobalScopeLinkToProject, IDelegatedAction refreshAction)
    {
        this.viewContext = viewContext;
        this.experimentCriteriaHolder = experimentCriteriaHolder;
        this.restrictGlobalScopeLinkToProject = restrictGlobalScopeLinkToProject;
        this.refreshAction = refreshAction;
        ExperimentChooserFieldAdaptor singleExperimentChooser = createSingleExperimentChooser();
        RadioGroup experimentRadioChooser = createExperimentRadio(singleExperimentChooser);

        setWidth(400);
        add(experimentRadioChooser);
        add(singleExperimentChooser.getField());
    }

    // without project restriction
    public SingleOrAllExperimentsChooser(IViewContext<?> viewContext,
            ExperimentSearchCriteriaHolder experimentCriteriaHolder, IDelegatedAction refreshAction)
    {
        this(viewContext, experimentCriteriaHolder, false, refreshAction);
    }

    private ExperimentSearchCriteria tryGetExperimentSearchCriteria()
    {
        return experimentCriteriaHolder.tryGetCriteria();
    }

    private boolean isAllExperimentsChoosen()
    {
        ExperimentSearchCriteria criteriaOrNull = tryGetExperimentSearchCriteria();
        return criteriaOrNull != null && criteriaOrNull.tryGetExperiment() == null;
    }

    private ExperimentChooserFieldAdaptor createSingleExperimentChooser()
    {
        ExperimentChooserFieldAdaptor experimentChooser =
                ExperimentChooserField.create("", true, null, viewContext.getCommonViewContext());
        final ExperimentChooserField chooserField = experimentChooser.getChooserField();
        chooserField
                .addChosenEntityListener(new IChosenEntityListener<TableModelRowWithObject<Experiment>>()
                    {
                        public void entityChosen(TableModelRowWithObject<Experiment> row)
                        {
                            if (row != null)
                            {
                                chooseSingleExperiment(chooserField, row.getObjectOrNull());
                            }
                        }
                    });

        chooserField.setEditable(false);
        ExperimentSearchCriteria criteriaOrNull = tryGetExperimentSearchCriteria();

        if (criteriaOrNull == null || criteriaOrNull.tryGetExperiment() == null)
        {
            // we search in all experiments or single experiment has not been chosen
            this.singleExperimentChooserStateOrNull = null;
            chooserField.reset();
        } else
        {
            // we search in a single experiment
            updateSingleExperimentChooser(chooserField, criteriaOrNull.tryGetExperiment());
        }
        if (criteriaOrNull == null || criteriaOrNull.tryGetExperiment() != null)
        {
            chooserField.setEmptyText(CHOOSE_ONE_EXPERIMENT_TEXT);
        } else
        {
            chooserField.setEmptyText(ALL_EXPERIMENTS_TEXT);
        }
        return experimentChooser;
    }

    private RadioGroup createExperimentRadio(
            final ExperimentChooserFieldAdaptor singleExperimentChooser)
    {
        final RadioGroup experimentRadio = new RadioGroup();
        experimentRadio.setSelectionRequired(true);
        experimentRadio.setOrientation(Orientation.HORIZONTAL);

        final Radio allExps = new Radio();
        allExps.setBoxLabel(ALL_EXPERIMENTS_TEXT);
        experimentRadio.add(allExps);

        final Radio oneExps = new Radio();
        oneExps.setBoxLabel(SINGLE_EXPERIMENT_TEXT);
        experimentRadio.add(oneExps);

        experimentRadio.setValue(isAllExperimentsChoosen() ? allExps : oneExps);
        experimentRadio.setAutoHeight(true);
        experimentRadio.addListener(Events.Change, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    if (allExps.getValue())
                    {
                        singleExperimentChooser.getChooserField().setEnabled(false);
                        singleExperimentChooser.getChooserField()
                                .setEmptyText(ALL_EXPERIMENTS_TEXT);
                        experimentCriteriaHolder.setCriteria(ExperimentSearchCriteria
                                .createAllExperiments());
                        refreshAction.execute();
                    } else
                    {
                        singleExperimentChooser.getChooserField().setEmptyText(
                                CHOOSE_ONE_EXPERIMENT_TEXT);

                        singleExperimentChooser.getChooserField().setEnabled(true);
                        if (singleExperimentChooserStateOrNull == null)
                        {
                            experimentCriteriaHolder.setCriteria(null);
                        } else
                        {
                            experimentCriteriaHolder.setCriteria(ExperimentSearchCriteria
                                    .createExperiment(singleExperimentChooserStateOrNull,
                                            restrictGlobalScopeLinkToProject));
                            refreshAction.execute();
                        }
                    }
                }
            });
        return experimentRadio;
    }

    private void chooseSingleExperiment(final ExperimentChooserField chooserField,
            Experiment experiment)
    {
        SingleExperimentSearchCriteria singleExperiment =
                new SingleExperimentSearchCriteria(experiment.getId(), experiment.getPermId(),
                        experiment.getIdentifier());
        updateSingleExperimentChooser(chooserField, singleExperiment);
        experimentCriteriaHolder.setCriteria(ExperimentSearchCriteria.createExperiment(
                singleExperiment, restrictGlobalScopeLinkToProject));
        refreshAction.execute();
    }

    private void updateSingleExperimentChooser(ExperimentChooserField chooserField,
            SingleExperimentSearchCriteria singleExperiment)
    {
        this.singleExperimentChooserStateOrNull = singleExperiment;
        chooserField.updateValue(new ExperimentIdentifier(singleExperiment
                .getExperimentIdentifier()));
    }

}