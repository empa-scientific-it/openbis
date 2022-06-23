package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

abstract class AbstractXLSExportHelper implements IXLSExportHelper
{

    protected String mapToJSON(final Map<?, ?> map)
    {
        if (map == null || map.isEmpty())
        {
            return "";
        } else
        {
            try
            {
                return new ObjectMapper().writeValueAsString(map);
            } catch (final JsonProcessingException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    protected void addRow(final Workbook wb, final int rowNumber, final boolean bold, final String... values)
    {
        final CellStyle cellStyle = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setBold(bold);
        cellStyle.setFont(font);

        final Sheet sheet = wb.getSheetAt(0);
        final Row row = sheet.createRow(rowNumber);
        for (int i = 0; i < values.length; i++)
        {
            final Cell cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(values[i]);
        }
    }

}
