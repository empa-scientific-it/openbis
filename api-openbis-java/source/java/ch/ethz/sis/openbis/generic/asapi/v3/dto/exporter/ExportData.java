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

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.exporter.ExportData")
public class ExportData implements Serializable
{

    private static final long serialVersionUID = 1L;

    private final List<ExportablePermId> permIds;

    private final ExportableFields fields;

    public ExportData(final List<ExportablePermId> permIds, final ExportableFields fields)
    {
        this.permIds = permIds;
        this.fields = fields;
    }

    public List<ExportablePermId> getPermIds()
    {
        return permIds;
    }

    public ExportableFields getFields()
    {
        return fields;
    }

}
