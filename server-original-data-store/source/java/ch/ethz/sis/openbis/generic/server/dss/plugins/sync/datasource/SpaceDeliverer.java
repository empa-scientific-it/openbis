/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.datasource;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * @author Franz-Josef Elmer
 *
 */
public class SpaceDeliverer extends AbstractEntityWithPermIdDeliverer
{

    SpaceDeliverer(DeliveryContext context)
    {
        super(context, "space", "spaces", "code");
    }

    @Override
    protected void deliverEntities(DeliveryExecutionContext context, List<String> allSpaces) throws XMLStreamException
    {
        XMLStreamWriter writer = context.getWriter();
        String sessionToken = context.getSessionToken();
        Set<String> spaces = context.getSpaces();
        IApplicationServerApi v3api = getV3Api();
        List<SpacePermId> permIds = allSpaces.stream().map(SpacePermId::new).collect(Collectors.toList());
        Collection<Space> fullSpaces = v3api.getSpaces(sessionToken, permIds, createSpaceFetchOptions()).values();
        int count = 0;
        for (Space space : fullSpaces)
        {
            String code = space.getCode();
            if (spaces.contains(code))
            {
                startUrlElement(writer, "SPACE", code, space.getModificationDate());
                startXdElement(writer);
                writer.writeAttribute("code", code);
                addAttributeAndExtractFilePaths(context, writer, "desc", space.getDescription());
                addAttributeIfSet(writer, "frozen", space.isFrozen());
                addAttributeIfSet(writer, "frozenForProjects", space.isFrozenForProjects());
                addAttributeIfSet(writer, "frozenForSamples", space.isFrozenForSamples());
                addKind(writer, "SPACE");
                addRegistrationDate(writer, space);
                addRegistrator(writer, space);
                writer.writeEndElement();
                writer.writeEndElement();
                count++;
            }
        }
        operationLog.info(count + " of " + allSpaces.size() + " spaces have been delivered.");
    }

    private SpaceFetchOptions createSpaceFetchOptions()
    {
        SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        fetchOptions.withRegistrator();
        return fetchOptions;
    }


}
