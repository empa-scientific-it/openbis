package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleDateRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleImageHtmlRenderer;

/**
 * @author Franz-Josef Elmer
 */
public class TypedTableGridColumnDefinition<T extends Serializable> implements
        IColumnDefinition<TableModelRowWithObject<T>>
{
    protected TableModelColumnHeader header;

    private String title;

    private String downloadURL;

    private String sessionID;

    public TypedTableGridColumnDefinition(TableModelColumnHeader header, String title,
            String downloadURL, String sessionID)
    {
        this.header = header;
        this.title = title;
        this.downloadURL = downloadURL;
        this.sessionID = sessionID;
    }

    // GWT only
    @SuppressWarnings("unused")
    private TypedTableGridColumnDefinition()
    {
    }

    @Override
    public String getHeader()
    {
        return title;
    }

    @Override
    public String getIdentifier()
    {
        return header.getId();
    }

    @Override
    public DataTypeCode tryToGetDataType()
    {
        return header.getDataType();
    }

    public int getIndex()
    {
        return header.getIndex();
    }

    @Override
    public String tryToGetProperty(String key)
    {
        return header.tryToGetProperty(key);
    }

    @Override
    public String getValue(GridRowModel<TableModelRowWithObject<T>> rowModel)
    {
        Comparable<?> cell = tryGetComparableValue(rowModel);
        if (cell instanceof ImageTableCell)
        {
            ImageTableCell imageCell = (ImageTableCell) cell;
            int width = imageCell.getMaxThumbnailWidth();
            int height = imageCell.getMaxThumbnailHeight();
            String imagePath = imageCell.getPath();
            return SimpleImageHtmlRenderer.createEmbededDatastoreImageHtml(imagePath, width,
                    height, downloadURL, sessionID);
        }
        if (cell instanceof DateTableCell)
        {
            return SimpleDateRenderer.renderDate(((DateTableCell) cell).getDateTime());
        }
        if (cell instanceof GeneratedImageTableCell)
        {
            return ((GeneratedImageTableCell) cell).getHTMLString(downloadURL, sessionID);
        }
        if (cell instanceof DssLinkTableCell)
        {
            return ((DssLinkTableCell) cell).getHtmlString(sessionID);
        }
        if (cell instanceof LinkTableCell)
        {
            return ((LinkTableCell) cell).getHtmlString();
        }
        return cell.toString();
    }

    @Override
    public Comparable<?> tryGetComparableValue(GridRowModel<TableModelRowWithObject<T>> rowModel)
    {
        List<ISerializableComparable> values = rowModel.getOriginalObject().getValues();
        int index = header.getIndex();
        // The check of index is needed because the column is applied to the wrong model
        return index < values.size() ? values.get(index) : null;
    }

    @Override
    public boolean isCustom()
    {
        return false;
    }
}