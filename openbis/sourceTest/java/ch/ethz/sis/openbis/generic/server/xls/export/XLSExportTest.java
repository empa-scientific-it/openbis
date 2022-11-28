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
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.VOCABULARY;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

public class XLSExportTest
{

    static final String SESSION_TOKEN = "test-token";

    static final String TEST_SCRIPT_CONTENT =
            "def getRenderedProperty(entity, property):\n" +
            "    entity.property(property).renderedValue()";

    static final String DATE_RANGE_VALIDATION_SCRIPT_CONTENT =
            "def validate(entity, isNew):\n" +
            "    start_date = getRenderedProperty(entity, \"START_DATE\")\n" +
            "    end_date = getRenderedProperty(entity, \"END_DATE\")\n" +
            "    if start_date is not None and end_date is not None and start_date > end_date:\n" +
            "        return \"End date cannot be before start date!\"";

    private static final String XLS_EXPORT_DATA_PROVIDER = "xlsExportData";
    public static final Map<String, Map<String, List<String>>> EXPORT_PROPERTIES =
            Map.of(
                    DATASET.toString(), Map.of(
                        "ATTACHMENT", List.of("$ATTACHMENT"),
                        "RAW_DATA", List.of("$NAME", "NOTES")
                    ),
                    EXPERIMENT.toString(), Map.of(
                        "COLLECTION", List.of("$DEFAULT_OBJECT_TYPE"),
                        "DEFAULT_EXPERIMENT", List.of("FINISHED_FLAG")
                    ),
                    SAMPLE.toString(), Map.of(
                        "DEFAULT", List.of(),
                        "STORAGE", List.of("$STORAGE.BOX_NUM")
                    )
            );

    private static final List<String> EXPERIMENT_IMPORT_WARNINGS = List.of(
            "Line: 6 Kind: /TEST/TEST/TEST ID: 'COLLECTION' - "
                    + "Value exceeds the maximum size supported by Excel: 32767.",
            "Line: 5 Kind: /ELN_SETTINGS/STORAGES/STORAGES_COLLECTION ID: 'COLLECTION' - "
                    + "Value exceeds the maximum size supported by Excel: 32767.");

    private Mockery mockery;

    private IApplicationServerApi api;

    @DataProvider
    protected Object[][] xlsExportData()
    {
        return new Object[][] {
                {
                        "export-vocabulary.xlsx",
                        Map.of(),
                        VocabularyExpectations.class,
                        Collections.singletonList(new ExportablePermId(VOCABULARY,
                                new VocabularyPermId("ANTIBODY.DETECTION"))),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-sample-type.xlsx",
                        Map.of(
                                "test", TEST_SCRIPT_CONTENT,
                                "test-dynamic", TEST_SCRIPT_CONTENT
                        ),
                        SampleTypeExpectations.class,
                        Collections.singletonList(new ExportablePermId(SAMPLE_TYPE,
                                new EntityTypePermId("ENTRY", EntityKind.SAMPLE))),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-experiment-type.xlsx",
                        Map.of("test", TEST_SCRIPT_CONTENT),
                        ExperimentTypeExpectations.class,
                        Collections.singletonList(new ExportablePermId(EXPERIMENT_TYPE,
                                new EntityTypePermId("DEFAULT_EXPERIMENT", EntityKind.EXPERIMENT))),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-data-set-type.xlsx",
                        Map.of(),
                        DataSetTypeExpectations.class,
                        Collections.singletonList(new ExportablePermId(DATASET_TYPE,
                                new EntityTypePermId("ATTACHMENT", EntityKind.DATA_SET))),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-sample-type-with-vocabulary-property.xlsx",
                        Map.of(),
                        SampleTypeWithVocabularyPropertyExpectations.class,
                        List.of(
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("ANTIBODY", EntityKind.SAMPLE)),
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("VIRUS", EntityKind.SAMPLE))),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-sample-type-with-omitted-vocabulary-property.xlsx",
                        Map.of(),
                        SampleTypeWithVocabularyPropertyExpectations.class,
                        List.of(
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("ANTIBODY", EntityKind.SAMPLE)),
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("VIRUS", EntityKind.SAMPLE))),
                        false,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-sample-type-with-sample-property.xlsx",
                        Map.of(
                                "date_range_validation", DATE_RANGE_VALIDATION_SCRIPT_CONTENT,
                                "test", TEST_SCRIPT_CONTENT
                        ),
                        SampleTypeWithSamplePropertyExpectations.class,
                        Collections.singletonList(
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-sample-type-with-bare-sample-property.xlsx",
                        Map.of("date_range_validation", DATE_RANGE_VALIDATION_SCRIPT_CONTENT),
                        SampleTypeWithBareSamplePropertyExpectations.class,
                        Collections.singletonList(
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-sample-type-with-omitted-sample-property.xlsx",
                        Map.of("date_range_validation", DATE_RANGE_VALIDATION_SCRIPT_CONTENT),
                        SampleTypeWithSamplePropertyExpectations.class,
                        Collections.singletonList(
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                        false,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-sample-type-with-chained-sample-properties.xlsx",
                        Map.of("date_range_validation", DATE_RANGE_VALIDATION_SCRIPT_CONTENT),
                        SampleTypeWithChainedSamplePropertiesExpectations.class,
                        Collections.singletonList(
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-sample-type-with-cyclic-sample-properties.xlsx",
                        Map.of("date_range_validation", DATE_RANGE_VALIDATION_SCRIPT_CONTENT),
                        SampleTypeWithCyclicSamplePropertiesExpectations.class,
                        Collections.singletonList(
                                new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-space.xlsx",
                        Map.of(),
                        SpaceExpectations.class,
                        List.of(
                                new ExportablePermId(SPACE, new SpacePermId("ELN_SETTINGS")),
                                new ExportablePermId(SPACE, new SpacePermId("MATERIALS")),
                                new ExportablePermId(SPACE, new SpacePermId("PUBLICATIONS"))
                        ),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-project.xlsx",
                        Map.of(),
                        ProjectExpectations.class,
                        List.of(
                                new ExportablePermId(PROJECT, new ProjectPermId("200001010000000-0001")),
                                new ExportablePermId(PROJECT, new ProjectPermId("200001010000000-0002"))
                        ),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-experiment.xlsx",
                        Map.of(),
                        ExperimentExpectations.class,
                        List.of(
                                new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0001")),
                                new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0002")),
                                new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0003"))
                        ),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        EXPERIMENT_IMPORT_WARNINGS
                },
                {
                        "export-experiment.xlsx",
                        Map.of(),
                        ExperimentExpectations.class,
                        List.of(
                                new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0001")),
                                new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0002")),
                                new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0003"))
                        ),
                        true,
                        Map.of(
                                DATASET.toString(), Map.of(
                                    "ATTACHMENT", List.of("$ATTACHMENT"),
                                    "RAW_DATA", List.of("$NAME", "NOTES")
                                ),
                                EXPERIMENT.toString(), Map.of(),
                                SAMPLE.toString(), Map.of(
                                    "DEFAULT", List.of(),
                                    "STORAGE", List.of("$STORAGE.BOX_NUM")
                                )
                        ),
                        XLSExport.TextFormatting.PLAIN,
                        EXPERIMENT_IMPORT_WARNINGS
                },
                {
                        "export-experiment.xlsx",
                        Map.of(),
                        ExperimentExpectations.class,
                        List.of(
                                new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0001")),
                                new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0002")),
                                new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0003"))
                        ),
                        true,
                        Map.of(
                                DATASET.toString(), Map.of(
                                    "ATTACHMENT", List.of("$ATTACHMENT"),
                                    "RAW_DATA", List.of("$NAME", "NOTES")
                                ),
                                SAMPLE.toString(), Map.of(
                                    "DEFAULT", List.of(),
                                    "STORAGE", List.of("$STORAGE.BOX_NUM")
                                )
                        ),
                        XLSExport.TextFormatting.PLAIN,
                        EXPERIMENT_IMPORT_WARNINGS
                },
                {
                        "export-experiment-filtered-properties.xlsx",
                        Map.of(),
                        ExperimentExpectations.class,
                        List.of(
                                new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0001")),
                                new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0002")),
                                new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0003"))
                        ),
                        true,
                        EXPORT_PROPERTIES,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-sample.xlsx",
                        Map.of(),
                        SampleExpectations.class,
                        List.of(
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0001")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0002")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0003")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0004")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0005"))
                        ),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-sample.xlsx",
                        Map.of(),
                        SampleExpectations.class,
                        List.of(
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0001")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0002")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0003")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0004")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0005"))
                        ),
                        true,
                        Map.of(
                                DATASET.toString(), Map.of(
                                    "ATTACHMENT", List.of("$ATTACHMENT"),
                                    "RAW_DATA", List.of("$NAME", "NOTES")
                                ),
                                EXPERIMENT.toString(), Map.of(
                                    "COLLECTION", List.of("$DEFAULT_OBJECT_TYPE"),
                                    "DEFAULT_EXPERIMENT", List.of("FINISHED_FLAG")
                                ),
                                SAMPLE.toString(), Map.of()
                        ),
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-sample.xlsx",
                        Map.of(),
                        SampleExpectations.class,
                        List.of(
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0001")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0002")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0003")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0004")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0005"))
                        ),
                        true,
                        Map.of(
                                DATASET.toString(), Map.of(
                                    "ATTACHMENT", List.of("$ATTACHMENT"),
                                    "RAW_DATA", List.of("$NAME", "NOTES")
                                ),
                                EXPERIMENT.toString(), Map.of(
                                        "COLLECTION", List.of("$DEFAULT_OBJECT_TYPE"),
                                        "DEFAULT_EXPERIMENT", List.of("FINISHED_FLAG")
                                )
                        ),
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-sample-filtered-properties.xlsx",
                        Map.of(),
                        SampleExpectations.class,
                        List.of(
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0001")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0002")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0003")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0004")),
                                new ExportablePermId(SAMPLE, new SpacePermId("200001010000000-0005"))
                        ),
                        true,
                        EXPORT_PROPERTIES,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-data-set-rich-text.xlsx",
                        Map.of(),
                        DataSetExpectations.class,
                        List.of(
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0001")),
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0002")),
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0003"))
                        ),
                        true,
                        null,
                        XLSExport.TextFormatting.RICH,
                        List.of()
                },
                {
                        "export-data-set-plain-text.xlsx",
                        Map.of(),
                        DataSetExpectations.class,
                        List.of(
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0001")),
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0002")),
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0003"))
                        ),
                        true,
                        null,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-data-set-plain-text.xlsx",
                        Map.of(),
                        DataSetExpectations.class,
                        List.of(
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0001")),
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0002")),
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0003"))
                        ),
                        true,
                        Map.of(
                                DATASET.toString(), Map.of(),
                                EXPERIMENT.toString(), Map.of(
                                        "COLLECTION", List.of("$DEFAULT_OBJECT_TYPE"),
                                        "DEFAULT_EXPERIMENT", List.of("FINISHED_FLAG")
                                ),
                                SAMPLE.toString(), Map.of(
                                        "DEFAULT", List.of(),
                                        "STORAGE", List.of("$STORAGE.BOX_NUM")
                                )
                        ),
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-data-set-plain-text.xlsx",
                        Map.of(),
                        DataSetExpectations.class,
                        List.of(
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0001")),
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0002")),
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0003"))
                        ),
                        true,
                        Map.of(
                                EXPERIMENT.toString(), Map.of(
                                        "COLLECTION", List.of("$DEFAULT_OBJECT_TYPE"),
                                        "DEFAULT_EXPERIMENT", List.of("FINISHED_FLAG")
                                ),
                                SAMPLE.toString(), Map.of(
                                        "DEFAULT", List.of(),
                                        "STORAGE", List.of("$STORAGE.BOX_NUM")
                                )
                        ),
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
                },
                {
                        "export-data-set-filtered-properties.xlsx",
                        Map.of(),
                        DataSetExpectations.class,
                        List.of(
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0001")),
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0002")),
                                new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0003"))
                        ),
                        true,
                        EXPORT_PROPERTIES,
                        XLSExport.TextFormatting.PLAIN,
                        List.of()
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
                        new ExportablePermId(VOCABULARY, new ObjectPermId("V1")),
                        new ExportablePermId(VOCABULARY, new ObjectPermId("V2")),
                        new ExportablePermId(VOCABULARY, new ObjectPermId("V3"))
                ),
                List.of(
                        new ExportablePermId(VOCABULARY, new ObjectPermId("V4")),
                        new ExportablePermId(VOCABULARY, new ObjectPermId("V5")),
                        new ExportablePermId(VOCABULARY, new ObjectPermId("V6"))
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
            if (group.iterator().next().getExportableKind() == VOCABULARY)
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
                new ExportablePermId(VOCABULARY, new ObjectPermId("V1")),
                new ExportablePermId(VOCABULARY, new ObjectPermId("V2")),
                new ExportablePermId(VOCABULARY, new ObjectPermId("V3")),
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
            final Class<IApplicationServerApi> expectationsClass, final Collection<ExportablePermId> exportablePermIds,
            final boolean exportReferred, final Map<String, Map<String, Collection<String>>> exportProperties,
            final XLSExport.TextFormatting textFormatting, final Collection<String> expectedWarnings) throws Exception
    {
        final Expectations expectations = (Expectations) expectationsClass.getConstructor(IApplicationServerApi.class,
                boolean.class).newInstance(api, exportReferred);
        mockery.checking(expectations);

        try
        {
            final XLSExport.PrepareWorkbookResult actualResult = XLSExport.prepareWorkbook(
                    api, SESSION_TOKEN, exportablePermIds, exportReferred, exportProperties,
                    textFormatting);
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