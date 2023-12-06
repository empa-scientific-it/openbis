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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.ExportOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.ExportOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.ExportResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

@Component
public class ExportOperationExecutor extends OperationExecutor<ExportOperation, ExportOperationResult>
{

    @Autowired
    private IExportExecutor executor;

    @Override
    protected Class<? extends ExportOperation> getOperationClass()
    {
        return ExportOperation.class;
    }

    @Override
    protected ExportOperationResult doExecute(final IOperationContext context, final ExportOperation operation)
    {
        final XLSExport.ExportResult exportResult = executor.doExport(context, operation);
        return new ExportOperationResult(new ExportResult(exportResult.getFileName(), exportResult.getWarnings()));
    }

}
