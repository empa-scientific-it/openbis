/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer.VocabularyPropertyColRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermTableCell;

public class VocabularyTermStringCellRenderer implements GridCellRenderer<BaseEntityModel<?>>
{

    private final int columnIndex;

    public VocabularyTermStringCellRenderer(int columnIndex)
    {
        this.columnIndex = columnIndex;
    }

    @Override
    public Object render(BaseEntityModel<?> model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
            Grid<BaseEntityModel<?>> grid)
    {
        Object obj = model.get(property);
        String originalValue = obj == null ? null : obj.toString();
        if (StringUtils.isBlank(originalValue))
        {
            return originalValue;
        } else
        {
            // We can not use colIndex because order of visible columns is often different from the
            // order in the model.
            List<ISerializableComparable> values =
                    ((TableModelRowWithObject<?>) model.getBaseObject()).getValues();
            if (columnIndex >= values.size())
            {
                return "";
            }

            ISerializableComparable cell = null;
            if (obj instanceof VocabularyTerm)
            {
                cell = new VocabularyTermTableCell((VocabularyTerm) obj);
                values.set(columnIndex, cell);
            }

            cell = values.get(columnIndex);
            if (cell instanceof VocabularyTermTableCell == false)
            {
                return cell.toString();
            } else
            {
                VocabularyTermTableCell vocabularyTermTableCell = (VocabularyTermTableCell) cell;
                List<VocabularyTerm> vocabularyTerms = vocabularyTermTableCell.getVocabularyTerm();
                StringBuilder builder = new StringBuilder();
                for (VocabularyTerm vocabularyTerm : vocabularyTerms)
                {
                    if (builder.length() > 0)
                    {
                        builder.append(", ");
                    }
                    builder.append(VocabularyPropertyColRenderer.renderTerm(vocabularyTerm));
                }
                return builder.toString();
            }
        }
    }
}
