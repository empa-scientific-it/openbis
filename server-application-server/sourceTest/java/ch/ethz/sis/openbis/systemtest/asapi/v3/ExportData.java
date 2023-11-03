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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.AUTO_GENERATE_CODES;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.CHILDREN;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.CODE;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.DESCRIPTION;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.DISALLOW_DELETION;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.GENERATED_CODE_PREFIX;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.IDENTIFIER;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.MODIFICATION_DATE;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.MODIFIER;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.PARENTS;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.PERM_ID;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.PRESENT_IN_ARCHIVE;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.PROJECT;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.REGISTRATION_DATE;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.REGISTRATOR;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.SAMPLE;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.SPACE;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.STORAGE_CONFIRMATION;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.UNIQUE_SUBCODES;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.AllFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportableKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.SelectedFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.XlsTextFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

public class ExportData
{

    static final String RICH_TEXT_PROPERTY_NAME = "MULTILINE";

    static final Object[][] EXPORT_DATA = {
            // All fields
            {
                    // Non-existing sample
                    "empty.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE, new SamplePermId("WrongPermId"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Space: TEST-SPACE
                    "export-space.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SPACE, new SpacePermId("TEST-SPACE"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Project: TEST-PROJECT
                    "export-project.xlsx",
                    List.of(new ExportablePermId(ExportableKind.PROJECT, new ProjectPermId("20120814110011738-105"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Experiment: EXP-SPACE-TEST
                    "export-experiment.xlsx",
                    List.of(new ExportablePermId(ExportableKind.EXPERIMENT, new ExperimentPermId("201206190940555-1032"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Sample: /TEST-SPACE/TEST-PROJECT/FV-TEST
                    "export-sample.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE, new SamplePermId("201206191219327-1054"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Sample: /TEST-SPACE/TEST-PROJECT/FV-TEST
                    "export-sample-compatible-with-import.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE, new SamplePermId("201206191219327-1054"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    true // withImportCompatibility
            },
            {
                    // Data set: "ROOT_CONTAINER"
                    "export-data-set.xlsx",
                    List.of(new ExportablePermId(
                            ExportableKind.DATASET, new DataSetPermId("ROOT_CONTAINER"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Sample Type: CELL_PLATE
                    "export-sample-type.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE_TYPE, new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    false, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Sample Type: CELL_PLATE
                    "export-sample-type-with-referred-types.zip",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE_TYPE, new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Experiment Type: SIRNA_HCS
                    "export-experiment-type.xlsx",
                    List.of(new ExportablePermId(ExportableKind.EXPERIMENT_TYPE, new EntityTypePermId("SIRNA_HCS", EntityKind.EXPERIMENT))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    false, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Dataset Type: HCS_IMAGE
                    "export-data-set-type.xlsx",
                    List.of(new ExportablePermId(ExportableKind.DATASET_TYPE, new EntityTypePermId("HCS_IMAGE", EntityKind.DATA_SET))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    false, // withReferredTypes
                    false // withImportCompatibility
            },

            // Selected fields
            {
                    // Space: TEST-SPACE
                    "export-space-filtered-attributes.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SPACE, new SpacePermId("TEST-SPACE"))),
                    new SelectedFields(List.of(CODE, REGISTRATOR, DESCRIPTION), List.of()),
                    XlsTextFormat.PLAIN,
                    false, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Project: TEST-PROJECT
                    "export-project-filtered-fields.xlsx",
                    List.of(new ExportablePermId(ExportableKind.PROJECT, new ProjectPermId("20120814110011738-105"))),
                    new SelectedFields(
                            List.of(REGISTRATOR, REGISTRATION_DATE, CODE, IDENTIFIER, SPACE, DESCRIPTION),
                            List.of()),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Experiment: EXP-SPACE-TEST
                    "export-experiment-filtered-fields.xlsx",
                    List.of(new ExportablePermId(ExportableKind.EXPERIMENT, new ExperimentPermId("201206190940555-1032"))),
                    new SelectedFields(
                            List.of(CODE, PERM_ID, IDENTIFIER, PROJECT, REGISTRATOR, MODIFIER),
                            List.of(new PropertyTypePermId("GENDER"), new PropertyTypePermId("DESCRIPTION"))),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Sample: /TEST-SPACE/TEST-PROJECT/FV-TEST
                    "export-sample-filtered-fields.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE,
                            new SamplePermId("201206191219327-1054"))),
                    new SelectedFields(
                            List.of(CODE, PERM_ID, IDENTIFIER, SPACE, PARENTS, CHILDREN, REGISTRATOR, REGISTRATION_DATE, MODIFIER, MODIFICATION_DATE),
                            List.of(new PropertyTypePermId("BACTERIUM"), new PropertyTypePermId("COMMENT"), new PropertyTypePermId("ORGANISM"))),
                    XlsTextFormat.PLAIN,
                    false, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Data set: "20081105092159188-3", type: "HCS_IMAGE"
                    "export-data-set-filtered-fields.xlsx",
                    List.of(new ExportablePermId(ExportableKind.DATASET, new DataSetPermId("20081105092159188-3"))),
                    new SelectedFields(
                            List.of(REGISTRATOR, REGISTRATION_DATE, CODE, IDENTIFIER, PARENTS, CHILDREN, STORAGE_CONFIRMATION, PRESENT_IN_ARCHIVE,
                                    SAMPLE, EXPERIMENT),
                            List.of(new PropertyTypePermId("COMMENT"), new PropertyTypePermId("GENDER"))),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Sample Type: CELL_PLATE
                    "export-sample-type-filtered-attributes.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE_TYPE, new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE))),
                    new SelectedFields(List.of(CODE, AUTO_GENERATE_CODES, DESCRIPTION, GENERATED_CODE_PREFIX, UNIQUE_SUBCODES), List.of()),
                    XlsTextFormat.PLAIN,
                    false, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Experiment Type: SIRNA_HCS
                    "export-experiment-type-filtered-attributes.xlsx",
                    List.of(new ExportablePermId(ExportableKind.EXPERIMENT_TYPE, new EntityTypePermId("SIRNA_HCS", EntityKind.EXPERIMENT))),
                    new SelectedFields(List.of(DESCRIPTION, CODE, MODIFICATION_DATE), List.of()),
                    XlsTextFormat.PLAIN,
                    false, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Dataset Type: HCS_IMAGE
                    "export-data-set-type-filtered-attributes.xlsx",
                    List.of(new ExportablePermId(ExportableKind.DATASET_TYPE, new EntityTypePermId("HCS_IMAGE", EntityKind.DATA_SET))),
                    new SelectedFields(List.of(CODE, DISALLOW_DELETION, DESCRIPTION), List.of()),
                    XlsTextFormat.PLAIN,
                    false, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Sample: /TEST-SPACE/TEST-PROJECT/FV-TEST
                    "export-sample-plain-text.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE, null)), // null perm ID indicates that the newly created value in set up should be used.
                    new SelectedFields(List.of(IDENTIFIER, CODE), List.of(new PropertyTypePermId(RICH_TEXT_PROPERTY_NAME))),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Sample: /TEST-SPACE/TEST-PROJECT/FV-TEST
                    "export-sample-rich-text.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE, null)), // null perm ID indicates that the newly created value in set up should be used.
                    new SelectedFields(List.of(IDENTIFIER, CODE), List.of(new PropertyTypePermId(RICH_TEXT_PROPERTY_NAME))),
                    XlsTextFormat.RICH,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Sample: /TEST-SPACE/TEST-PROJECT/FV-TEST
                    "export-sample-rich-text.xlsx",
                    List.of(new ExportablePermId(ExportableKind.SAMPLE, null)), // null perm ID indicates that the newly created value in set up should be used.
                    new SelectedFields(List.of(IDENTIFIER, CODE), List.of(new PropertyTypePermId(RICH_TEXT_PROPERTY_NAME))),
                    XlsTextFormat.RICH,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
    };

    private ExportData()
    {
        throw new UnsupportedOperationException();
    }

}
