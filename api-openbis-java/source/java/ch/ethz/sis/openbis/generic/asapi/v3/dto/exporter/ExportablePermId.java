/*
 * Copyright ETH 2023 Zürich, Scientific IT Services
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
 */

package ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter;

import java.util.Objects;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;

public class ExportablePermId
{

    private final ExportableKind exportableKind;

    private final ObjectPermId permId;

    public ExportablePermId(final ExportableKind exportableKind, final ObjectPermId permId)
    {
        this.exportableKind = Objects.requireNonNull(exportableKind);
        this.permId = Objects.requireNonNull(permId);
    }

    public ExportableKind getExportableKind()
    {
        return exportableKind;
    }

    public ObjectPermId getPermId()
    {
        return permId;
    }

}
