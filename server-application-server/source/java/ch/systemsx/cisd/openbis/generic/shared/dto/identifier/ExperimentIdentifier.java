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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;

/**
 * Identifies an experiment.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentIdentifier extends ProjectIdentifier
{
    private static final long serialVersionUID = IServer.VERSION;

    final static String CODE_SEPARATOR = "/";

    private String experimentCode;

    public ExperimentIdentifier(Experiment experiment)
    {
        this(experiment.getProject().getSpace().getCode(), experiment.getProject().getCode(),
                experiment.getCode());
    }

    public ExperimentIdentifier()
    {
        this(null, null, null);
    }

    public ExperimentIdentifier(final ProjectIdentifier projectIdentifier,
            final String experimentCode)
    {
        this(projectIdentifier.getSpaceCode(),
                projectIdentifier.getProjectCode(), experimentCode);
    }

    public ExperimentIdentifier(final String projectCode, final String experimentCode)
    {
        this(getHomeSpaceCode(), projectCode, experimentCode);
    }

    public ExperimentIdentifier(final String groupCode,
            final String projectCode, final String experimentCode)
    {
        super(groupCode, projectCode);
        setExperimentCode(experimentCode);
    }

    public String getExperimentCode()
    {
        return StringUtils.upperCase(experimentCode);
    }

    public void setExperimentCode(final String experimentCode)
    {
        this.experimentCode = experimentCode;
    }

    /**
     * Returns an unique description of this object.
     */
    public final String describe()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(getProjectCode());
        builder.append(CODE_SEPARATOR);
        builder.append(getExperimentCode());
        return builder.toString();
    }

    //
    // ProjectIdentifier
    //

    @Override
    public final String toString()
    {
        return super.toString() + Constants.IDENTIFIER_SEPARATOR + experimentCode;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ExperimentIdentifier == false)
        {
            return false;
        }
        final ExperimentIdentifier that = (ExperimentIdentifier) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getSpaceCode(), that.getSpaceCode());
        builder.append(getProjectCode(), that.getProjectCode());
        builder.append(getExperimentCode(), that.getExperimentCode());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getSpaceCode());
        builder.append(getProjectCode());
        builder.append(getExperimentCode());
        return builder.toHashCode();
    }
}
