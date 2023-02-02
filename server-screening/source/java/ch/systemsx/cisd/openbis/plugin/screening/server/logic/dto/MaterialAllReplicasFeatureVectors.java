package ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;

/**
 * Feature vectors (details and summaries) for one material.
 * 
 * @author Tomasz Pylak
 */
public class MaterialAllReplicasFeatureVectors
{
    // NOTE: has the same length as feature vectors in all summaries
    private final List<CodeAndLabel> featureDescriptions;

    private final MaterialIdFeatureVectorSummary generalSummary;

    // NOTE: Can be empty.
    private final List<MaterialBiologicalReplicateFeatureVector> biologicalReplicates;

    // NOTE: Can be empty. Used for replicas which have no subgroups
    private final List<MaterialTechnicalReplicateFeatureVector> directTechnicalReplicates;

    public MaterialAllReplicasFeatureVectors(List<CodeAndLabel> featureDescriptions,
            MaterialIdFeatureVectorSummary generalSummary,
            List<MaterialBiologicalReplicateFeatureVector> subgroups,
            List<MaterialTechnicalReplicateFeatureVector> replicas)
    {
        this.featureDescriptions = featureDescriptions;
        this.generalSummary = generalSummary;
        this.biologicalReplicates = subgroups;
        this.directTechnicalReplicates = replicas;
    }

    public List<CodeAndLabel> getFeatureDescriptions()
    {
        return featureDescriptions;
    }

    public MaterialIdFeatureVectorSummary getGeneralSummary()
    {
        return generalSummary;
    }

    public List<MaterialBiologicalReplicateFeatureVector> getBiologicalReplicates()
    {
        return biologicalReplicates;
    }

    public List<MaterialTechnicalReplicateFeatureVector> getDirectTechnicalReplicates()
    {
        return directTechnicalReplicates;
    }
}