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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;

/**
 * The toolbar of experiment browser.
 * 
 * @author Izabela Adamczyk
 * @author Christian Ribeaud
 */
class ExperimentBrowserToolbar extends ToolBar
{
    public static final String ID = "experiment-browser-toolbar";

    private final ExperimentTypeSelectionWidget selectExperimentTypeCombo;

    private final ProjectSelectionWidget selectProjectCombo;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public ExperimentBrowserToolbar(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        selectExperimentTypeCombo = new ExperimentTypeSelectionWidget(viewContext, ID);
        selectProjectCombo = new ProjectSelectionWidget(viewContext, ID);
        display();
    }

    public void setCriteriaChangedListener(SelectionChangedListener<?> criteriaChangedListener)
    {
        selectProjectCombo.addSelectionChangedListener(criteriaChangedListener);
        selectExperimentTypeCombo.addSelectionChangedListener(criteriaChangedListener);
    }

    private void display()
    {
        setBorders(true);
        add(new LabelToolItem(viewContext.getMessage(Dict.EXPERIMENT_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        add(new AdapterToolItem(selectExperimentTypeCombo));
        add(new SeparatorToolItem());
        add(new LabelToolItem(viewContext.getMessage(Dict.PROJECT)
                + GenericConstants.LABEL_SEPARATOR));
        add(new AdapterToolItem(selectProjectCombo));
    }

    public final ListExperimentsCriteria tryGetCriteria()
    {
        final ExperimentType selectedType =
                selectExperimentTypeCombo.tryGetSelectedExperimentType();
        if (selectedType == null)
        {
            return null;
        }
        final Project selectedProject = selectProjectCombo.tryGetSelectedProject();
        if (selectedProject == null)
        {
            return null;
        }
        ListExperimentsCriteria criteria = new ListExperimentsCriteria();
        criteria.setExperimentType(selectedType);
        criteria.setProjectCode(selectedProject.getCode());
        criteria.setGroupCode(selectedProject.getGroup().getCode());
        return criteria;
    }

    @Override
    protected final void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
    }

}