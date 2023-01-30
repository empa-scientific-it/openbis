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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.entity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComposite;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsCount;

/**
 * @author pkupczyk
 */
public class MetaprojectEntities extends LayoutContainer implements IDisposableComponent
{

    public static final String ID_SUFFIX = "_metaproject-entities";

    public static final EventType ENTITIES_CHANGED = new EventType();

    private IViewContext<?> viewContext;

    private SectionsPanel sectionsPanel;

    private Map<EntityKind, DisposableTabContent> sectionsMap =
            new LinkedHashMap<EntityKind, DisposableTabContent>();

    private Long currentMetaprojectId;

    private EntityKind currentEntityKind;

    private DisposableComposite composite = new DisposableComposite(this);

    public MetaprojectEntities(IViewContext<?> viewContext, String idPrefix)
    {
        this.viewContext = viewContext;

        setLayout(new FitLayout());
        setId(idPrefix + ID_SUFFIX);
    }

    private void initSections(final Long metaprojectId, final IDelegatedAction callback)
    {
        clearSections();

        final Html loading = new Html(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        add(loading);
        layout();

        viewContext.getCommonService().getMetaprojectAssignmentsCount(metaprojectId,
                new AbstractAsyncCallback<MetaprojectAssignmentsCount>(viewContext)
                    {
                        @Override
                        protected void process(MetaprojectAssignmentsCount count)
                        {
                            sectionsPanel =
                                    new SectionsPanel(viewContext.getCommonViewContext(),
                                            getElement().getId());

                            if (count.getExperimentCount() > 0)
                            {
                                addSection(EntityKind.EXPERIMENT,
                                        new MetaprojectExperimentsSection(viewContext, new TechId(
                                                metaprojectId)));
                            }
                            if (count.getSampleCount() > 0)
                            {
                                addSection(EntityKind.SAMPLE, new MetaprojectSamplesSection(
                                        viewContext, new TechId(metaprojectId)));
                            }
                            if (count.getDataSetCount() > 0)
                            {
                                addSection(EntityKind.DATA_SET, new MetaprojectDataSetsSection(
                                        viewContext, new TechId(metaprojectId)));
                            }
                            if (count.getMaterialCount() > 0)
                            {
                                addSection(EntityKind.MATERIAL, new MetaprojectMaterialsSection(
                                        viewContext, new TechId(metaprojectId)));
                            }

                            if (sectionsMap.isEmpty())
                            {
                                remove(loading);
                                clearSections();
                            } else
                            {
                                if (hasSection(currentEntityKind))
                                {
                                    selectSection(currentEntityKind);
                                } else
                                {
                                    currentEntityKind =
                                            sectionsMap.entrySet().iterator().next().getKey();
                                    sectionsPanel.selectFirstSection();
                                }
                                remove(loading);
                                add(sectionsPanel);
                                layout();
                            }

                            if (callback != null)
                            {
                                callback.execute();
                            }
                        }
                    });
    }

    private void clearSections()
    {
        if (sectionsPanel != null)
        {
            sectionsPanel.removeFromParent();
            sectionsPanel = null;
        }
        sectionsMap.clear();
        composite.clearSubcomponents();
    }

    private boolean hasSection(EntityKind entityKind)
    {
        return sectionsMap.containsKey(entityKind);
    }

    private void addSection(EntityKind entityKind, final MetaprojectEntitySection section)
    {
        section.addListener(MetaprojectEntitySection.SECTION_CHANGED_EVENT,
                new Listener<BaseEvent>()
                    {
                        @Override
                        public void handleEvent(BaseEvent be)
                        {
                            fireEvent(ENTITIES_CHANGED);
                        }
                    });
        sectionsPanel.addSection(section);
        sectionsMap.put(entityKind, section);
        composite.addSubcomponent(new IDisposableComponentProvider()
            {
                @Override
                public IDisposableComponent getDisposableComponent()
                {
                    return section.tryGetDisposableComponentOrNull();
                }
            });
    }

    private void selectSection(EntityKind entityKind)
    {
        DisposableTabContent section = sectionsMap.get(entityKind);
        if (section != null && sectionsPanel != null)
        {
            sectionsPanel.selectSection(section);
        }
    }

    public void showEntities(Long metaprojectId, IDelegatedAction callback)
    {
        if (currentMetaprojectId != metaprojectId)
        {
            currentMetaprojectId = metaprojectId;
            currentEntityKind = null;
            initSections(metaprojectId, callback);
        }
    }

    public void showEntities(Long metaprojectId, final EntityKind entityKind,
            IDelegatedAction callback)
    {
        if (currentMetaprojectId != metaprojectId)
        {
            currentMetaprojectId = metaprojectId;
            currentEntityKind = entityKind;
            initSections(metaprojectId, callback);
        } else
        {
            currentEntityKind = entityKind;
            selectSection(entityKind);
            if (callback != null)
            {
                callback.execute();
            }
        }
    }

    public void hideEntities()
    {
        currentMetaprojectId = null;
        currentEntityKind = null;
        clearSections();
    }

    public void refresh()
    {
        if (currentMetaprojectId != null)
        {
            initSections(currentMetaprojectId, IDelegatedAction.DO_NOTHING);
        }
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refresh();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return composite.getRelevantModifications();
    }

    @Override
    public Component getComponent()
    {
        return this;
    }

    @Override
    public void dispose()
    {
        composite.dispose();
    }

}
