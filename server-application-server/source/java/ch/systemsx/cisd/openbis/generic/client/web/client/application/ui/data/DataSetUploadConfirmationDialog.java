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

import java.util.List;

import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUploadParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 */
public final class DataSetUploadConfirmationDialog extends
        AbstractDataConfirmationDialog<List<TableModelRowWithObject<AbstractExternalData>>>
{
    private static final int FIELD_WIDTH_IN_UPLOAD_DIALOG = 200;

    private static final int LABEL_WIDTH_IN_UPLOAD_DIALOG = 120;

    private final IViewContext<?> viewContext;

    private final int displayedItemsCount;

    private final IDelegatedActionWithResult<SelectedAndDisplayedItems> selectedAndDisplayedItemsAction;

    private String cifexURL;

    private TextField<String> passwordField;

    private TextField<String> fileNameField;

    private TextArea commentField;

    private TextField<String> userField;

    private Radio uploadSelectedRadio;

    public DataSetUploadConfirmationDialog(List<TableModelRowWithObject<AbstractExternalData>> dataSets,
            IDelegatedActionWithResult<SelectedAndDisplayedItems> selectedAndDisplayedItemsAction,
            int displayedItemsCount, IViewContext<?> viewContext)
    {
        super(viewContext, dataSets, viewContext.getMessage(Dict.CONFIRM_DATASET_UPLOAD_TITLE));
        this.viewContext = viewContext;
        this.displayedItemsCount = displayedItemsCount;
        this.selectedAndDisplayedItemsAction = selectedAndDisplayedItemsAction;
        cifexURL = viewContext.getModel().getApplicationInfo().getCifexURL();
        setWidth(LABEL_WIDTH_IN_UPLOAD_DIALOG + FIELD_WIDTH_IN_UPLOAD_DIALOG + 50);
    }

    @Override
    protected String createMessage()
    {
        return viewContext.getMessage(Dict.CONFIRM_DATASET_UPLOAD_MSG, cifexURL);
    }

    @Override
    protected final void extendForm()
    {
        formPanel.setLabelWidth(LABEL_WIDTH_IN_UPLOAD_DIALOG);
        formPanel.setFieldWidth(FIELD_WIDTH_IN_UPLOAD_DIALOG);
        formPanel.setBodyBorder(false);
        formPanel.setHeaderVisible(false);

        if (displayedItemsCount > 1)
        {
            formPanel.add(createDataSetsRadio());
        }

        fileNameField = new TextField<String>();
        fileNameField.setFieldLabel(viewContext
                .getMessage(Dict.CONFIRM_DATASET_UPLOAD_FILE_NAME_FIELD));
        fileNameField.setSelectOnFocus(true);
        fileNameField.setMaxLength(BasicConstant.MAX_LENGTH_OF_FILE_NAME + ".zip".length());
        fileNameField.setAutoValidate(true);
        fileNameField.addKeyListener(keyListener);
        formPanel.add(fileNameField);

        commentField = new TextArea();
        commentField.setMaxLength(BasicConstant.MAX_LENGTH_OF_CIFEX_COMMENT);
        commentField.setFieldLabel(viewContext
                .getMessage(Dict.CONFIRM_DATASET_UPLOAD_COMMENT_FIELD));
        commentField.addKeyListener(keyListener);
        commentField.setAutoValidate(true);
        formPanel.add(commentField);

        userField = new TextField<String>();
        userField.setFieldLabel(viewContext.getMessage(Dict.CONFIRM_DATASET_UPLOAD_USER_FIELD));
        userField.setValue(viewContext.getModel().getSessionContext().getUser().getUserName());
        FieldUtil.setMandatoryFlag(userField, true);
        userField.addKeyListener(keyListener);
        userField.setAutoValidate(true);
        formPanel.add(userField);

        passwordField = new TextField<String>();
        passwordField.setPassword(true);
        passwordField.setFieldLabel(viewContext
                .getMessage(Dict.CONFIRM_DATASET_UPLOAD_PASSWORD_FIELD));
        FieldUtil.setMandatoryFlag(passwordField, false);
        passwordField.addKeyListener(keyListener);
        passwordField.setAutoValidate(true);
        formPanel.add(passwordField);

        DialogWithOnlineHelpUtils.addHelpButton(viewContext.getCommonViewContext(), this,
                createHelpPageIdentifier());
    }

    private final RadioGroup createDataSetsRadio()
    {
        return WidgetUtils.createAllOrSelectedRadioGroup(
                uploadSelectedRadio =
                        WidgetUtils.createRadio(viewContext.getMessage(Dict.ONLY_SELECTED_RADIO,
                                data.size())), WidgetUtils.createRadio(viewContext.getMessage(
                        Dict.ALL_RADIO, displayedItemsCount)), viewContext
                        .getMessage(Dict.DATA_SETS_RADIO_GROUP_LABEL), data.size());
    }

    private boolean getUploadSelected()
    {
        return uploadSelectedRadio == null ? true : WidgetUtils.isSelected(uploadSelectedRadio);
    }

    @Override
    protected void executeConfirmedAction()
    {
        DataSetUploadParameters parameters = new DataSetUploadParameters();
        parameters.setCifexURL(cifexURL);
        parameters.setFileName(fileNameField.getValue());
        parameters.setComment(commentField.getValue());
        parameters.setUserID(userField.getValue());
        parameters.setPassword(passwordField.getValue());

        final boolean uploadSelected = getUploadSelected();
        final SelectedAndDisplayedItems selectedAndDisplayedItems =
                selectedAndDisplayedItemsAction.execute();
        final DisplayedOrSelectedDatasetCriteria uploadCriteria =
                createCriteria(selectedAndDisplayedItems, uploadSelected);

        viewContext.getCommonService().uploadDataSets(uploadCriteria, parameters,
                new UploadCallback(viewContext));
    }

    private static final class UploadCallback extends AbstractAsyncCallback<String>
    {
        private UploadCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(String message)
        {
            if (message.length() > 0)
            {
                GWTUtils.alert("Warning", message);
            }
        }
    }

    private DisplayedOrSelectedDatasetCriteria createCriteria(
            SelectedAndDisplayedItems selectedAndDisplayedItems, boolean uploadSelected)
    {
        return selectedAndDisplayedItems.createCriteria(uploadSelected);
    }

    private HelpPageIdentifier createHelpPageIdentifier()
    {
        return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.EXPORT_DATA,
                HelpPageIdentifier.HelpPageAction.ACTION);
    }

}
