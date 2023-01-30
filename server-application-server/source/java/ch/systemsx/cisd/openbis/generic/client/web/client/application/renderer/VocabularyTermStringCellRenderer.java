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
                VocabularyTerm vocabularyTerm = vocabularyTermTableCell.getVocabularyTerm();
                return VocabularyPropertyColRenderer.renderTerm(vocabularyTerm);
            }
        }
    }
}
