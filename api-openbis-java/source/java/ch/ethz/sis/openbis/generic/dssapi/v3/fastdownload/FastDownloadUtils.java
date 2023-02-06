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

package ch.ethz.sis.openbis.generic.dssapi.v3.fastdownload;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import ch.ethz.sis.filetransfer.DownloadException;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * @author Franz-Josef Elmer
 */
public class FastDownloadUtils
{
    private static final String ERROR_FIELD = "error";

    private static final String EXCEPTION_CLASS_FIELD = "exception-class";

    private static final String RETRIABLE_FIELD = "retriable";

    private FastDownloadUtils()
    {
    }

    public static void renderAsJson(JsonGenerator jsonGenerator, Throwable throwable)
    {
        try
        {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField(ERROR_FIELD, throwable.getMessage());
            jsonGenerator.writeObjectField(EXCEPTION_CLASS_FIELD, throwable.getClass().getName());
            if (throwable instanceof DownloadException)
            {
                boolean retriable = Boolean.TRUE.equals(((DownloadException) throwable).isRetriable());
                jsonGenerator.writeBooleanField(RETRIABLE_FIELD, retriable);
            }
            jsonGenerator.writeEndObject();
            jsonGenerator.flush();
        } catch (IOException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    public static RuntimeException createExceptionFromJson(JsonNode tree)
    {
        JsonNode errorNode = tree.get(ERROR_FIELD);
        if (errorNode != null)
        {
            String errorText = errorNode.textValue();
            String exceptionClassName = tree.get(EXCEPTION_CLASS_FIELD).asText();
            if (DownloadException.class.getName().equals(exceptionClassName))
            {
                return new DownloadException(errorText, tree.get(RETRIABLE_FIELD).asBoolean());
            }
            return new RuntimeException(exceptionClassName + ": " + errorText);
        }
        return null;
    }
}
