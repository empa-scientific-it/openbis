package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Statistics
{

    private int loadedSpaces;

    private int loadedProjects;

    private int loadedExperiments;

    private int loadedSamples;

    private int loadedEvents;

    private int createdEvents;

    public void increaseLoadedSpaces(int count)
    {
        loadedSpaces += count;
    }

    public void increaseLoadedProjects(int count)
    {
        loadedProjects += count;
    }

    public void increaseLoadedExperiments(int count)
    {
        loadedExperiments += count;
    }

    public void increaseLoadedSamples(int count)
    {
        loadedSamples += count;
    }

    public void increaseLoadedEvents(int count)
    {
        loadedEvents += count;
    }

    public void increaseCreatedEvents(int count)
    {
        createdEvents += count;
    }

    @Override
    public String toString()
    {
        return new ReflectionToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE).toString();
    }
}
