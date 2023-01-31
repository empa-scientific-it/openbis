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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;

/**
 * @author Franz-Josef Elmer
 *
 */
public class SampleDeliverer extends AbstractEntityWithPermIdDeliverer
{

    SampleDeliverer(DeliveryContext context)
    {
        super(context, "sample", "samples");
    }

    @Override
    protected void deliverEntities(DeliveryExecutionContext context, List<String> samplePermIds) throws XMLStreamException
    {
        XMLStreamWriter writer = context.getWriter();
        String sessionToken = context.getSessionToken();
        Set<String> spaces = context.getSpaces();
        IApplicationServerApi v3api = getV3Api();
        List<SamplePermId> permIds = samplePermIds.stream().map(SamplePermId::new).collect(Collectors.toList());
        Collection<Sample> fullSamples = v3api.getSamples(sessionToken, permIds, createFullFetchOptions()).values();
        int count = 0;
        for (Sample sample : fullSamples)
        {
            if (sample.getSpace() == null || spaces.contains(sample.getSpace().getCode()))
            {
                String permId = sample.getPermId().getPermId();
                startUrlElement(writer, "SAMPLE", permId, sample.getModificationDate());
                startXdElement(writer);
                writer.writeAttribute("code", sample.getCode());
                addAttributeIfSet(writer, "frozen", sample.isFrozen());
                addAttributeIfSet(writer, "frozenForChildren", sample.isFrozenForChildren());
                addAttributeIfSet(writer, "frozenForParents", sample.isFrozenForParents());
                addAttributeIfSet(writer, "frozenForComponents", sample.isFrozenForComponents());
                addAttributeIfSet(writer, "frozenForDataSets", sample.isFrozenForDataSets());
                addExperiment(writer, sample.getExperiment());
                addKind(writer, EntityKind.SAMPLE);
                addModifier(writer, sample);
                addProject(writer, sample.getProject());
                addRegistrationDate(writer, sample);
                addRegistrator(writer, sample);
                addSpace(writer, sample.getSpace());
                addType(writer, sample.getType());
                addProperties(writer, sample.getProperties(), context);
                ConnectionsBuilder connectionsBuilder = new ConnectionsBuilder();
                connectionsBuilder.addConnections(sample.getDataSets());
                connectionsBuilder.addChildren(sample.getChildren());
                connectionsBuilder.addComponents(sample.getComponents());
                connectionsBuilder.writeTo(writer);
                addAttachments(writer, sample.getAttachments());
                writer.writeEndElement();
                writer.writeEndElement();
                count++;
            }
        }
        operationLog.info(count + " of " + samplePermIds.size() + " samples have been delivered.");
    }

    @Override
    protected List<String> getAllEntities(DeliveryExecutionContext executionContext, String sessionToken)
    {
        String sql = "select perm_id from samples where space_id is null";
        Set<String> spaces = executionContext.getSpaces();
        if (spaces.isEmpty() == false)
        {
            CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
            spaces.stream().forEach(space -> builder.append("'" + space + "'"));
            sql += " or space_id in (select id from spaces where code in (" + builder + "))";
        }
        return super.getAllEntities(executionContext, sessionToken, sql);
    }

    private SampleFetchOptions createFullFetchOptions()
    {
            SampleFetchOptions fo = new SampleFetchOptions();
            fo.withRegistrator();
            fo.withModifier();
            fo.withProperties();
            fo.withDataSets();
            fo.withType();
            fo.withExperiment();
            fo.withProject();
            fo.withSpace();
            fo.withAttachments();
            fo.withChildren();
            fo.withComponents();
            fo.withDataSets();
            return fo;
    }

}
