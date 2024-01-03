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
package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public interface IXLSExportHelper<ENTITY_TYPE extends IEntityType>
{

    AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final List<String> permIds, final int rowNumber,
            final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap,
            final XLSExport.TextFormatting textFormatting, final boolean compatibleWithImport);

    ENTITY_TYPE getEntityType(final IApplicationServerApi api, final String sessionToken, final String permId);

    class AdditionResult
    {
        private final int rowNumber;

        private final Collection<String> warnings;

        private final Map<String, String> valueFiles;

        public AdditionResult(final int rowNumber, final Collection<String> warnings, final Map<String, String> valueFiles)
        {
            this.rowNumber = rowNumber;
            this.warnings = warnings;
            this.valueFiles = valueFiles;
        }

        public int getRowNumber()
        {
            return rowNumber;
        }

        public Collection<String> getWarnings()
        {
            return warnings;
        }

        public Map<String, String> getValueFiles()
        {
            return valueFiles;
        }
    }

}
