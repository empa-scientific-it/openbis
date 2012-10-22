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

/**
 * @author anttil
 */
public abstract class Script
{
    public abstract String getName();

    public abstract ScriptType getType();

    public abstract EntityKind getKind();

    public abstract String getDescription();

    public abstract String getContent();

    @Override
    public final boolean equals(Object o)
    {
        if (o instanceof Script)
        {
            return ((Script) o).getName().equalsIgnoreCase(getName());
        }
        return false;
    }

    @Override
    public final int hashCode()
    {
        return getName().toUpperCase().hashCode();
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " " + this.getName();
    }

}
