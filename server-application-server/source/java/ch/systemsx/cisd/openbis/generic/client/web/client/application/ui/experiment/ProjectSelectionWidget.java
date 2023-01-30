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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * {@link ComboBox} containing list of projects loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class ProjectSelectionWidget extends
        DropDownList<ProjectSelectionWidget.ProjectComboModel, TableModelRowWithObject<Project>>
{
    private static final String EMPTY_RESULT_SUFFIX = "projects";

    private static final String CHOOSE_SUFFIX = "project";

    // @Private
    static final String DISPLAY_COLUMN_ID = "id";

    public static class ProjectComboModel extends SimplifiedBaseModelData
    {
        private static final long serialVersionUID = 1L;

        public ProjectComboModel(Project project, boolean withGroup)
        {
            if (withGroup)
            {
                set(DISPLAY_COLUMN_ID, renderProjectWithGroup(project));
            } else
            {
                set(DISPLAY_COLUMN_ID, renderProject(project));
            }
            set(ModelDataPropertyNames.PROJECT_IDENTIFIER, project.getIdentifier());
            set(ModelDataPropertyNames.OBJECT, project);
        }

        private String renderProjectWithGroup(final Project p)
        {
            return p.getCode() + " (" + p.getSpace().getCode() + ")";
        }

        private String renderProject(final Project p)
        {
            return p.getCode();
        }
    }

    public static final String SUFFIX = CHOOSE_SUFFIX;

    private final IViewContext<?> viewContext;

    private final String spaceCodeOrNull;

    private String initialProjectIdentifierOrNull;

    private boolean dataLoaded = false;

    public ProjectSelectionWidget(final IViewContext<?> viewContext, final String idSuffix)
    {
        this(viewContext, idSuffix, null, null);
    }

    public ProjectSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            String initialProjectIdentifier)
    {
        this(viewContext, idSuffix, null, initialProjectIdentifier);
    }

    public ProjectSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            String spaceCodeOrNull, String initialProjectIdentifier)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.PROJECT, DISPLAY_COLUMN_ID, CHOOSE_SUFFIX,
                EMPTY_RESULT_SUFFIX);
        this.viewContext = viewContext;
        this.spaceCodeOrNull = spaceCodeOrNull;
        this.initialProjectIdentifierOrNull = initialProjectIdentifier;
    }

    /**
     * Returns the {@link Project} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final Project tryGetSelectedProject()
    {
        Object selected = super.tryGetSelected();
        return selected instanceof Project ? (Project) selected : null;
    }

    private final class ListProjectsCallback extends
            AbstractAsyncCallback<TypedTableResultSet<Project>>
    {
        ListProjectsCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final TypedTableResultSet<Project> result)
        {
            resultSetKey = result.getResultSet().getResultSetKey();
            final ListStore<ProjectComboModel> projectStore = getStore();
            projectStore.removeAll();
            projectStore
                    .add(convertItems(result.getResultSet().getList().extractOriginalObjects()));
            if (projectStore.getCount() > 0)
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, CHOOSE_SUFFIX));
                setReadOnly(false);
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, EMPTY_RESULT_SUFFIX));
                setReadOnly(true);
            }
            applyEmptyText();
            dataLoaded = true;

            if (initialProjectIdentifierOrNull != null)
            {
                selectProjectAndUpdateOriginal(initialProjectIdentifierOrNull);
            } else
            {
                selectProjectAndUpdateOriginal(viewContext.getDisplaySettingsManager().getDefaultProject());
            }
            removeResultSetFromCache();
        }

    }

    public void selectProjectAndUpdateOriginal(String newProjectIdentifier)
    {
        this.initialProjectIdentifierOrNull = newProjectIdentifier;
        if (dataLoaded && initialProjectIdentifierOrNull != null)
        {
            trySelectByIdentifier(initialProjectIdentifierOrNull);
            updateOriginalValue();
        }
    }

    @Override
    protected List<ProjectComboModel> convertItems(List<TableModelRowWithObject<Project>> projects)
    {
        final List<ProjectComboModel> result = new ArrayList<ProjectComboModel>();
        for (final TableModelRowWithObject<Project> p : projects)
        {
            if (matchesTheGroup(p.getObjectOrNull()))
            {
                result.add(new ProjectComboModel(p.getObjectOrNull(), spaceCodeOrNull == null));
            }
        }
        return result;
    }

    private boolean matchesTheGroup(Project project)
    {
        if (spaceCodeOrNull == null)
        {
            return true;
        }
        Space projectGroup = project.getSpace();
        return projectGroup.getCode().equals(spaceCodeOrNull);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<TableModelRowWithObject<Project>>> callback)
    {
        DefaultResultSetConfig<String, TableModelRowWithObject<Project>> config =
                DefaultResultSetConfig.createFetchAll();
        viewContext.getCommonService().listProjects(config, new ListProjectsCallback(viewContext));
        callback.ignore();
    }

    public void trySelectByIdentifier(String projectIdentifier)
    {
        List<ProjectComboModel> projects = getStore().findModels(ModelDataPropertyNames.PROJECT_IDENTIFIER, projectIdentifier);

        if (projects != null && projects.size() > 0)
        {
            GWTUtils.setSelectedItem(this, ModelDataPropertyNames.PROJECT_IDENTIFIER, projectIdentifier);
        } else
        {
            setValue(null);
        }
    }

    public String getSpaceCodeOrNull()
    {
        return spaceCodeOrNull;
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.PROJECT);
    }

}
