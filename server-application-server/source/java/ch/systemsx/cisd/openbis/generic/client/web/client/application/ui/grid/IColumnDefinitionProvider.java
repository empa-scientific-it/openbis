package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * Interface allowing the access to the column definitions.
 * 
 * @author Izabela Adamczyk
 */
public interface IColumnDefinitionProvider<T>
{
    public List<IColumnDefinition<T>> getColumnDefinitions(List<String> columnIds);
}
