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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.script;

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DataSetChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MaterialChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField.SampleChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DynamicPropertyEvaluationInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityValidationEvaluationInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;

/**
 * @author Izabela Adamczyk
 */
public class ScriptExecutionFramework
{

    FormPanel panel;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final SampleChooserFieldAdaptor sampleChooser;

    private final ExperimentChooserFieldAdaptor experimentChooser;

    private final MaterialChooserField materialChooser;

    private final DataSetChooserField datasetChooser;

    private final FieldSet evaluationResultPanel;

    private final EntityKindSelectionWidget entityKindChooser;

    private final State state = new State();

    private final AdapterField entityLink;

    private final CheckBox isNewEntity;

    private final MultilineHTML html;

    private final IValidable validable;

    private ScriptType scriptType;

    private static class State
    {
        private String scriptName;

        private String script;

        private PluginType pluginType;

        public String getScript()
        {
            return script;
        }

        public void setScript(String script)
        {
            this.script = script;
        }

        public PluginType getPluginType()
        {
            return pluginType;
        }

        public void setPluginType(PluginType pluginType)
        {
            this.pluginType = pluginType;
        }

        public String getScriptName()
        {
            return scriptName;
        }

        public void setScriptName(String scriptName)
        {
            this.scriptName = scriptName;
        }
    }

    public ScriptExecutionFramework(IViewContext<ICommonClientServiceAsync> viewContext,
            IValidable validable, EntityKind entityKindOrNull)
    {
        this.viewContext = viewContext;
        this.validable = validable;
        entityKindChooser =
                new EntityKindSelectionWidget(viewContext, entityKindOrNull, true, false);
        sampleChooser =
                SampleChooserField.create(viewContext.getMessage(Dict.SAMPLE), true, null, true,
                        true, false, viewContext.getCommonViewContext(),
                        SampleTypeDisplayID.SCRIPT_EDITOR_SAMPLE_CHOOSER, false);
        experimentChooser =
                ExperimentChooserField.create(viewContext.getMessage(Dict.EXPERIMENT), true, null,
                        viewContext);
        materialChooser =
                MaterialChooserField.create(viewContext.getMessage(Dict.MATERIAL), true, null,
                        null, viewContext);
        datasetChooser =
                DataSetChooserField
                        .create(viewContext.getMessage(Dict.DATA_SET), true, viewContext);
        final Map<EntityKind, Field<?>> map =
                createEntitySelectionMap(sampleChooser, experimentChooser, materialChooser,
                        datasetChooser);
        entityLink = createEntityLink();

        isNewEntity = createCheckBox();

        html = new MultilineHTML("");
        evaluationResultPanel = createResultField(html);
        updateVisibleEntityChooser(map, entityKindChooser, entityLink);
        entityKindChooser
                .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<LabeledItem<EntityKind>>>()
                    {
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<SimpleComboValue<LabeledItem<EntityKind>>> se)
                        {
                            updateVisibleEntityChooser(map, entityKindChooser, entityLink);
                            evaluationResultPanel.setVisible(false);
                        }
                    });

        panel = createPanel();
        panel.add(entityKindChooser);
        panel.add(sampleChooser.getChooserField());
        panel.add(experimentChooser.getChooserField());
        panel.add(materialChooser);
        panel.add(datasetChooser);
        panel.add(isNewEntity);
        panel.add(entityLink);
        panel.add(createButtonsField());
        panel.add(evaluationResultPanel);

        scriptType = ScriptType.DYNAMIC_PROPERTY;
    }

    private AdapterField createButtonsField()
    {
        AdapterField field =
                new AdapterField(WidgetUtils.inRow(createCalculateButton(), createSeparator(),
                        createResetButton()));
        field.setLabelSeparator("");
        return field;
    }

    private Html createSeparator()
    {
        return new Html("&nbsp;");
    }

    private void reset()
    {
        panel.reset();
        html.setMultilineHTML("");
        evaluationResultPanel.setVisible(false);
    }

    private Button createResetButton()
    {
        Button button = new Button(viewContext.getMessage(Dict.BUTTON_RESET));
        button.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    reset();
                }
            });
        return button;
    }

    private Button createCalculateButton()
    {
        Button refresh = new Button(viewContext.getMessage(Dict.BUTTON_EVALUATE));
        refresh.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    boolean thisValid = panel.isValid();
                    boolean dependantValid = validable.isValid();
                    if (thisValid && dependantValid)
                    {
                        evaluationResultPanel.setVisible(true);
                        evaluate();
                    }
                }
            });
        return refresh;
    }

    public FieldSet createResultField(MultilineHTML widget)
    {
        FieldSet fieldSet = new FieldSet();
        fieldSet.setHeading(viewContext.getMessage(Dict.EVALUATION_RESULT));
        fieldSet.add(widget);
        fieldSet.setVisible(false);
        return fieldSet;
    }

    private BasicEntityDescription tryGetSelectedEntity()
    {

        if (StringUtils.isBlank(sampleChooser.getValue()) == false)
        {
            return new BasicEntityDescription(EntityKind.SAMPLE, sampleChooser.getValue());
        }
        if (experimentChooser.tryToGetValue() != null
                && StringUtils.isBlank(experimentChooser.tryToGetValue().getIdentifier()) == false)
        {
            return new BasicEntityDescription(EntityKind.EXPERIMENT, experimentChooser
                    .tryToGetValue().getIdentifier());
        }
        if (StringUtils.isBlank(materialChooser.getValue()) == false)
        {
            return new BasicEntityDescription(EntityKind.MATERIAL, materialChooser.getValue());
        }
        if (StringUtils.isBlank(datasetChooser.getValue()) == false)
        {
            return new BasicEntityDescription(EntityKind.DATA_SET, datasetChooser.getValue());
        }
        return null;
    }

    private AdapterField createEntityLink()
    {
        ClickHandler listener = new ClickHandler()
            {

                @Override
                public void onClick(final ClickEvent event)
                {
                    final boolean ifSpecialKeyPressed =
                            WidgetUtils.ifSpecialKeyPressed(event.getNativeEvent());
                    BasicEntityDescription entity = tryGetSelectedEntity();
                    if (entity != null)
                    {
                        AsyncCallback<IEntityInformationHolderWithPermId> callback =
                                new AbstractAsyncCallback<IEntityInformationHolderWithPermId>(
                                        viewContext)
                                    {
                                        @Override
                                        protected void process(
                                                IEntityInformationHolderWithPermId result)
                                        {
                                            new OpenEntityDetailsTabAction(result, viewContext,
                                                    ifSpecialKeyPressed).execute();
                                        }
                                    };
                        viewContext.getCommonService().getEntityInformationHolder(
                                tryGetSelectedEntity(), callback);
                    } else
                    {
                        MessageBox.info("Entity not selected", "Please choose the entity", null);
                    }
                }
            };
        Widget linkWidget =
                LinkRenderer.getLinkWidget(viewContext.getMessage(Dict.SHOW_DETAILS), listener);
        AdapterField field = new AdapterField(linkWidget);
        field.setFieldLabel(viewContext.getMessage(Dict.ENTITY_DETAILS));
        return field;

    }

    private CheckBox createCheckBox()
    {
        final CheckBox checkBox = new CheckBox();
        checkBox.setFieldLabel("Is New Entity?");
        checkBox.setBoxLabel("");
        checkBox.setValue(false);
        checkBox.setVisible(false);

        return checkBox;
    }

    public Widget getWidget()
    {
        FieldSet set = new FieldSet();
        set.setHeading(viewContext.getMessage(Dict.SCRIPT_TESTER));
        set.add(panel);
        return set;
    }

    public void update(String scriptName, String script, PluginType pluginTypeOrNull)
    {
        state.setScriptName(scriptName);
        state.setScript(script);

        if (pluginTypeOrNull != null)
        {
            state.setPluginType(pluginTypeOrNull);
        }
    }

    public void updateEntityKind(EntityKind kind)
    {
        if (kind != null)
        {
            entityKindChooser.setSimpleValue(EntityKindSelectionWidget.createLabeledItem(kind, viewContext));
            entityKindChooser.disable();
        } else
        {
            entityKindChooser.enable();
        }
    }

    private void evaluate()
    {
        BasicEntityDescription selectedEntityOrNull = tryGetSelectedEntity();
        if (selectedEntityOrNull != null)
        {
            evaluate(selectedEntityOrNull.getEntityKind(),
                    selectedEntityOrNull.getEntityIdentifier(), state.getPluginType(),
                    state.getScriptName(), state.getScript());
        }
    }

    private void evaluate(EntityKind kind, String entity, PluginType pluginType, String scriptName,
            String script)
    {
        if (entity == null)
        {
            return;
        }
        if (this.scriptType == ScriptType.DYNAMIC_PROPERTY)
        {
            updateEvaluationResultField(viewContext.getMessage(Dict.EVALUATION_IN_PROGRESS));
            viewContext.getCommonService()
                    .evaluate(
                            new DynamicPropertyEvaluationInfo(kind, entity, pluginType, scriptName,
                                    script), new AbstractAsyncCallback<String>(viewContext)
                                {

                                    @Override
                                    protected void process(String result)
                                    {
                                        updateEvaluationResultField(result);
                                    }

                                    @Override
                                    public void finishOnFailure(Throwable caught)
                                    {
                                        updateEvaluationResultField("");
                                        evaluationResultPanel.setVisible(false);
                                    }
                                });
        } else if (this.scriptType == ScriptType.ENTITY_VALIDATION)
        {
            updateEvaluationResultField(viewContext.getMessage(Dict.EVALUATION_IN_PROGRESS));
            viewContext.getCommonService().evaluate(
                    new EntityValidationEvaluationInfo(kind, entity, isNewEntity.getValue(),
                            pluginType, scriptName, script),
                    new AbstractAsyncCallback<String>(viewContext)
                        {

                            @Override
                            protected void process(String result)
                            {
                                updateEvaluationResultField(result);
                            }

                            @Override
                            public void finishOnFailure(Throwable caught)
                            {
                                updateEvaluationResultField("");
                                evaluationResultPanel.setVisible(false);
                            }
                        });
        }
    }

    private void updateEvaluationResultField(String result)
    {
        html.setMultilineHTML(result == null ? "(null)" : result);
    }

    private static void updateVisibleEntityLink(boolean visible, Field<?> entityLink)
    {
        FieldUtil.setVisibility(visible, entityLink);
    }

    private static void updateVisibleEntityChooser(Map<EntityKind, Field<?>> map,
            EntityKindSelectionWidget entityKindChooser, Field<?> entityLink)
    {
        boolean atLeastOneEntityChooserVisible = false;
        for (Field<?> w : map.values())
        {
            w.reset();
            EntityKind kind = entityKindChooser.tryGetEntityKind();
            boolean visible = kind != null && w == map.get(kind);
            atLeastOneEntityChooserVisible = atLeastOneEntityChooserVisible || visible;
            FieldUtil.setVisibility(visible, w);
        }
        updateVisibleEntityLink(atLeastOneEntityChooserVisible, entityLink);
    }

    private static FormPanel createPanel()
    {
        FormPanel p = new FormPanel();
        p.setHeaderVisible(false);
        p.setBodyBorder(false);
        p.setBorders(false);
        p.setScrollMode(Scroll.AUTO);
        p.setWidth(AbstractRegistrationForm.DEFAULT_LABEL_WIDTH
                + AbstractRegistrationForm.DEFAULT_FIELD_WIDTH / 2
                + AbstractRegistrationForm.PANEL_MARGIN);
        p.setLabelWidth(AbstractRegistrationForm.DEFAULT_LABEL_WIDTH);
        p.setFieldWidth(AbstractRegistrationForm.DEFAULT_FIELD_WIDTH / 2);
        return p;
    }

    private static Map<EntityKind, Field<?>> createEntitySelectionMap(
            SampleChooserFieldAdaptor sampleChooser,
            ExperimentChooserFieldAdaptor experimentChooser, MaterialChooserField materialChooser,
            DataSetChooserField datasetChooser)
    {
        Map<EntityKind, Field<?>> m = new HashMap<EntityKind, Field<?>>();
        m.put(EntityKind.SAMPLE, sampleChooser.getChooserField());
        m.put(EntityKind.EXPERIMENT, experimentChooser.getChooserField());
        m.put(EntityKind.MATERIAL, materialChooser);
        m.put(EntityKind.DATA_SET, datasetChooser);
        return m;
    }

    public ScriptType getScriptType()
    {
        return scriptType;
    }

    public void setScriptType(ScriptType scriptType)
    {
        this.scriptType = scriptType;
        this.isNewEntity.setVisible(scriptType == ScriptType.ENTITY_VALIDATION);
    }
}
