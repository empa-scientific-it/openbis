package ch.ethz.sis.openbis.generic.server.xls.export;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

public class XLSExportTest
{

    private static final String SESSION_TOKEN = "test-token";

    private static final String XLS_EXPORT_DATA_PROVIDER = "xlsExportData";

    private final XLSExport xlsExport = new XLSExport();

    private Mockery mockery;

    private IApplicationServerApi api;

    @DataProvider
    protected Object[][] xlsExportData()
    {
        return new Object[][] {
                {
                    SampleTypeExpectations.class,
                    Collections.singleton(new ExportablePermId(ExportableKind.SAMPLE_TYPE,
                            new EntityTypePermId("ENTRY", EntityKind.SAMPLE))),
                    "export-sample.xlsx"
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
    public void testXlsExport(final Class<IApplicationServerApi> expectationsClass,
            final Collection<ExportablePermId> exportablePermIds, final String expectedResultFileName) throws Exception
    {
        final Expectations expectations = (Expectations) expectationsClass.getConstructor(IApplicationServerApi.class)
                .newInstance(api);
        mockery.checking(expectations);

        final Workbook actualResult = xlsExport.prepareWorkbook(api, SESSION_TOKEN, exportablePermIds, false);

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
        final Iterator<Row> actualRowIterator = actual.rowIterator();
        expected.rowIterator().forEachRemaining(expectedRow ->
        {
            assertTrue(actualRowIterator.hasNext());
            assertRowsEqual(actualRowIterator.next(), expectedRow);
        });
        assertFalse(actualRowIterator.hasNext());
    }

    private static void assertRowsEqual(final Row actual, final Row expected)
    {
        final Iterator<Cell> actualCellIterator = actual.cellIterator();
        expected.cellIterator().forEachRemaining(expectedCell ->
        {
            assertTrue(actualCellIterator.hasNext());
            assertCellsEqual(actualCellIterator.next(), expectedCell);
        });
        assertFalse(actualCellIterator.hasNext());
    }

    private static void assertCellsEqual(final Cell actual, final Cell expected)
    {
        final CellType actualCellType = actual.getCellTypeEnum();
        final CellType expectedCellType = expected.getCellTypeEnum();
        assertEquals(actualCellType, expectedCellType);

        switch (expectedCellType)
        {
            case NUMERIC:
            {
                assertEquals(actual.getNumericCellValue(), expected.getNumericCellValue(), 0.000001);
                break;
            }
            case STRING:
            {
                assertEquals(actual.getStringCellValue(), expected.getStringCellValue());
                break;
            }
            case FORMULA:
            {
                assertEquals(actual.getCellFormula(), expected.getCellFormula());
                break;
            }
            case BOOLEAN:
            {
                assertEquals(actual.getBooleanCellValue(), expected.getBooleanCellValue());
                break;
            }
            case ERROR:
            {
                assertEquals(actual.getErrorCellValue(), expected.getErrorCellValue());
                break;
            }
        }
    }

    private static class SampleTypeExpectations extends Expectations
    {

        public SampleTypeExpectations(final IApplicationServerApi api)
        {
            allowing(api).getSampleTypes(with(SESSION_TOKEN), with(new CollectionMatcher<>(
                    Collections.singletonList(new EntityTypePermId("ENTRY", EntityKind.SAMPLE)))),
                    with(any(SampleTypeFetchOptions.class)));

            will(new CustomAction("execute callback")
            {

                @Override
                public Object invoke(final Invocation invocation) throws Throwable
                {
                    final SampleTypeFetchOptions fetchOptions = (SampleTypeFetchOptions) invocation.getParameter(2);
                    final PluginFetchOptions pluginFetchOptions = fetchOptions.withValidationPlugin();

                    final SampleType sampleType = new SampleType();
                    sampleType.setFetchOptions(fetchOptions);
                    sampleType.setCode("ENTRY");
                    sampleType.setAutoGeneratedCode(true);
                    sampleType.setGeneratedCodePrefix("ENTRY");
                    sampleType.setPropertyAssignments(getPropertyAssignments(fetchOptions));

                    final Plugin validationPlugin = new Plugin();
                    validationPlugin.setScript("test.py");
                    validationPlugin.setFetchOptions(pluginFetchOptions);

                    sampleType.setValidationPlugin(validationPlugin);

                    return Collections.singletonMap(new EntityTypePermId("ENTRY"), sampleType);
                }

                private List<PropertyAssignment> getPropertyAssignments(final SampleTypeFetchOptions fetchOptions)
                {
                    final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions =
                            fetchOptions.withPropertyAssignments();
                    final PropertyTypeFetchOptions propertyTypeFetchOptions =
                            propertyAssignmentFetchOptions.withPropertyType();
                    propertyTypeFetchOptions.withVocabulary();

                    final PluginFetchOptions pluginFetchOptions = propertyAssignmentFetchOptions.withPlugin();
                    pluginFetchOptions.withScript();

                    final PropertyAssignment[] propertyAssignments = new PropertyAssignment[3];

                    propertyAssignments[0] = new PropertyAssignment();
                    propertyAssignments[0].setFetchOptions(propertyAssignmentFetchOptions);
                    propertyAssignments[0].setPropertyType(new PropertyType());
                    propertyAssignments[0].getPropertyType().setFetchOptions(propertyTypeFetchOptions);
                    propertyAssignments[0].setPlugin(new Plugin());
                    propertyAssignments[0].getPlugin().setFetchOptions(pluginFetchOptions);
                    propertyAssignments[0].getPropertyType().setCode("$NAME");
                    propertyAssignments[0].setMandatory(false);
                    propertyAssignments[0].setShowInEditView(true);
                    propertyAssignments[0].setSection("General info");
                    propertyAssignments[0].getPropertyType().setLabel("Name");
                    propertyAssignments[0].getPropertyType().setDataType(DataType.VARCHAR);
                    propertyAssignments[0].getPropertyType().setDescription("Name");

                    propertyAssignments[1] = new PropertyAssignment();
                    propertyAssignments[1].setFetchOptions(propertyAssignmentFetchOptions);
                    propertyAssignments[1].setPropertyType(new PropertyType());
                    propertyAssignments[1].getPropertyType().setFetchOptions(propertyTypeFetchOptions);
                    propertyAssignments[1].setPlugin(new Plugin());
                    propertyAssignments[1].getPlugin().setFetchOptions(pluginFetchOptions);
                    propertyAssignments[1].getPropertyType().setCode("$DOCUMENT");
                    propertyAssignments[1].setMandatory(false);
                    propertyAssignments[1].setShowInEditView(true);
                    propertyAssignments[1].setSection("General info");
                    propertyAssignments[1].getPropertyType().setLabel("Document");
                    propertyAssignments[1].getPropertyType().setDataType(DataType.MULTILINE_VARCHAR);
                    propertyAssignments[1].getPropertyType().setDescription("Document");
                    propertyAssignments[1].getPropertyType().setMetaData(
                            Collections.singletonMap("custom_widget", "Word Processor"));

                    propertyAssignments[2] = new PropertyAssignment();
                    propertyAssignments[2].setFetchOptions(propertyAssignmentFetchOptions);
                    propertyAssignments[2].setPropertyType(new PropertyType());
                    propertyAssignments[2].getPropertyType().setFetchOptions(propertyTypeFetchOptions);
                    propertyAssignments[2].setPlugin(new Plugin());
                    propertyAssignments[2].getPlugin().setFetchOptions(pluginFetchOptions);
                    propertyAssignments[2].getPropertyType().setCode("$ANNOTATIONS_STATE");
                    propertyAssignments[2].setMandatory(false);
                    propertyAssignments[2].setShowInEditView(false);
                    propertyAssignments[2].getPropertyType().setLabel("Annotations State");
                    propertyAssignments[2].getPropertyType().setDataType(DataType.XML);
                    propertyAssignments[2].getPropertyType().setDescription("Annotations State");
                    propertyAssignments[2].getPlugin().setScript("print(\"Hello world\");");

                    return Arrays.asList(propertyAssignments);
                }

            });
        }

    }

}