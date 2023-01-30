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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SpaceModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * {@link ComboBox} containing list of {@link Space} instances loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public class SpaceSelectionWidget extends DropDownList<SpaceModel, Space>
{

    public static final String SUFFIX = "group-select";

    private final IViewContext<?> viewContext;

    private String initialSpaceOrNull;

    public static final boolean isSharedSpace(Space g)
    {
        return SHARED_SPACE_CODE.equals(g.getCode());
    }

    public static final boolean isAllSpaces(Space g)
    {
        return ALL_SPACES_CODE.equals(g.getCode());
    }

    public static final String tryToGetSpaceCode(Space space)
    {
        String code = space.getCode();
        return code.equals(ALL_SPACES_CODE) ? null : code;
    }

    public static final String SHARED_SPACE_CODE = "(Shared)";

    public static final String ALL_SPACES_CODE = "(all)";

    private final boolean addShared;

    public boolean dataLoaded = false;

    private final boolean addAll;

    public SpaceSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            boolean addShared, boolean addAll)
    {
        this(viewContext, idSuffix, addShared, addAll, null);
    }

    public SpaceSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            boolean addShared, boolean addAll, final String initialSpaceCodeOrNull)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.GROUP, ModelDataPropertyNames.CODE, viewContext
                .getMessage(Dict.GROUP), viewContext.getMessage(Dict.GROUP));
        this.viewContext = viewContext;
        this.addShared = addShared;
        this.addAll = addAll;
        this.initialSpaceOrNull = initialSpaceCodeOrNull;
    }

    /**
     * Returns the {@link Space} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final Space tryGetSelectedSpace()
    {
        return super.tryGetSelected();
    }

    private Space createSharedSpace()
    {
        final Space space = new Space();
        space.setCode(SHARED_SPACE_CODE);
        space.setIdentifier("/");
        return space;
    }

    private Space createAllSpaces()
    {
        Space space = new Space();
        space.setCode(ALL_SPACES_CODE);
        return space;
    }

    private final class ListSpaceCallback extends AbstractAsyncCallback<TypedTableResultSet<Space>>
    {

        ListSpaceCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final TypedTableResultSet<Space> result)
        {
            resultSetKey = result.getResultSet().getResultSetKey();
            final ListStore<SpaceModel> spaceStore = getStore();
            spaceStore.removeAll();
            if (addShared)
            {
                spaceStore.add(new SpaceModel(createSharedSpace()));
            }
            if (addAll)
            {
                spaceStore.add(new SpaceModel(createAllSpaces()));
            }
            List<TableModelRowWithObject<Space>> tableRows =
                    result.getResultSet().getList().extractOriginalObjects();
            List<Space> spaces = new ArrayList<Space>();
            for (TableModelRowWithObject<Space> tableModelRowWithObject : tableRows)
            {
                spaces.add(tableModelRowWithObject.getObjectOrNull());
            }
            spaceStore.add(convertItems(spaces));
            dataLoaded = true;
            if (spaceStore.getCount() > 0)
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE,
                        viewContext.getMessage(Dict.GROUP)));
                setReadOnly(false);
                if (initialSpaceOrNull != null)
                {
                    selectSpaceAndUpdateOriginal(initialSpaceOrNull);
                } else
                {
                    final int homeGroupIndex = getSpaceGroupIndex(spaceStore);
                    if (homeGroupIndex > -1)
                    {
                        setValue(spaceStore.getAt(homeGroupIndex));
                        setOriginalValue(getValue());
                    }
                }
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY,
                        viewContext.getMessage(Dict.GROUPS)));
                setReadOnly(true);
            }
            removeResultSetFromCache();
        }

        int getSpaceGroupIndex(ListStore<SpaceModel> spaceStore)
        {
            final SessionContext sessionContext = viewContext.getModel().getSessionContext();
            final User user = sessionContext.getUser();
            final String homeSpace = user.getHomeGroupCode();
            if (homeSpace != null)
            {
                for (int i = 0; i < spaceStore.getCount(); i++)
                {
                    if (spaceStore.getAt(i).get(ModelDataPropertyNames.CODE).equals(homeSpace))
                    {
                        return i;
                    }
                }
            }
            return -1;
        }
    }

    public void selectSpaceAndUpdateOriginal(String space)
    {
        initialSpaceOrNull = space;
        if (dataLoaded && initialSpaceOrNull != null)
        {
            try
            {
                GWTUtils.setSelectedItem(SpaceSelectionWidget.this, ModelDataPropertyNames.CODE,
                        initialSpaceOrNull);
            } catch (IllegalArgumentException ex)
            {
                GWTUtils.alert("Error", "Space '" + space + "' doesn't exist.");
            }
            updateOriginalValue();
        }
    }

    @Override
    protected List<SpaceModel> convertItems(List<Space> result)
    {
        return SpaceModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<Space>> callback)
    {
        DefaultResultSetConfig<String, TableModelRowWithObject<Space>> config =
                DefaultResultSetConfig.createFetchAll();
        viewContext.getCommonService().listSpaces(config, new ListSpaceCallback(viewContext));
        callback.ignore();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.SPACE);
    }
}
