/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;

/**
 * Adds module sections the viewer after both {@link SectionsPanel} and {@link IModule}s are initialized.
 * 
 * @author Izabela Adamczyk
 */
public class ModulesSectionsManager
{

    private SectionsPanel container;

    private IEntityInformationHolderWithIdentifier entity;

    private List<IModule> modules;

    /**
     * Sets the values of chosen fields. Adds module sections to given container if called after {@link #initialize(List)}.
     */
    @SuppressWarnings("hiding")
    public void initialize(final SectionsPanel container,
            final IEntityInformationHolderWithIdentifier entity)
    {
        this.container = container;
        this.entity = entity;
        if (modules != null)
        {
            attachModulesSections();
        }
    }

    /**
     * Sets the values of chosen fields. Adds module sections to given container if called after
     * {@link #initialize(SectionsPanel, IEntityInformationHolderWithIdentifier)}.
     */
    @SuppressWarnings("hiding")
    public void initialize(final List<IModule> modules)
    {
        this.modules = modules;
        if (container != null)
        {
            attachModulesSections();
        }
    }

    private void attachModulesSections()
    {
        for (final IModule module : modules)
        {
            final Collection<? extends TabContent> sections = module.getSections(entity);
            for (final TabContent panel : sections)
            {
                container.addSection(panel);
            }
        }
        container.layout();
    }
}