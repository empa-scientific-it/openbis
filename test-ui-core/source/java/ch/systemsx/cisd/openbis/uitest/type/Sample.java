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

package ch.systemsx.cisd.openbis.uitest.type;

import java.util.Collection;
import java.util.Map;

/**
 * @author anttil
 */
public abstract class Sample implements Entity
{
    public abstract String getCode();

    public abstract SampleType getType();

    public abstract Experiment getExperiment();

    public abstract Space getSpace();

    public abstract Collection<Sample> getParents();

    public abstract Sample getContainer();

    public abstract Map<PropertyType, Object> getProperties();

    @Override
    public final boolean equals(Object o)
    {
        if (o instanceof Sample)
        {
            return ((Sample) o).getCode().equalsIgnoreCase(getCode());
        }
        return false;
    }

    @Override
    public final int hashCode()
    {
        return getCode().toUpperCase().hashCode();
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " " + this.getCode();
    }
}
