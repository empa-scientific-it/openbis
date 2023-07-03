/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.server.FileServiceServlet;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IDataSourceQueryService;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractEntityDeliverer<T> implements IDeliverer
{
    private static final int CHUNK_SIZE = 1000;
    
    private static final Pattern FILE_SERVICE_PATTERN = Pattern.compile("openbis/" + FileServiceServlet.FILE_SERVICE_PATH
            + "/([^\"']*)");

    private static interface IConsumer<T>
    {
        public void consume(T t) throws XMLStreamException;
    }

    protected final Logger operationLog;

    protected final DeliveryContext context;

    private final String entityKind;

    AbstractEntityDeliverer(DeliveryContext context, String entityKind)
    {
        this.context = context;
        this.entityKind = entityKind;
        operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());
    }

    @Override
    public void deliverEntities(DeliveryExecutionContext context) throws XMLStreamException
    {
        String sessionToken = context.getSessionToken();
        List<T> allEntities = getAllEntities(context, sessionToken);
        executeInBatches(allEntities, entities -> deliverEntities(context, entities));
    }
    
    protected List<T> getAllEntities(DeliveryExecutionContext executionContext, String sessionToken)
    {
        return Collections.emptyList();
    }

    protected void deliverEntities(DeliveryExecutionContext context, List<T> entities) throws XMLStreamException
    {
    }

    protected IApplicationServerApi getV3Api()
    {
        return context.getV3api();
    }

    protected void addProperties(XMLStreamWriter writer, Map<String, Serializable> properties, DeliveryExecutionContext context) throws XMLStreamException
    {
        if (properties.isEmpty() == false)
        {
            writer.writeStartElement("x:properties");
            Set<Entry<String, Serializable>> entrySet = properties.entrySet();
            for (Entry<String, Serializable> entry : entrySet)
            {
                writer.writeStartElement("x:property");
                writer.writeStartElement("x:code");
                writer.writeCharacters(entry.getKey());
                writer.writeEndElement();
                writer.writeStartElement("x:value");
                String value = (String) entry.getValue();
                extractFileServicePath(context, value);
                writer.writeCharacters(value);
                writer.writeEndElement();
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }
    
    protected void extractFileServicePath(DeliveryExecutionContext context, String value)
    {
        if (value != null)
        {
            Set<String> fileServicePaths = context.getFileServicePaths();
            Matcher matcher = FILE_SERVICE_PATTERN.matcher(value);
            while (matcher.find())
            {
                fileServicePaths.add(matcher.group(1));
            }
        }
    }

    protected void addAttachments(XMLStreamWriter writer, List<Attachment> attachments) throws XMLStreamException
    {
        if (attachments.isEmpty())
        {
            return;
        }
        startBinaryDataElement(writer);
        for (Attachment attachment : attachments)
        {
            writer.writeStartElement("x:attachment");
            addAttribute(writer, "description", attachment.getDescription());
            addAttribute(writer, "fileName", attachment.getFileName());
            addAttribute(writer, "latestVersion", attachment.getVersion(), v -> Integer.toString(v));
            addAttribute(writer, "permLink", attachment.getPermlink());
            addAttribute(writer, "title", attachment.getTitle());
            writer.writeEndElement();
        }
        writer.writeEndElement();

    }

    protected void addSpace(XMLStreamWriter writer, Space space) throws XMLStreamException
    {
        addAttribute(writer, "space", space, s -> s.getCode());
    }

    protected void addProject(XMLStreamWriter writer, Project project) throws XMLStreamException
    {
        addAttribute(writer, "project", project, p -> p.getCode());
    }

    protected void addSample(XMLStreamWriter writer, Sample sample) throws XMLStreamException
    {
        addAttribute(writer, "sample", sample, s -> s.getIdentifier().getIdentifier());
    }

    protected void addExperiment(XMLStreamWriter writer, Experiment experiment) throws XMLStreamException
    {
        addAttribute(writer, "experiment", experiment, e -> e.getIdentifier().getIdentifier());
    }

    protected void addKind(XMLStreamWriter writer, Object kind) throws XMLStreamException
    {
        addAttribute(writer, "kind", kind, k -> k.toString());
    }

    protected void addType(XMLStreamWriter writer, ICodeHolder type) throws XMLStreamException
    {
        addAttribute(writer, "type", type, t -> t.getCode());
    }

    protected void addModifier(XMLStreamWriter writer, IModifierHolder dataSet) throws XMLStreamException
    {
        addAttribute(writer, "modifier", dataSet.getModifier(), m -> m.getUserId());
    }

    protected void addRegistrator(XMLStreamWriter writer, IRegistratorHolder dataSet) throws XMLStreamException
    {
        addAttribute(writer, "registrator", dataSet.getRegistrator(), r -> r.getUserId());
    }

    protected void addRegistrationDate(XMLStreamWriter writer, IRegistrationDateHolder dateHolder) throws XMLStreamException
    {
        addAttribute(writer, "registration-timestamp", dateHolder.getRegistrationDate(), h -> DataSourceUtils.convertToW3CDate(h));
    }

    protected void addAttributeAndExtractFilePaths(DeliveryExecutionContext context, XMLStreamWriter writer, String attributeName, String value) throws XMLStreamException
    {
        addAttribute(writer, attributeName, value, v -> v);
        extractFileServicePath(context, value);
    }

    protected void addAttribute(XMLStreamWriter writer, String attributeName, String value) throws XMLStreamException
    {
        addAttribute(writer, attributeName, value, v -> v);
    }
    
    protected void addAttributeIfSet(XMLStreamWriter writer, String attributeName, Boolean value) throws XMLStreamException
    {
        if (Boolean.TRUE.equals(value))
        {
            addAttribute(writer, attributeName, value);
        }
    }

    protected void addAttribute(XMLStreamWriter writer, String attributeName, Boolean value) throws XMLStreamException
    {
        addAttribute(writer, attributeName, value, v -> String.valueOf(v));
    }

    protected <O> void addAttribute(XMLStreamWriter writer, String attributeName, O object, Function<O, String> mapper) throws XMLStreamException
    {
        if (object != null)
        {
            writer.writeAttribute(attributeName, mapper.apply(object));
        }
    }

    protected void addLink(XMLStreamWriter writer, String code, String entityKind) throws XMLStreamException
    {
        addLink(writer, "?viewMode=SIMPLE&anonymous=true#entity=" + entityKind + "&permId=" + code);
    }

    protected void addLink(XMLStreamWriter writer, String urlPart2) throws XMLStreamException
    {
        writer.writeStartElement("rs:ln");
        writer.writeAttribute("href", context.getServerUrl() + urlPart2);
        writer.writeAttribute("rel", "describes");
        writer.writeEndElement();
    }

    protected void addLocation(XMLStreamWriter writer, String code, String entityKind) throws XMLStreamException
    {
        writer.writeStartElement("loc");
        writer.writeCharacters(context.getServerUrl() + "/" + entityKind + "/" + code + "/M");
        writer.writeEndElement();
    }

    protected void addLastModificationDate(XMLStreamWriter writer, IModificationDateHolder dateHolder) throws XMLStreamException
    {
        addLastModificationDate(writer, dateHolder.getModificationDate());
    }

    protected void addLastModificationDate(XMLStreamWriter writer, Date modificationDate) throws XMLStreamException
    {
        writer.writeStartElement("lastmod");
        writer.writeCharacters(DataSourceUtils.convertToW3CDate(modificationDate));
        writer.writeEndElement();
    }

    protected void startUrlElement(XMLStreamWriter writer, String entityKind, String permId, Date modificationDate) throws XMLStreamException
    {
        startUrlElement(writer);
        addLocation(writer, permId, entityKind);
        addLastModificationDate(writer, modificationDate);
        addLink(writer, permId, entityKind);
    }

    protected void startUrlElement(XMLStreamWriter writer) throws XMLStreamException
    {
        writer.writeStartElement("url");
    }

    protected void startXdElement(XMLStreamWriter writer) throws XMLStreamException
    {
        writer.writeStartElement("x:xd");
    }

    protected void startBinaryDataElement(XMLStreamWriter writer) throws XMLStreamException
    {
        writer.writeStartElement("x:binaryData");
    }

    private void executeInBatches(List<T> allEntities, IConsumer<List<T>> action)
    {
        operationLog.info(allEntities.size() + " " + entityKind + "s in total.");
        BatchOperationExecutor.executeInBatches(new IBatchOperation<T>()
            {
                @Override
                public void execute(List<T> entities)
                {
                    try
                    {
                        action.consume(entities);
                    } catch (XMLStreamException e)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(e);
                    }
                }

                @Override
                public List<T> getAllEntities()
                {
                    return allEntities;
                }

                @Override
                public String getEntityName()
                {
                    return entityKind;
                }

                @Override
                public String getOperationName()
                {
                    return "deliver";
                }
            }, CHUNK_SIZE);
    }

}
