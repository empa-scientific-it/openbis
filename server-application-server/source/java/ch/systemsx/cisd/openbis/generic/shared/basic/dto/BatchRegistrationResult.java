/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

/**
 * Batch registration is based on file input. This class returns to the client a small message after each file has been uploaded and handled on the
 * server side.
 * 
 * @author Christian Ribeaud
 */
public class BatchRegistrationResult implements Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String fileName;

    private String message;

    public BatchRegistrationResult()
    {
    }

    public BatchRegistrationResult(final String fileName, final String message)
    {
        assert fileName != null : "Unspecified file name";
        assert message != null : "Unspecified message";
        setFileName(fileName);
        setMessage(message);
    }

    public final String getFileName()
    {
        return fileName;
    }

    public final String getMessage()
    {
        return message;
    }

    public final void setFileName(final String fileName)
    {
        this.fileName = fileName;
    }

    public final void setMessage(final String message)
    {
        this.message = message;
    }
}
