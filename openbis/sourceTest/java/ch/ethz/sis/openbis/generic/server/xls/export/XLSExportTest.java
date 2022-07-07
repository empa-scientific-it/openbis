package ch.ethz.sis.openbis.generic.server.xls.export;

import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

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
                        Collections.singletonList(new ExportablePermId(VOCABULARY,
                                new VocabularyPermId("ANTIBODY.DETECTION"))),
                        true
                },
                {
                        "export-sample-type.xlsx",
                        SampleTypeExpectations.class,
                        Collections.singletonList(new ExportablePermId(SAMPLE_TYPE,
                                new EntityTypePermId("ENTRY", EntityKind.SAMPLE))),
                        true
                },
                {
                        "export-experiment-type.xlsx",
                        ExperimentTypeExpectations.class,
                        Collections.singletonList(new ExportablePermId(EXPERIMENT_TYPE,
                                new EntityTypePermId("DEFAULT_EXPERIMENT", EntityKind.EXPERIMENT))),
                        true
                },
                {
                        "export-data-set-type.xlsx",
                        DataSetTypeExpectations.class,
                        Collections.singletonList(new ExportablePermId(DATASET_TYPE,
                                new EntityTypePermId("ATTACHMENT", EntityKind.DATA_SET))),
                        true
                },
                {
                        "export-sample-type-with-vocabulary-property.xlsx",
                        SampleTypeWithVocabularyPropertyExpectations.class,
                        Arrays.asList(
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("ANTIBODY", EntityKind.SAMPLE)),
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("VIRUS", EntityKind.SAMPLE))),
                        true
                },
                {
                        "export-sample-type-with-omitted-vocabulary-property.xlsx",
                        SampleTypeWithVocabularyPropertyExpectations.class,
                        Arrays.asList(
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("ANTIBODY", EntityKind.SAMPLE)),
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("VIRUS", EntityKind.SAMPLE))),
                        false
                },
                {
                        "export-sample-type-with-sample-property.xlsx",
                        SampleTypeWithSamplePropertyExpectations.class,
                        Collections.singletonList(
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                        true
                },
                {
                        "export-sample-type-with-omitted-sample-property.xlsx",
                        SampleTypeWithSamplePropertyExpectations.class,
                        Collections.singletonList(
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                        false
                },
                {
                        "export-sample-type-with-chained-sample-properties.xlsx",
                        SampleTypeWithChainedSamplePropertiesExpectations.class,
                        Collections.singletonList(
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                        true
                },
                {
                        "export-sample-type-with-cyclic-sample-properties.xlsx",
                        SampleTypeWithCyclicSamplePropertiesExpectations.class,
                        Collections.singletonList(
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                        true
                },
                {
                        "export-space.xlsx",
                        SpaceExpectations.class,
                        Arrays.asList(
                                new ExportablePermId(SPACE, new SpacePermId("ELN_SETTINGS")),
                                new ExportablePermId(SPACE, new SpacePermId("MATERIALS")),
                                new ExportablePermId(SPACE, new SpacePermId("PUBLICATIONS"))
                        ),
                        true
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

    @Test()
    public void testGroup()
    {
        final List<ExportablePermId> permIds = new ArrayList<>();

        permIds.add(new ExportablePermId(SPACE, new ObjectPermId("SP1")));
        permIds.add(new ExportablePermId(SPACE, new ObjectPermId("SP2")));
        permIds.add(new ExportablePermId(SPACE, new ObjectPermId("SP3")));
        permIds.add(new ExportablePermId(PROJECT, new ObjectPermId("P1")));
        permIds.add(new ExportablePermId(PROJECT, new ObjectPermId("P2")));
        permIds.add(new ExportablePermId(PROJECT, new ObjectPermId("P3")));
        permIds.add(new ExportablePermId(SAMPLE, new ObjectPermId("S1")));
        permIds.add(new ExportablePermId(SAMPLE, new ObjectPermId("S2")));
        permIds.add(new ExportablePermId(SAMPLE, new ObjectPermId("S3")));
        permIds.add(new ExportablePermId(EXPERIMENT, new ObjectPermId("E1")));
        permIds.add(new ExportablePermId(EXPERIMENT, new ObjectPermId("E2")));
        permIds.add(new ExportablePermId(EXPERIMENT, new ObjectPermId("E3")));
        permIds.add(new ExportablePermId(DATASET, new ObjectPermId("D1")));
        permIds.add(new ExportablePermId(DATASET, new ObjectPermId("D2")));
        permIds.add(new ExportablePermId(DATASET, new ObjectPermId("D3")));

        permIds.add(new ExportablePermId(SAMPLE_TYPE, new ObjectPermId("ST1")));
        permIds.add(new ExportablePermId(SAMPLE_TYPE, new ObjectPermId("ST2")));
        permIds.add(new ExportablePermId(SAMPLE_TYPE, new ObjectPermId("ST3")));
        permIds.add(new ExportablePermId(EXPERIMENT_TYPE, new ObjectPermId("ET1")));
        permIds.add(new ExportablePermId(EXPERIMENT_TYPE, new ObjectPermId("ET2")));
        permIds.add(new ExportablePermId(EXPERIMENT_TYPE, new ObjectPermId("ET3")));
        permIds.add(new ExportablePermId(DATASET_TYPE, new ObjectPermId("DT1")));
        permIds.add(new ExportablePermId(DATASET_TYPE, new ObjectPermId("DT2")));
        permIds.add(new ExportablePermId(DATASET_TYPE, new ObjectPermId("DT3")));
        permIds.add(new ExportablePermId(PROPERTY_TYPE, new ObjectPermId("PT1")));
        permIds.add(new ExportablePermId(PROPERTY_TYPE, new ObjectPermId("PT2")));
        permIds.add(new ExportablePermId(PROPERTY_TYPE, new ObjectPermId("PT3")));
        permIds.add(new ExportablePermId(VOCABULARY, new ObjectPermId("V1")));
        permIds.add(new ExportablePermId(VOCABULARY, new ObjectPermId("V2")));
        permIds.add(new ExportablePermId(VOCABULARY, new ObjectPermId("V3")));

        Collections.shuffle(permIds);

        final Collection<Collection<ExportablePermId>> groupedPermIds = xlsExport.group(permIds);

        final Set<ExportableKind> checkedExportableKinds = EnumSet.noneOf(ExportableKind.class);
        groupedPermIds.forEach(permIdGroup ->
        {
            final int size = permIdGroup.size();
            assertTrue(size > 0, "Empty group found.");

            if (size == 1)
            {
                final ExportableKind exportableKind = permIdGroup.iterator().next().getExportableKind();
                assertTrue(MASTER_DATA_EXPORTABLE_KINDS.contains(exportableKind),
                        String.format("Not grouped exportable kinds should be master data exportable kinds. "
                                + "The first one is %s.", exportableKind));
            } else
            {
                final Iterator<ExportablePermId> iterator = permIdGroup.stream().iterator();
                final ExportableKind exportableKind = iterator.next().getExportableKind();

                assertFalse(checkedExportableKinds.contains(exportableKind),
                        String.format("Exportable kind %s should be in one group.", exportableKind));
                checkedExportableKinds.add(exportableKind);

                assertFalse(MASTER_DATA_EXPORTABLE_KINDS.contains(exportableKind),
                        String.format("Grouped exportable kinds should not be master data exportable kinds. "
                                + "The first exportable kind in the group is %s.", exportableKind));
                iterator.forEachRemaining(permId -> assertEquals(permId.getExportableKind(), exportableKind,
                        "Only same exportable kinds should be grouped."));
            }
        });

        final Set<ExportablePermId> expectedPermIds = new HashSet<>(permIds);
        final Set<ExportablePermId> actualPermIds = groupedPermIds.stream().flatMap(Collection::stream)
                .collect(Collectors.toSet());

        assertEquals(actualPermIds, expectedPermIds, "All resulting exportable permIds should be present "
                + "in the grouped collection and only once.");
    }

    /**
     * @param expectationsClass this class is a generator of mockup data.
     */
    @Test(dataProvider = XLS_EXPORT_DATA_PROVIDER)
    public void testXlsExport(final String expectedResultFileName, final Class<IApplicationServerApi> expectationsClass,
            final Collection<ExportablePermId> exportablePermIds, final boolean exportReferred) throws Exception
    {
        final Expectations expectations = (Expectations) expectationsClass.getConstructor(IApplicationServerApi.class,
                boolean.class).newInstance(api, exportReferred);
        mockery.checking(expectations);

        try
        {
            final Workbook actualResult = xlsExport.prepareWorkbook(api, SESSION_TOKEN, exportablePermIds, exportReferred);

            final InputStream stream = getClass().getClassLoader().getResourceAsStream(
                    "ch/ethz/sis/openbis/generic/server/xls/export/resources/" + expectedResultFileName);
            if (stream == null)
            {
                throw new IllegalArgumentException("File not found.");
            }
            final Workbook expectedResult = new XSSFWorkbook(stream);

            assertWorkbooksEqual(actualResult, expectedResult);
        } catch (final UserFailureException e)
        {
            // When the file name is not specified we expect a UserFailureException to be thrown.
            if (expectedResultFileName != null)
            {
                throw e;
            }
        }
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
                fail(String.format("Actual row #%d (1 based) is not empty.", actual.getRowNum() + 1));
            }
        } else
        {
            if (rowContainsValues(expected))
            {
                fail(String.format("Actual row #%d (1 based) is empty.", expected.getRowNum() + 1));
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