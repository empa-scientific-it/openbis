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

/**
 * Processing instruction needed to process raw data.
 * 
 * @author Franz-Josef Elmer
 */
public class ProcessingInstructionDTO extends AbstractRegistrationHolder
{
    private static final long serialVersionUID = 1L;

    public static final ProcessingInstructionDTO[] EMPTY_ARRAY = new ProcessingInstructionDTO[0];

    private ExperimentPE experiment;

    private String procedureTypeCode;

    private String path;

    private String description;

    private byte[] parameters;

    public final ExperimentPE getExperiment()
    {
        return experiment;
    }

    public final void setExperiment(final ExperimentPE experiment)
    {
        this.experiment = experiment;
    }

    public final String getProcedureTypeCode()
    {
        return procedureTypeCode;
    }

    public final void setProcedureTypeCode(final String procedureTypeCode)
    {
        this.procedureTypeCode = procedureTypeCode;
    }

    public final String getPath()
    {
        return path;
    }

    public final void setPath(final String path)
    {
        this.path = path;
    }

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }

    /** can be null */
    public final byte[] getParameters()
    {
        return parameters;
    }

    public final void setParameters(final byte[] parameters)
    {
        this.parameters = parameters;
    }

}
