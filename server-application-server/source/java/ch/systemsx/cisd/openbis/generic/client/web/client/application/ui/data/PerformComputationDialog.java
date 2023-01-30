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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

class PerformComputationDialog extends AbstractDataConfirmationDialog<ComputationData>
{

    private static final int LABEL_WIDTH = ColumnConfigFactory.DEFAULT_COLUMN_WIDTH - 20;

    private static final int FIELD_WIDTH = 2 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH - 20;

    private static final int DIALOG_WIDTH = 4 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH + 30;

    private static final String BR = "<br/>";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private List<String> selectedDataSetTypeCodes;

    private final SelectedOrAllDataSetsRadioProvider radioProvider;

    private final DatastoreServiceDescription pluginTask;

    private Html selectedDataSetTypesText;

    protected PerformComputationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            ComputationData data, String title)
    {
        super(viewContext, data, title);
        this.viewContext = viewContext;
        this.pluginTask = data.getService();
        this.radioProvider = new SelectedOrAllDataSetsRadioProvider(data);

        setWidth(DIALOG_WIDTH);

        DialogWithOnlineHelpUtils.addHelpButton(viewContext, this, createHelpPageIdentifier());
    }

    @Override
    protected void initializeData()
    {
        super.initializeData();

        Set<DataSetType> selectedDataSetTypes = getSelectedDataSetTypes();
        selectedDataSetTypeCodes = new ArrayList<String>(selectedDataSetTypes.size());
        for (DataSetType type : selectedDataSetTypes)
        {
            selectedDataSetTypeCodes.add(type.getCode());
        }
    }

    private Set<DataSetType> getSelectedDataSetTypes()
    {
        Set<DataSetType> result = new TreeSet<DataSetType>();
        for (AbstractExternalData dataSet : data.getSelectedDataSets())
        {
            result.add(dataSet.getDataSetType());
        }
        return result;
    }

    @Override
    protected String createMessage()
    {
        int size = data.getSelectedDataSets().size();
        String computationName = pluginTask.getLabel();
        if (size == 0)
        {
            final String msgIntroduction = viewContext.getMessage(Dict.NO_DATASETS_SELECTED);
            return viewContext.getMessage(Dict.PERFORM_COMPUTATION_ON_ALL_DATASETS_MSG_TEMPLATE,
                    msgIntroduction, computationName);
        } else
        {
            return viewContext.getMessage(
                    Dict.PERFORM_COMPUTATION_ON_SELECTED_OR_ALL_DATASETS_MSG_TEMPLATE,
                    computationName, size);
        }
    }

    @Override
    protected boolean validate()
    {
        final boolean computeOnSelected = getComputeOnSelected();
        if (computeOnSelected)
        {
            // show error message if plugin does not support all types of selected data sets
            Set<String> supportedDataSetTypes = getSupportedDataSetTypes(pluginTask);
            List<String> unsupportedDataSetTypes = new ArrayList<String>();
            for (String selectedDataSetType : selectedDataSetTypeCodes)
            {
                if (supportedDataSetTypes.contains(selectedDataSetType) == false)
                {
                    unsupportedDataSetTypes.add(selectedDataSetType);
                }
            }
            if (unsupportedDataSetTypes.size() > 0)
            {
                final String msg = createUnsupportedDataSetTypesText(unsupportedDataSetTypes);
                GWTUtils.alert("Error", msg);
                return false;
            }
        }
        return super.validate();
    }

    @Override
    protected void executeConfirmedAction()
    {
        final IComputationAction computationAction = data.getComputationAction();
        final boolean computeOnSelected = getComputeOnSelected();
        computationAction.execute(pluginTask, computeOnSelected);
    }

    private Set<String> getSupportedDataSetTypes(DatastoreServiceDescription plugin)
    {
        return new HashSet<String>(Arrays.asList(plugin.getDatasetTypeCodes()));
    }

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(LABEL_WIDTH);
        formPanel.setFieldWidth(FIELD_WIDTH);

        if (data.getSelectedDataSets().size() > 0)
        {
            formPanel.add(createComputationDataSetsRadio());
            selectedDataSetTypesText = formPanel.addText(createSelectedDataSetTypeText());
            updateComputationDataSetsState();
        }

        Button confirmButton = getButtonById(Dialog.OK);
        confirmButton.setText("Run");
    }

    private final String createSelectedDataSetTypeText()
    {
        return createDataSetTypeMsg("Types of selected Data Sets", selectedDataSetTypeCodes);
    }

    private final String createUnsupportedDataSetTypesText(List<String> dataSetTypes)
    {
        return createDataSetTypeMsg(
                "Selected service does not support all types of selected Data Sets. " + BR + BR
                        + "Unsupported Data Set types", dataSetTypes);
    }

    private final String createDataSetTypeMsg(String msgPrefix, List<String> dataSetTypes)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(msgPrefix);
        sb.append(": ");
        sb.append(StringUtils.joinList(dataSetTypes));
        return sb.toString();
    }

    private final RadioGroup createComputationDataSetsRadio()
    {
        return radioProvider.createComputationDataSetsRadio();
    }

    private boolean getComputeOnSelected()
    {
        return radioProvider.getComputeOnSelected();
    }

    private final void updateComputationDataSetsState()
    {
        boolean showSelectedDataSetTypes = getComputeOnSelected();
        selectedDataSetTypesText.setVisible(showSelectedDataSetTypes);
    }

    private HelpPageIdentifier createHelpPageIdentifier()
    {
        return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.PERFORM_COMPUTATION,
                HelpPageIdentifier.HelpPageAction.ACTION);
    }
}