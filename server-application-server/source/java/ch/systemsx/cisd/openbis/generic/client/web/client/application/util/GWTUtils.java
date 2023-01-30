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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.MainTabPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleDialog;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.TextMetrics;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Some utility methods around <i>GWT</i>.
 * 
 * @author Christian Ribeaud
 */
public final class GWTUtils
{

    /**
     * The <code>null</code> value label for {@link ListBox} resp. {@link ComboBox} when no value has been selected by the user.
     */
    public static final String NONE_LIST_ITEM = "(None)";

    private GWTUtils()
    {
        // Can not be instantiated.
    }

    private static boolean testing = false;

    /**
     * Whether we are in testing mode.
     */
    public static boolean isTesting()
    {
        return testing;
    }

    /**
     * Sets <code>testing</code> flag to <code>true</code>.
     */
    public static void testing()
    {
        testing = true;
    }

    /**
     * Updates prefix of the page title to the specified one.
     */
    public static void updatePageTitle(String prefix)
    {
        String title =
                (prefix.equals(MainTabPanel.BLANK_TAB_TITLE) ? "" : prefix + " - ")
                        + CommonViewContext.ClientStaticState.getPageTitleSuffix();
        Window.setTitle(title);
    }

    public static void alert(String title, String message)
    {
        MessageBox.alert(title, message, null).getDialog().setResizable(true);
    }

    public static com.extjs.gxt.ui.client.widget.Window createErrorMessageWithDetailsDialog(
            final IMessageProvider messageProvider, final String basicMsg, final String detailedMsg)
    {
        final String heading = "Error";
        final String okButtonLabel = "Show Details";
        final HorizontalPanel panel = new HorizontalPanel();
        panel.setLayout(new FitLayout());
        panel.addText(basicMsg);
        panel.setBorders(false);
        final SimpleDialog dialog =
                new SimpleDialog(panel, heading, okButtonLabel, messageProvider);
        dialog.setAcceptAction(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    alert("Error Details", detailedMsg);
                }
            });
        return dialog;
    }

    /**
     * Sets up {@link ComboBox} to have width of list auto adjusted to maximum width of elements in the list.
     */
    // TODO 2010-05-19, Piotr Buczek: the width is not updated when store is changed;
    public static void setupAutoWidth(final ComboBox<?> comboBox)
    {
        // can't get el() of listView before it is rendered
        comboBox.getListView().addListener(Events.Render, new Listener<BaseEvent>()
            {
                private final static int MARGIN = 30;

                @Override
                public void handleEvent(BaseEvent be)
                {
                    comboBox.setMinListWidth(MARGIN
                            + computeMaxLabelWidth(comboBox.getListView().el()));
                }

                public int computeMaxLabelWidth(El el)
                {
                    final TextMetrics metrics = TextMetrics.get();
                    metrics.bind(el);
                    final String displayField = comboBox.getDisplayField();

                    int maxLabelWidth = 0;
                    for (ModelData model : comboBox.getStore().getModels())
                    {
                        String displayedString = model.get(displayField).toString();
                        maxLabelWidth = Math.max(maxLabelWidth, metrics.getWidth(displayedString));
                    }
                    // It makes sense to limit width of the list because it doesn't look good
                    // if it takes whole width of the window.
                    return Math.min(Window.getClientWidth() / 2, maxLabelWidth);
                }
            });
    }

    /**
     * Defers a task allowing browser to handle events.
     */
    public static void executeDelayed(final IDelegatedAction delegatedAction)
    {
        // In testing mode the action has to be performed without delay - otherwise system tests
        // will perform new commands before the deferred action starts.
        if (isTesting() == false)
        {
            Scheduler.get().scheduleDeferred(delegatedAction);
        } else
        {
            delegatedAction.execute();
        }
    }

    /**
     * Selects given <var>value</var> of given <var>listBox</var>.
     */
    public final static void setSelectedItem(final ListBox listBox, final String value)
    {
        assert listBox != null : "Unspecified list box.";
        assert value != null : "Unspecified value.";
        for (int index = 0; index < listBox.getItemCount(); index++)
        {
            if (listBox.getItemText(index).equals(value))
            {
                listBox.setSelectedIndex(index);
                return;
            }
        }
        throw new IllegalArgumentException("Given value '" + value
                + "' not found in given list box.");
    }

    /**
     * Selects given <var>value</var> of given <var>comboBox</var>.
     */
    public final static <T extends ModelData> void setSelectedItem(final ComboBox<T> comboBox,
            final String property, final String value)
    {
        assert comboBox != null : "Unspecified combo box.";
        assert property != null : "Unspecified model property.";
        assert value != null : "Unspecified model property value.";
        final ListStore<T> store = comboBox.getStore();
        final List<T> list = store.findModels(property, value);
        if (list.size() == 0)
        {
            final List<Object> possibleValues = new ArrayList<Object>();
            for (final T t : store.getModels())
            {
                possibleValues.add(t.get(property));
            }
            throw new IllegalArgumentException("Given value '" + value + "' for property '"
                    + property + "' not found in the combo box. Possible values are: '"
                    + possibleValues + "'.");
        }
        final List<T> selection = new ArrayList<T>();
        selection.add(list.get(0));
        comboBox.setSelection(selection);
    }

    /**
     * Selects first element of the specified combobox if nothing was selected before.
     */
    public static <T extends ModelData> void autoselect(ComboBox<T> combo)
    {
        autoselect(combo, true);
    }

    /**
     * Selects first element of the specified combobox.
     * 
     * @onlyIfEmpty if true nothing is done if something is already selected
     */
    public static <T extends ModelData> void autoselect(ComboBox<T> combo, boolean onlyIfEmpty)
    {
        if (onlyIfEmpty && combo.getValue() != null)
        {
            return;
        }
        List<T> models = combo.getStore().getModels();
        if (models.size() > 0)
        {
            combo.setValue(models.get(0));
        }
    }

    /**
     * Returns true if given <var>value</var> is in given <var>comboBox</var> list.
     */
    public final static <T extends ModelData> boolean isPropertyNotInList(
            final ComboBox<T> comboBox, final String property, final String value)
    {
        assert comboBox != null : "Unspecified combo box.";
        assert property != null : "Unspecified model property.";
        assert value != null : "Unspecified model property value.";
        final ListStore<T> store = comboBox.getStore();
        final List<T> list = store.findModels(property, value);
        return list.size() == 0;
    }

    /**
     * Unselects given <var>comboBox</var>.
     */
    public final static <T extends ModelData> void unselect(final ComboBox<T> comboBox)
    {
        assert comboBox != null : "Unspecified comboBox.";
        comboBox.setValue(null);
    }

    /**
     * Selects given <var>value</var> of given <var>tree</var>.
     */
    public final static void setSelectedItem(final TreeGrid<ModelData> tree, final String property,
            final String value)
    {
        ModelData model = tryFindModel(tree.getTreeStore().getAllItems(), property, value);
        if (model != null)
        {
            GridSelectionModel<ModelData> selectionModel = tree.getSelectionModel();
            selectionModel.select(model, false);
            int row = tree.getStore().indexOf(model);
            tree.getView().ensureVisible(row, 0, false);
        }
    }

    /**
     * Sets the tooltip of the component using default configuration (disappear after mouse moved), replace new lines with html breaks. If null value
     * is passed, tooltip is removed.
     */
    public static void setToolTip(Component component, String textOrNull)
    {
        ToolTipConfig config = null;
        if (textOrNull != null)
        {
            String preparedText = translateToHtmlLineBreaks(textOrNull);
            config = new ToolTipConfig(preparedText);
        }
        component.setToolTip(config);
    }

    /**
     * Sets the tooltip of the component using given configuration and replaces new lines with html breaks.
     */
    public static void setToolTip(Component component, ToolTipConfig config)
    {
        String textOrNull = config.getText();
        if (textOrNull != null)
        {
            String preparedText = translateToHtmlLineBreaks(textOrNull);
            config.setText(preparedText);
        }
        component.setToolTip(config);
    }

    public static String translateToHtmlLineBreaks(String textOrNull)
    {
        return textOrNull != null ? textOrNull.replace("\n", "<br>") : null;
    }

    /** @return specified model from the list if it's found, null otherwise */
    public final static ModelData tryFindModel(final List<ModelData> models, final String property,
            final String value)
    {
        assert models != null : "Unspecified models.";
        assert property != null : "Unspecified model property.";
        assert value != null : "Unspecified model property value.";

        for (ModelData model : models)
        {
            if (model != null)
            {
                Object val = model.get(property);
                if (val == value || (val != null && val.equals(value)))
                {
                    return model;
                }
            }
        }
        return null;
    }

    /**
     * Tries to return the selected {@link ModelData} from the given {@link ComboBox}.
     * 
     * @returns <code>null</code> if nothing is selected.
     */
    public final static <T extends ModelData> T tryGetSingleSelectedModel(final ComboBox<T> comboBox)
    {
        assert comboBox != null : "Unspecified combo box.";
        final List<T> selection = comboBox.getSelection();
        final int size = selection.size();
        if (size > 0)
        {
            assert size == 1 : "Only one item must be selected.";
            return selection.get(0);
        }
        return null;
    }

    /**
     * Tries to return the selected object property from the given {@link ComboBox}.
     * 
     * @returns <code>null</code> if nothing is selected.
     */
    public final static <T extends ModelData, O> O tryGetSingleSelectedProperty(
            final ComboBox<T> comboBox, String propertyName)
    {
        T selectedModel = GWTUtils.tryGetSingleSelectedModel(comboBox);
        if (selectedModel == null)
        {
            return null;
        }
        return selectedModel.get(propertyName);
    }

    /**
     * Tries to return the selected object code (saved as {@link ModelDataPropertyNames#CODE} in the model) from the given {@link ComboBox}.
     * 
     * @returns <code>null</code> if nothing is selected.
     */
    public final static <T extends ModelData> String tryGetSingleSelectedCode(
            final ComboBox<T> comboBox)
    {
        return tryGetSingleSelectedProperty(comboBox, ModelDataPropertyNames.CODE);
    }

    /**
     * Tries to return the selected object (saved as {@link ModelDataPropertyNames#OBJECT} in the model) from the given {@link ComboBox}.
     * 
     * @returns <code>null</code> if nothing is selected.
     */
    public final static <T extends ModelData, O> O tryGetSingleSelected(final ComboBox<T> comboBox)
    {
        return tryGetSingleSelectedProperty(comboBox, ModelDataPropertyNames.OBJECT);
    }

    /** Returns base URL to the index page of the application. */
    public final static String getBaseIndexURL()
    {
        return GWT.getModuleBaseURL() + "index.html";
    }

    /**
     * Enables and makes visible the field with appropriate 'on' flag set to true. Performs its work when one and only one 'on' flag is set to true.
     */
    public static final void updateVisibleField(boolean firstOn, boolean secondOn,
            Field<?> firstField, Field<?> secondField)
    {
        if (firstOn ^ secondOn)
        {
            firstField.setEnabled(firstOn);
            firstField.setVisible(firstOn);
            secondField.setEnabled(secondOn);
            secondField.setVisible(secondOn);
            if (firstOn)
            {
                firstField.validate();
                secondField.clearInvalid();
            } else
            {
                firstField.clearInvalid();
                secondField.validate();
            }
        }
    }

    public final static String escapeToFormId(final String original)
    {
        return original.toLowerCase().replace(".", "-DOT-").replace("_", "-UNDERSCORE-")
                .replace("$", "-DOLLAR-").replace("/", "-SLASH-").replace(":", "-COLON-");
    }

    // confirm on exit message

    private final static String CONFIRM_EXIT_MSG =
            "WARNING: By doing this you will in fact leave openBIS!";

    public final static void setConfirmExitMessage()
    {
        setConfirmExitMessage(CONFIRM_EXIT_MSG);
    }

    public final static void removeConfirmExitMessage()
    {
        setConfirmExitMessage(null);
    }

    private final static void setConfirmExitMessage(final String msgOrNull)
    {
        Window.addWindowClosingHandler(new ClosingHandler()
            {
                @Override
                public void onWindowClosing(ClosingEvent event)
                {
                    event.setMessage(msgOrNull);
                }
            });
    }

    /**
     * Returns the <i>search</i> of a <i>Javascript</i> window location (without the starting <code>?</code> if any).
     * 
     * @return something like <code>key1=value1&key2=value2</code>.
     */
    public final static String getParamString()
    {
        return Location.getQueryString();
    }

    /**
     * Whether this application is deployed.
     */
    public final static boolean isDeployed()
    {
        return GWT.isScript();
    }

    public enum DisplayInfoTime
    {
        /** short time of display (2,5s) */
        SHORT(2500),
        /** medium time of display (5s) */
        MEDIUM(5000),
        /** long time of display (10s) */
        LONG(10000);

        private final int time;

        DisplayInfoTime(int time)
        {
            this.time = time;
        }

        public int getTime()
        {
            return time;
        }
    }

    public final static void displayInfo(String title)
    {
        displayInfo(title, "", DisplayInfoTime.MEDIUM);
    }

    public final static void displayInfo(String title, DisplayInfoTime time)
    {
        displayInfo(title, "", time.getTime());
    }

    public final static void displayInfo(String title, String text)
    {
        displayInfo(title, text, DisplayInfoTime.MEDIUM);
    }

    public final static void displayInfo(String title, String text, DisplayInfoTime time)
    {
        displayInfo(title, text, time.getTime());
    }

    private final static void displayInfo(String title, String text, int displayTime)
    {
        InfoConfig config = new InfoConfig(title, text);
        config.display = displayTime;
        Info.display(config);
    }

    //
    // native JavaScript
    //

    /**
     * Tooltip template displayed when mouse is over drop down list.
     */
    public final static native String getTooltipTemplate(String displayField, String tooltipField)
    /*-{ 
       return  [ 
       '<tpl for=".">', 
       '<div class="x-combo-list-item" qtip="{[values.',
       tooltipField,
       ']}">{[values.',
       displayField,
       ']}</div>', 
       '</tpl>' 
       ].join(""); 
     }-*/;

}
