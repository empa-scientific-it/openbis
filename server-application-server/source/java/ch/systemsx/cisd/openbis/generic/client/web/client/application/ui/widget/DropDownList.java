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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * @author Izabela Adamczyk
 */
abstract public class DropDownList<M extends ModelData, E> extends ComboBox<M> implements
        IDisposableComponent
{
    abstract protected void loadData(AbstractAsyncCallback<List<E>> callback);

    abstract protected List<M> convertItems(final List<E> result);

    // ----------

    private static final int DEFAULT_WIDTH = 200;

    private static final String PREFIX = "select_";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final String valueNotInListMsg;

    private final boolean mandatory;

    private final boolean reloadWhenRendering;

    private boolean autoSelectFirst;

    private final IViewContext<?> viewContextOrNull;

    protected boolean allowValueNotFromList = false;

    protected String chooseMsg;

    protected String emptyMsg;

    public String callbackIdOrNull;

    protected String resultSetKey;

    private final List<IDataRefreshCallback> dataRefreshCallbacks;

    /*
     * Twin trigger related
     */
    private final boolean twinTriggerEnabled;

    protected El twinTrigger;

    private String twinTriggerStyle = "x-form-trigger-add";

    protected El span;

    public DropDownList(final IViewContext<?> viewContext, String idSuffix, String labelDictCode,
            String displayField, String chooseSuffix, String nothingFoundSuffix, boolean twinTriggerEnabled)
    {
        this(idSuffix, displayField, viewContext.getMessage(labelDictCode), viewContext.getMessage(
                Dict.COMBO_BOX_CHOOSE, chooseSuffix), viewContext.getMessage(Dict.COMBO_BOX_EMPTY,
                nothingFoundSuffix), viewContext
                .getMessage(Dict.COMBO_BOX_EXPECTED_VALUE_FROM_THE_LIST), true, viewContext, true, twinTriggerEnabled);
    }

    public DropDownList(final IViewContext<?> viewContext, String idSuffix, String labelDictCode,
            String displayField, String chooseSuffix, String nothingFoundSuffix)
    {
        this(viewContext, idSuffix, labelDictCode, displayField, chooseSuffix, nothingFoundSuffix, false);
    }

    public DropDownList(String idSuffix, String displayField, String label, String chooseMsg,
            String emptyMsg, String valueNotInListMsg, boolean mandatory,
            final IViewContext<?> viewContextOrNull, boolean reloadWhenRendering)
    {
        this(idSuffix, displayField, label, chooseMsg, emptyMsg, valueNotInListMsg, mandatory,
                viewContextOrNull, reloadWhenRendering, false);
    }

    /** if viewContextOrNull is null the combobox is not able to refresh itself */
    protected DropDownList(String idSuffix, String displayField, String label, String chooseMsg,
            String emptyMsg, String valueNotInListMsg, boolean mandatory,
            final IViewContext<?> viewContextOrNull, boolean reloadWhenRendering,
            boolean twinTriggerEnabled)
    {
        this.twinTriggerEnabled = twinTriggerEnabled;
        this.chooseMsg = chooseMsg;
        this.emptyMsg = emptyMsg;
        this.valueNotInListMsg = valueNotInListMsg;
        this.mandatory = mandatory;
        this.reloadWhenRendering = reloadWhenRendering;
        this.viewContextOrNull = viewContextOrNull;
        this.dataRefreshCallbacks = new ArrayList<IDataRefreshCallback>();

        setId(createId(idSuffix));
        setEnabled(true);
        setValidateOnBlur(true);
        setAllowBlank(mandatory == false);
        setWidth(DEFAULT_WIDTH);
        setDisplayField(displayField);
        setFieldLabel(label);
        setStore(createEmptyStoreWithContainsFilter(this));
        GWTUtils.setupAutoWidth(this);
    }

    public static <M extends ModelData> ListStore<M> createEmptyStoreWithContainsFilter(
            final ComboBox<M> comboBox)
    {

        StoreFilter<M> filter = new StoreFilter<M>()
            {
                @Override
                public boolean select(Store<M> s, M parent, M item, String property)
                {
                    String v = comboBox.getRawValue();
                    // WORKAROUND: (GXT2.1) only one option in the list when something
                    // selected and trigger field clicked
                    if (StringUtils.isBlank(v) || comboBox.isExpanded() == false)
                    {
                        return true;
                    }
                    if (item != null)
                    {
                        String displayFieldValue = (String) item.get(comboBox.getDisplayField());
                        if (displayFieldValue != null)
                        {
                            return displayFieldValue.toLowerCase().indexOf(v.toLowerCase()) >= 0;
                        }
                    }
                    return false;
                }
            };
        ListStore<M> newStore = new ListStore<M>()
            {
                @Override
                public void filter(String property, String beginsWith)
                {
                    super.filter(property, null);
                }

                @Override
                public void add(List<? extends M> models)
                {
                    clearFilters();
                    super.add(models);
                    applyFilters(comboBox.getDisplayField());
                }
            };
        newStore.addFilter(filter);
        return newStore;
    }

    protected void setCallbackId(String callbackId)
    {
        this.callbackIdOrNull = callbackId;
    }

    private static String createId(String idSuffix)
    {
        return ID + idSuffix;
    }

    public DatabaseModificationAwareField<M> asDatabaseModificationAware()
    {
        return new DatabaseModificationAwareField<M>(this, this);
    }

    /** if <var>autoSelectFirst</var> and no value is restored, first item will be selected */
    public void setAutoSelectFirst(boolean autoSelectFirst)
    {
        this.autoSelectFirst = autoSelectFirst;
    }

    /**
     * if <var>allowValueNotFromList</var> and value not from list is selected it can still be valid
     */
    public void setAllowValueNotFromList(boolean allowValueNotFromList)
    {
        this.allowValueNotFromList = allowValueNotFromList;
    }

    @Override
    public Component getComponent()
    {
        return this;
    }

    @Override
    public void dispose()
    {
        removeResultSetFromCache();
    }

    protected void removeResultSetFromCache()
    {
        if (resultSetKey != null && viewContextOrNull != null)
        {
            viewContextOrNull.getCommonService().removeResultSet(resultSetKey,
                    new VoidAsyncCallback<Void>(viewContextOrNull));
        }
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refreshStore();
    }

    /**
     * Refreshes the whole store of the combobox. If the previously chosen value is no longer present in the store, it will be changed to empty.
     * Otherwise the previous selection will be preserved.
     */
    public void refreshStore()
    {
        refreshStore(new ListItemsCallback(viewContextOrNull));
    }

    /**
     * Additionally executes a callback after the data refresh is done.
     * 
     * @see DropDownList#refreshStore()
     */
    public void refreshStore(final IDataRefreshCallback dataRefreshCallback)
    {
        if (viewContextOrNull == null)
        {
            return;
        }
        AbstractAsyncCallback<List<E>> callback = mergeWithStandardCallback(dataRefreshCallback);
        refreshStore(callback);
    }

    private AbstractAsyncCallback<List<E>> mergeWithStandardCallback(
            final IDataRefreshCallback dataRefreshCallback)
    {
        return new AbstractAsyncCallback<List<E>>(viewContextOrNull)
            {
                @Override
                protected void process(List<E> result)
                {
                    new ListItemsCallback(viewContextOrNull).process(result);
                    dataRefreshCallback.postRefresh(true);
                }

            };
    }

    private void refreshStore(AbstractAsyncCallback<List<E>> callback)
    {
        if (viewContextOrNull != null)
        {
            loadData(callback);
        } else
        {
            callback.ignore();
        }
    }

    protected class ListItemsCallback extends AbstractAsyncCallback<List<E>>
    {

        protected ListItemsCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(final List<E> result)
        {
            List<M> convertedItems = convertItems(result);
            updateStore(convertedItems);
            for (IDataRefreshCallback c : dataRefreshCallbacks)
            {
                c.postRefresh(true);
            }
        }

        @Override
        public String getCallbackId()
        {
            if (callbackIdOrNull != null)
            {
                return callbackIdOrNull;
            } else
            {
                return super.getCallbackId();
            }
        }

    }

    protected final void updateStore(final List<M> models)
    {
        final ListStore<M> termsStore = getStore();
        termsStore.removeAll();
        termsStore.add(models);

        int termsCount = termsStore.getCount();
        if (termsCount == 0)
        {
            setEmptyText(emptyMsg);
            setReadOnly(true);
        } else if (termsCount == 1)
        {
            selectFirstModel(models);
        } else
        {
            setEmptyText(chooseMsg);
            setReadOnly(false);
            if (getValue() != null && getSelection().size() == 0)
            {
                validate(); // maybe the value became a valid selection
            }
            restoreSelection(getSelection());
            if (autoSelectFirst && getSelection().size() == 0)
            {
                selectFirstModel(models);
            }
        }
        applyEmptyText();
    }

    private void selectFirstModel(final List<M> models)
    {
        setSelection(models);
    }

    private void restoreSelection(List<M> previousSelection)
    {
        List<M> newSelection = cleanSelection(previousSelection, getStore());
        if (previousSelection.size() != newSelection.size())
        {
            setSelection(newSelection);
        }
    }

    // removes the no longer existing items from the selection
    private static <M extends ModelData> List<M> cleanSelection(List<M> previousSelection,
            Store<M> newStore)
    {
        List<M> newSelection = new ArrayList<M>();
        for (M prevItem : previousSelection)
        {
            if (containsModel(newStore, prevItem))
            {
                newSelection.add(prevItem);
            }
        }
        return newSelection;
    }

    private static <M extends ModelData> boolean containsModel(Store<M> store, M item)
    {
        for (M elem : store.getModels())
        {
            if (equalsModel(elem, item))
            {
                return true;
            }
        }
        return false;
    }

    private static <M extends ModelData> boolean equalsModel(M model1, M model2)
    {
        Collection<String> props1 = model1.getPropertyNames();
        Collection<String> props2 = model1.getPropertyNames();
        if (props1.equals(props2) == false)
        {
            return false;
        }
        for (String propName : props1)
        {
            Object val1 = model1.get(propName);
            Object val2 = model2.get(propName);
            if (val1 == null)
            {
                if (val2 != null)
                {
                    return false;
                } else
                {
                    continue;
                }
            } else
            {
                if (val1.equals(val2) == false)
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String getRawValue()
    {
        if (mandatory && optionNoneSelected())
        {
            return "";
        }
        return super.getRawValue();
    }

    @Override
    public M getValue()
    {
        final M val = super.getValue();
        if (optionNoneSelected())
        {
            return null;
        }
        return val;
    }

    private boolean optionNoneSelected()
    {
        return super.getRawValue() != null && super.getRawValue().equals(GWTUtils.NONE_LIST_ITEM);
    }

    /**
     * Assumes that M contains field OBJECT of type E.
     */
    public E tryGetSelected()
    {
        if (optionNoneSelected())
        {
            return null;
        }
        return GWTUtils.tryGetSingleSelected(this);
    }

    /**
     * @return true if anything has been selected. <br>
     *         Note that the result can be different from tryGetSelected() != null if there are null values in the model.
     */
    protected boolean isAnythingSelected()
    {
        if (optionNoneSelected())
        {
            return false;
        }
        return GWTUtils.tryGetSingleSelectedCode(this) != null;
    }

    private boolean valueNotInTheList()
    {
        return isEnabled() && getValue() == null && getRawValue() != null
                && getRawValue().equals("") == false
                && getRawValue().equals(getEmptyText()) == false && optionNoneSelected() == false;
    }

    private boolean clearInvalidAndCheckValueFromList()
    {
        clearInvalid();
        if (allowValueNotFromList == false && valueNotInTheList())
        {
            forceInvalid(valueNotInListMsg);
            return false;
        } else
        {
            return true;
        }
    }

    @Override
    public boolean isValid()
    {
        return clearInvalidAndCheckValueFromList() && super.isValid();
    }

    @Override
    public boolean validate()
    {
        return clearInvalidAndCheckValueFromList() && super.validate();
    }

    @Override
    protected void onRender(final Element target, final int index)
    {
        if (this.twinTriggerEnabled)
        {
            input = new El(DOM.createInputText());
            setElement(DOM.createDiv(), target, index);
            addStyleName("x-form-field-wrap");

            trigger = new El(DOM.createImg());
            trigger.dom.setClassName("x-form-trigger " + triggerStyle);
            trigger.dom.setPropertyString("src", GXT.BLANK_IMAGE_URL);

            twinTrigger = new El(DOM.createImg());
            twinTrigger.dom.setClassName("x-form-trigger " + twinTriggerStyle);
            twinTrigger.dom.setPropertyString("src", GXT.BLANK_IMAGE_URL);

            span = new El(DOM.createSpan());
            span.dom.setClassName("x-form-twin-triggers");

            span.appendChild(trigger.dom);
            span.appendChild(twinTrigger.dom);

            el().appendChild(input.dom);
            el().appendChild(span.dom);

            if (isHideTrigger())
            {
                span.setVisible(false);
            }

            addStyleOnOver(twinTrigger.dom, "x-form-trigger-over");
        }

        super.onRender(target, index);
        if (reloadWhenRendering)
        {
            refreshStore();
        }
    }

    @Override
    public void onComponentEvent(ComponentEvent ce)
    {
        super.onComponentEvent(ce);
        if (twinTriggerEnabled)
        {
            int type = ce.getEventTypeInt();
            if (ce.getTarget() == twinTrigger.dom && type == Event.ONCLICK)
            {
                onTwinTriggerClick(ce);
            }
        }
    }

    @Override
    protected Size adjustInputSize()
    {
        return twinTriggerEnabled ? new Size(isHideTrigger() ? 0
                : (trigger.getStyleSize().width + twinTrigger.getStyleSize().width), 0) : super
                .adjustInputSize();
    }

    protected void onTwinTriggerClick(ComponentEvent ce)
    {
        fireEvent(Events.TwinTriggerClick, ce);
    }

    public void addPostRefreshCallback(IDataRefreshCallback callback)
    {
        dataRefreshCallbacks.add(callback);
    }

    protected void updateOriginalValue()
    {
        setOriginalValue(getValue());
    }

    protected void trySelectByPropertyValue(String property, String propertyValue, String errorMsg)
    {
        if (allowValueNotFromList && GWTUtils.isPropertyNotInList(this, property, propertyValue))
        {
            setRawValue(propertyValue);
        } else
        {
            try
            {
                GWTUtils.setSelectedItem(this, property, propertyValue);
            } catch (IllegalArgumentException ex)
            {
                GWTUtils.alert("Error", errorMsg);
            }
        }
    }
}
