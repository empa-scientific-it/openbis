/*
 * Copyright 2012 ETH Zuerich, CISD
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
import java.util.HashSet;
import java.util.Set;

/**
 * @author pkupczyk
 */
public class ExperimentFetchOptions implements Serializable
{

    private static final long serialVersionUID = 1L;

    private Set<ExperimentFetchOption> options = new HashSet<ExperimentFetchOption>();

    public ExperimentFetchOptions()
    {
        this((ExperimentFetchOption[]) null);
    }

    public ExperimentFetchOptions(ExperimentFetchOption... options)
    {
        // add BASIC option by default
        addOption(ExperimentFetchOption.BASIC);

        if (options != null)
        {
            for (ExperimentFetchOption option : options)
            {
                addOption(option);
            }
        }
    }

    public void addOption(ExperimentFetchOption option)
    {
        if (option == null)
        {
            throw new IllegalArgumentException("Option cannot be null");
        }
        options.add(option);
    }

    public boolean containsOption(ExperimentFetchOption option)
    {
        return options.contains(option);
    }

    public boolean containsOnlyOption(ExperimentFetchOption option)
    {
        return containsOption(option) && options.size() == 1;
    }

    @Override
    public String toString()
    {
        return options.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((options == null) ? 0 : options.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExperimentFetchOptions other = (ExperimentFetchOptions) obj;
        if (options == null)
        {
            if (other.options != null)
                return false;
        } else if (!options.equals(other.options))
            return false;
        return true;
    }

}
