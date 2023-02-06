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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author pkupczyk
 */
public class FetchOptions<T extends FetchOption> implements Serializable
{

    private static final long serialVersionUID = 1L;

    private Set<T> optionsSet = new HashSet<T>();

    public FetchOptions()
    {
        this((T[]) null);
    }

    public FetchOptions(T... options)
    {
        if (options != null)
        {
            for (T option : options)
            {
                addOption(option);
            }
        }
    }

    public void addOption(T option)
    {
        if (option == null)
        {
            throw new IllegalArgumentException("Option cannot be null");
        }
        optionsSet.add(option);
    }

    public boolean isSetOf(T... options)
    {
        if (options == null)
        {
            return optionsSet.isEmpty();
        } else
        {
            HashSet<T> anOptionsSet = new HashSet<T>(Arrays.asList(options));
            return optionsSet.equals(anOptionsSet);
        }
    }

    public boolean isSupersetOf(T... options)
    {
        if (options == null)
        {
            return true;
        } else
        {
            HashSet<T> anOptionsSet = new HashSet<T>(Arrays.asList(options));
            return optionsSet.containsAll(anOptionsSet);
        }
    }

    public boolean isSubsetOf(T... options)
    {
        if (options == null)
        {
            return optionsSet.isEmpty();
        } else
        {
            HashSet<T> anOptionsSet = new HashSet<T>(Arrays.asList(options));
            return anOptionsSet.containsAll(optionsSet);
        }
    }

    @Override
    public String toString()
    {
        return optionsSet.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((optionsSet == null) ? 0 : optionsSet.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FetchOptions other = (FetchOptions) obj;
        if (optionsSet == null)
        {
            if (other.optionsSet != null)
                return false;
        } else if (!optionsSet.equals(other.optionsSet))
            return false;
        return true;
    }

}
