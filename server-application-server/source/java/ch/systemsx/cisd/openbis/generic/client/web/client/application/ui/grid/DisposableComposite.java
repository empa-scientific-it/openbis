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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.SetUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * @author pkupczyk
 */
public class DisposableComposite implements IDisposableComponent
{

    private Component component;

    private List<IDisposableComponentProvider> subcomponentProviders =
            new ArrayList<IDisposableComponentProvider>();

    public DisposableComposite(Component component)
    {
        if (component == null)
        {
            throw new IllegalArgumentException("Component cannot be null");
        }
        this.component = component;
    }

    @Override
    public Component getComponent()
    {
        return component;
    }

    public void addSubcomponent(final IDisposableComponent subcomponent)
    {
        if (subcomponent != null)
        {
            subcomponentProviders.add(new IDisposableComponentProvider()
                {
                    @Override
                    public IDisposableComponent getDisposableComponent()
                    {
                        return subcomponent;
                    }
                });
        }
    }

    public void addSubcomponent(final IDisposableComponentProvider subcomponentProvider)
    {
        if (subcomponentProvider != null)
        {
            subcomponentProviders.add(subcomponentProvider);
        }
    }

    public void clearSubcomponents()
    {
        subcomponentProviders.clear();
    }

    @Override
    public void dispose()
    {
        for (IDisposableComponentProvider subcomponentProvider : subcomponentProviders)
        {
            IDisposableComponent subcomponent = subcomponentProvider.getDisposableComponent();

            if (subcomponent != null)
            {
                subcomponent.dispose();
            }
        }
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        Set<DatabaseModificationKind> relevantModifications =
                new HashSet<DatabaseModificationKind>();

        for (IDisposableComponentProvider subcomponentProvider : subcomponentProviders)
        {
            IDisposableComponent subcomponent = subcomponentProvider.getDisposableComponent();

            if (subcomponent != null)
            {
                DatabaseModificationKind[] subcomponentRelevantModifications =
                        subcomponent.getRelevantModifications();

                if (subcomponentRelevantModifications != null)
                {
                    SetUtils.addAll(relevantModifications, subcomponentRelevantModifications);
                }
            }
        }

        return relevantModifications.toArray(DatabaseModificationKind.EMPTY_ARRAY);
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        for (IDisposableComponentProvider subcomponentProvider : subcomponentProviders)
        {
            IDisposableComponent subcomponent = subcomponentProvider.getDisposableComponent();

            if (subcomponent != null)
            {
                DatabaseModificationKind[] subcomponentRelevantModifications =
                        subcomponent.getRelevantModifications();

                if (subcomponentRelevantModifications != null
                        && SetUtils.containsAny(observedModifications,
                                subcomponentRelevantModifications))
                {
                    subcomponent.update(observedModifications);
                }
            }
        }
    }
}
