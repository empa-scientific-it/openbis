package ch.ethz.sis.openbis.generic.server.xls.importxls.delay;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DelayedExecution
{
    private IObjectId identifier;

    private String variable;

    private Serializable creationOrUpdate;

    private Set<IObjectId> dependencies;

    private int page;

    private int line;

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
