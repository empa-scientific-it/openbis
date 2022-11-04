package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityTypeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

abstract class AbstractXLSExportHelper implements IXLSExportHelper
{

    protected static final String[] ENTITY_ASSIGNMENT_COLUMNS = new String[] {"Version", "Code", "Mandatory",
            "Show in edit views", "Section", "Property label", "Data type", "Vocabulary code", "Description",
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
                    plugin != null ? (plugin.getName() != null ? plugin.getName() + ".py" : "") : "");
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

    protected <T extends IEntityTypeHolder> Collection<Collection<T>> groupByType(
            final Collection<T> entityTypeHolders)
    {
        final HashMap<String, Collection<T>> permIdToEntityTypeMap = entityTypeHolders.stream().collect(
                Collectors.toMap(
                entityTypeHolder -> entityTypeHolder.getType().getPermId().toString(),
                List::of,
                (entityTypeHolders1, entityTypeHolders2) -> Stream.of(entityTypeHolders1, entityTypeHolders2)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()),
                HashMap::new));
        return permIdToEntityTypeMap.values();
    }

    @Override
    public IEntityType getEntityType(final IApplicationServerApi api, final String sessionToken, final String permId)
    {
        return null;
    }

    protected static Function<Map.Entry<String, DataType>, String> getPropertiesMappingFunction(
            final XLSExport.TextFormatting textFormatting, final Map<String, String> properties)
    {
        return textFormatting == XLSExport.TextFormatting.PLAIN
                ? codeToTypeEntry -> codeToTypeEntry.getValue() == DataType.MULTILINE_VARCHAR
                        ? properties.get(codeToTypeEntry.getKey()) != null
                                ? properties.get(codeToTypeEntry.getKey()).replaceAll("<[^>]+>", "")
                                : null
                        : properties.get(codeToTypeEntry.getKey())
                : codeToTypeEntry -> properties.get(codeToTypeEntry.getKey());
    }
    
}
