package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

abstract class AbstractXLSExportHelper implements IXLSExportHelper
{

    protected static final String[] ENTITY_ASSIGNMENT_COLUMNS = new String[] {"Version", "Code", "Mandatory",
            "Show in edit views", "Section", "Property label", "Data type", "Vocabulary code", "Description",
            "Metadata", "Dynamic script"};

    final Workbook wb;
    
    final CellStyle normalCellStyle;
    
    final CellStyle boldCellStyle;

    final CellStyle errorCellStyle;

    public AbstractXLSExportHelper(final Workbook wb)
    {
        this.wb = wb;
        
        normalCellStyle = wb.createCellStyle();
        boldCellStyle = wb.createCellStyle();
        errorCellStyle = wb.createCellStyle();
        
        final Font boldFont = wb.createFont();
        boldFont.setBold(true);
        boldCellStyle.setFont(boldFont);
        
        final Font normalFont = wb.createFont();
        normalFont.setBold(false);
        normalCellStyle.setFont(normalFont);
        
        errorCellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
        errorCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

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

    protected Collection<String> addRow(final int rowNumber, final boolean bold,
            final ExportableKind exportableKind, final String permId, final String... values)
    {
        final Collection<String> warnings = new ArrayList<>();

        final Row row = wb.getSheetAt(0).createRow(rowNumber);
        for (int i = 0; i < values.length; i++)
        {
            final Cell cell = row.createCell(i);
            final String value = values[i] != null ? values[i] : "";

            if (value.length() <= Short.MAX_VALUE)
            {
                cell.setCellStyle(bold ? boldCellStyle : normalCellStyle);
                cell.setCellValue(value);
            } else
            {
                warnings.add(String.format("The value of the exportable with the perm ID '%s' of the kind %s exceeds " +
                        "the maximum value supported by Excel: %d.", permId, exportableKind.toString(),
                        Short.MAX_VALUE));
                cell.setCellStyle(errorCellStyle);
            }
        }

        return warnings;
    }

    protected AdditionResult addEntityTypePropertyAssignments(int rowNumber,
            final Collection<PropertyAssignment> propertyAssignments, final ExportableKind exportableKind,
            final String permId)
    {
        final Collection<String> warnings = new ArrayList<>(
                addRow(rowNumber++, true, exportableKind, permId, ENTITY_ASSIGNMENT_COLUMNS));
        for (final PropertyAssignment propertyAssignment : propertyAssignments)
        {
            final PropertyType propertyType = propertyAssignment.getPropertyType();
            final Plugin plugin = propertyAssignment.getPlugin();
            final Vocabulary vocabulary = propertyType.getVocabulary();
            warnings.addAll(addRow(rowNumber++, false, exportableKind, permId, "1",
                    propertyType.getCode(),
                    String.valueOf(propertyAssignment.isMandatory()).toUpperCase(),
                    String.valueOf(propertyAssignment.isShowInEditView()).toUpperCase(),
                    propertyAssignment.getSection(), propertyType.getLabel(),
                    getFullDataTypeString(propertyType), String.valueOf(vocabulary != null ? vocabulary.getCode() : ""),
                    propertyType.getDescription(),
                    mapToJSON(propertyType.getMetaData()),
                    plugin != null ? (plugin.getName() != null ? plugin.getName() + ".py" : "") : "")
            );
        }
        return new AdditionResult(rowNumber, warnings);
    }

    private String getFullDataTypeString(final PropertyType propertyType)
    {
        final String dataTypeString = String.valueOf(propertyType.getDataType());
        switch (propertyType.getDataType())
        {
            case SAMPLE:
            {
                return dataTypeString +
                        ((propertyType.getSampleType() != null) ? ':' + propertyType.getSampleType().getCode() : "");
            }
            case MATERIAL:
            {
                return dataTypeString +
                        ((propertyType.getMaterialType() != null)
                                ? ':' + propertyType.getMaterialType().getCode() : "");
            }
            default:
            {
                return dataTypeString;
            }
        }
    }

    @Override
    public IEntityType getEntityType(final IApplicationServerApi api, final String sessionToken, final String permId)
    {
        return null;
    }

    protected static Predicate<PropertyType> getPropertiesFilterFunction(final Collection<String> propertiesToInclude)
    {
        return propertiesToInclude == null
                ? propertyType -> true
                : propertyType -> propertiesToInclude.contains(propertyType.getCode());
    }

    protected static Function<PropertyType, String> getPropertiesMappingFunction(
            final XLSExport.TextFormatting textFormatting, final Map<String, String> properties)
    {
        return textFormatting == XLSExport.TextFormatting.PLAIN
                ? propertyType -> propertyType.getDataType() == DataType.MULTILINE_VARCHAR
                        ? properties.get(propertyType.getCode()) != null
                                ? properties.get(propertyType.getCode()).replaceAll("<[^>]+>", "")
                                : null
                        : properties.get(propertyType.getCode())
                : propertyType -> properties.get(propertyType.getCode());
    }

}
