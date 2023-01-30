package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.ButtonGroup;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IParameterField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ParameterField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisplayTypeIDProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.TextColumnFilterWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextToolItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ColumnDistinctValues;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridColumnFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridFilters;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableGridColumnDefinition;

/**
 * Toolbar with filters.
 * 
 * @author Izabela Adamczyk
 */
public class FilterToolbar<T> extends ToolBar implements IDatabaseModificationObserver
{
    static final String APPLY_ID = "apply_button";

    static final String RESET_ID = "reset_button";

    private final IMessageProvider messageProvider;

    // stores the state of column filters, even if custom filter is currently used
    private final List<IColumnFilterWidget<T>> columnFilters;

    private final ContentPanel filterContainer;

    private final Collection<IParameterField> parameterFields; // parameters of custom filter

    private final FilterSelectionWidget filterSelectionWidget;

    private final IDelegatedAction applyFiltersAction;

    private final TextToolItem applyTool;

    private final TextToolItem resetTool;

    private boolean disableApply = false;

    // 6 filter fields fit into browser with 1024px width
    private final static int MAX_FILTER_FIELDS_COLUMNS = 6;

    private final static int MAX_FILTER_FIELDS_ROWS = 4;

    public final static int MAX_FILTER_FIELDS = MAX_FILTER_FIELDS_COLUMNS * MAX_FILTER_FIELDS_ROWS;

    private String gridId;

    public FilterToolbar(IViewContext<ICommonClientServiceAsync> viewContext, String gridId,
            IDisplayTypeIDProvider displayTypeIDProvider, IDelegatedAction applyFiltersAction)
    {
        int logID = viewContext.log("create filter toolbar for " + gridId);
        this.messageProvider = viewContext;
        this.columnFilters = new ArrayList<IColumnFilterWidget<T>>();
        this.applyFiltersAction = applyFiltersAction;
        add(new LabelToolItem(messageProvider.getMessage(Dict.FILTER) + ": "));
        filterSelectionWidget =
                new FilterSelectionWidget(viewContext, gridId, displayTypeIDProvider);
        filterContainer = new ButtonGroup(MAX_FILTER_FIELDS_COLUMNS);
        parameterFields = new HashSet<IParameterField>();
        add(filterSelectionWidget);
        add(filterContainer);
        applyTool = new TextToolItem(messageProvider.getMessage(Dict.APPLY_FILTER));
        applyTool.setId(createId(APPLY_ID, gridId));
        applyTool.setEnabled(false);
        applyTool.hide();
        add(applyTool);
        resetTool = new TextToolItem(messageProvider.getMessage(Dict.RESET_FILTER));
        resetTool.setId(createId(RESET_ID, gridId));
        add(resetTool);

        filterSelectionWidget
                .addSelectionChangedListener(new SelectionChangedListener<FilterModel>()
                    {
                        @Override
                        public void selectionChanged(SelectionChangedEvent<FilterModel> se)
                        {
                            updateFilterFields();
                            apply();
                        }

                    });
        filterSelectionWidget.addPostRefreshCallback(new IDataRefreshCallback()
            {
                @Override
                public void postRefresh(boolean wasSuccessful)
                {
                    updateFilterFields();
                    // Because of this code there is a reload of data visible on the client
                    // when user shows filter toolbar. It is quite fast as data are taken from cache
                    // but the loading mask is visible.
                    // On the other hand if we don't perform apply here column filters are not
                    // working properly. Somehow they are not refreshed correctly first load of
                    // data. To reproduce it show Database Instance and Code columns and filters in
                    // experiment detail sample browser:
                    // - Database Instance filter should be a combo box filter
                    // - Database Instance filter removes all letters on first usage (after refresh)
                    // - Code filter looses focus on first usage after refresh
                    // Refreshing the data fixes all problems.
                    apply();
                }
            });
        applyTool.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    apply();
                }
            });
        resetTool.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    resetFilterFields();
                    apply();
                }
            });
        viewContext.logStop(logID);
        this.gridId = gridId;
        setId(gridId + "-filter-toolbar");
    }

    public static String createId(String prefix, String gridId)
    {
        return GenericConstants.ID_PREFIX + prefix + gridId;
    }

    private boolean isColumnFilterSelected()
    {
        return isCustomFilterSelected() == false;
    }

    private boolean isCustomFilterSelected()
    {
        return getCustomFilterSelectedState(false);
    }

    // requiredState - true for column filter, false for custom filters
    /**
     *
     */
    private boolean getCustomFilterSelectedState(boolean requiredState)
    {
        GridCustomFilter selected = filterSelectionWidget.tryGetSelected();
        return (selected != null)
                && (selected.getName().equals(FilterModel.COLUMN_FILTER) == requiredState);
    }

    private CustomFilterInfo<T> tryGetCustomFilter()
    {
        if (isCustomFilterSelected() && isValid())
        {
            GridCustomFilter selected = filterSelectionWidget.tryGetSelected();
            CustomFilterInfo<T> info = new CustomFilterInfo<T>();
            info.setExpression(selected.getExpression());
            Set<ParameterWithValue> parameters = new HashSet<ParameterWithValue>();
            for (IParameterField parameterField : parameterFields)
            {
                parameters.add(parameterField.getParameterWithValue());
            }
            info.setParameters(parameters);
            return info;
        }
        return null;
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return filterSelectionWidget.getRelevantModifications();
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        filterSelectionWidget.update(observedModifications);
    }

    private void updateFilterFields()
    {
        // Only show the filter selection widget if there are user choices
        boolean isFilterSelectionWidgetVisible = filterSelectionWidget.getStore().getCount() > 1;
        filterSelectionWidget.setVisible(isFilterSelectionWidgetVisible);

        filterContainer.removeAll();
        if (isColumnFilterSelected())
        {
            updateFilterContainer(getColumnFilterWidgets(columnFilters));
            applyTool.hide();
        } else
        {
            GridCustomFilter filter = filterSelectionWidget.tryGetSelected();
            if (filter != null)
            {
                updateFilterContainer(createCustomFilterWidgets(filter));
                applyTool.show();
                updateApplyToolEnabledState();
            }
        }
        // don't show reset button if there are no fields to reset
        resetTool.setVisible(filterContainer.getItemCount() > 0);
    }

    private static <T> List<Widget> getColumnFilterWidgets(
            List<IColumnFilterWidget<T>> columnFilters)
    {
        List<Widget> filterWidgets = new ArrayList<Widget>();
        for (IColumnFilterWidget<T> filterWidget : columnFilters)
        {
            filterWidgets.add(filterWidget.getWidget());
        }
        return filterWidgets;
    }

    private List<Widget> createCustomFilterWidgets(GridCustomFilter filter)
    {
        List<Widget> filterWidgets = new ArrayList<Widget>();
        IDelegatedAction updateApplyButtonAction = new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateApplyToolEnabledState();
                }
            };
        parameterFields.clear();
        for (String parameter : filter.getParameters())
        {
            IParameterField parameterField =
                    ParameterField.create(null, parameter, null, updateApplyButtonAction, null);
            parameterFields.add(parameterField);
            filterWidgets.add(parameterField.asWidget());
        }
        return filterWidgets;
    }

    // Replaces current filter widgets with the new ones, but tries to make the minimal amount of
    // changes to avoid screen flickering.
    // Does not work great when a filter is added or removed (especially at the beginning), but
    // copes well with cases when one filter widget changes into another.
    private void updateFilterContainer(List<Widget> filterWidgets)
    {
        filterContainer.hide();
        boolean changed = false;
        filterContainer.disableEvents(true);
        for (int i = 0; i < filterWidgets.size(); i++)
        {
            Widget filterWidget = filterWidgets.get(i);
            if (i < filterContainer.getItemCount())
            {
                Widget previousItem = filterContainer.getItem(i);
                if (previousItem.equals(filterWidget) == false)
                {
                    filterContainer.remove(previousItem);
                    filterContainer.insert(filterWidget, i);
                    changed = true;
                }
            } else
            {
                filterContainer.add(filterWidget);
                changed = true;
            }
        }
        for (int i = filterWidgets.size(); i < filterContainer.getItemCount(); i++)
        {
            filterContainer.remove(filterContainer.getItem(i));
            changed = true;
        }
        filterContainer.enableEvents(true);
        if (filterContainer.getItemCount() > 0)
        {
            filterContainer.show();
        }
        if (changed)
        {
            // contentPanel of AbstractBrowserGrid was listening on this layout and synchronized
            // size
            layout(); // TODO 2010-04-16, Piotr Buczek: second layout
        }
    }

    public void resetFilterFields()
    {
        for (Component field : filterContainer.getItems())
        {
            if (field instanceof Field<?>)
            {
                ((Field<?>) field).reset();
            }
        }
        updateApplyToolEnabledState();
    }

    /** resets filter selection to column filter but doesn't invoke apply */
    public void resetFilterSelectionWithoutApply()
    {
        disableApply = true;
        filterSelectionWidget.reset();
        disableApply = false;
    }

    private void apply()
    {
        if (disableApply == false)
        {
            // NOTE: table will be refreshed without applying any filter if custom filter is invalid
            // - see tryGetCustomFilter()
            applyFiltersAction.execute();
        }
    }

    private void updateApplyToolEnabledState()
    {
        applyTool.setEnabled(isValid());
    }

    private boolean isValid()
    {
        if (isCustomFilterSelected())
        {
            boolean valid = true;
            for (IParameterField parameterField : parameterFields)
            {
                valid = parameterField.asWidget().isValid() && valid;
            }
            return valid;
        } else
        {
            return true; // column filters are always valid
        }
    }

    public void refreshAndReset()
    {
        refresh();
        resetFilterFields();
    }

    public void refresh()
    {
        filterSelectionWidget.refreshStore();
    }

    // ------------------------------------

    /** @return filters specified on this filter toolbar to be applied to the rows of the grid. */
    public GridFilters<T> getFilters()
    {
        if (isColumnFilterSelected())
        {
            return GridFilters.createColumnFilter(getColumnFiltersInfo());
        }
        CustomFilterInfo<T> customFilter = tryGetCustomFilter();
        if (customFilter != null)
        {
            return GridFilters.createCustomFilter(customFilter);
        }
        return GridFilters.createEmptyFilter();
    }

    // returns filters which user wants to apply to the data
    private List<GridColumnFilterInfo<T>> getColumnFiltersInfo()
    {
        List<GridColumnFilterInfo<T>> filters = new ArrayList<GridColumnFilterInfo<T>>();

        for (IColumnFilterWidget<T> filterWidget : columnFilters)
        {
            filters.add(filterWidget.getFilter());
        }
        return filters;
    }

    public void refreshColumnFiltersDistinctValues(List<ColumnDistinctValues> columnDistinctValues)
    {
        List<IColumnFilterWidget<T>> newColumnFilters = new ArrayList<IColumnFilterWidget<T>>();
        boolean refreshNeeded = false;
        for (IColumnFilterWidget<T> columnFilter : columnFilters)
        {
            ColumnDistinctValues distinctValues =
                    tryFindForColumn(columnFilter.getFilteredColumnId(), columnDistinctValues);
            List<String> valuesOrNull =
                    distinctValues == null ? null : distinctValues.getDistinctValues();
            IColumnFilterWidget<T> newFilter = columnFilter.createOrRefresh(valuesOrNull);
            refreshNeeded = refreshNeeded || newFilter != columnFilter;
            newColumnFilters.add(newFilter);
        }
        if (refreshNeeded)
        {
            updateColumnFilters(newColumnFilters);
        }
    }

    private static ColumnDistinctValues tryFindForColumn(String columnId,
            List<ColumnDistinctValues> columnDistinctValues)
    {
        for (ColumnDistinctValues columnValues : columnDistinctValues)
        {
            if (columnValues.getColumnIdentifier().equals(columnId))
            {
                return columnValues;
            }
        }
        return null;
    }

    /**
     * @return true if the toolbar with filters has just disappeared or appeared
     */
    public boolean rebuildColumnFilters(List<IColumnDefinition<T>> filteredColumns)
    {
        if (hasFilteredColumnsChanged(filteredColumns, this.columnFilters))
        {
            List<IColumnFilterWidget<T>> newColumnFilters =
                    createColumnFilterWidgets(pruneIfNecessary(filteredColumns),
                            this.columnFilters, applyFiltersAction, gridId);
            updateColumnFilters(newColumnFilters);
            return true;
        } else
        {
            return false;
        }
    }

    // WORKAROUND to broken layout if more than 4 rows with filter fields are shown
    private List<IColumnDefinition<T>> pruneIfNecessary(List<IColumnDefinition<T>> filteredColumns)
    {
        if (filteredColumns.size() > MAX_FILTER_FIELDS)
        {
            GWTUtils.alert("Warning", "Only up to " + MAX_FILTER_FIELDS
                    + " column filters can be used at the same time for one table.");
            return filteredColumns.subList(0, MAX_FILTER_FIELDS);
        } else
        {
            return filteredColumns;
        }
    }

    private void updateColumnFilters(List<IColumnFilterWidget<T>> newColumnFilters)
    {
        this.columnFilters.clear();
        this.columnFilters.addAll(newColumnFilters);
        updateFilterFields();
    }

    private boolean hasFilteredColumnsChanged(List<IColumnDefinition<T>> filters1,
            List<IColumnFilterWidget<T>> filters2)
    {
        if (filters1.size() != filters2.size())
        {
            return true;
        }
        for (int i = 0; i < filters1.size(); i++)
        {
            IColumnDefinition<T> filter1 = filters1.get(i);
            IColumnDefinition<T> filter2 = filters2.get(i).getFilter().getFilteredField();
            if (filter1.getIdentifier().equals(filter2.getIdentifier()) == false)
            {
                return true;
            }
            if (filter1 instanceof TypedTableGridColumnDefinition
                    && filter2 instanceof TypedTableGridColumnDefinition)
            {
                TypedTableGridColumnDefinition<?> coldef1 =
                        (TypedTableGridColumnDefinition<?>) filter1;
                TypedTableGridColumnDefinition<?> coldef2 =
                        (TypedTableGridColumnDefinition<?>) filter2;
                if (coldef1.getIndex() != coldef2.getIndex())
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static <T> List<IColumnFilterWidget<T>> createColumnFilterWidgets(
            List<IColumnDefinition<T>> availableFilters,
            List<IColumnFilterWidget<T>> previousColumnFilters, IDelegatedAction onFilterAction,
            String gridId)
    {
        List<IColumnFilterWidget<T>> filterWidgets = new ArrayList<IColumnFilterWidget<T>>();
        for (IColumnDefinition<T> columnDefinition : availableFilters)
        {
            IColumnFilterWidget<T> filterWidget =
                    tryFindColumnFilter(columnDefinition.getIdentifier(), previousColumnFilters);
            if (filterWidget == null)
            {
                // we do not have distinct values in the columns at this moment, so plain filter
                // widget is always created
                filterWidget =
                        new TextColumnFilterWidget<T>(columnDefinition, onFilterAction, gridId);
            } else
            {
                filterWidget.setFilteredField(columnDefinition);
            }
            filterWidgets.add(filterWidget);
        }
        return filterWidgets;
    }

    private static <T> IColumnFilterWidget<T> tryFindColumnFilter(String columnId,
            List<IColumnFilterWidget<T>> columnFilters)
    {
        for (IColumnFilterWidget<T> columnFilter : columnFilters)
        {
            if (columnFilter.getFilteredColumnId().equals(columnId))
            {
                return columnFilter;
            }
        }
        return null;
    }

    /** @return ids of columns which have filtering switched on at the column filters toolbar */
    public List<String> extractFilteredColumnIds()
    {
        return extractColumnIds(columnFilters);
    }

    private static <T> List<String> extractColumnIds(List<IColumnFilterWidget<T>> columns)
    {
        List<String> filteredColumnsIds = new ArrayList<String>();
        for (IColumnFilterWidget<T> column : columns)
        {
            filteredColumnsIds.add(column.getFilteredColumnId());
        }
        return filteredColumnsIds;
    }

}
