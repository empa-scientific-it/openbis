package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;

/**
 * A class that converts {@link Experiment} objects to {@link DataSetRelatedEntities} objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class ExperimentToDataSetRelatedEntitiesTranslator
{
    // A map from experiment type Id to experiment type.
    private final HashMap<String, ExperimentType> experimentTypesMap;

    private final List<Experiment> experiments;

    private final ArrayList<BasicEntityInformationHolder> entityInformationHolders;

    /**
     * Creates a translator from public {@Experiment} objects to the internal {@link DataSetRelatedEntities} objects.
     * <p>
     * A list of experiment types known to the DB must be provided because Experiment knows only the code of the ExperimentType.
     * 
     * @param experimentTypes A list of ExperimentTypes known to the DB.
     * @param experiments The experiments to convert.
     */
    public ExperimentToDataSetRelatedEntitiesTranslator(List<ExperimentType> experimentTypes,
            List<Experiment> experiments)
    {
        this.experimentTypesMap = convertExperimentTypesListToMap(experimentTypes);
        this.experiments = experiments;
        entityInformationHolders = new ArrayList<BasicEntityInformationHolder>(experiments.size());
    }

    private static HashMap<String, ExperimentType> convertExperimentTypesListToMap(
            List<ExperimentType> experimentTypes)
    {
        HashMap<String, ExperimentType> map =
                new HashMap<String, ExperimentType>(experimentTypes.size());

        for (ExperimentType experimentType : experimentTypes)
        {
            map.put(experimentType.getCode(), experimentType);
        }

        return map;
    }

    public DataSetRelatedEntities convertToDataSetRelatedEntities()
    {
        for (Experiment experiment : experiments)
        {
            BasicEntityInformationHolder holderOrNull =
                    tryConvertExperimentToEntityInformationHolder(experiment);
            if (null != holderOrNull)
            {
                entityInformationHolders.add(holderOrNull);
            }
        }
        return new DataSetRelatedEntities(entityInformationHolders);
    }

    private BasicEntityInformationHolder tryConvertExperimentToEntityInformationHolder(
            Experiment experiment)
    {
        EntityType entityType = experimentTypesMap.get(experiment.getExperimentTypeCode());
        if (null == entityType)
        {
            return null;
        }
        BasicEntityInformationHolder holder =
                new BasicEntityInformationHolder(EntityKind.EXPERIMENT, entityType,
                        experiment.getCode(), experiment.getId(), experiment.getPermId());
        return holder;
    }
}