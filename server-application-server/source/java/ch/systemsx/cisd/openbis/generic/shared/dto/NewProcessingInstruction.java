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

import ch.systemsx.cisd.common.parser.BeanProperty;
import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Processing instruction is a specific experiment attachment.
 * 
 * @author Franz-Josef Elmer
 */
public final class NewProcessingInstruction extends AbstractHashable implements Serializable
{
    public final static NewProcessingInstruction[] EMPTY_ARRAY = new NewProcessingInstruction[0];

    private static final long serialVersionUID = IServer.VERSION;

    private String procedureTypeCode;

    private String path;

    private String description;

    private ProcessingParameters parameters;

    public final String getPath()
    {
        return path;
    }

    @BeanProperty(label = "path")
    public final void setPath(final String path)
    {
        this.path = path;
    }

    public final String getDescription()
    {
        return description;
    }

    @BeanProperty(optional = true, label = "description")
    public final void setDescription(final String description)
    {
        this.description = description;
    }

    /** can be null */
    public final byte[] getParameters()
    {
        return parameters == null ? null : parameters.getProcessingParameters();
    }

    public String getParametersFileDescription()
    {
        return parameters == null ? null : parameters.getFileDescription();
    }

    @BeanProperty(optional = true, label = "parameters")
    public final void setProcessingParameters(final ProcessingParameters parameters)
    {
        this.parameters = parameters;
    }

    public final String getProcedureTypeCode()
    {
        return procedureTypeCode;
    }

    @BeanProperty(label = "procedure_type")
    public final void setProcedureTypeCode(final String procedureTypeCode)
    {
        this.procedureTypeCode = procedureTypeCode;
    }
}
