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

import java.util.EnumSet;
import java.util.Set;

public enum ExportableKind
{
    SAMPLE_TYPE, EXPERIMENT_TYPE, DATASET_TYPE, VOCABULARY_TYPE,
    SPACE, PROJECT, SAMPLE, EXPERIMENT, DATASET;
//    TODO:
//    USER, GROUP

    public static final Set<ExportableKind>
            MASTER_DATA_EXPORTABLE_KINDS = EnumSet.of(SAMPLE_TYPE, EXPERIMENT_TYPE, DATASET_TYPE, VOCABULARY_TYPE);

}
