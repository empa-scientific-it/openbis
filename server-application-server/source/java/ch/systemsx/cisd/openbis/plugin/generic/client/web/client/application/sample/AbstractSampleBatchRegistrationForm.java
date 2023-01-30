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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractBatchRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>abstract</i> sample batch registration panel.
 * 
 * @author Izabela Adamczyk
 */
public abstract class AbstractSampleBatchRegistrationForm extends AbstractBatchRegistrationForm
{

    protected final IViewContext<IGenericClientServiceAsync> genericViewContext;

    protected final SampleType sampleType;

    AbstractSampleBatchRegistrationForm(
            IViewContext<IGenericClientServiceAsync> genericViewContext, SampleType sampleType,
            String sessionKey)
    {
        super(genericViewContext.getCommonViewContext(), GenericConstants.ID_PREFIX + sessionKey,
                sessionKey);
        this.genericViewContext = genericViewContext;
        this.sampleType = sampleType;
    }

}
