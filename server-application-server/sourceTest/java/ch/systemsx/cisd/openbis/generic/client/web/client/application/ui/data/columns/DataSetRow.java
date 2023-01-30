/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.shared.basic.Row;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetRow extends Row
{
    public DataSetRow()
    {
    }

    public DataSetRow(String code)
    {
        withCell(ExternalDataGridColumnIDs.CODE, code);
    }

    public DataSetRow deleted()
    {
        withDeletion(true);
        return this;
    }

    public DataSetRow valid()
    {
        withDeletion(false);
        return this;
    }

    private void withDeletion(boolean isDeleted)
    {
        withCell(ExternalDataGridColumnIDs.IS_DELETED, SimpleYesNoRenderer.render(isDeleted));
    }

    public DataSetRow withIsComplete(Boolean complete)
    {
        String value = complete == null ? "?" : SimpleYesNoRenderer.render(complete);
        withCell(ExternalDataGridColumnIDs.IS_COMPLETE, value);
        return this;
    }

    public DataSetRow derived()
    {
        return withIsDerived(true);
    }

    public DataSetRow notDerived()
    {
        return withIsDerived(false);
    }

    private DataSetRow withIsDerived(boolean derived)
    {
        withCell(ExternalDataGridColumnIDs.SOURCE_TYPE, SourceType.create(derived).name());
        return this;
    }

    public DataSetRow withFileFormatType(String type)
    {
        withCell(ExternalDataGridColumnIDs.FILE_FORMAT_TYPE, type);
        return this;
    }

    public DataSetRow withLocation(String location)
    {
        withCell(ExternalDataGridColumnIDs.LOCATION, location);
        return this;
    }

    public DataSetRow withSample(String sampleIdentifier)
    {
        withCell(ExternalDataGridColumnIDs.EXTERNAL_DATA_SAMPLE_IDENTIFIER, sampleIdentifier);
        return this;
    }

    public DataSetRow withExperiment(String experimentIdentifier)
    {
        withCell(ExternalDataGridColumnIDs.EXTERNAL_DATA_EXPERIMENT_IDENTIFIER,
                experimentIdentifier);
        return this;
    }

    public DataSetRow withSampleType(String sampleType)
    {
        withCell(ExternalDataGridColumnIDs.SAMPLE_TYPE, sampleType);
        return this;
    }
}
