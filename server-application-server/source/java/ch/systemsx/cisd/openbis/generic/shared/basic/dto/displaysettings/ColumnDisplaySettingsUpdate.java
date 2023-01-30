/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * A command that updates column settings for a one specified grid.
 * 
 * @author pkupczyk
 */
public class ColumnDisplaySettingsUpdate implements IDisplaySettingsUpdate
{

    private static final long serialVersionUID = 1L;

    private String displayTypeId;

    private List<ColumnSetting> columnSettings;

    // GWT
    @SuppressWarnings("unused")
    private ColumnDisplaySettingsUpdate()
    {
    }

    public ColumnDisplaySettingsUpdate(String displayTypeId, List<ColumnSetting> columnSettings)
    {
        if (displayTypeId == null)
        {
            throw new IllegalArgumentException("Display type id cannot be null");
        }
        this.displayTypeId = displayTypeId;
        this.columnSettings = columnSettings;
    }

    @SuppressWarnings("deprecation")
    @Override
    public DisplaySettings update(DisplaySettings displaySettings)
    {
        displaySettings.getColumnSettings().put(displayTypeId, columnSettings);
        return displaySettings;
    }

    @Override
    public String toString()
    {
        return "update of column settings for: " + displayTypeId;
    }
}
