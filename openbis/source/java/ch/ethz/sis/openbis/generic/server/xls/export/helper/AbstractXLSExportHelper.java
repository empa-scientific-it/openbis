package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collection;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;

abstract class AbstractXLSExportHelper implements IXLSExportHelper
{

    protected static final String[] ENTITY_ASSIGNMENT_COLUMNS = new String[] {"Version", "Code", "Mandatory",
            "Show in edit views", "Section", "Property label", "Data type", "Vocabulary Code", "Description",
            "Metadata", "Dynamic script"};

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

        final Row row = wb.getSheetAt(0).createRow(rowNumber);
        for (int i = 0; i < values.length; i++)
        {
            final Cell cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(values[i]);
        }
    }

    protected int addEntityTypePropertyAssignments(final Workbook wb, int rowNumber,
            final Collection<PropertyAssignment> propertyAssignments)
    {
        addRow(wb, rowNumber++, true, ENTITY_ASSIGNMENT_COLUMNS);
        for (final PropertyAssignment propertyAssignment : propertyAssignments)
        {
            final PropertyType propertyType = propertyAssignment.getPropertyType();
            final Plugin plugin = propertyAssignment.getPlugin();
            final Vocabulary vocabulary = propertyType.getVocabulary();
            addRow(wb, rowNumber++, false, "1", propertyType.getCode(),
                    String.valueOf(propertyAssignment.isMandatory()).toUpperCase(),
                    String.valueOf(propertyAssignment.isShowInEditView()).toUpperCase(),
                    propertyAssignment.getSection(),
                    propertyType.getLabel(), getFullDataTypeString(propertyType),
                    String.valueOf(vocabulary != null ? vocabulary.getCode() : ""), propertyType.getDescription(),
                    mapToJSON(propertyType.getMetaData()),
                    plugin != null ? (plugin.getScript() != null ? plugin.getScript() : "") : "");
        }
        return rowNumber;
    }

    private String getFullDataTypeString(final PropertyType propertyType)
    {
        final String dataTypeString = String.valueOf(propertyType.getDataType());
        switch (propertyType.getDataType())
        {
            case SAMPLE:
            {
                return dataTypeString + ':' + propertyType.getSampleType().getCode();
            }
            case MATERIAL:
            {
                return dataTypeString + ':' + propertyType.getMaterialType().getCode();
            }
            default:
            {
                return dataTypeString;
            }
        }
    }

}
