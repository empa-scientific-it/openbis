/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.importer.delay;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DelayedExecution
{
    private final IObjectId identifier;

    private final String variable;

    private final Serializable creationOrUpdate;

    private final Set<IObjectId> dependencies;

    private final int page;

    private final int line;

    public DelayedExecution(String variable, IObjectId identifier, Serializable creationOrUpdate, int page, int line)
    {
        this.identifier = identifier;
        this.variable = variable;
        this.creationOrUpdate = creationOrUpdate;
        this.dependencies = new HashSet<>();
        this.page = page;
        this.line = line;
    }

    public void addDependencies(List<? extends IObjectId> dependencies)
    {
        this.dependencies.addAll(dependencies);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        DelayedExecution that = (DelayedExecution) o;
        return creationOrUpdate.getClass() == that.creationOrUpdate.getClass() && identifier.equals(that.identifier);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(creationOrUpdate.getClass(), identifier);
    }

    public IObjectId getIdentifier()
    {
        return identifier;
    }

    public String getVariable() {
        return variable;
    }

    public Serializable getCreationOrUpdate()
    {
        return creationOrUpdate;
    }

    public int getPage()
    {
        return page;
    }

    public int getLine()
    {
        return line;
    }

    public Set<? extends IObjectId> getDependencies()
    {
        return dependencies;
    }
}
