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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.http.client.URL;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField.SampleChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.DataSetUploadInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.DataSetUploadInfo.DataSetUploadInfoHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;

/**
 * Panel that allows to specify information necessary to upload data sets and redirects user to specified upload service.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetUploadForm extends AbstractRegistrationForm
{

    private final String cifexURL;

    private final String cifexRecipient;

    private final DataSetTypeSelectionWidget dataSetTypeSelectionWidget;

    private final FileFormatTypeSelectionWidget fileTypeSelectionWidget;

    private final CheckBoxField connectedWithSampleCheckbox;

    // two options:
    // 1. connected with sample
    private final SampleChooserFieldAdaptor sampleChooser;

    // 2. not connected with sample
    private final ExperimentChooserFieldAdaptor experimentChooser;

    private final DataSetsArea parentsArea;

    //

    public static String createId(String suffix)
    {
        return GenericConstants.ID_PREFIX + createSimpleId(suffix);
    }

    private static String createSimpleId(String suffix)
    {

        String withoutSuffix = "data-set-upload-panel";
        if (suffix == null)
        {
            return withoutSuffix;
        } else
        {
            return withoutSuffix + GWTUtils.escapeToFormId(suffix.toUpperCase());
        }
    }

    public DataSetUploadForm(IViewContext<ICommonClientServiceAsync> viewContext,
            String sampleIdentifierOrNull)
    {
        super(viewContext, createSimpleId(sampleIdentifierOrNull));
        setResetButtonVisible(true);
        cifexURL = viewContext.getModel().getApplicationInfo().getCifexURL();
        cifexRecipient = viewContext.getModel().getApplicationInfo().getCifexRecipient();
        saveButton.setText(viewContext.getMessage(Dict.BUTTON_UPLOAD_DATA_VIA_CIFEX));

        connectedWithSampleCheckbox = new CheckBoxField("Connected with " + viewContext.getMessage(Dict.SAMPLE), false);
        connectedWithSampleCheckbox.setValue(true);
        connectedWithSampleCheckbox.addListener(Events.Change, new Listener<FieldEvent>()
            {
                @Override
                public void handleEvent(FieldEvent be)
                {
                    updateFieldsVisibility();
                }
            });
        formPanel.add(connectedWithSampleCheckbox);

        // both sample and experiment choosers are mandatory but only one will be shown
        sampleChooser =
                SampleChooserField.create(viewContext.getMessage(Dict.SAMPLE), true,
                        sampleIdentifierOrNull, false, false, false, viewContext,
                        SampleTypeDisplayID.DATA_SET_UPLOAD_SAMPLE_CHOOSER, false);
        formPanel.add(sampleChooser.getField());
        experimentChooser =
                ExperimentChooserField.create(viewContext.getMessage(Dict.EXPERIMENT), true, null,
                        viewContext);
        formPanel.add(experimentChooser.getField());
        formPanel.add(parentsArea =
                new DataSetParentsArea(viewContext, createSimpleId(sampleIdentifierOrNull)));
        parentsArea.setMaxLength(1500);

        updateFieldsVisibility();

        formPanel
                .add(dataSetTypeSelectionWidget =
                        new DataSetTypeSelectionWidget(viewContext,
                                createSimpleId(sampleIdentifierOrNull)));
        FieldUtil.markAsMandatory(dataSetTypeSelectionWidget);
        formPanel.add(fileTypeSelectionWidget =
                new FileFormatTypeSelectionWidget(viewContext,
                        createSimpleId(sampleIdentifierOrNull)));
        FieldUtil.markAsMandatory(fileTypeSelectionWidget);
        if (StringUtils.isBlank(cifexRecipient) || StringUtils.isBlank(cifexURL))
        {
            formPanel.disable();
            infoBox.displayError(viewContext.getMessage(Dict.MESSAGE_NO_EXTERNAL_UPLOAD_SERVICE));
        }
    }

    private void updateFieldsVisibility()
    {
        boolean connectedWithSample = isConnectedWithSample();
        FieldUtil.setVisibility(connectedWithSample, sampleChooser.getField());
        FieldUtil.setVisibility(connectedWithSample == false, experimentChooser.getField(),
                parentsArea);
    }

    public static DatabaseModificationAwareComponent create(
            IViewContext<ICommonClientServiceAsync> viewContext, String sampleIdentifierOrNull)
    {
        DataSetUploadForm form = new DataSetUploadForm(viewContext, sampleIdentifierOrNull);
        IDatabaseModificationObserver observer = form.createDatabaseModificationObserver();
        return new DatabaseModificationAwareComponent(form, observer);
    }

    private IDatabaseModificationObserver createDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver observer =
                new CompositeDatabaseModificationObserver();
        observer.addObserver(dataSetTypeSelectionWidget);
        observer.addObserver(fileTypeSelectionWidget);
        return observer;
    }

    @Override
    protected void submitValidForm()
    {
        final String sample = extractSampleIdentifier();
        final String experiment = extractExperimentIdentifier();
        final String dataSetType = extractDataSetTypeCode();
        final String fileType = extractFileTypeCode();
        final String[] parents = extractParentDatasetCodes();
        String comment;
        if (isConnectedWithSample())
        {
            comment = encodeDataSetInfo(sample, dataSetType, fileType);
        } else
        {
            comment = encodeDataSetInfo(experiment, parents, dataSetType, fileType);
        }
        WindowUtils.openWindow(encodeCifexRequest(cifexURL, cifexRecipient, comment));
    }

    private Boolean isConnectedWithSample()
    {
        return connectedWithSampleCheckbox.getValue();
    }

    private String extractSampleIdentifier()
    {
        return sampleChooser.getValue();
    }

    private String extractExperimentIdentifier()
    {
        ExperimentIdentifier identifierOrNull = experimentChooser.tryToGetValue();
        return identifierOrNull == null ? null : identifierOrNull.getIdentifier();
    }

    private String extractDataSetTypeCode()
    {
        return dataSetTypeSelectionWidget.tryGetSelected().getCode();
    }

    private String extractFileTypeCode()
    {
        return fileTypeSelectionWidget.tryGetSelected().getCode();
    }

    protected String[] extractParentDatasetCodes()
    {
        return parentsArea.tryGetModifiedDataSetCodes();
    }

    private String encodeDataSetInfo(String sample, String dataSetType, String fileType)
    {
        return DataSetUploadInfoHelper.encodeAsCifexComment(new DataSetUploadInfo(sample, null,
                null, dataSetType, fileType));
    }

    private String encodeDataSetInfo(String experiment, String[] parents, String dataSetType,
            String fileType)
    {
        return DataSetUploadInfoHelper.encodeAsCifexComment(new DataSetUploadInfo(null, experiment,
                parents, dataSetType, fileType));
    }

    private static String encodeCifexRequest(String cifexUrl, String recipient, String comment)
    {
        URLMethodWithParameters url = new URLMethodWithParameters(cifexUrl);
        url.addParameterWithoutEncoding(BasicConstant.CIFEX_URL_PARAMETER_COMMENT, comment);
        url.addParameterWithoutEncoding(BasicConstant.CIFEX_URL_PARAMETER_RECIPIENT, recipient);
        return URL.encode(url.toString());
    }

}
