/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleModelComboBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplaySettingsManager;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.AnalysisProcedures;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteriaHolder;

/**
 * An UI panel for selecting analysis procedures.
 * 
 * @author Kaloyan Enimanev
 */
class AnalysisProcedureChooser extends LayoutContainer
{

    private final static String UNSPECIFIED_PROCEDURE = "Unspecified";

    private final static String ALL_PROCEDURES = "All";

    /**
     * Can be used from external classes wishing to be notified when the analysis procedure selection changes.
     */
    public static interface IAnalysisProcedureSelectionListener
    {
        void analysisProcedureSelected(AnalysisProcedureCriteria criteria);
    }

    private static interface AnalysisProcedureLister
    {
        void listNumericalDatasetsAnalysisProcedures(
                AsyncCallback<AnalysisProcedures> resultsCallback);
    }

    /**
     * Creates the combobox with a list of numerical datasets analysis procedures (HCS_ANALYSIS_WELL*), they are fetched from the server for the
     * specified experiment criteria.<br>
     * The label will be in the same row before the combobox.
     */
    public static final AnalysisProcedureChooser createHorizontal(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteriaHolder experimentCriteriaHolder,
            AnalysisProcedureCriteria initialSelection,
            IAnalysisProcedureSelectionListener selectionListener,
            boolean triggerInitialSelectionEvent)
    {
        AnalysisProcedureLister analysisProcedureLister =
                createNumericalDatasetsAnalysisProcedureLister(viewContext,
                        experimentCriteriaHolder);
        return new AnalysisProcedureChooser(viewContext, analysisProcedureLister, initialSelection,
                selectionListener, triggerInitialSelectionEvent, true);
    }

    /**
     * Creates the combobox with a list of numerical datasets analysis procedures (HCS_ANALYSIS_WELL*), they are fetched from the server for the
     * specified experiment criteria.<br>
     * The label will be in the separate row above the combobox.
     */
    public static final AnalysisProcedureChooser createVertical(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteriaHolder experimentCriteriaHolder,
            AnalysisProcedureCriteria initialSelection,
            IAnalysisProcedureSelectionListener selectionListener,
            boolean triggerInitialSelectionEvent)
    {
        AnalysisProcedureLister analysisProcedureLister =
                createNumericalDatasetsAnalysisProcedureLister(viewContext,
                        experimentCriteriaHolder);
        return new AnalysisProcedureChooser(viewContext, analysisProcedureLister, initialSelection,
                selectionListener, triggerInitialSelectionEvent, false);
    }

    /**
     * Creates the combobox for a specified list of analysis procedures. The label will be in the separate row above the combobox.
     */
    public static final AnalysisProcedureChooser create(IViewContext<?> viewContext,
            final AnalysisProcedures analysisProcedures,
            AnalysisProcedureCriteria initialSelection,
            IAnalysisProcedureSelectionListener selectionListener)
    {
        // dummy lister which always returns the same result
        AnalysisProcedureLister analysisProcedureLister = new AnalysisProcedureLister()
            {
                @Override
                public void listNumericalDatasetsAnalysisProcedures(
                        AsyncCallback<AnalysisProcedures> resultsCallback)
                {
                    resultsCallback.onSuccess(analysisProcedures);
                }
            };
        return new AnalysisProcedureChooser(viewContext, analysisProcedureLister, initialSelection,
                selectionListener, true, false);
    }

    private static AnalysisProcedureLister createNumericalDatasetsAnalysisProcedureLister(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            final ExperimentSearchCriteriaHolder experimentCriteriaHolder)
    {
        return new AnalysisProcedureLister()
            {
                @Override
                public void listNumericalDatasetsAnalysisProcedures(
                        AsyncCallback<AnalysisProcedures> resultsCallback)
                {
                    viewContext.getService().listNumericalDatasetsAnalysisProcedures(
                            experimentCriteriaHolder.tryGetCriteria(), resultsCallback);
                }
            };
    }

    private final IMessageProvider messageProvider;

    private final ScreeningDisplaySettingsManager screeningDisplaySettingsManager;

    private final IAnalysisProcedureSelectionListener selectionListener;

    private final AnalysisProcedureLister analysisProcedureLister;

    private final SimpleModelComboBox<String> analysisProceduresComboBox;

    private final boolean displaySelectAllProcedures;

    private final Listener<BaseEvent> selectionChangeListener = new Listener<BaseEvent>()
        {

            @Override
            public void handleEvent(BaseEvent be)
            {
                selectionChanged();
            }

        };

    private AnalysisProcedureChooser(IViewContext<?> viewContext,
            AnalysisProcedureLister analysisProcedureLister,
            AnalysisProcedureCriteria initialSelection,
            IAnalysisProcedureSelectionListener selectionListener,
            boolean triggerInitialSelectionEvent, boolean horizontalLayout)
    {
        this.messageProvider = viewContext;
        this.screeningDisplaySettingsManager = new ScreeningDisplaySettingsManager(viewContext);

        this.selectionListener = selectionListener;
        this.analysisProcedureLister = analysisProcedureLister;
        this.analysisProceduresComboBox = createLayout(horizontalLayout);
        this.displaySelectAllProcedures = initialSelection.isAllProcedures();

        initSelection(initialSelection, triggerInitialSelectionEvent);
    }

    private SimpleModelComboBox<String> createLayout(boolean horizontalLayout)
    {

        SimpleModelComboBox<String> comboBox = createProceduresComboBox();
        Widget layoutPanel;
        if (horizontalLayout)
        {
            layoutPanel = GuiUtils.withLabel(comboBox, getAnalysisProcedureLabel());
        } else
        {
            LayoutContainer panel = createVerticalPanel();
            panel.setAutoHeight(true);
            panel.add(createComboLabel());
            panel.add(comboBox);
            layoutPanel = panel;
        }
        add(layoutPanel);

        return comboBox;
    }

    private static VerticalPanel createVerticalPanel()
    {
        final VerticalPanel layoutPanel = new VerticalPanel();
        layoutPanel.setStyleAttribute("margin-right", "40px");
        return layoutPanel;
    }

    private void initSelection(AnalysisProcedureCriteria initialSelection, boolean triggerEvents)
    {
        if (false == triggerEvents)
        {
            disableEvents(true);
        }

        refresh(initialSelection);

        if (false == triggerEvents)
        {
            enableEvents(true);
        }

    }

    public void updateAnalysisProcedures()
    {
        refresh(getSelectionAsCriteria());
    }

    private void refresh(final AnalysisProcedureCriteria selectedProcedureCriteria)
    {

        analysisProceduresComboBox.removeAll();

        analysisProcedureLister
                .listNumericalDatasetsAnalysisProcedures(new AsyncCallback<AnalysisProcedures>()
                    {
                        @Override
                        public void onSuccess(AnalysisProcedures analysisProcedures)
                        {
                            refresh(selectedProcedureCriteria, analysisProcedures);
                        }

                        @Override
                        public void onFailure(Throwable caught)
                        {
                            // do nothing
                        }
                    });
    }

    private void refresh(AnalysisProcedureCriteria selectedProcedureCriteria,
            AnalysisProcedures analysisProcedures)
    {
        addAnalysisProcedures(analysisProcedures.getProcedureCodes());
        setInitialSelection(selectedProcedureCriteria);
    }

    private Component createComboLabel()
    {
        final LabelToolItem label = new LabelToolItem(getAnalysisProcedureLabel());
        label.addStyleName("default-text");
        return label;
    }

    private String getAnalysisProcedureLabel()
    {
        return messageProvider.getMessage(Dict.ANALYSIS_PROCEDURE)
                + GenericConstants.LABEL_SEPARATOR;
    }

    private SimpleModelComboBox<String> createProceduresComboBox()
    {
        SimpleModelComboBox<String> comboBox =
                new SimpleModelComboBox<String>(messageProvider,
                        new ArrayList<LabeledItem<String>>(), null);

        comboBox.setTriggerAction(TriggerAction.ALL);
        comboBox.setAllowBlank(false);
        comboBox.setEditable(false);
        comboBox.setEmptyText(messageProvider.getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.LOAD_IN_PROGRESS));
        comboBox.addListener(Events.SelectionChange, selectionChangeListener);

        return comboBox;
    }

    private void addAnalysisProcedures(List<String> codes)
    {
        for (String code : transformAndSortCodes(codes))
        {
            addCodeToComboBox(code);
        }
    }

    private List<String> transformAndSortCodes(List<String> codes)
    {
        List<String> sortedCodes = new ArrayList<String>();
        for (String code : codes)
        {
            sortedCodes.add(unspecifiedIfEmpty(code));

        }
        boolean unspecifiedPresent = sortedCodes.remove(UNSPECIFIED_PROCEDURE);
        Collections.sort(sortedCodes);
        if (displaySelectAllProcedures)
        {
            sortedCodes.add(0, ALL_PROCEDURES);
        }
        if (unspecifiedPresent)
        {
            // unspecified is always displayed at the end
            sortedCodes.add(UNSPECIFIED_PROCEDURE);
        }
        return sortedCodes;
    }

    private void addCodeToComboBox(String code)
    {
        if (analysisProceduresComboBox.findModelForVal(code) == null)
        {
            analysisProceduresComboBox.add(new LabeledItem<String>(code, code));
        }
    }

    private void setInitialSelection(AnalysisProcedureCriteria selectedProcedureCriteria)
    {
        String analysisProcedureOrNull = selectedProcedureCriteria.tryGetAnalysisProcedureCode();
        if (StringUtils.isBlank(analysisProcedureOrNull))
        {
            analysisProcedureOrNull = getDefaultAnalysisProcedure();
        }

        String comboBoxValue = analysisProcedureOrNull;
        if (StringUtils.isBlank(comboBoxValue))
        {
            comboBoxValue =
                    selectedProcedureCriteria.isNoProcedures() ? UNSPECIFIED_PROCEDURE
                            : ALL_PROCEDURES;
        }

        LabeledItem<String> valueToSelect =
                analysisProceduresComboBox.findModelForVal(comboBoxValue);
        if (UNSPECIFIED_PROCEDURE.equals(comboBoxValue) || valueToSelect == null)
        {
            valueToSelect = getFirstValueFromCombo();
        }

        analysisProceduresComboBox.setSimpleValue(valueToSelect);

    }

    private LabeledItem<String> getFirstValueFromCombo()
    {
        return analysisProceduresComboBox.getStore().getAt(0).getValue();
    }

    private void selectionChanged()
    {
        AnalysisProcedureCriteria analysisProcedureCriteria = getSelectionAsCriteria();
        selectionListener.analysisProcedureSelected(analysisProcedureCriteria);
        setDefaultAnalysisProcedure(analysisProcedureCriteria);
    }

    private AnalysisProcedureCriteria getSelectionAsCriteria()
    {
        String selectedAP = analysisProceduresComboBox.tryGetChosenItem();
        if (UNSPECIFIED_PROCEDURE.equals(selectedAP))
        {
            return AnalysisProcedureCriteria.createNoProcedures();
        } else if (StringUtils.isBlank(selectedAP) || ALL_PROCEDURES.equals(selectedAP))
        {
            return AnalysisProcedureCriteria.createAllProcedures();
        } else
        {
            return AnalysisProcedureCriteria.createFromCode(selectedAP);
        }
    }

    private String getDefaultAnalysisProcedure()
    {
        return screeningDisplaySettingsManager.getDefaultAnalysisProcedure();
    }

    private void setDefaultAnalysisProcedure(AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        String analysisProcedureCode =
                (analysisProcedureCriteria.isAllProcedures()) ? ALL_PROCEDURES
                        : analysisProcedureCriteria.tryGetAnalysisProcedureCode();
        if (StringUtils.isNotBlank(analysisProcedureCode))
        {
            screeningDisplaySettingsManager.setDefaultAnalysisProcedure(analysisProcedureCode);
        }
    }

    private String unspecifiedIfEmpty(String analysisProcedureOrNull)
    {
        return StringUtils.isBlank(analysisProcedureOrNull) ? UNSPECIFIED_PROCEDURE
                : analysisProcedureOrNull;
    }

}