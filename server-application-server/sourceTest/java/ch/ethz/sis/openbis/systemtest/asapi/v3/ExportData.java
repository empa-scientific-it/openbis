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

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.CHILDREN;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.CODE;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.DESCRIPTION;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute.EXPERIMENT;
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

    static final Object[][] EXPORT_DATA = {
            // All fields
            {
                    // Non-existing sample
                    "empty.xlsx",
                    List.of(new ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId(ExportableKind.SAMPLE, new SamplePermId("WrongPermId"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Space: TEST-SPACE
                    "export-space.xlsx",
                    List.of(new ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId(ExportableKind.SPACE, new SpacePermId("TEST-SPACE"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Project: TEST-PROJECT
                    "export-project.xlsx",
                    List.of(new ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId(ExportableKind.PROJECT, new ProjectPermId("20120814110011738-105"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Experiment: EXP-SPACE-TEST
                    "export-experiment.xlsx",
                    List.of(new ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId(ExportableKind.EXPERIMENT, new ExperimentPermId("201206190940555-1032"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Sample: /TEST-SPACE/TEST-PROJECT/FV-TEST
                    "export-sample.xlsx",
                    List.of(new ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId(ExportableKind.SAMPLE, new SamplePermId("201206191219327-1054"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Sample: /TEST-SPACE/TEST-PROJECT/FV-TEST
                    "export-sample-compatible-with-import.xlsx",
                    List.of(new ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId(ExportableKind.SAMPLE, new SamplePermId("201206191219327-1054"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    true // withImportCompatibility
            },
            {
                    // Sample Type: CELL_PLATE
                    "export-sample-type-with-referred-types.zip",
                    List.of(new ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId(ExportableKind.SAMPLE_TYPE, new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Experiment Type: SIRNA_HCS
                    "export-experiment-type.xlsx",
                    List.of(new ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId(ExportableKind.EXPERIMENT_TYPE, new EntityTypePermId("SIRNA_HCS", EntityKind.EXPERIMENT))),
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
                    // Sample: /TEST-SPACE/TEST-PROJECT/FV-TEST
                    "export-sample-filtered-fields.xlsx",
                    List.of(new ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId(ExportableKind.SAMPLE,
                            new SamplePermId("201206191219327-1054"))),
                    new SelectedFields(
                            List.of(CODE, PERM_ID, IDENTIFIER, SPACE, PARENTS, CHILDREN, REGISTRATOR, REGISTRATION_DATE, MODIFIER, MODIFICATION_DATE),
                            List.of(new PropertyTypePermId("BACTERIUM"), new PropertyTypePermId("COMMENT"), new PropertyTypePermId("ORGANISM"))),
                    XlsTextFormat.PLAIN,
                    false, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Experiment: EXP-SPACE-TEST
                    "export-experiment-filtered-fields.xlsx",
                    List.of(new ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId(ExportableKind.EXPERIMENT,
                            new ExperimentPermId("201206190940555-1032"))),
                    new SelectedFields(
                            List.of(CODE, PERM_ID, IDENTIFIER, PROJECT, REGISTRATOR, MODIFIER),
                            List.of(new PropertyTypePermId("GENDER"), new PropertyTypePermId("DESCRIPTION"))),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Project: TEST-PROJECT
                    "export-project-filtered-fields.xlsx",
                    List.of(new ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId(ExportableKind.PROJECT, new ProjectPermId("20120814110011738-105"))),
                    new SelectedFields(
                            List.of(REGISTRATOR, REGISTRATION_DATE, CODE, IDENTIFIER, SPACE, DESCRIPTION),
                            List.of()),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // Data set: "ROOT_CONTAINER"
                    "export-data-set.xlsx",
                    List.of(new ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId(
                            ExportableKind.DATASET, new DataSetPermId("ROOT_CONTAINER"))),
                    new AllFields(),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
            {
                    // TODO: the dataset does has the property "COMMENT", but that one does not appear in the search results!!!!!!!

                    // 2023-10-30 17:07:21,620 INFO  [main] OPERATION.AbstractSQLExecutor - QUERY: SELECT DISTINCT t0.id
                    //FROM property_types t0
                    //WHERE (t0.code IN (SELECT UNNEST(?)))
                    //2023-10-30 17:07:21,620 INFO  [main] OPERATION.AbstractSQLExecutor - ARGS: [[GENDER, COMMENT]]
                    //2023-10-30 17:07:21,688 INFO  [main] OPERATION.AbstractSQLExecutor - RESULTS COUNT: 2
                    //2023-10-30 17:07:21,688 INFO  [main] OPERATION.AbstractSQLExecutor - RESULTS: [{id=13}, {id=14}]
                    //2023-10-30 17:07:21,692 INFO  [main] OPERATION.AbstractSQLExecutor - QUERY: SELECT DISTINCT t0.id <-- t0.dsty_id is needed here!!!!!!
                    //FROM data_set_type_property_types t0
                    //WHERE (t0.prty_id IN (SELECT UNNEST(?)))
                    //2023-10-30 17:07:21,692 INFO  [main] OPERATION.AbstractSQLExecutor - ARGS: [[13, 14]]
                    //2023-10-30 17:07:21,694 INFO  [main] OPERATION.AbstractSQLExecutor - RESULTS COUNT: 2
                    //2023-10-30 17:07:21,695 INFO  [main] OPERATION.AbstractSQLExecutor - RESULTS: [{id=1}, {id=4}]
                    //2023-10-30 17:07:21,695 INFO  [main] OPERATION.AbstractSQLExecutor - QUERY: SELECT DISTINCT t0.id
                    //FROM data_set_types t0
                    //WHERE (t0.id IN (SELECT UNNEST(?)))

                    // Data set: "20081105092159188-3", type: "HCS_IMAGE"
                    "export-data-set-filtered-fields.xlsx",
                    List.of(new ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId(
                            ExportableKind.DATASET, new DataSetPermId("20081105092159188-3"))),
                    new SelectedFields(
                            List.of(REGISTRATOR, REGISTRATION_DATE, CODE, IDENTIFIER, PARENTS, CHILDREN, STORAGE_CONFIRMATION, PRESENT_IN_ARCHIVE,
                                    SAMPLE, EXPERIMENT),
                            List.of(new PropertyTypePermId("COMMENT"), new PropertyTypePermId("GENDER"))),
                    XlsTextFormat.PLAIN,
                    true, // withReferredTypes
                    false // withImportCompatibility
            },
    };

    private ExportData()
    {
        throw new UnsupportedOperationException();
    }

}
