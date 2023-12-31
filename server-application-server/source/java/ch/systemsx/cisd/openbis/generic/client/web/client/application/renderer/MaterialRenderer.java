/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext.ClientStaticState;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Pawel Glyzewski
 */
public class MaterialRenderer implements GridCellRenderer<BaseEntityModel<?>>
{

    private int columnIndex;

    public MaterialRenderer(int columnIndex)
    {
        this.columnIndex = columnIndex;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Object render(BaseEntityModel<?> model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
            Grid<BaseEntityModel<?>> grid)
    {
        if (model.get(property) == null)
        {
            return "";
        } else
        {
            String originalValue = model.get(property).toString();
            String tokenOrNull = model.tryGetLink(property);
            // We can not use colIndex because order of visible columns is often different from the
            // order in the model.
            List<ISerializableComparable> values =
                    ((TableModelRowWithObject<?>) model.getBaseObject()).getValues();
            if (columnIndex < values.size())
            {
                EntityTableCell newEntityCell =
                        new EntityTableCell(EntityKind.MATERIAL, originalValue, originalValue);
                values.set(columnIndex, newEntityCell);
            }
            if (tokenOrNull == null && (ClientStaticState.isSimpleMode()))
            {
                return new MultilineHTML(originalValue).toString();
            } else
            {
                if (ClientStaticState.isSimpleMode())
                {
                    String href = "#" + tokenOrNull;
                    return LinkRenderer.renderAsLinkWithAnchor(originalValue, href, false);
                } else
                {
                    return LinkRenderer.renderAsLinkWithAnchor(originalValue);
                }
            }
        }
    }
}
