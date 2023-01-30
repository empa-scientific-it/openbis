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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetArchivingMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.IComputationAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.SelectedOrAllExperimentsRadioProvider.ISelectedExperimentsProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextToolItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ArchivingResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedCriteriaOrSelectedEntityHolder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchivingServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * 'Archiving' menu for data sets connected to experiments. Here, experiments are the central entity, as opposed to {@link DataSetArchivingMenu},
 * where data sets are the central entity.
 * 
 * @author Piotr Buczek
 * @author Chandrasekhar Ramakrishnan
 */
public class ExperimentDataSetArchivingMenu extends TextToolItem
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final IDelegatedActionWithResult<SelectedAndDisplayedItems> selectedDataSetsGetter;

    private final IDelegatedAction postArchivingAction;

    public ExperimentDataSetArchivingMenu(IViewContext<ICommonClientServiceAsync> viewContext,
            IDelegatedActionWithResult<SelectedAndDisplayedItems> selectedDataSetsGetter,
            IDelegatedAction postArchivingAction)
    {
        super(viewContext.getMessage(Dict.MENU_ARCHIVING));
        this.viewContext = viewContext;
        this.postArchivingAction = postArchivingAction;
        this.selectedDataSetsGetter = selectedDataSetsGetter;

        Menu submenu = new Menu();
        addMenuItem(submenu, ArchivingActionMenuKind.ARCHIVING_MENU_BACKUP);
        addMenuItem(submenu, ArchivingActionMenuKind.ARCHIVING_MENU_ARCHIVE);
        addMenuItem(submenu, ArchivingActionMenuKind.ARCHIVING_MENU_UNARCHIVE);
        addMenuItem(submenu, ArchivingActionMenuKind.ARCHIVING_MENU_LOCK);
        addMenuItem(submenu, ArchivingActionMenuKind.ARCHIVING_MENU_UNLOCK);
        setMenu(submenu);
    }

    /** {@link ActionMenu} kind enum with names matching dictionary keys */
    private static enum ArchivingActionMenuKind implements IActionMenuItem
    {
        ARCHIVING_MENU_BACKUP(ArchivingServiceKind.BACKUP),
        ARCHIVING_MENU_ARCHIVE(ArchivingServiceKind.ARCHIVE), ARCHIVING_MENU_UNARCHIVE(
                ArchivingServiceKind.UNARCHIVE), ARCHIVING_MENU_LOCK(ArchivingServiceKind.LOCK),
        ARCHIVING_MENU_UNLOCK(ArchivingServiceKind.UNLOCK);

        private final ArchivingServiceKind taskKind;

        ArchivingActionMenuKind(ArchivingServiceKind taskKind)
        {
            this.taskKind = taskKind;
        }

        public ArchivingServiceKind getDssTaskKind()
        {
            return taskKind;
        }

        @Override
        public String getMenuId()
        {
            return this.name();
        }

        @Override
        public String getMenuText(IMessageProvider messageProvider)
        {
            return messageProvider.getMessage(this.name());
        }
    }

    private final void addMenuItem(Menu submenu, ArchivingActionMenuKind menuItemKind)
    {
        final IDelegatedAction menuItemAction =
                createArchivingMenuAction(menuItemKind.getDssTaskKind());
        submenu.add(new ActionMenu(menuItemKind, viewContext, menuItemAction));
    }

    //

    private IDelegatedAction createArchivingMenuAction(final ArchivingServiceKind taskKind)
    {
        return new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    final SelectedAndDisplayedItems selectedAndDisplayedItems =
                            selectedDataSetsGetter.execute();
                    final IComputationAction computationAction =
                            createComputationAction(selectedAndDisplayedItems, taskKind);
                    final ComputationData data =
                            new ComputationData(taskKind, computationAction,
                                    selectedAndDisplayedItems);
                    createPerformComputationDialog(data).show();
                }

                private Window createPerformComputationDialog(ComputationData data)
                {
                    final String title = "Perform " + taskKind.getDescription();
                    return new PerformArchivingDialog(viewContext, data, title);
                }
            };
    }

    private static class ComputationData implements ISelectedExperimentsProvider
    {
        private final ArchivingServiceKind taskKind;

        private final IComputationAction computationAction;

        private final SelectedAndDisplayedItems selectedAndDisplayedItems;

        public ComputationData(ArchivingServiceKind taskKind, IComputationAction computationAction,
                SelectedAndDisplayedItems selectedAndDisplayedItems)
        {
            super();
            this.taskKind = taskKind;
            this.computationAction = computationAction;
            this.selectedAndDisplayedItems = selectedAndDisplayedItems;
        }

        public ArchivingServiceKind getTaskKind()
        {
            return taskKind;
        }

        public IComputationAction getComputationAction()
        {
            return computationAction;
        }

        @Override
        public List<Experiment> getSelectedExperiments()
        {
            List<TableModelRowWithObject<Experiment>> items = selectedAndDisplayedItems.getSelectedItems();
            List<Experiment> experiments = new ArrayList<Experiment>();
            for (TableModelRowWithObject<Experiment> row : items)
            {
                experiments.add(row.getObjectOrNull());
            }
            return experiments;
        }
    }

    private IComputationAction createComputationAction(
            final SelectedAndDisplayedItems selectedAndDisplayedItems,
            final ArchivingServiceKind taskKind)
    {
        return new IComputationAction()
            {
                @Override
                public void execute(DatastoreServiceDescription service, boolean computeOnSelected)
                {
                    DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> criteria =
                            selectedAndDisplayedItems.createCriteria(computeOnSelected);
                    switch (taskKind)
                    {
                        case BACKUP:
                            viewContext.getService().archiveDatasets(
                                    criteria, false,
                                    createArchivingDisplayCallback(taskKind.getDescription(),
                                            computeOnSelected));
                            break;
                        case ARCHIVE:
                            viewContext.getService().archiveDatasets(
                                    criteria, true,
                                    createArchivingDisplayCallback(taskKind.getDescription(),
                                            computeOnSelected));
                            break;
                        case UNARCHIVE:
                            viewContext.getService().unarchiveDatasets(
                                    criteria,
                                    createArchivingDisplayCallback(taskKind.getDescription(),
                                            computeOnSelected));
                            break;
                        case LOCK:
                            viewContext.getService().lockDatasets(
                                    criteria,
                                    createArchivingDisplayCallback(taskKind.getDescription(),
                                            computeOnSelected));
                            break;
                        case UNLOCK:
                            viewContext.getService().unlockDatasets(
                                    criteria,
                                    createArchivingDisplayCallback(taskKind.getDescription(),
                                            computeOnSelected));
                            break;
                    }
                }
            };
    }

    private AsyncCallback<ArchivingResult> createArchivingDisplayCallback(String actionName,
            boolean computeOnSelected)
    {
        return AsyncCallbackWithProgressBar.decorate(new ArchivingDisplayCallback(viewContext,
                actionName, computeOnSelected), "Scheduling " + actionName + "...");
    }

    private final class ArchivingDisplayCallback extends AbstractAsyncCallback<ArchivingResult>
    {
        private final String actionName;

        private final boolean computeOnSelected;

        private ArchivingDisplayCallback(IViewContext<?> viewContext, String actionName,
                boolean computeOnSelected)
        {
            super(viewContext);
            this.actionName = actionName;
            this.computeOnSelected = computeOnSelected;
        }

        @Override
        public final void process(final ArchivingResult result)
        {
            final String source = computeOnSelected ? "selected" : "provided";
            if (result.getScheduled() == 0)
            {
                MessageBox.info(actionName, actionName + " couldn't be performed on " + source
                        + " data set(s).", null);
            } else
            {
                boolean subset = result.getProvided() > result.getScheduled();
                MessageBox.info(actionName, actionName + " has been scheduled on "
                        + (subset ? "a subset of " : "all ") + source + " data set(s).", null);
                postArchivingAction.execute();
            }
        }

    }

    private static class PerformArchivingDialog extends
            AbstractDataConfirmationDialog<ComputationData>
    {
        private static final int LABEL_WIDTH = ColumnConfigFactory.DEFAULT_COLUMN_WIDTH - 20;

        private static final int FIELD_WIDTH = 2 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH - 20;

        private static final int DIALOG_WIDTH = 4 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH + 30;

        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final SelectedOrAllExperimentsRadioProvider radioProvider;

        protected PerformArchivingDialog(IViewContext<ICommonClientServiceAsync> viewContext,
                ComputationData data, String title)
        {
            super(viewContext, data, title);
            this.viewContext = viewContext;
            this.radioProvider = new SelectedOrAllExperimentsRadioProvider(viewContext, data);

            setWidth(DIALOG_WIDTH);
        }

        @Override
        protected String createMessage()
        {
            int size = data.getSelectedExperiments().size();
            String computationName = data.getTaskKind().getDescription();
            String requiredStatusName = getRequiredStatus(data.getTaskKind()).getDescription();
            if (size == 0)
            {
                final String msgIntroduction = viewContext.getMessage(Dict.NO_DATASETS_SELECTED);
                String dictKey =
                        Dict.PERFORM_ARCHIVING_ON_ALL_DATASETS_CONNECTED_TO_EXPERIMENTS_MSG_TEMPLATE;
                return viewContext.getMessage(dictKey, msgIntroduction, computationName,
                        requiredStatusName);
            } else
            {
                String dictKey =
                        Dict.PERFORM_ARCHIVING_ON_SELECTED_OR_ALL_DATASETS_CONNECTED_TO_EXPERIMENTS_MSG_TEMPLATE;
                return viewContext.getMessage(dictKey, computationName, size, requiredStatusName);
            }
        }

        private DataSetArchivingStatus getRequiredStatus(ArchivingServiceKind taskKind)
        {
            switch (taskKind)
            {
                case BACKUP:
                    return DataSetArchivingStatus.AVAILABLE;
                case ARCHIVE:
                    return DataSetArchivingStatus.AVAILABLE;
                case UNARCHIVE:
                    return DataSetArchivingStatus.ARCHIVED;
                case LOCK:
                    return DataSetArchivingStatus.AVAILABLE;
                case UNLOCK:
                    return DataSetArchivingStatus.LOCKED;
                default:
                    return null; // not possible
            }
        }

        @Override
        protected void executeConfirmedAction()
        {
            final IComputationAction computationAction = data.getComputationAction();
            final boolean computeOnSelected = getComputeOnSelected();
            computationAction.execute(null, computeOnSelected);
        }

        @Override
        protected void extendForm()
        {
            formPanel.setLabelWidth(LABEL_WIDTH);
            formPanel.setFieldWidth(FIELD_WIDTH);

            if (data.getSelectedExperiments().size() > 0)
            {
                formPanel.add(createComputationDataSetsRadio());
            }
            Button confirmButton = getButtonById(Dialog.OK);
            confirmButton.setText("Run");
        }

        private final RadioGroup createComputationDataSetsRadio()
        {
            return radioProvider.createComputationDataSetsRadio();
        }

        private boolean getComputeOnSelected()
        {
            return radioProvider.getComputeOnSelected();
        }
    }

    public final static class SelectedAndDisplayedItems
    {
        // describes all items which are displayed in the grid (including all grid pages)
        private final TableExportCriteria<TableModelRowWithObject<Experiment>> displayedItemsConfig;

        // currently selected items
        private final List<TableModelRowWithObject<Experiment>> selectedItems;

        private final int displayedItemsCount;

        public SelectedAndDisplayedItems(List<TableModelRowWithObject<Experiment>> selectedItems,
                TableExportCriteria<TableModelRowWithObject<Experiment>> displayedItemsConfig, int displayedItemsCount)
        {
            this.displayedItemsConfig = displayedItemsConfig;
            this.selectedItems = selectedItems;
            // this.selectedItems = new ArrayList<Experiment>();
            // for (TableModelRowWithObject<Experiment> row : selectedItems)
            // {
            // this.selectedItems.add(row.getObjectOrNull());
            // }
            this.displayedItemsCount = displayedItemsCount;
        }

        public TableExportCriteria<TableModelRowWithObject<Experiment>> getDisplayedItemsConfig()
        {
            return displayedItemsConfig;
        }

        public int getDisplayedItemsCount()
        {
            return displayedItemsCount;
        }

        public List<TableModelRowWithObject<Experiment>> getSelectedItems()
        {
            return selectedItems;
        }

        public DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> createCriteria(boolean selected)
        {
            if (selected)
            {
                return DisplayedCriteriaOrSelectedEntityHolder.createSelectedItems(getSelectedItems());
            } else
            {
                return DisplayedCriteriaOrSelectedEntityHolder
                        .createDisplayedItems(getDisplayedItemsConfig());
            }
        }
    }

}
