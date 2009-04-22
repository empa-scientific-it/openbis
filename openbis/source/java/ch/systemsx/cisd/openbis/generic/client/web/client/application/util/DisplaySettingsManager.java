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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * Manager of {@link DisplaySettings}. The manager itself is stateless. It only changes the wrapped
 * {@link DisplaySettings} object.
 *
 * @author Franz-Josef Elmer
 */
public class DisplaySettingsManager
{
    private final DisplaySettings displaySettings;

    /**
     * Creates an instance for the specified display settings.
     */
    public DisplaySettingsManager(DisplaySettings displaySettings)
    {
        if (displaySettings == null)
        {
            throw new IllegalArgumentException("Unspecified display manager.");
        }
        this.displaySettings = displaySettings;
    }
    
    public void prepareGrid(final String id, final Grid<?> grid)
    {
        Listener<ColumnModelEvent> listener = new Listener<ColumnModelEvent>()
                {
                    public void handleEvent(ColumnModelEvent event)
                    {
                        updateColumnSettings(id, grid);
                    }
                };
        ColumnModel columnModel = grid.getColumnModel();
        columnModel.addListener(Events.HiddenChange, listener);
        columnModel.addListener(Events.WidthChange, listener);
        synchronizeColumnModel(id, grid);
    }
    
    /**
     * Synchronizes the {@link ColumnModel} of the specified grid with the {@link ColumnSetting}s.
     * The grid ID is used to get the appropriated column settings. If there are no settings found
     * nothing will be done.
     */
    private void synchronizeColumnModel(String id, Grid<?> grid)
    {
        List<ColumnSetting> columnSettings = displaySettings.getColumnSettings().get(id);
        if (columnSettings == null)
        {
            return;
        }
        boolean refreshNeeded = false;
        ColumnModel columnModel = grid.getColumnModel();
        for (ColumnSetting columnSetting : columnSettings)
        {
            ColumnConfig columnConfig = columnModel.getColumnById(columnSetting.getColumnID());
            if (columnConfig != null)
            {
                boolean hidden = columnSetting.isHidden();
                if (columnConfig.isHidden() != hidden)
                {
                    columnConfig.setHidden(hidden);
                    refreshNeeded = true;
                }
                int width = columnSetting.getWidth();
                if (columnConfig.getWidth() != width)
                {
                    columnConfig.setWidth(width);
                    refreshNeeded = true;
                }
            }
        }
        if (refreshNeeded)
        {
            GridView view = grid.getView();
            if (view != null && grid.isRendered())
            {
                view.refresh(true);
            }
        }
    }
    
    /**
     * Updates the column settings for the specified grid. The grid ID will be used to identify
     * its column settings in the method {@link #synchronizeColumnModel(Grid)}.
     */
    private void updateColumnSettings(String id, Grid<?> grid)
    {
        ColumnModel columnModel = grid.getColumnModel();
        List<ColumnSetting> columnSettings = new ArrayList<ColumnSetting>();
        for (int i = 0; i < columnModel.getColumnCount(); i++)
        {
            ColumnConfig columnConfig = columnModel.getColumn(i);
            ColumnSetting columnSetting = new ColumnSetting();
            columnSetting.setColumnID(columnConfig.getId());
            columnSetting.setHidden(columnConfig.isHidden());
            columnSetting.setWidth(columnConfig.getWidth());
            columnSettings.add(columnSetting);
        }
        displaySettings.getColumnSettings().put(id, columnSettings);
    }
}
