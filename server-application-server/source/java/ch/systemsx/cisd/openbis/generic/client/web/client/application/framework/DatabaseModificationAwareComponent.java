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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import com.extjs.gxt.ui.client.widget.Component;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;

/**
 * Component which is aware of database modifications.
 * 
 * @author Tomasz Pylak
 */
public class DatabaseModificationAwareComponent extends DatabaseModificationAwareObject<Component>
{
    /**
     * Creates an instance by conversion from a disposable component, these two classes have the same functionality.
     */
    public static DatabaseModificationAwareComponent create(IDisposableComponent disposableComponent)
    {
        return new DatabaseModificationAwareComponent(disposableComponent.getComponent(),
                disposableComponent);
    }

    /**
     * Creates a mock with a dummy database modification observer. Use this method if your component does not need to be refreshed when the database
     * changes.
     */
    public static DatabaseModificationAwareComponent wrapUnaware(Component component)
    {
        return new DatabaseModificationAwareComponent(component, createDummyModificationObserver());
    }

    public DatabaseModificationAwareComponent(Component holder,
            IDatabaseModificationObserver modificationObserver)
    {
        super(holder, modificationObserver);
    }

}
