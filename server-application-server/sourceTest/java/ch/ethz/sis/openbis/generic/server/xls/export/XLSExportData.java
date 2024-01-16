/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.xls.export;

import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.DATASET;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.DATASET_TYPE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.EXPERIMENT_TYPE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.PROJECT;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.SAMPLE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.SAMPLE_TYPE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.SPACE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.VOCABULARY_TYPE;
import static ch.ethz.sis.openbis.generic.server.xls.export.helper.AbstractXLSExportHelper.FIELD_ID_KEY;
import static ch.ethz.sis.openbis.generic.server.xls.export.helper.AbstractXLSExportHelper.FIELD_TYPE_KEY;

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;

public class XLSExportData
{

    static final String TEST_SCRIPT_CONTENT =
            "def getRenderedProperty(entity, property):\n" +
            "    entity.property(property).renderedValue()";

    static final String DATE_RANGE_VALIDATION_SCRIPT_CONTENT =
            "def validate(entity, isNew):\n" +
            "    start_date = getRenderedProperty(entity, \"START_DATE\")\n" +
            "    end_date = getRenderedProperty(entity, \"END_DATE\")\n" +
            "    if start_date is not None and end_date is not None and start_date > end_date:\n" +
            "        return \"End date cannot be before start date!\"";

    public static final Map<String, Map<String, List<Map<String, String>>>> EXPORT_FIELDS =
            Map.of(
                    "TYPE", Map.of(
                            SAMPLE_TYPE.toString(), List.of(
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "CODE"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "DESCRIPTION"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "AUTO_GENERATE_CODES"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "GENERATED_CODE_PREFIX")
                            ),
                            EXPERIMENT_TYPE.toString(), List.of(
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "DESCRIPTION"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "CODE")
                            ),
                            DATASET_TYPE.toString(), List.of(
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "DESCRIPTION"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "CODE")
                            ),
                            PROJECT.toString(), List.of(
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "IDENTIFIER"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "CODE"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "SPACE"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "REGISTRATION_DATE"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "REGISTRATOR")
                            ),
                            SPACE.toString(), List.of(
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "CODE"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "REGISTRATOR")
                            ),
                            VOCABULARY_TYPE.toString(), List.of(
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "CODE")
                            )
                    ),
                    DATASET.toString(), Map.of(
                            "ATTACHMENT", List.of(
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "CODE"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "SAMPLE"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(), FIELD_ID_KEY, "$ATTACHMENT"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "REGISTRATION_DATE"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "REGISTRATOR")),
                            "RAW_DATA", List.of(
                                    Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(), FIELD_ID_KEY, "$NAME"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "EXPERIMENT"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "CODE"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(), FIELD_ID_KEY, "NOTES"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "MODIFIER"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "MODIFICATION_DATE"))
                    ),
                    EXPERIMENT.toString(), Map.of(
                            "COLLECTION", List.of(
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "IDENTIFIER"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "PROJECT"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(), FIELD_ID_KEY, "$DEFAULT_OBJECT_TYPE")),
                            "DEFAULT_EXPERIMENT", List.of(Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                            FIELD_ID_KEY, "FINISHED_FLAG"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "IDENTIFIER"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "CODE"))
                    ),
                    SAMPLE.toString(), Map.of(
                            "DEFAULT", List.of(
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "IDENTIFIER"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "CODE"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "SPACE"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "PROJECT"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "EXPERIMENT")),
                            "STORAGE", List.of(
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "CODE"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "IDENTIFIER"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(), FIELD_ID_KEY, "$STORAGE.BOX_NUM"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "PARENTS"),
                                    Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "CHILDREN"))
                    )
            );

    static final Object[][] EXPORT_DATA = {
                            {
                                    "export-vocabulary.xlsx",
                                    Map.of(),
                                    VocabularyExpectations.class,
                                    List.of(new ExportablePermId(VOCABULARY_TYPE,
                                            new VocabularyPermId("ANTIBODY.DETECTION"))),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-vocabulary-compatible-with-import.xlsx",
                                    Map.of(),
                                    VocabularyExpectations.class,
                                    List.of(new ExportablePermId(VOCABULARY_TYPE,
                                            new VocabularyPermId("ANTIBODY.DETECTION"))),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
                            },
                            {
                                    "export-vocabulary-filtered-attributes.xlsx",
                                    Map.of(),
                                    VocabularyExpectations.class,
                                    List.of(new ExportablePermId(VOCABULARY_TYPE,
                                            new VocabularyPermId("ANTIBODY.DETECTION"))),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-vocabulary-filtered-attributes-compatible-with-import.xlsx",
                                    Map.of(),
                                    VocabularyExpectations.class,
                                    List.of(new ExportablePermId(VOCABULARY_TYPE,
                                            new VocabularyPermId("ANTIBODY.DETECTION"))),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
                            },
                            {
                                    "export-sample-type.xlsx",
                                    Map.of(
                                            "test", TEST_SCRIPT_CONTENT,
                                            "test-dynamic", TEST_SCRIPT_CONTENT
                                    ),
                                    SampleTypeExpectations.class,
                                    List.of(new ExportablePermId(SAMPLE_TYPE,
                                            new EntityTypePermId("ENTRY", EntityKind.SAMPLE))),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-sample-type-compatible-with-import.xlsx",
                                    Map.of(
                                            "test", TEST_SCRIPT_CONTENT,
                                            "test-dynamic", TEST_SCRIPT_CONTENT
                                    ),
                                    SampleTypeExpectations.class,
                                    List.of(new ExportablePermId(SAMPLE_TYPE,
                                            new EntityTypePermId("ENTRY", EntityKind.SAMPLE))),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
                            },
                            {
                                    "export-sample-type-filtered-attributes.xlsx",
                                    Map.of(
                                            "test", TEST_SCRIPT_CONTENT,
                                            "test-dynamic", TEST_SCRIPT_CONTENT
                                    ),
                                    SampleTypeExpectations.class,
                                    List.of(new ExportablePermId(SAMPLE_TYPE,
                                            new EntityTypePermId("ENTRY", EntityKind.SAMPLE))),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-sample-type-filtered-attributes-compatible-with-import.xlsx",
                                    Map.of(
                                            "test", TEST_SCRIPT_CONTENT,
                                            "test-dynamic", TEST_SCRIPT_CONTENT
                                    ),
                                    SampleTypeExpectations.class,
                                    List.of(new ExportablePermId(SAMPLE_TYPE,
                                            new EntityTypePermId("ENTRY", EntityKind.SAMPLE))),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
                            },
                            {
                                    "export-experiment-type.xlsx",
                                    Map.of("test", TEST_SCRIPT_CONTENT),
                                    ExperimentTypeExpectations.class,
                                    List.of(new ExportablePermId(EXPERIMENT_TYPE,
                                            new EntityTypePermId("DEFAULT_EXPERIMENT", EntityKind.EXPERIMENT))),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-experiment-type-filtered-attributes.xlsx",
                                    Map.of("test", TEST_SCRIPT_CONTENT),
                                    ExperimentTypeExpectations.class,
                                    List.of(new ExportablePermId(EXPERIMENT_TYPE,
                                            new EntityTypePermId("DEFAULT_EXPERIMENT", EntityKind.EXPERIMENT))),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-experiment-type-compatible-with-import.xlsx",
                                    Map.of("test", TEST_SCRIPT_CONTENT),
                                    ExperimentTypeExpectations.class,
                                    List.of(new ExportablePermId(EXPERIMENT_TYPE,
                                            new EntityTypePermId("DEFAULT_EXPERIMENT", EntityKind.EXPERIMENT))),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
                            },
                            {
                                    "export-experiment-type-filtered-attributes-compatible-with-import.xlsx",
                                    Map.of("test", TEST_SCRIPT_CONTENT),
                                    ExperimentTypeExpectations.class,
                                    List.of(new ExportablePermId(EXPERIMENT_TYPE,
                                            new EntityTypePermId("DEFAULT_EXPERIMENT", EntityKind.EXPERIMENT))),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
                            },
                            {
                                    "export-data-set-type.xlsx",
                                    Map.of(),
                                    DataSetTypeExpectations.class,
                                    List.of(new ExportablePermId(DATASET_TYPE,
                                            new EntityTypePermId("ATTACHMENT", EntityKind.DATA_SET))),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-data-set-type-filtered-attributes.xlsx",
                                    Map.of(),
                                    DataSetTypeExpectations.class,
                                    List.of(new ExportablePermId(DATASET_TYPE,
                                            new EntityTypePermId("ATTACHMENT", EntityKind.DATA_SET))),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-data-set-type-compatible-with-import.xlsx",
                                    Map.of(),
                                    DataSetTypeExpectations.class,
                                    List.of(new ExportablePermId(DATASET_TYPE,
                                            new EntityTypePermId("ATTACHMENT", EntityKind.DATA_SET))),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
                            },
                            {
                                    "export-data-set-type-filtered-attributes-compatible-with-import.xlsx",
                                    Map.of(),
                                    DataSetTypeExpectations.class,
                                    List.of(new ExportablePermId(DATASET_TYPE,
                                            new EntityTypePermId("ATTACHMENT", EntityKind.DATA_SET))),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
                            },
                            {
                                    "export-sample-type-with-vocabulary-property-compatible-with-import.xlsx",
                                    Map.of(),
                                    SampleTypeWithVocabularyPropertyExpectations.class,
                                    List.of(
                                            new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("ANTIBODY", EntityKind.SAMPLE)),
                                            new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("VIRUS", EntityKind.SAMPLE))),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
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
                                    List.of(),
                                    false
                            },
                            {
                                    "export-sample-type-with-omitted-vocabulary-property-compatible-with-import.xlsx",
                                    Map.of(),
                                    SampleTypeWithVocabularyPropertyExpectations.class,
                                    List.of(
                                            new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("ANTIBODY", EntityKind.SAMPLE)),
                                            new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("VIRUS", EntityKind.SAMPLE))),
                                    false,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
                            },
                            {
                                    "export-sample-type-with-sample-property.xlsx",
                                    Map.of(
                                            "date_range_validation", DATE_RANGE_VALIDATION_SCRIPT_CONTENT,
                                            "test", TEST_SCRIPT_CONTENT
                                    ),
                                    SampleTypeWithSamplePropertyExpectations.class,
                                    List.of(new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-sample-type-with-bare-sample-property.xlsx",
                                    Map.of("date_range_validation", DATE_RANGE_VALIDATION_SCRIPT_CONTENT),
                                    SampleTypeWithBareSamplePropertyExpectations.class,
                                    List.of(new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-sample-type-with-omitted-sample-property.xlsx",
                                    Map.of(),
                                    SampleTypeWithSamplePropertyExpectations.class,
                                    List.of(new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                                    false,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-sample-type-with-chained-sample-properties.xlsx",
                                    Map.of("date_range_validation", DATE_RANGE_VALIDATION_SCRIPT_CONTENT),
                                    SampleTypeWithChainedSamplePropertiesExpectations.class,
                                    List.of(new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-sample-type-with-cyclic-sample-properties.xlsx",
                                    Map.of("date_range_validation", DATE_RANGE_VALIDATION_SCRIPT_CONTENT),
                                    SampleTypeWithCyclicSamplePropertiesExpectations.class,
                                    List.of(new ExportablePermId(SAMPLE_TYPE, new EntityTypePermId("COURSE", EntityKind.SAMPLE))),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
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
                                    List.of(),
                                    false
                            },
                            {
                                    "export-space-compatible-with-import.xlsx",
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
                                    List.of(),
                                    true
                            },
                            {
                                    "export-space-filtered-attributes.xlsx",
                                    Map.of(),
                                    SpaceExpectations.class,
                                    List.of(
                                            new ExportablePermId(SPACE, new SpacePermId("ELN_SETTINGS")),
                                            new ExportablePermId(SPACE, new SpacePermId("MATERIALS")),
                                            new ExportablePermId(SPACE, new SpacePermId("PUBLICATIONS"))
                                    ),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-space-compatible-with-import.xlsx",
                                    Map.of(),
                                    SpaceExpectations.class,
                                    List.of(
                                            new ExportablePermId(SPACE, new SpacePermId("ELN_SETTINGS")),
                                            new ExportablePermId(SPACE, new SpacePermId("MATERIALS")),
                                            new ExportablePermId(SPACE, new SpacePermId("PUBLICATIONS"))
                                    ),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
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
                                    List.of(),
                                    false
                            },
                            {
                                    "export-project-compatible-with-import.xlsx",
                                    Map.of(),
                                    ProjectExpectations.class,
                                    List.of(
                                            new ExportablePermId(PROJECT, new ProjectPermId("200001010000000-0001")),
                                            new ExportablePermId(PROJECT, new ProjectPermId("200001010000000-0002"))
                                    ),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
                            },
                            {
                                    "export-project-filtered-attributes.xlsx",
                                    Map.of(),
                                    ProjectExpectations.class,
                                    List.of(
                                            new ExportablePermId(PROJECT, new ProjectPermId("200001010000000-0001")),
                                            new ExportablePermId(PROJECT, new ProjectPermId("200001010000000-0002"))
                                    ),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-project-filtered-attributes-compatible-with-import.xlsx",
                                    Map.of(),
                                    ProjectExpectations.class,
                                    List.of(
                                            new ExportablePermId(PROJECT, new ProjectPermId("200001010000000-0001")),
                                            new ExportablePermId(PROJECT, new ProjectPermId("200001010000000-0002"))
                                    ),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
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
                                    List.of(),
                                    false
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
                                                    "ATTACHMENT", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$ATTACHMENT")),
                                                    "RAW_DATA", List.of(Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$NAME"),
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "NOTES"))
                                            ),
                                            EXPERIMENT.toString(), Map.of(),
                                            SAMPLE.toString(), Map.of(
                                                    "DEFAULT", List.of(),
                                                    "STORAGE", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$STORAGE.BOX_NUM"))
                                            )
                                    ),
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
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
                                                    "ATTACHMENT", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.name(),
                                                                    FIELD_ID_KEY, "$ATTACHMENT")),
                                                    "RAW_DATA", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.name(), FIELD_ID_KEY, "$NAME"),
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.name(), FIELD_ID_KEY, "NOTES")
                                                    )
                                            ),
                                            SAMPLE.toString(), Map.of(
                                                    "DEFAULT", List.of(),
                                                    "STORAGE", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.name(),
                                                                    FIELD_ID_KEY, "$STORAGE.BOX_NUM"))
                                            )
                                    ),
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-experiment-filtered-fields.xlsx",
                                    Map.of(),
                                    ExperimentExpectations.class,
                                    List.of(
                                            new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0001")),
                                            new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0002")),
                                            new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0003"))
                                    ),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-experiment-filtered-fields.xlsx",
                                    Map.of(),
                                    ExperimentExpectations.class,
                                    List.of(
                                            new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0001")),
                                            new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0002")),
                                            new ExportablePermId(EXPERIMENT, new ExperimentPermId("200001010000000-0003"))
                                    ),
                                    true,
                                    Map.of(
                                            EXPERIMENT.toString(), Map.of(
                                                    "COLLECTION", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "IDENTIFIER"),
                                                            Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "PROJECT"),
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(), FIELD_ID_KEY,
                                                                    "$DEFAULT_OBJECT_TYPE"),
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(), FIELD_ID_KEY, "FINISHED_FLAG")),
                                                    "DEFAULT_EXPERIMENT", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(), FIELD_ID_KEY, "FINISHED_FLAG"),
                                                            Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "IDENTIFIER"),
                                                            Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, "CODE"),
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(), FIELD_ID_KEY,
                                                                    "$DEFAULT_OBJECT_TYPE"))
                                            )
                                    ),
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-sample-compatible-with-import.xlsx",
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
                                    List.of(),
                                    true
                            },
                            {
                                    "export-sample-filtered-fields-compatible-with-import.xlsx",
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
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    true
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
                                    List.of(),
                                    false
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
                                                    "ATTACHMENT", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$ATTACHMENT")),
                                                    "RAW_DATA", List.of(Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$NAME"),
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "NOTES"))
                                            ),
                                            EXPERIMENT.toString(), Map.of(
                                                    "COLLECTION", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$DEFAULT_OBJECT_TYPE")),
                                                    "DEFAULT_EXPERIMENT", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "FINISHED_FLAG"))
                                            ),
                                            SAMPLE.toString(), Map.of()
                                    ),
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
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
                                                    "ATTACHMENT", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$ATTACHMENT")),
                                                    "RAW_DATA", List.of(Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$NAME"),
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "NOTES"))
                                            ),
                                            EXPERIMENT.toString(), Map.of(
                                                    "COLLECTION", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$DEFAULT_OBJECT_TYPE")),
                                                    "DEFAULT_EXPERIMENT", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "FINISHED_FLAG"))
                                            ),
                                            SAMPLE.toString(), Map.of(
                                                    "DEFAULT", List.of()
                                            )
                                    ),
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
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
                                                    "ATTACHMENT", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$ATTACHMENT")),
                                                    "RAW_DATA", List.of(Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$NAME"),
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "NOTES"))
                                            ),
                                            EXPERIMENT.toString(), Map.of(
                                                    "COLLECTION", List.of(Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                            FIELD_ID_KEY, "$DEFAULT_OBJECT_TYPE")),
                                                    "DEFAULT_EXPERIMENT", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "FINISHED_FLAG"))
                                            )
                                    ),
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-sample-filtered-fields.xlsx",
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
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "empty.xlsx",
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
                                    List.of(),
                                    true
                            },
                            {
                                    "empty.xlsx",
                                    Map.of(),
                                    DataSetExpectations.class,
                                    List.of(),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.RICH,
                                    List.of(),
                                    true
                            },
                            {
                                    "empty.xlsx",
                                    Map.of(),
                                    SampleExpectations.class,
                                    List.of(),
                                    true,
                                    null,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "empty.xlsx",
                                    Map.of(),
                                    DataSetExpectations.class,
                                    List.of(
                                            new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0001")),
                                            new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0002")),
                                            new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0003"))
                                    ),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.RICH,
                                    List.of(),
                                    true
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
                                    List.of(),
                                    false
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
                                    List.of(),
                                    false
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
                                                    "COLLECTION", List.of(Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                            FIELD_ID_KEY, "$DEFAULT_OBJECT_TYPE")),
                                                    "DEFAULT_EXPERIMENT", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "FINISHED_FLAG"))
                                            ),
                                            SAMPLE.toString(), Map.of(
                                                    "DEFAULT", List.of(),
                                                    "STORAGE", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$STORAGE.BOX_NUM"))
                                            )
                                    ),
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
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
                                                    "COLLECTION", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$DEFAULT_OBJECT_TYPE")),
                                                    "DEFAULT_EXPERIMENT", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "FINISHED_FLAG"))
                                            ),
                                            SAMPLE.toString(), Map.of(
                                                    "DEFAULT", List.of(),
                                                    "STORAGE", List.of(
                                                            Map.of(FIELD_TYPE_KEY, FieldType.PROPERTY.toString(),
                                                                    FIELD_ID_KEY, "$STORAGE.BOX_NUM"))
                                            )
                                    ),
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                            {
                                    "export-data-set-filtered-fields.xlsx",
                                    Map.of(),
                                    DataSetExpectations.class,
                                    List.of(
                                            new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0001")),
                                            new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0002")),
                                            new ExportablePermId(DATASET, new DataSetPermId("200001010000000-0003"))
                                    ),
                                    true,
                                    EXPORT_FIELDS,
                                    XLSExport.TextFormatting.PLAIN,
                                    List.of(),
                                    false
                            },
                    };

    private XLSExportData()
    {
        throw new UnsupportedOperationException();
    }

}
