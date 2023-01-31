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

import java.io.File;
import java.util.Base64;
import java.util.Date;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Franz-Josef Elmer
 *
 */
public class FileDeliverer extends AbstractEntityDeliverer<String>
{

    FileDeliverer(DeliveryContext context)
    {
        super(context, "file");
    }

    @Override
    public void deliverEntities(DeliveryExecutionContext context) throws XMLStreamException
    {
        Set<String> paths = context.getFileServicePaths();
        if (paths.isEmpty())
        {
            return;
        }
        File repository = this.context.getFileServiceRepository();
        XMLStreamWriter writer = context.getWriter();
        int count = 0;
        long totalSize = 0;
        for (String path : paths)
        {
            File file = new File(repository, path);
            if (file.isFile())
            {
                startUrlElement(writer, "FILE", path, new Date(file.lastModified()));
                startXdElement(writer);
                addKind(writer, "FILE");
                writer.writeAttribute("path", path);
                byte[] content = FileUtilities.loadToByteArray(file);
                String contentAsBase64String = Base64.getEncoder().encodeToString(content);
                writer.writeCharacters(contentAsBase64String);
                writer.writeEndElement();
                writer.writeEndElement();
                count++;
                totalSize += content.length;
            } else
            {
                operationLog.warn("File expected but does not exists: " + file.getAbsolutePath());
            }
        }
        operationLog.info(count + " files (total size: " + FileUtilities.byteCountToDisplaySize(totalSize) + ") have been delivered.");
    }

}
