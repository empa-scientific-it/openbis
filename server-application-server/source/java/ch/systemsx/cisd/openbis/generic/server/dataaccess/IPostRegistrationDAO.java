package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.Collection;

public interface IPostRegistrationDAO
{
    /**
     * Adds dataset to post-registration queue.
     */
    public void addDataSet(String dataSetCode);

    /**
     * Removes dataset from the post-registration queue.
     */
    public void removeDataSet(String dataSetCode);

    /**
     * Find all datasets wich are in the post-registration queue.
     */
    public Collection<Long> listDataSetsForPostRegistration();

}
