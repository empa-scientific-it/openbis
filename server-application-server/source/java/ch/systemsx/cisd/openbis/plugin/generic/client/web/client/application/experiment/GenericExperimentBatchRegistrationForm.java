/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> experiment batch registration panel.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericExperimentBatchRegistrationForm extends
        AbstractExperimentBatchRegistrationForm
{
    private static final String SESSION_KEY = "experiment-batch-registration";

    public GenericExperimentBatchRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext, final ExperimentType type)
    {
        super(viewContext, type, BatchOperationKind.REGISTRATION, SESSION_KEY);
        setResetButtonVisible(true);
    }

    @Override
    protected void save()
    {
        genericViewContext.getService().registerExperiments(experimentType, SESSION_KEY, isAsync(), emailField.getValue(),
                new BatchRegistrationCallback(viewContext));
    }

}
