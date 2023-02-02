package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.IOException;
import java.io.ObjectInputStream;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Unique identifier for a plate which is assigned to an experiment. This class really should be called <code>ExperimentPlateIdentifier</code>.
 * 
 * @author Tomasz Pylak
 */
@SuppressWarnings("unused")
@JsonObject("Plate")
public class Plate extends PlateIdentifier
{
    private static final long serialVersionUID = 1L;

    // Keep for backward compatibility
    private String experimentCode, projectCode;

    private ExperimentIdentifier experimentIdentifier;

    @Deprecated
    public Plate(String plateCode, String experimentCode, String projectCode, String spaceCode)
    {
        this(plateCode, spaceCode, null, new ExperimentIdentifier(spaceCode, projectCode,
                experimentCode, null));
    }

    public Plate(String plateCode, String spaceCode, String permId,
            ExperimentIdentifier experimentIdentifier)
    {
        this(plateCode, spaceCode, null, permId, experimentIdentifier);
    }
    
    public Plate(String plateCode, String spaceCode, String sampleProjectCode, String permId,
            ExperimentIdentifier experimentIdentifier)
    {
        super(plateCode, spaceCode, sampleProjectCode, permId);
        this.experimentCode = experimentIdentifier.getExperimentCode();
        this.projectCode = experimentIdentifier.getProjectCode();
        this.experimentIdentifier = experimentIdentifier;
    }

    /**
     * Get the identifier of the experiment that this plate is assigned to.
     * 
     * @since 1.1
     */
    public ExperimentIdentifier getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    /**
     * The code of the experiment to which the plate belongs.
     */
    public String getExperimentCode()
    {
        return experimentCode;
    }

    /**
     * The code of the project to which the plate belongs.
     */
    public String getProjectCode()
    {
        return projectCode;
    }

    // Special method for customizing Java deserialization.
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        // Kick-off the default serialization procedure.
        in.defaultReadObject();
        // V1.0 didn't have the experimentIdentifier, so it may be null here.
        if (experimentIdentifier == null)
        {
            experimentIdentifier =
                    new ExperimentIdentifier(experimentCode, projectCode, tryGetSpaceCode(), null);
        }
    }

    @Override
    public String toString()
    {
        return super.toString() + " { Experiment: " + experimentIdentifier.getAugmentedCode()
                + " }";
    }

    //
    // JSON-RPC
    //

    private Plate()
    {
        super(null, null);
    }

    private void setExperimentCode(String experimentCode)
    {
        this.experimentCode = experimentCode;
    }

    private void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    private void setExperimentIdentifier(ExperimentIdentifier experimentIdentifier)
    {
        this.experimentIdentifier = experimentIdentifier;
    }

}