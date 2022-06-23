package ch.ethz.sis.openbis.generic.server.xls.export;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;

public class XLSExportTest
{

    static final String SESSION_TOKEN = "test-token";

    private static final String XLS_EXPORT_DATA_PROVIDER = "xlsExportData";

    private final XLSExport xlsExport = new XLSExport();

    private Mockery mockery;

    private IApplicationServerApi api;

    @DataProvider
    protected Object[][] xlsExportData()
    {
        return new Object[][] {
                {
                        "export-vocabulary.xlsx",
                        VocabularyExpectations.class,
                        Collections.singletonList(new ExportablePermId(ExportableKind.VOCABULARY,
                                new VocabularyPermId("ANTIBODY.DETECTION"))),
                        true
                },
                {
                        "export-sample-type.xlsx",
                        SampleTypeExpectations.class,
                        Collections.singletonList(new ExportablePermId(ExportableKind.SAMPLE_TYPE,
                                new EntityTypePermId("ENTRY", EntityKind.SAMPLE))),
                        true
                },
                {
                        "export-experiment-type.xlsx",
                        ExperimentTypeExpectations.class,
                        Collections.singletonList(new ExportablePermId(ExportableKind.EXPERIMENT_TYPE,
                                new EntityTypePermId("DEFAULT_EXPERIMENT", EntityKind.EXPERIMENT))),
                        true
                },
                {
                        "export-data-set-type.xlsx",
                        DataSetTypeExpectations.class,
                        Collections.singletonList(new ExportablePermId(ExportableKind.DATASET_TYPE,
                                new EntityTypePermId("ATTACHMENT", EntityKind.DATA_SET))),
                        true
                },
                {
                        "export-sample-type-with-dependent-vocabulary.xlsx",
                        SampleTypeWithDependentVocabularyExpectations.class,
                        Arrays.asList(
                                new ExportablePermId(ExportableKind.SAMPLE_TYPE,
                                        new EntityTypePermId("ANTIBODY", EntityKind.SAMPLE)),
                                new ExportablePermId(ExportableKind.SAMPLE_TYPE,
                                        new EntityTypePermId("VIRUS", EntityKind.SAMPLE))),
                        true
                },
                {
                        "export-sample-type-with-omitted-dependent-vocabulary.xlsx",
                        SampleTypeWithDependentVocabularyExpectations.class,
                        Arrays.asList(
                                new ExportablePermId(ExportableKind.SAMPLE_TYPE,
                                        new EntityTypePermId("ANTIBODY", EntityKind.SAMPLE)),
                                new ExportablePermId(ExportableKind.SAMPLE_TYPE,
                                        new EntityTypePermId("VIRUS", EntityKind.SAMPLE))),
                        false
                },
                {
                        "export-sample-type-with-dependent-sample.xlsx",
                        SampleTypeWithDependentSampleExpectations.class,
                        Collections.singletonList(
                                new ExportablePermId(ExportableKind.SAMPLE_TYPE,
                                        new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                        true
                },
                {
                        "export-sample-type-with-omitted-dependent-sample.xlsx",
                        SampleTypeWithDependentSampleExpectations.class,
                        Collections.singletonList(
                                new ExportablePermId(ExportableKind.SAMPLE_TYPE,
                                        new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                        false
                },
        };
    }

    @BeforeMethod
    public void beforeMethod()
    {
        mockery = new Mockery();
        api = mockery.mock(IApplicationServerApi.class);
    }

    @AfterMethod
    public void afterMethod()
    {
        mockery.assertIsSatisfied();
    }

    @Test(dataProvider = XLS_EXPORT_DATA_PROVIDER)
    public void testXlsExport(final String expectedResultFileName, final Class<IApplicationServerApi> expectationsClass,
            final Collection<ExportablePermId> exportablePermIds, final boolean exportReferred) throws Exception
    {
        final Expectations expectations = (Expectations) expectationsClass.getConstructor(IApplicationServerApi.class,
                boolean.class).newInstance(api, exportReferred);
        mockery.checking(expectations);

        final Workbook actualResult = xlsExport.prepareWorkbook(api, SESSION_TOKEN, exportablePermIds, exportReferred);

        final InputStream stream = getClass().getClassLoader().getResourceAsStream(
                "ch/ethz/sis/openbis/generic/server/xls/export/resources/" + expectedResultFileName);
        if (stream == null)
        {
            throw new IllegalArgumentException("File not found.");
        }
        final Workbook expectedResult = new XSSFWorkbook(stream);

        assertWorkbooksEqual(actualResult, expectedResult);
    }

    private static void assertWorkbooksEqual(final Workbook actual, final Workbook expected)
    {
        final int sheetsCount = expected.getNumberOfSheets();
        assertEquals(actual.getNumberOfSheets(), sheetsCount);
        IntStream.range(0, sheetsCount).forEachOrdered(i ->
                assertSheetsEqual(actual.getSheetAt(i), expected.getSheetAt(i)));
    }

    private static void assertSheetsEqual(final Sheet actual, final Sheet expected)
    {
        final int firstRowNum = Math.min(actual.getFirstRowNum(), expected.getFirstRowNum());
        final int lastRowNum = Math.max(actual.getLastRowNum(), expected.getLastRowNum());
        for (int i = firstRowNum; i <= lastRowNum; i++)
        {
            assertRowsEqual(actual.getRow(i), expected.getRow(i));
        }
    }

    private static void assertRowsEqual(final Row actual, final Row expected)
    {
        if (expected != null && actual != null)
        {
            final int firstCellNum = Math.min(actual.getFirstCellNum(), expected.getFirstCellNum());
            final int lastCellNum = Math.max(actual.getLastCellNum(), expected.getLastCellNum());
            for (int i = firstCellNum; i < lastCellNum; i++)
            {
                assertCellsEqual(actual.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK),
                        expected.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
            }
        } else if (expected == null)
        {
            if (rowContainsValues(actual))
            {
                fail(String.format("Actual row number %d (1 based) is not empty.", actual.getRowNum() + 1));
            }
        } else
        {
            if (rowContainsValues(expected))
            {
                fail(String.format("Actual row number %d (1 based) is empty.", expected.getRowNum() + 1));
            }
        }
    }

    private static boolean rowContainsValues(final Row row)
    {
        if (row != null)
        {
            final int firstCellNum = row.getFirstCellNum();
            final int lastCellNum = row.getLastCellNum();
            for (int i = firstCellNum; i < lastCellNum; i++)
            {
                final Cell cell = row.getCell(i);
                if (cell != null && cell.getStringCellValue().length() > 0)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static void assertCellsEqual(final Cell actual, final Cell expected)
    {
        assertEquals(getStringValue(actual), getStringValue(expected), getErrorMessage(actual, "Values"));
    }

    private static String getStringValue(final Cell cell)
    {
        switch (cell.getCellTypeEnum())
        {
            case NUMERIC:
            {
                final double numericCellValue = cell.getNumericCellValue();
                return numericCellValue % 1 == 0 ? String.valueOf((long) numericCellValue)
                        : String.valueOf(numericCellValue);
            }
            case BLANK:
            {
                return "";
            }
            case STRING:
            {
                return cell.getStringCellValue();
            }
            case FORMULA:
            {
                return cell.getCellFormula();
            }
            case BOOLEAN:
            {
                return String.valueOf(cell.getBooleanCellValue()).toUpperCase();
            }
            case ERROR:
            {
                fail("Error cell type found.");
                return null;
            }
            default:
            {
                fail("Not supported type found.");
                return null;
            }
        }
    }

    private static String getErrorMessage(final Cell cell, final String what)
    {
        return String.format(what + " are not compatible at %c:%d.", 'A' + cell.getColumnIndex(),
                cell.getRowIndex() + 1);
    }

}