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

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.AllFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportableKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.XlsTextFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

public class ExportData
{

    static final Object[][] EXPORT_DATA = {
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
    };

    private ExportData()
    {
        throw new UnsupportedOperationException();
    }

}
