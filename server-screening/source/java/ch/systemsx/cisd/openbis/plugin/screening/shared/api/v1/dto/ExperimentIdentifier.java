/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Unique identifier for an experiment in openBIS.
 * 
 * @author Bernd Rinn
 */
@SuppressWarnings("unused")
@JsonObject("ExperimentIdentifier")
public class ExperimentIdentifier extends PermanentIdentifier
{
    private static final long serialVersionUID = 1L;

    private String spaceCode;

    private String projectCode;

    private String experimentCode;

    /**
     * Creates an {@link ExperimentIdentifier} from the given <var>augmentedCode</code>.
     * 
     * @param augmentedCode The <var>augmentedCode</code> in the form <code>/SPACE/PROJECT/EXPERIMENT</code>
     * @return An experiment identifer corresponding to <var>augmentedCode</code>. Note that this experiment identifier has no perm id set.
     * @throws IllegalArgumentException If the <var>augmentedCode</code> is not in the form <code>/SPACE/PROJECT/EXPERIMENT</code> or
     *             <code>PROJECT/EXPERIMENT</code>.
     */
    public static ExperimentIdentifier createFromAugmentedCode(String augmentedCode)
            throws IllegalArgumentException
    {
        final String[] splitted = augmentedCode.split("/");
        if (splitted.length == 2 && splitted[0].length() > 0) // Experiment in home space
        {
            return new ExperimentIdentifier(splitted[1], splitted[0], null, null);
        }
        if (splitted.length != 4 || splitted[0].length() != 0)
        {
            throw new IllegalArgumentException("Augmented code '" + augmentedCode
                    + "' needs to be either of the form '/SPACE/PROJECT/EXPERIMENT' "
                    + "or 'PROJECT/EXPERIMENT'.");
        }
        return new ExperimentIdentifier(splitted[3], splitted[2], splitted[1], null);
    }

    /**
     * Creates an {@link ExperimentIdentifier} from the given <var>permId</code>.
     * 
     * @param permId The <var>permId</code>
     * @return An experiment identifer corresponding to <var>permId</code>. Note that this experiment identifier has no code, project or space
     *         information.
     */
    public static ExperimentIdentifier createFromPermId(String permId)
            throws IllegalArgumentException
    {
        return new ExperimentIdentifier(null, null, null, permId);
    }

    /**
     * An <code>spaceCode == null</code> is interpreted as the home space.
     */
    public ExperimentIdentifier(String experimentCode, String projectCode, String spaceCode,
            String permId)
    {
        super(permId);
        this.spaceCode = spaceCode;
        this.projectCode = projectCode;
        this.experimentCode = experimentCode;
    }

    /**
     * The code of the space of this experiment.
     */
    public String getSpaceCode()
    {
        return spaceCode;
    }

    /**
     * The code of the project of this experiment.
     */
    public String getProjectCode()
    {
        return projectCode;
    }

    public String getExperimentCode()
    {
        return experimentCode;
    }

    /**
     * Returns the augmented (full) code of this experiment.
     */
    @JsonIgnore
    public String getAugmentedCode()
    {
        if (spaceCode != null)
        {
            return "/" + spaceCode + "/" + projectCode + "/" + experimentCode;
        } else
        {
            return projectCode + "/" + experimentCode;
        }
    }

    @Override
    public String toString()
    {
        if (getPermId() == null)
        {
            return getAugmentedCode();
        } else
        {
            return getAugmentedCode() + " [" + getPermId() + "]";
        }
    }

    //
    // JSON-RPC
    //

    private ExperimentIdentifier()
    {
        super(null);
    }

    private void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    private void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    private void setExperimentCode(String experimentCode)
    {
        this.experimentCode = experimentCode;
    }

}
