/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.widget;

import com.google.gwt.user.client.ui.InlineLabel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeEntityKindItemData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author pkupczyk
 */
public class MetaprojectTreeEntityKindItemWidget extends MetaprojectTreeItemWidget
{

    public MetaprojectTreeEntityKindItemWidget(IViewContext<?> viewContext,
            MetaprojectTreeEntityKindItemData model)
    {
        super(viewContext);

        EntityKind entityKind = model.getEntityKind();
        String messageKey;

        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            messageKey = Dict.METAPROJECT_ENTITIES_EXPERIMENTS;
        } else if (EntityKind.SAMPLE.equals(entityKind))
        {
            messageKey = Dict.METAPROJECT_ENTITIES_SAMPLES;
        } else if (EntityKind.DATA_SET.equals(entityKind))
        {
            messageKey = Dict.METAPROJECT_ENTITIES_DATA_SETS;
        } else if (EntityKind.MATERIAL.equals(entityKind))
        {
            messageKey = Dict.METAPROJECT_ENTITIES_MATERIALS;
        } else
        {
            throw new IllegalArgumentException("Unsupported entity kind: " + entityKind);
        }

        String message = viewContext.getMessage(messageKey) + " (" + model.getEntityCount() + ")";

        initWidget(new InlineLabel(message));
    }
}
