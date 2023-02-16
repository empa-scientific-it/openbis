/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;

/**
 * Renderer of {@link Boolean} value. Render <code>true</code> to <code>yes</code> and <code>false</code> to <code>no</code>.
 * 
 * @author Franz-Josef Elmer
 */
public final class YesNoRenderer implements GridCellRenderer<ModelData>
{

    @Override
    public Object render(ModelData model, String property, ColumnData config, int rowIndex,
            int colIndex, ListStore<ModelData> store, Grid<ModelData> grid)
    {
        Object value = model.get(property);
        if (value == null)
        {
            return "";
        }
        if (value instanceof Boolean == false)
        {
            return value.toString();
        }
        Boolean b = (Boolean) value;
        return SimpleYesNoRenderer.render(b.booleanValue());
    }
}
