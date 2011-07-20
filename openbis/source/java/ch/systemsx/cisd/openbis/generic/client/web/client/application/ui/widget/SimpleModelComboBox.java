package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * A simple combobox which provides a model class {@link LabeledItem} to store any value and the its
 * label.
 * 
 * @author Tomasz Pylak
 */
public class SimpleModelComboBox<T> extends SimpleComboBox<LabeledItem<T>>
{

    /**
     * Creates a combobox and selects the first value. For each item there should be one
     * corresponding label.
     */
    public SimpleModelComboBox(IMessageProvider messageProvider, List<T> items,
            List<String> labels, List<String> tooltips, int widthPx)
    {
        this(messageProvider, createModelItems(items, labels, tooltips), widthPx);
    }

    /**
     * Creates a combobox with a specified model.
     */
    public SimpleModelComboBox(IMessageProvider messageProvider, List<LabeledItem<T>> model,
            int widthPx)
    {
        configure(messageProvider, widthPx);
        setModel(model);
        GWTUtils.autoselect(this);
    }

    private void configure(IMessageProvider messageProvider, int widthPx)
    {
        setTriggerAction(TriggerAction.ALL);
        setAllowBlank(false);
        setEditable(false);
        setEmptyText(messageProvider.getMessage(Dict.COMBO_BOX_CHOOSE));
        setWidth("" + widthPx);
        setTemplate(GWTUtils.getTooltipTemplate(LabeledItem.LABEL_FIELD, LabeledItem.TOOLTIP_FIELD));
    }

    private static <T> List<LabeledItem<T>> createModelItems(List<T> items, List<String> labels,
            List<String> tooltips)
    {
        assert items.size() == labels.size() : "for each item there should be one corresponding label";
        assert items.size() == tooltips.size() : "for each item there should be one corresponding tooltip";

        List<LabeledItem<T>> model = new ArrayList<LabeledItem<T>>();
        int i = 0;
        for (T item : items)
        {
            model.add(new LabeledItem<T>(item, labels.get(i), tooltips.get(i)));
            i++;
        }
        return model;
    }

    private void setModel(List<LabeledItem<T>> modelItems)
    {
        int i = 0;
        for (LabeledItem<T> item : modelItems)
        {
            add(item);
            i++;
        }

        for (int j = 0; j < getStore().getCount(); j++)
        {
            SimpleComboValue<LabeledItem<T>> item = getStore().getAt(j);
            item.set(LabeledItem.LABEL_FIELD, item.getValue().toString());
            item.set(LabeledItem.TOOLTIP_FIELD, item.getValue().getTooltip());
        }
    }

    public T getChosenItem()
    {
        return getSimpleValue().getItem();
    }

    public static <T> T getChosenItem(SelectionChangedEvent<SimpleComboValue<LabeledItem<T>>> se)
    {
        return se.getSelectedItem().getValue().getItem();
    }
}
