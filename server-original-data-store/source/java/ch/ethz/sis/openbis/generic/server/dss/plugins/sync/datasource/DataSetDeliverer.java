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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.LinkedData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetDeliverer extends AbstractEntityWithPermIdDeliverer
{

    DataSetDeliverer(DeliveryContext context)
    {
        super(context, "data set", "data", "code");
    }

    @Override
    protected void deliverEntities(DeliveryExecutionContext context, List<String> dataSets) throws XMLStreamException
    {
        XMLStreamWriter writer = context.getWriter();
        String sessionToken = context.getSessionToken();
        Set<String> spaces = context.getSpaces();
        IApplicationServerApi v3api = getV3Api();
        List<DataSetPermId> permIds = dataSets.stream().map(DataSetPermId::new).collect(Collectors.toList());
        Collection<DataSet> fullDataSets = v3api.getDataSets(sessionToken, permIds, createDataSetFetchOptions()).values();
        int count = 0;
        for (DataSet dataSet : fullDataSets)
        {
            if (accept(dataSet, spaces))
            {
                String code = dataSet.getCode();
                startUrlElement(writer, "DATA_SET", code, dataSet.getModificationDate());
                startXdElement(writer);
                writer.writeAttribute("code", code);
                writer.writeAttribute("dsKind", dataSet.getKind().toString());
                addAttributeIfSet(writer, "frozen", dataSet.isFrozen());
                addAttributeIfSet(writer, "frozenForChildren", dataSet.isFrozenForChildren());
                addAttributeIfSet(writer, "frozenForParents", dataSet.isFrozenForParents());
                addAttributeIfSet(writer, "frozenForComponents", dataSet.isFrozenForComponents());
                addAttributeIfSet(writer, "frozenForContainers", dataSet.isFrozenForContainers());
                addExperiment(writer, dataSet.getExperiment());
                addKind(writer, EntityKind.DATA_SET);
                addModifier(writer, dataSet);
                addRegistrationDate(writer, dataSet);
                addRegistrator(writer, dataSet);
                addSample(writer, dataSet.getSample());
                addType(writer, dataSet.getType());
                addProperties(writer, dataSet.getProperties(), context);
                addPhysicalData(writer, dataSet, code);
                addLinkedData(writer, dataSet, code);
                ConnectionsBuilder connectionsBuilder = new ConnectionsBuilder();
                connectionsBuilder.addChildren(dataSet.getChildren());
                connectionsBuilder.addComponents(dataSet.getComponents());
                connectionsBuilder.writeTo(writer);
                writer.writeEndElement();
                writer.writeEndElement();
                count++;
            }
        }
        operationLog.info(count + " of " + dataSets.size() + " data sets have been delivered.");
    }

    private void addPhysicalData(XMLStreamWriter writer, DataSet dataSet, String code) throws XMLStreamException
    {
        PhysicalData physicalData = dataSet.getPhysicalData();
        if (physicalData != null)
        {
            startBinaryDataElement(writer);
            addFileNodes(writer, code, context.getContentProvider().asContent(code).getRootNode());
            writer.writeEndElement();
        }
    }

    private void addLinkedData(XMLStreamWriter writer, DataSet dataSet, String code) throws XMLStreamException
    {
        LinkedData linkedData = dataSet.getLinkedData();
        if (linkedData != null)
        {
            startBinaryDataElement(writer);
            List<ContentCopy> contentCopies = linkedData.getContentCopies();
            for (ContentCopy contentCopy : contentCopies)
            {
                writer.writeStartElement("x:contentCopy");
                addAttribute(writer, "externalCode", contentCopy.getExternalCode());
                addAttribute(writer, "externalDMS", contentCopy.getExternalDms(), edms -> edms.getCode());
                addAttribute(writer, "gitCommitHash", contentCopy.getGitCommitHash());
                addAttribute(writer, "gitRepositoryId", contentCopy.getGitRepositoryId());
                addAttribute(writer, "id", contentCopy.getId(), id -> id.getPermId());
                addAttribute(writer, "path", contentCopy.getPath());
                writer.writeEndElement();
            }
            IHierarchicalContent content = context.getContentProvider().asContent(code);
            if (content != null)
            {
                addFileNodes(writer, code, content.getRootNode());
            }
            writer.writeEndElement();
        }
    }

    private void addFileNodes(XMLStreamWriter writer, String dataSetCode, IHierarchicalContentNode node) throws XMLStreamException
    {
        if (node.isDirectory())
        {
            List<IHierarchicalContentNode> childNodes = node.getChildNodes();
            for (IHierarchicalContentNode childNode : childNodes)
            {
                addFileNodes(writer, dataSetCode, childNode);
            }
        } else
        {
            writer.writeStartElement("x:fileNode");
            addAttribute(writer, "checksum", node.getChecksum());
            if (node.isChecksumCRC32Precalculated())
            {
                writer.writeAttribute("crc32checksum", Integer.toString(node.getChecksumCRC32()));
            }
            writer.writeAttribute("length", Long.toString(node.getFileLength()));
            writer.writeAttribute("path", context.getDownloadUrl() + "/datastore_server/" + dataSetCode + "/"
                    + node.getRelativePath() + "?");
            writer.writeEndElement();
        }
    }

    private boolean accept(DataSet dataSet, Set<String> spaces)
    {
        Experiment experiment = dataSet.getExperiment();
        if (experiment != null)
        {
            return spaces.contains(experiment.getProject().getSpace().getCode());
        }
        return spaces.contains(dataSet.getSample().getSpace().getCode());
    }

    private DataSetFetchOptions createDataSetFetchOptions()
    {
        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withRegistrator();
        fo.withModifier();
        fo.withType();
        fo.withSample().withSpace();
        fo.withExperiment().withProject().withSpace();
        fo.withProperties();
        fo.withChildren();
        fo.withComponents();
        fo.withPhysicalData();
        fo.withLinkedData().withExternalDms();
        fo.sortBy().code();
        return fo;
    }

}
