package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;

/**
 * {@link IBatchOperation} creating new experiments.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentBatchRegistration implements IBatchOperation<NewBasicExperiment>
{
    private final IExperimentTable experimentTable;

    private final List<NewBasicExperiment> newExperiments;

    private final ExperimentTypePE experimentType;

    public ExperimentBatchRegistration(IExperimentTable experimentTable,
            List<NewBasicExperiment> newExperiments, ExperimentTypePE experimentType)
    {
        this.experimentTable = experimentTable;
        this.newExperiments = newExperiments;
        this.experimentType = experimentType;
    }

    @Override
    public void execute(List<NewBasicExperiment> entities)
    {
        experimentTable.add(entities, experimentType);
        experimentTable.save();
    }

    @Override
    public List<NewBasicExperiment> getAllEntities()
    {
        return newExperiments;
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