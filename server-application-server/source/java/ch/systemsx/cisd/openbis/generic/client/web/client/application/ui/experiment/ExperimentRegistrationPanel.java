/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.EntityRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * The {@link EntityRegistrationPanel} extension for registering an experiment.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentRegistrationPanel extends
        EntityRegistrationPanel<ExperimentTypeModel, ExperimentTypeSelectionWidget>
{

    public static final String ID = EntityRegistrationPanel.createId(EntityKind.EXPERIMENT);

    public static final DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, final ActionContext context)
    {
        ExperimentRegistrationPanel panel = new ExperimentRegistrationPanel(viewContext, context);
        return new DatabaseModificationAwareComponent(panel, panel);
    }

    private ExperimentRegistrationPanel(final IViewContext<ICommonClientServiceAsync> viewContext,
            ActionContext context)
    {
        super(viewContext, EntityKind.EXPERIMENT, new ExperimentTypeSelectionWidget(viewContext,
                EntityRegistrationPanel.createId(EntityKind.EXPERIMENT), context
                        .tryGetExperimentTypeCode()), context);
    }
}
