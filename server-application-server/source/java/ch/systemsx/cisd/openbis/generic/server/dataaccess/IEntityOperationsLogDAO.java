package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import ch.systemsx.cisd.openbis.generic.shared.dto.EntityOperationsLogEntryPE;

public interface IEntityOperationsLogDAO
{
    /**
     * Adds an entry to the log for the given registrationId.
     */
    public void addLogEntry(Long registrationId);

    /**
     * Return the entry with the given registrationId or null if none exists
     */
    public EntityOperationsLogEntryPE tryFindLogEntry(Long registrationId);

}
