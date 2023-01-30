/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentBrowserGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.RowWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;

/**
 * Allows to define experiment table row expectations.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentRow extends RowWithProperties
{
    public ExperimentRow(final String code)
    {
        super();
        withCell(ExperimentBrowserGridColumnIDs.CODE, code);
    }

    public ExperimentRow(final String code, final String typeCode)
    {
        this(code);
        withCell(ExperimentBrowserGridColumnIDs.EXPERIMENT_TYPE, typeCode);
    }

    public ExperimentRow deleted()
    {
        withDeletion(true);
        return this;
    }

    public ExperimentRow valid()
    {
        withDeletion(false);
        return this;
    }

    private void withDeletion(boolean isDeleted)
    {
        withCell(ExperimentBrowserGridColumnIDs.IS_DELETED, SimpleYesNoRenderer.render(isDeleted));
    }

}
