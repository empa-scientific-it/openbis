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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDirectlyConnectedController;

/**
 * Class capsulating a {@link CheckBox} and delegating check box changes to an associated {@link IDelegatedAction}.
 * 
 * @author Franz-Josef Elmer
 */
public class EntityConnectionTypeProvider implements IDirectlyConnectedController
{

    private final CheckBox showOnlyDirectlyConnectedCheckBox;

    private IDelegatedAction onChangeAction;

    public EntityConnectionTypeProvider(final CheckBox showOnlyDirectlyConnectedCheckBox)
    {
        this.showOnlyDirectlyConnectedCheckBox = showOnlyDirectlyConnectedCheckBox;
        addChangeListener();
    }

    private void addChangeListener()
    {
        showOnlyDirectlyConnectedCheckBox.addListener(Events.Change, new Listener<FieldEvent>()
            {
                @Override
                public void handleEvent(FieldEvent be)
                {
                    if (onChangeAction != null)
                    {
                        onChangeAction.execute();
                    }
                }
            });
    }

    @Override
    public void setOnChangeAction(IDelegatedAction onChangeAction)
    {
        this.onChangeAction = onChangeAction;
    }

    @Override
    public boolean isOnlyDirectlyConnected()
    {
        return showOnlyDirectlyConnectedCheckBox.getValue();
    }
}