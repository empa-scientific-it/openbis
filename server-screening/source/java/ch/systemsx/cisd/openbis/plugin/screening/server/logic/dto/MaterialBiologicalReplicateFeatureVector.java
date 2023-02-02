package ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaSummaryAggregationType;

/**
 * Feature vector summary for the subgroup of well replicas together with detailed feature vectors which were used to calculate the summary.
 * 
 * @author Tomasz Pylak
 */
public class MaterialBiologicalReplicateFeatureVector implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<MaterialTechnicalReplicateFeatureVector> technicalReplicatesValues;

    // e.g. average or median of all replica values in this supgroup
    // This is the aggregation of a subgroup of replicas for e.g. the same SIRNA
    private float[] aggregatedSummary;

    // aggregation type of the subgroup summary
    private MaterialReplicaSummaryAggregationType summaryAggregationType;

    private String subgroupLabel;

    // GWT only
    @SuppressWarnings("unused")
    private MaterialBiologicalReplicateFeatureVector()
    {
    }

    public MaterialBiologicalReplicateFeatureVector(
            List<MaterialTechnicalReplicateFeatureVector> singleReplicaValues,
            float[] aggregatedSummary,
            MaterialReplicaSummaryAggregationType summaryAggregationType, String subgroupLabel)
    {
        this.technicalReplicatesValues = singleReplicaValues;
        this.aggregatedSummary = aggregatedSummary;
        this.summaryAggregationType = summaryAggregationType;
        this.subgroupLabel = subgroupLabel;
    }

    public List<MaterialTechnicalReplicateFeatureVector> getTechnicalReplicatesValues()
    {
        return technicalReplicatesValues;
    }

    public float[] getAggregatedSummary()
    {
        return aggregatedSummary;
    }

    public MaterialReplicaSummaryAggregationType getSummaryAggregationType()
    {
        return summaryAggregationType;
    }

    public String getSubgroupLabel()
    {
        return subgroupLabel;
    }
}