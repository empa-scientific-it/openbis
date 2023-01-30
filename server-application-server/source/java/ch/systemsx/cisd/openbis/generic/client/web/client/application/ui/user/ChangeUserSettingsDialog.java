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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.user;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SpaceModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IntegerField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractSaveDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PortletConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RealNumberFormatingParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StandardPortletNames;

/**
 * {@link Window} containing form for changing logged user settings.
 * 
 * @author Piotr Buczek
 */
public class ChangeUserSettingsDialog extends AbstractSaveDialog
{
    public static final String DIALOG_ID = GenericConstants.ID_PREFIX + "change-user-settings-dialog";

    public static final String GROUP_FIELD_ID = DIALOG_ID + "-group-field";

    public static final String LEGACYUI_FIELD_ID = GROUP_FIELD_ID + "-legacyUI-field";

    private final IViewContext<?> viewContext;

    private final SpaceSelectionWidget homeSpaceField;

    private ProjectSelectionWidget defaultProject;

    private final CheckBoxField reopenLastTabField;

    private final CheckBoxField enableLegacyMetadataUI;

    private final CheckBoxField showLastVisitsField;

    private final FieldSet formatingFields;

    private final CheckBoxField scientificFormatingField;

    private final CheckBoxField debuggingModeField;

    private final IntegerField precisionField;

    private final IDelegatedAction resetCallback;

    public ChangeUserSettingsDialog(final IViewContext<?> viewContext,
            final IDelegatedAction saveCallback, final IDelegatedAction resetCallback)
    {
        super(viewContext, viewContext.getMessage(Dict.CHANGE_USER_SETTINGS_DIALOG_TITLE),
                saveCallback);
        this.viewContext = viewContext;
        this.resetCallback = resetCallback;
        form.setLabelWidth(150);
        form.setFieldWidth(400);
        setWidth(640);

        addField(homeSpaceField = createHomeGroupField());

        final SessionContext sessionContext = viewContext.getModel().getSessionContext();
        initDefaultProjectUIField(sessionContext.getUser().getHomeGroupCode());

        addField(reopenLastTabField = createReopenLastTabOnLoginField());
        addField(enableLegacyMetadataUI = createEnableLegacyMetadataUIField());
        addField(showLastVisitsField = createShowLastVisitsField());
        formatingFields = createRealFormatingFieldSet();
        precisionField = createPrecisionField();
        formatingFields.add(precisionField);
        scientificFormatingField = createScientificCheckBox();
        formatingFields.add(scientificFormatingField);
        addField(formatingFields);
        debuggingModeField = createDebuggingModeCheckBox();
        addField(debuggingModeField);
        fbar.insert(createResetButton(), 1); // inserting Reset button in between Save and Cancel

        homeSpaceField.addSelectionChangedListener(new SelectionChangedListener<SpaceModel>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<SpaceModel> se)
                {
                    Space space = homeSpaceField.tryGetSelected();
                    initDefaultProjectUIField(space != null ? space.getCode() : null);
                }
            });

        homeSpaceField.addListener(Events.Blur, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    String spaceCode = homeSpaceField.getRawValue();
                    initDefaultProjectUIField(spaceCode != null && spaceCode.isEmpty() == false ? spaceCode : null);
                }
            });

        DialogWithOnlineHelpUtils.addHelpButton(viewContext.getCommonViewContext(), this,
                createHelpPageIdentifier());
    }

    //
    // Change
    //

    private final SpaceSelectionWidget createHomeGroupField()
    {
        SpaceSelectionWidget field =
                new SpaceSelectionWidget(viewContext, GROUP_FIELD_ID, false, false);
        FieldUtil.setMandatoryFlag(field, false);
        field.setFieldLabel(viewContext.getMessage(Dict.HOME_GROUP_LABEL));
        return field;
    }

    private CheckBoxField createEnableLegacyMetadataUIField()
    {
        CheckBoxField field = new CheckBoxField("Enable Legacy Metadata UI (Needs Re-login)", false);
        GWTUtils.setToolTip(field,
                "When selected the legacy  UI to manage metadata is available and the new one is hidden. A change on this field needs to Re-Login the application.");
        field.setValue(viewContext.getDisplaySettingsManager().isLegacyMedadataUIEnabled());
        field.setId(LEGACYUI_FIELD_ID);
        return field;
    }

    private void initDefaultProjectUIField(String spaceCodeOrNull)
    {
        if (spaceCodeOrNull == null)
        {
            if (defaultProject != null)
            {
                removeField(defaultProject);
                defaultProject = null;
            }
        } else
        {
            String oldSpaceCodeOrNull = defaultProject != null ? defaultProject.getSpaceCodeOrNull() : null;

            if (spaceCodeOrNull.equals(oldSpaceCodeOrNull) == false)
            {
                if (defaultProject != null)
                {
                    removeField(defaultProject);
                }
                int homeSpaceIndex = indexOfField(homeSpaceField);
                defaultProject = createDefaultProjectUIField(spaceCodeOrNull);
                insertField(defaultProject, homeSpaceIndex + 1);
            }
        }

        form.layout(true);
        layout(true);
    }

    private ProjectSelectionWidget createDefaultProjectUIField(String spaceCode)
    {
        String idSuffix = DropDownList.ID + ProjectSelectionWidget.SUFFIX + DIALOG_ID;
        final ProjectSelectionWidget field =
                new ProjectSelectionWidget(viewContext, idSuffix, spaceCode, null);
        field.setFieldLabel("Default Project");
        GWTUtils.setToolTip(field, "Default Project Code");
        field.setAllowBlank(true);
        return field;
    }

    private CheckBoxField createReopenLastTabOnLoginField()
    {
        CheckBoxField field =
                new CheckBoxField(viewContext.getMessage(Dict.REOPEN_LAST_TAB_ON_LOGIN_LABEL),
                        false);
        GWTUtils.setToolTip(field, viewContext.getMessage(Dict.REOPEN_LAST_TAB_ON_LOGIN_INFO));
        AbstractImagePrototype infoIcon =
                AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
        FieldUtil.addInfoIcon(field, viewContext.getMessage(Dict.REOPEN_LAST_TAB_ON_LOGIN_INFO),
                infoIcon.createImage());
        field.setValue(viewContext.getDisplaySettingsManager().isReopenLastTabOnLogin());
        return field;
    }

    private CheckBoxField createShowLastVisitsField()
    {
        CheckBoxField field =
                new CheckBoxField(viewContext.getMessage(Dict.SHOW_LAST_VISITS_LABEL),
                        false);
        GWTUtils.setToolTip(field, viewContext.getMessage(Dict.SHOW_LAST_VISITS_INFO));
        field.setValue(viewContext.getDisplaySettingsManager().getPortletConfigurations().containsKey(StandardPortletNames.HISTORY));
        return field;
    }

    @Override
    protected void save(AsyncCallback<Void> saveCallback)
    {
        String groupCodeOrNull = null;
        TechId groupIdOrNull = null;

        String spaceRaw = homeSpaceField.getRawValue();

        if (spaceRaw != null && spaceRaw.isEmpty() == false)
        {
            Space space = homeSpaceField.tryGetSelected();
            groupCodeOrNull = space == null ? null : space.getCode();
            groupIdOrNull = TechId.create(space);
        }

        viewContext.getModel().getSessionContext().getUser().setHomeGroupCode(groupCodeOrNull);
        viewContext.getService().changeUserHomeSpace(groupIdOrNull, saveCallback);

        RealNumberFormatingParameters formatingParameters = getRealNumberFormatingParameters();
        formatingParameters.setFormatingEnabled(formatingFields.isExpanded());
        formatingParameters.setPrecision(precisionField.getValue().intValue());
        formatingParameters.setScientific(scientificFormatingField.getValue());

        DisplaySettingsManager displaySettingsManager = viewContext.getDisplaySettingsManager();

        boolean debuggingModeEnabled = debuggingModeField.getValue();
        displaySettingsManager.setDebuggingModeEnabled(debuggingModeEnabled);

        boolean restoreLastTab = reopenLastTabField.getValue();
        displaySettingsManager.setReopenLastTabOnLogin(restoreLastTab);

        boolean isLegacyMetadataEnabled = enableLegacyMetadataUI.getValue();
        viewContext.getDisplaySettingsManager().setLegacyMedadataUIEnabled(isLegacyMetadataEnabled);

        if (groupCodeOrNull != null && defaultProject != null)
        {
            String defaultProjectIdentifierOrNull = null;

            String projectRaw = defaultProject.getRawValue();
            if (projectRaw != null && projectRaw.isEmpty() == false)
            {
                Project defaultProjectObject = defaultProject.tryGetSelectedProject();
                if (defaultProjectObject != null)
                {
                    defaultProjectIdentifierOrNull = defaultProjectObject.getIdentifier();
                }
            }
            viewContext.getDisplaySettingsManager().setDefaultProject(defaultProjectIdentifierOrNull);
        } else
        {
            viewContext.getDisplaySettingsManager().setDefaultProject(null);
        }

        if (showLastVisitsField.getValue())
        {
            displaySettingsManager.addPortlet(new PortletConfiguration(StandardPortletNames.HISTORY));
        } else
        {
            displaySettingsManager.getPortletConfigurations().remove(StandardPortletNames.HISTORY);
        }
        displaySettingsManager.storeSettings();
    }

    private FieldSet createRealFormatingFieldSet()
    {
        FieldSet fields = new FieldSet();
        fields.setHeading(viewContext.getMessage(Dict.REAL_NUMBER_FORMATING_FIELDS));
        fields.setCheckboxToggle(true);
        fields.setExpanded(getRealNumberFormatingParameters().isFormatingEnabled());
        FormLayout layout = new FormLayout();
        layout.setLabelWidth(139);
        fields.setLayout(layout);
        Listener<BaseEvent> listener = new Listener<BaseEvent>()
            {

                @Override
                public void handleEvent(BaseEvent be)
                {
                    layout(true);
                }

            };
        fields.addListener(Events.Expand, listener);
        fields.addListener(Events.Collapse, listener);
        return fields;
    }

    private IntegerField createPrecisionField()
    {
        IntegerField field =
                new IntegerField(viewContext.getMessage(Dict.REAL_NUMBER_FORMATING_PRECISION),
                        false);
        field.setValue(getRealNumberFormatingParameters().getPrecision());
        field.setMaxValue(RealNumberFormatingParameters.MAX_PRECISION);
        return field;
    }

    private CheckBoxField createScientificCheckBox()
    {
        CheckBoxField field =
                new CheckBoxField(viewContext.getMessage(Dict.SCIENTIFIC_FORMATING), false);
        field.setValue(getRealNumberFormatingParameters().isScientific());
        return field;
    }

    private CheckBoxField createDebuggingModeCheckBox()
    {
        CheckBoxField field = new CheckBoxField(viewContext.getMessage(Dict.DEBUGGING_MODE), false);
        GWTUtils.setToolTip(field, viewContext.getMessage(Dict.DEBUGGING_MODE_INFO));
        AbstractImagePrototype infoIcon =
                AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
        FieldUtil.addInfoIcon(field, viewContext.getMessage(Dict.DEBUGGING_MODE_INFO),
                infoIcon.createImage());
        field.setValue(viewContext.getDisplaySettingsManager().isDebuggingModeEnabled());
        return field;
    }

    private RealNumberFormatingParameters getRealNumberFormatingParameters()
    {
        return viewContext.getDisplaySettingsManager().getRealNumberFormatingParameters();
    }

    //
    // Reset
    //

    private Button createResetButton()
    {
        final String buttonTitle = viewContext.getMessage(Dict.RESET_USER_SETTINGS_BUTTON);
        final Button button = new Button(buttonTitle, new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(ButtonEvent ce)
                {
                    final String title = viewContext.getMessage(Dict.CONFIRM_TITLE);
                    final String msg =
                            viewContext.getMessage(Dict.RESET_USER_SETTINGS_CONFIRMATION_MSG);
                    MessageBox.confirm(title, msg, new Listener<MessageBoxEvent>()
                        {
                            @Override
                            public void handleEvent(MessageBoxEvent messageEvent)
                            {
                                if (messageEvent.getButtonClicked().getItemId().equals(Dialog.YES))
                                {
                                    resetUserSettings();
                                }
                            }
                        });
                }
            });
        return button;
    }

    private void resetUserSettings()
    {
        viewContext.getService().resetDisplaySettings(new ResetUserSettingsCallback(viewContext));
    }

    public final class ResetUserSettingsCallback extends AbstractAsyncCallback<DisplaySettings>
    {
        private ResetUserSettingsCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public final void process(final DisplaySettings defaultDisplaySettings)
        {
            // reinitialize DisplaySettingsManager with updated SessionContext
            viewContext.getModel().getSessionContext().setDisplaySettings(defaultDisplaySettings);
            viewContext.initDisplaySettingsManager();
            resetCallback.execute();
            hide();
        }
    }

    private HelpPageIdentifier createHelpPageIdentifier()
    {
        return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.CHANGE_USER_SETTINGS,
                HelpPageIdentifier.HelpPageAction.ACTION);
    }
}
