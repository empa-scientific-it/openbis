/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Processing parameters are a binary byte sequence from a file. It is stored BASE64 encoded in order to be marshalling and unmarshalling for SOAP.
 * 
 * @author Franz-Josef Elmer
 */
public class ProcessingParameters implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    /**
     * Creates an instance based on the specified byte array.
     * 
     * @return <code>null</code> if the argument is also <code>null</code>.
     */
    public final static ProcessingParameters tryCreateFrom(final byte[] bytesOrNull)
    {
        if (bytesOrNull == null)
        {
            return null;
        }
        final ProcessingParameters processingParameters = new ProcessingParameters();
        processingParameters.setProcessingParameters(bytesOrNull);
        return processingParameters;
    }

    private byte[] processingParameters;

    private String fileDescription;

    public final String getFileDescription()
    {
        return fileDescription;
    }

    public final void setFileDescription(final String fileDescription)
    {
        this.fileDescription = fileDescription;
    }

    public final byte[] getProcessingParameters()
    {
        return processingParameters;
    }

    public final void setProcessingParameters(final byte[] encodedProcessingParameters)
    {
        this.processingParameters = encodedProcessingParameters;
    }
}
