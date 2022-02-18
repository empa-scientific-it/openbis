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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
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
                        "export-vocabulary.xlsx",
                        VocabularyExpectations.class,
                        Collections.singleton(new ExportablePermId(ExportableKind.VOCABULARY,
                                new VocabularyPermId("ANTIBODY.DETECTION")))
                },
                {
                    "export-sample.xlsx",
                    SampleTypeExpectations.class,
                    Collections.singleton(new ExportablePermId(ExportableKind.SAMPLE_TYPE,
                            new EntityTypePermId("ENTRY", EntityKind.SAMPLE)))
                },
                {
                    "export-experiment.xlsx",
                    ExperimentTypeExpectations.class,
                    Collections.singleton(new ExportablePermId(ExportableKind.EXPERIMENT_TYPE,
                            new EntityTypePermId("DEFAULT_EXPERIMENT", EntityKind.EXPERIMENT)))
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
            final Collection<ExportablePermId> exportablePermIds) throws Exception
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
        assertEquals(actual.getFirstRowNum(), expected.getFirstRowNum(),
                String.format("First row numbers to not match on sheet: '%s'.", actual.getSheetName()));
        assertEquals(actual.getLastRowNum(), expected.getLastRowNum(),
                String.format("Last row numbers to not match on sheet: '%s'.", actual.getSheetName()));
        expected.rowIterator().forEachRemaining(expectedRow -> assertRowsEqual(actualRowIterator.next(), expectedRow));
    }

    private static void assertRowsEqual(final Row actual, final Row expected)
    {
        final Iterator<Cell> actualCellIterator = actual.cellIterator();
        assertEquals(actual.getFirstCellNum(), expected.getFirstCellNum(),
                String.format("First cell numbers to not match on sheet: '%s', row number (starting from 1): %d.",
                actual.getSheet().getSheetName(), actual.getRowNum() + 1));
        assertEquals(actual.getLastCellNum(), expected.getLastCellNum(),
                String.format("Last cell numbers to not match on sheet: '%s', row number (starting from 1): %d.",
                actual.getSheet().getSheetName(), actual.getRowNum() + 1));
        expected.cellIterator().forEachRemaining(expectedCell ->
                assertCellsEqual(actualCellIterator.next(), expectedCell));
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
                assertEquals(actual.getNumericCellValue(), expected.getNumericCellValue(), 0.000001,
                        getErrorMessage(actual));
                break;
            }
            case STRING:
            {
                assertEquals(actual.getStringCellValue(), expected.getStringCellValue(), getErrorMessage(actual));
                break;
            }
            case FORMULA:
            {
                assertEquals(actual.getCellFormula(), expected.getCellFormula(), getErrorMessage(actual));
                break;
            }
            case BOOLEAN:
            {
                assertEquals(actual.getBooleanCellValue(), expected.getBooleanCellValue(), getErrorMessage(actual));
                break;
            }
            case ERROR:
            {
                assertEquals(actual.getErrorCellValue(), expected.getErrorCellValue(), getErrorMessage(actual));
                break;
            }
        }
    }

    private static String getErrorMessage(final Cell cell)
    {
        return String.format("Values are not equal at %c:%d.", 'A' + cell.getColumnIndex(), cell.getRowIndex() + 1);
    }

    private static class VocabularyExpectations extends Expectations
    {

        public VocabularyExpectations(final IApplicationServerApi api)
        {
            allowing(api).getVocabularies(with(SESSION_TOKEN), with(new CollectionMatcher<>(
                    Collections.singletonList(new VocabularyPermId("ANTIBODY.DETECTION")))),
                    with(any(VocabularyFetchOptions.class)));

            will(new CustomAction("getting vocabularies")
            {

                @Override
                public Object invoke(final Invocation invocation) throws Throwable
                {
                    final VocabularyFetchOptions fetchOptions = (VocabularyFetchOptions) invocation.getParameter(2);

                    final Vocabulary vocabulary = new Vocabulary();
                    vocabulary.setFetchOptions(fetchOptions);
                    vocabulary.setCode("ANTIBODY.DETECTION");
                    vocabulary.setDescription("Protein detection system");

                    vocabulary.setTerms(getVocabularyTerms(fetchOptions));

                    return Collections.singletonMap(new EntityTypePermId("ANTIBODY.DETECTION"), vocabulary);
                }

                private List<VocabularyTerm> getVocabularyTerms(final VocabularyFetchOptions fetchOptions)
                {
                    final VocabularyTermFetchOptions vocabularyTermFetchOptions = fetchOptions.withTerms();

                    final VocabularyTerm[] vocabularyTerms = new VocabularyTerm[2];

                    vocabularyTerms[0] = new VocabularyTerm();
                    vocabularyTerms[0].setFetchOptions(vocabularyTermFetchOptions);
                    vocabularyTerms[0].setCode("FLUORESCENCE");
                    vocabularyTerms[0].setLabel("fluorescent probe");
                    vocabularyTerms[0].setDescription("The antibody is conjugated with a fluorescent probe");

                    vocabularyTerms[1] = new VocabularyTerm();
                    vocabularyTerms[1].setFetchOptions(vocabularyTermFetchOptions);
                    vocabularyTerms[1].setCode("HRP");
                    vocabularyTerms[1].setLabel("horseradish peroxydase");
                    vocabularyTerms[1].setDescription("The antibody is conjugated with the horseradish peroxydase");

                    return Arrays.asList(vocabularyTerms);
                }

            });
        }

    }

    private static class SampleTypeExpectations extends Expectations
    {

        public SampleTypeExpectations(final IApplicationServerApi api)
        {
            allowing(api).getSampleTypes(with(SESSION_TOKEN), with(new CollectionMatcher<>(
                            Collections.singletonList(new EntityTypePermId("ENTRY", EntityKind.SAMPLE)))),
                    with(any(SampleTypeFetchOptions.class)));

            will(new CustomAction("getting sample types")
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

    private static class ExperimentTypeExpectations extends Expectations
    {

        public ExperimentTypeExpectations(final IApplicationServerApi api)
        {
            allowing(api).getExperimentTypes(with(SESSION_TOKEN), with(new CollectionMatcher<>(
                            Collections.singletonList(
                                    new EntityTypePermId("DEFAULT_EXPERIMENT", EntityKind.EXPERIMENT)))),
                    with(any(ExperimentTypeFetchOptions.class)));

            will(new CustomAction("getting experiment types")
            {

                @Override
                public Object invoke(final Invocation invocation) throws Throwable
                {
                    final ExperimentTypeFetchOptions fetchOptions =
                            (ExperimentTypeFetchOptions) invocation.getParameter(2);
                    final PluginFetchOptions pluginFetchOptions = fetchOptions.withValidationPlugin();

                    final ExperimentType experimentType = new ExperimentType();
                    experimentType.setFetchOptions(fetchOptions);
                    experimentType.setCode("DEFAULT_EXPERIMENT");
                    experimentType.setPropertyAssignments(getPropertyAssignments(fetchOptions));

                    final Plugin validationPlugin = new Plugin();
                    validationPlugin.setScript("def getRenderedProperty(entity, property):\n"
                            + "    value = entity.property(property)\n"
                            + "    if value is not None:\n"
                            + "        return value.renderedValue()\n"
                            + "\n"
                            + "def validate(entity, isNew):\n"
                            + "    start_date = getRenderedProperty(entity, \"START_DATE\")\n"
                            + "    end_date = getRenderedProperty(entity, \"END_DATE\")\n"
                            + "    if start_date is not None and end_date is not None and start_date > end_date:\n"
                            + "        return \"End date cannot be before start date!\"\n");
                    validationPlugin.setFetchOptions(pluginFetchOptions);

                    experimentType.setValidationPlugin(validationPlugin);

                    return Collections.singletonMap(new EntityTypePermId("DEFAULT_EXPERIMENT"), experimentType);
                }

                private List<PropertyAssignment> getPropertyAssignments(final ExperimentTypeFetchOptions fetchOptions)
                {
                    final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions =
                            fetchOptions.withPropertyAssignments();
                    final PropertyTypeFetchOptions propertyTypeFetchOptions =
                            propertyAssignmentFetchOptions.withPropertyType();
                    propertyTypeFetchOptions.withVocabulary();

                    final PluginFetchOptions pluginFetchOptions = propertyAssignmentFetchOptions.withPlugin();
                    pluginFetchOptions.withScript();

                    final PropertyAssignment[] propertyAssignments = new PropertyAssignment[4];

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
                    propertyAssignments[1].getPropertyType().setCode("$DEFAULT_OBJECT_TYPE");
                    propertyAssignments[1].setMandatory(false);
                    propertyAssignments[1].setShowInEditView(true);
                    propertyAssignments[1].setSection("General info");
                    propertyAssignments[1].getPropertyType().setLabel("Default object type");
                    propertyAssignments[1].getPropertyType().setDataType(DataType.VARCHAR);
                    propertyAssignments[1].getPropertyType().setDescription(
                            "Enter the code of the object type for which the collection is used");

                    propertyAssignments[2] = new PropertyAssignment();
                    propertyAssignments[2].setFetchOptions(propertyAssignmentFetchOptions);
                    propertyAssignments[2].setPropertyType(new PropertyType());
                    propertyAssignments[2].getPropertyType().setFetchOptions(propertyTypeFetchOptions);
                    propertyAssignments[2].setPlugin(new Plugin());
                    propertyAssignments[2].getPlugin().setFetchOptions(pluginFetchOptions);
                    propertyAssignments[2].getPropertyType().setCode("NOTES");
                    propertyAssignments[2].setMandatory(false);
                    propertyAssignments[2].setShowInEditView(true);
                    propertyAssignments[2].getPropertyType().setLabel("Notes");
                    propertyAssignments[2].getPropertyType().setDataType(DataType.MULTILINE_VARCHAR);
                    propertyAssignments[2].getPropertyType().setDescription("Notes");
                    propertyAssignments[2].getPropertyType().setMetaData(
                            Collections.singletonMap("custom_widget", "Word Processor"));

                    propertyAssignments[3] = new PropertyAssignment();
                    propertyAssignments[3].setFetchOptions(propertyAssignmentFetchOptions);
                    propertyAssignments[3].setPropertyType(new PropertyType());
                    propertyAssignments[3].getPropertyType().setFetchOptions(propertyTypeFetchOptions);
                    propertyAssignments[3].setPlugin(new Plugin());
                    propertyAssignments[3].getPlugin().setFetchOptions(pluginFetchOptions);
                    propertyAssignments[3].getPropertyType().setCode("$XMLCOMMENTS");
                    propertyAssignments[3].setMandatory(false);
                    propertyAssignments[3].setShowInEditView(false);
                    propertyAssignments[3].getPropertyType().setLabel("Comments List");
                    propertyAssignments[3].getPropertyType().setDataType(DataType.XML);
                    propertyAssignments[3].getPropertyType().setDescription("Comments log");

                    return Arrays.asList(propertyAssignments);
                }

            });
        }

    }

}