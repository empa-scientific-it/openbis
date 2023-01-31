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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;

/**
 * @author Franz-Josef Elmer
 *
 */
public class ExperimentDeliverer extends AbstractEntityWithPermIdDeliverer
{

    ExperimentDeliverer(DeliveryContext context)
    {
        super(context, "experiment", "experiments");
    }

    @Override
    protected void deliverEntities(DeliveryExecutionContext context, List<String> experiments) throws XMLStreamException
    {
        XMLStreamWriter writer = context.getWriter();
        String sessionToken = context.getSessionToken();
        Set<String> spaces = context.getSpaces();
        IApplicationServerApi v3api = getV3Api();
        List<ExperimentPermId> permIds = experiments.stream().map(ExperimentPermId::new).collect(Collectors.toList());
        Collection<Experiment> fullExperiments = v3api.getExperiments(sessionToken, permIds, createFullFetchOptions()).values();
        int count = 0;
        for (Experiment experiment : fullExperiments)
        {
            if (spaces.contains(experiment.getProject().getSpace().getCode()))
            {
                String permId = experiment.getPermId().getPermId();
                startUrlElement(writer, "EXPERIMENT", permId, experiment.getModificationDate());
                startXdElement(writer);
                writer.writeAttribute("code", experiment.getCode());
                addKind(writer, EntityKind.EXPERIMENT);
                addAttributeIfSet(writer, "frozen", experiment.isFrozen());
                addAttributeIfSet(writer, "frozenForSamples", experiment.isFrozenForSamples());
                addAttributeIfSet(writer, "frozenForDataSets", experiment.isFrozenForDataSets());
                addModifier(writer, experiment);
                addProject(writer, experiment.getProject());
                addRegistrationDate(writer, experiment);
                addRegistrator(writer, experiment);
                addSpace(writer, experiment.getProject().getSpace());
                addType(writer, experiment.getType());
                addProperties(writer, experiment.getProperties(), context);
//                ConnectionsBuilder connectionsBuilder = new ConnectionsBuilder();
//                connectionsBuilder.addConnections(experiment.getSamples());
//                connectionsBuilder.addConnections(experiment.getDataSets());
//                connectionsBuilder.writeTo(writer);
                addAttachments(writer, experiment.getAttachments());
                writer.writeEndElement();
                writer.writeEndElement();
                count++;
            }
        }
        operationLog.info(count + " of " + experiments.size() + " experiments have been delivered.");
    }

    private ExperimentFetchOptions createFullFetchOptions()
    {
        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withRegistrator();
        fo.withModifier();
        fo.withProperties();
        fo.withProject().withSpace();
        fo.withType();
        fo.withAttachments();
//        fo.withSamples();
//        fo.withDataSets();
        return fo;
    }

}
