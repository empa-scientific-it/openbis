package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

/**
 * Interface to delegate export and refresh actions.
 */
public interface IBrowserGridActionInvoker
{
    boolean supportsExportForUpdate();

    void export(TableExportType type);

    void refresh();

    void configure();

    void toggleFilters(boolean show);

}
