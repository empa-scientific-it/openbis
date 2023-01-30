/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.event;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.EntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.Event;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.EventType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions.EventFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.id.EventTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpaceTechId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.event.IEventAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author pkupczyk
 */
@Component
public class EventTranslator extends AbstractCachingTranslator<Long, Event, EventFetchOptions> implements IEventTranslator
{

    @Autowired
    private IEventAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IEventBaseTranslator baseTranslator;

    @Autowired
    private IEventRegistratorTranslator registratorTranslator;

    @Override protected Set<Long> shouldTranslate(final TranslationContext context, final Collection<Long> inputs,
            final EventFetchOptions fetchOptions)
    {
        authorizationExecutor.canGet(new OperationContext(context.getSession()));
        return new HashSet<>(inputs);
    }

    @Override
    protected Event createObject(TranslationContext context, Long input, EventFetchOptions fetchOptions)
    {
        Event event = new Event();
        event.setFetchOptions(new EventFetchOptions());
        return event;
    }

    @Override protected Object getObjectsRelations(final TranslationContext context, final Collection<Long> inputs,
            final EventFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IEventBaseTranslator.class, baseTranslator.translate(context, inputs, null));

        if (fetchOptions.hasRegistrator())
        {
            relations.put(IEventRegistratorTranslator.class, registratorTranslator.translate(context, inputs, fetchOptions.withRegistrator()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long input, Event output, Object objectRelations, EventFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        EventBaseRecord baseRecord = relations.get(IEventBaseTranslator.class, input);

        output.setId(new EventTechId(baseRecord.id));
        output.setEventType(translateEventType(baseRecord.eventType));
        output.setEntityType(translateEntityType(baseRecord.entityType));
        output.setEntitySpace(baseRecord.entitySpace);
        output.setEntitySpaceId(baseRecord.entitySpaceId != null ? new SpaceTechId(Long.valueOf(baseRecord.entitySpaceId)) : null);
        output.setEntityProject(baseRecord.entityProject);
        output.setEntityProjectId(baseRecord.entityProjectId != null ? new ProjectPermId(baseRecord.entityProjectId) : null);
        output.setEntityRegistrator(baseRecord.entityRegistrator);
        output.setEntityRegistrationDate(baseRecord.entityRegistrationDate);
        output.setIdentifier(baseRecord.identifier);
        output.setDescription(baseRecord.description);
        output.setReason(baseRecord.reason);
        output.setContent(baseRecord.content);
        output.setRegistrationDate(baseRecord.registrationDate);

        if (fetchOptions.hasRegistrator())
        {
            output.setRegistrator(relations.get(IEventRegistratorTranslator.class, input));
            output.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }
    }

    private EventType translateEventType(String eventType)
    {
        return EventType.valueOf(eventType);
    }

    private EntityType translateEntityType(String entityType)
    {
        if (EventPE.EntityType.DATASET.name().equals(entityType))
        {
            return EntityType.DATA_SET;
        } else if (EventPE.EntityType.METAPROJECT.name().equals(entityType))
        {
            return EntityType.TAG;
        } else
        {
            return EntityType.valueOf(entityType);
        }
    }
}
