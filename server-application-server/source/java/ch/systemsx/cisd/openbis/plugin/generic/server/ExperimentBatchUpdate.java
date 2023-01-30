package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;

/**
 * {@link IBatchOperation} updating experiments. Based on {@link ExperimentBatchRegistration}
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentBatchUpdate implements IBatchOperation<ExperimentBatchUpdatesDTO>
{
    private final IExperimentTable experimentTable;

    private final List<ExperimentBatchUpdatesDTO> entities;

    public ExperimentBatchUpdate(IExperimentTable experimentTable,
            List<ExperimentBatchUpdatesDTO> newExperiments, ExperimentTypePE experimentType)
    {
        this.experimentTable = experimentTable;
        this.entities = newExperiments;
    }

    @Override
    public void execute(List<ExperimentBatchUpdatesDTO> entitiesToUpdate)
    {
        experimentTable.prepareForUpdate(entitiesToUpdate);
        experimentTable.save();
    }

    @Override
    public List<ExperimentBatchUpdatesDTO> getAllEntities()
    {
        return entities;
    }

    @Override
    public String getEntityName()
    {
        return "experiment";
    }

    @Override
    public String getOperationName()
    {
        return "register";
    }

}