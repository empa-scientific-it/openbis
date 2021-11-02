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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;

/**
 * @author pkupczyk
 */
public class DeletionForceOptions extends Composite
{

    private FieldSet fieldSet;

    private DeletionForceCheckBox forceToDeleteDependentDeletionSets;

    private DeletionForceCheckBox forceDisallowedTypes;

    public DeletionForceOptions(IViewContext<?> viewContext)
    {
        forceToDeleteDependentDeletionSets = new DeletionForceCheckBox();
        forceToDeleteDependentDeletionSets.setText("Force dependent deletions: ");
        forceToDeleteDependentDeletionSets.setTooltip("Dependent deletions have entities "
                + "which have to be permanently deleted together or before the entities of the selected deletions "
                + "can be permanently deleted, too.");
        forceDisallowedTypes = new DeletionForceCheckBox();
        forceDisallowedTypes.setText(viewContext.getMessage(Dict.DELETING_FORCE_DISALLOWED_TYPES));
        forceDisallowedTypes.setTooltip(viewContext
                .getMessage(Dict.DELETING_FORCE_DISALLOWED_TYPES_TOOLTIP));

        Panel panel = new VerticalPanel();
        panel.addStyleName("deletionForceOptions");
        panel.add(forceToDeleteDependentDeletionSets);
        panel.add(forceDisallowedTypes);

        fieldSet = new FieldSet();
        fieldSet.setHeading(viewContext.getMessage(Dict.DELETING_FORCE_SECTION));
        fieldSet.setCheckboxToggle(true);
        fieldSet.setExpanded(false);
        fieldSet.addListener(Events.Collapse, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    forceToDeleteDependentDeletionSets.setValue(false);
                    forceDisallowedTypes.setValue(false);
                }
            });
        fieldSet.add(panel);

        initWidget(fieldSet);
    }

    public boolean getForceDisallowedTypesValue()
    {
        return fieldSet.isExpanded() ? forceDisallowedTypes.getValue() : false;
    }
    
    public boolean getForceToDeleteDependentDeletionSetsValue()
    {
        return fieldSet.isExpanded() ? forceToDeleteDependentDeletionSets.getValue() : false;
    }

}
