package ch.systemsx.cisd.openbis.generic.server.task.events_search;

abstract class EventProcessor
{

    protected IDataSource dataSource;

    EventProcessor(IDataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    abstract void process(LastTimestamps lastTimestamps, SnapshotsFacade snapshots);
}