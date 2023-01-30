/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IPerson;

/**
 * Object that declaratively describes a UI for an action (e.g. describing UI of a dialog that should be shown after clicking on a button).
 * 
 * @author Piotr Buczek
 */
public class ManagedUiActionDescription implements IManagedUiAction, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String name;

    private String description;

    private IPerson person; // invoker of the action

    private List<IManagedInputWidgetDescription> inputWidgets =
            new ArrayList<IManagedInputWidgetDescription>();

    // for serialization
    public ManagedUiActionDescription()
    {
    }

    public ManagedUiActionDescription(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public IManagedUiAction setDescription(String aDescription)
    {
        this.description = aDescription;
        return this;
    }

    @Override
    public List<IManagedInputWidgetDescription> getInputWidgetDescriptions()
    {
        return inputWidgets;
    }

    public void setInputWidgetDescriptions(List<IManagedInputWidgetDescription> widgetDescriptions)
    {
        this.inputWidgets = widgetDescriptions;
    }

    @Override
    public void addInputWidgets(IManagedInputWidgetDescription... widgetDescriptions)
    {
        for (IManagedInputWidgetDescription widget : widgetDescriptions)
        {
            inputWidgets.add(widget);
        }
    }

    @Override
    public String getInputValue(String inputLabel)
    {
        for (IManagedInputWidgetDescription inputDescription : getInputWidgetDescriptions())
        {
            if (inputDescription.getLabel().equals(inputLabel))
            {
                return inputDescription.getValue();
            }
        }
        return null;
    }

    @Override
    public IPerson getPerson()
    {
        return person;
    }

    public void setPerson(IPerson person)
    {
        this.person = person;
    }

}
