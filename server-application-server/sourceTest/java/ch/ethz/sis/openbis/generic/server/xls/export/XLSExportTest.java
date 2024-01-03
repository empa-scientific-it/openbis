/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.export;

import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.DATASET;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.DATASET_TYPE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.EXPERIMENT_TYPE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.MASTER_DATA_EXPORTABLE_KINDS;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.PROJECT;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.SAMPLE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.SAMPLE_TYPE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.SPACE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.VOCABULARY_TYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
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
import ch.systemsx.cisd.common.exceptions.UserFailureException;

public class XLSExportTest
{

    static final String SESSION_TOKEN = "test-token";

    private static final String XLS_EXPORT_DATA_PROVIDER = "xlsExportData";

    private Mockery mockery;

    private IApplicationServerApi api;

    @DataProvider
    protected Object[][] xlsExportData()
    {
        return XLSExportData.EXPORT_DATA;
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
    public void testPutVocabulariesFirst()
    {
        final List<Collection<ExportablePermId>> permIds = new ArrayList<>(List.of(
                List.of(
                        new ExportablePermId(SAMPLE_TYPE, new ObjectPermId("ST1")),
                        new ExportablePermId(SAMPLE_TYPE, new ObjectPermId("ST2")),
                        new ExportablePermId(SAMPLE_TYPE, new ObjectPermId("ST3"))
                ),
                List.of(
                        new ExportablePermId(EXPERIMENT_TYPE, new ObjectPermId("ET1")),
                        new ExportablePermId(EXPERIMENT_TYPE, new ObjectPermId("ET2")),
                        new ExportablePermId(EXPERIMENT_TYPE, new ObjectPermId("ET3"))
                ),
                List.of(
                        new ExportablePermId(DATASET_TYPE, new ObjectPermId("DT1")),
                        new ExportablePermId(DATASET_TYPE, new ObjectPermId("DT2")),
                        new ExportablePermId(DATASET_TYPE, new ObjectPermId("DT3"))
                ),
                List.of(
                        new ExportablePermId(VOCABULARY_TYPE, new ObjectPermId("V1")),
                        new ExportablePermId(VOCABULARY_TYPE, new ObjectPermId("V2")),
                        new ExportablePermId(VOCABULARY_TYPE, new ObjectPermId("V3"))
                ),
                List.of(
                        new ExportablePermId(VOCABULARY_TYPE, new ObjectPermId("V4")),
                        new ExportablePermId(VOCABULARY_TYPE, new ObjectPermId("V5")),
                        new ExportablePermId(VOCABULARY_TYPE, new ObjectPermId("V6"))
                ),
                List.of(
                        new ExportablePermId(SPACE, new ObjectPermId("SP1")),
                        new ExportablePermId(SPACE, new ObjectPermId("SP2")),
                        new ExportablePermId(SPACE, new ObjectPermId("SP3"))
                ),
                List.of(
                        new ExportablePermId(PROJECT, new ObjectPermId("P1")),
                        new ExportablePermId(PROJECT, new ObjectPermId("P2")),
                        new ExportablePermId(PROJECT, new ObjectPermId("P3"))
                ),
                List.of(
                        new ExportablePermId(SAMPLE, new ObjectPermId("S1")),
                        new ExportablePermId(SAMPLE, new ObjectPermId("S2")),
                        new ExportablePermId(SAMPLE, new ObjectPermId("S3"))
                ),
                List.of(
                        new ExportablePermId(EXPERIMENT, new ObjectPermId("E1")),
                        new ExportablePermId(EXPERIMENT, new ObjectPermId("E2")),
                        new ExportablePermId(EXPERIMENT, new ObjectPermId("E3"))
                ),
                List.of(
                        new ExportablePermId(DATASET, new ObjectPermId("D1")),
                        new ExportablePermId(DATASET, new ObjectPermId("D2")),
                        new ExportablePermId(DATASET, new ObjectPermId("D3"))
                )
        ));

        Collections.shuffle(permIds);

        final Collection<Collection<ExportablePermId>> reorderedPermIds = XLSExport.putVocabulariesFirst(permIds);
        boolean nonVocabularyFound = false;

        assertEquals(reorderedPermIds.size(), permIds.size(), "The size after reordering should not change.");

        for (final Collection<ExportablePermId> group : reorderedPermIds)
        {
            if (group.iterator().next().getExportableKind() == VOCABULARY_TYPE)
            {
                assertFalse(nonVocabularyFound, "All vocabularies should be at the beginning.");
            } else
            {
                nonVocabularyFound = true;
            }
        }
    }

    @Test()
    public void testGroup()
    {
        final List<ExportablePermId> permIds = new ArrayList<>(List.of(
                new ExportablePermId(SAMPLE_TYPE, new ObjectPermId("ST1")),
                new ExportablePermId(SAMPLE_TYPE, new ObjectPermId("ST2")),
                new ExportablePermId(SAMPLE_TYPE, new ObjectPermId("ST3")),
                new ExportablePermId(EXPERIMENT_TYPE, new ObjectPermId("ET1")),
                new ExportablePermId(EXPERIMENT_TYPE, new ObjectPermId("ET2")),
                new ExportablePermId(EXPERIMENT_TYPE, new ObjectPermId("ET3")),
                new ExportablePermId(DATASET_TYPE, new ObjectPermId("DT1")),
                new ExportablePermId(DATASET_TYPE, new ObjectPermId("DT2")),
                new ExportablePermId(DATASET_TYPE, new ObjectPermId("DT3")),
                new ExportablePermId(VOCABULARY_TYPE, new ObjectPermId("V1")),
                new ExportablePermId(VOCABULARY_TYPE, new ObjectPermId("V2")),
                new ExportablePermId(VOCABULARY_TYPE, new ObjectPermId("V3")),
                new ExportablePermId(SPACE, new ObjectPermId("SP1")),
                new ExportablePermId(SPACE, new ObjectPermId("SP2")),
                new ExportablePermId(SPACE, new ObjectPermId("SP3")),
                new ExportablePermId(PROJECT, new ObjectPermId("P1")),
                new ExportablePermId(PROJECT, new ObjectPermId("P2")),
                new ExportablePermId(PROJECT, new ObjectPermId("P3")),
                new ExportablePermId(SAMPLE, new ObjectPermId("S1")),
                new ExportablePermId(SAMPLE, new ObjectPermId("S2")),
                new ExportablePermId(SAMPLE, new ObjectPermId("S3")),
                new ExportablePermId(EXPERIMENT, new ObjectPermId("E1")),
                new ExportablePermId(EXPERIMENT, new ObjectPermId("E2")),
                new ExportablePermId(EXPERIMENT, new ObjectPermId("E3")),
                new ExportablePermId(DATASET, new ObjectPermId("D1")),
                new ExportablePermId(DATASET, new ObjectPermId("D2")),
                new ExportablePermId(DATASET, new ObjectPermId("D3"))
        ));

        Collections.shuffle(permIds);

        final Collection<Collection<ExportablePermId>> groupedPermIds = XLSExport.group(permIds);

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
    public void testXlsExport(final String expectedResultFileName, final Map<String, String> expectedScripts,
            final Class<IApplicationServerApi> expectationsClass, final List<ExportablePermId> exportablePermIds,
            final boolean exportReferred, final Map<String, Map<String, List<Map<String, String>>>> exportFields,
            final XLSExport.TextFormatting textFormatting, final List<String> expectedWarnings,
            final boolean compatibleWithImport) throws Exception
    {
        final Expectations expectations = (Expectations) expectationsClass.getConstructor(IApplicationServerApi.class,
                boolean.class).newInstance(api, exportReferred);
        mockery.checking(expectations);

        try
        {
            final XLSExport.PrepareWorkbookResult actualResult = XLSExport.prepareWorkbook(
                    api, SESSION_TOKEN, exportablePermIds, exportReferred, exportFields,
                    textFormatting, compatibleWithImport);
            assertEquals(actualResult.getScripts(), expectedScripts);
            assertEquals(new HashSet<>(actualResult.getWarnings()), new HashSet<>(expectedWarnings));

            final InputStream stream = getClass().getClassLoader().getResourceAsStream(
                    "ch/ethz/sis/openbis/generic/server/xls/export/resources/" + expectedResultFileName);
            if (stream == null)
            {
                throw new IllegalArgumentException("File not found.");
            }
            final Workbook expectedResult = new XSSFWorkbook(stream);

            assertWorkbooksEqual(actualResult.getWorkbook(), expectedResult);
        } catch (final UserFailureException e)
        {
            // When the file name is not specified we expect a UserFailureException to be thrown.
            if (expectedResultFileName != null)
            {
                throw e;
            }
        }
    }

//    /**
//     * Tests export of cells larger than 32k.
//     */
//    @Test
//    public void testLargeCellExport()
//    {
//
//    }

    public static void assertWorkbooksEqual(final Workbook actual, final Workbook expected)
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
        if (rowContainsValues(expected) && rowContainsValues(actual))
        {
            final int firstCellNum = Math.min(actual.getFirstCellNum(), expected.getFirstCellNum());
            final int lastCellNum = Math.max(actual.getLastCellNum(), expected.getLastCellNum());
            for (int i = firstCellNum; i < lastCellNum; i++)
            {
                assertCellsEqual(actual.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK),
                        expected.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
            }
        } else if (rowContainsValues(expected))
        {
            fail(String.format("Actual row #%d (1 based) is empty.", expected.getRowNum() + 1));
        } else if (rowContainsValues(actual))
        {
            fail(String.format("Actual row #%d (1 based) is not empty.", actual.getRowNum() + 1));
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
                if (cell != null && !StringUtils.isEmpty(getStringValue(cell)))
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

    private static String getErrorMessage(final Cell cell, final String prefix)
    {
        return String.format("%s are not compatible at %c:%d.", prefix, 'A' + cell.getColumnIndex(),
                cell.getRowIndex() + 1);
    }

}