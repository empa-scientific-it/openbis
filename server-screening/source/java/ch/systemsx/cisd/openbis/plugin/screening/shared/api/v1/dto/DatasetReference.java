package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Description of one plate-based screening dataset.
 * 
 * @author Tomasz Pylak
 */
@SuppressWarnings("unused")
@JsonObject("DatasetReference")
public class DatasetReference extends DatasetIdentifier implements Serializable
{
    private static final long serialVersionUID = 1L;

    private PlateIdentifier plate;

    private ExperimentIdentifier experimentIdentifier;

    private Geometry plateGeometry;

    private Date registrationDate;

    private Map<String, String> properties = Collections.<String, String> emptyMap();

    private String dataSetType;

    @Deprecated
    public DatasetReference(String datasetCode, String datastoreServerUrl, PlateIdentifier plate)
    {
        this(datasetCode, datastoreServerUrl, plate, null, null);
    }

    @Deprecated
    public DatasetReference(String datasetCode, String datastoreServerUrl, PlateIdentifier plate,
            Geometry plateGeometry, Date registrationDate)
    {
        this(datasetCode, datastoreServerUrl, plate, null, plateGeometry, registrationDate, null);
    }

    @Deprecated
    public DatasetReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plateWithExperiment, ExperimentIdentifier experiment,
            Geometry plateGeometry, Date registrationDate, Map<String, String> propertiesOrNull)
    {
        this(datasetCode, null, datastoreServerUrl, plateWithExperiment, experiment, plateGeometry,
                registrationDate, propertiesOrNull);
    }

    public DatasetReference(String datasetCode, String dataSetTypeOrNull,
            String datastoreServerUrl, PlateIdentifier plateWithExperiment,
            ExperimentIdentifier experiment, Geometry plateGeometry, Date registrationDate,
            Map<String, String> propertiesOrNull)
    {
        super(datasetCode, datastoreServerUrl);
        this.dataSetType = dataSetTypeOrNull;
        this.plate = plateWithExperiment;
        this.experimentIdentifier = (experiment == null) ? createFakeExperiment(plate) : experiment;
        this.plateGeometry = plateGeometry;
        this.registrationDate = registrationDate;
        if (propertiesOrNull != null)
        {
            this.properties = Collections.unmodifiableMap(propertiesOrNull);
        }
    }

    private static ExperimentIdentifier createFakeExperiment(PlateIdentifier plate)
    {
        return new ExperimentIdentifier("?", "?", (plate == null) ? "?" : plate.tryGetSpaceCode(),
                "?");
    }

    /**
     * Returns data set type.
     * 
     * @since 1.7
     */
    public String getDataSetType()
    {
        return dataSetType;
    }

    /**
     * Returns the plate geometry.
     */
    public final Geometry getPlateGeometry()
    {
        return plateGeometry;
    }

    /**
     * Returns the registration date.
     */
    public final Date getRegistrationDate()
    {
        return registrationDate;
    }

    /**
     * Returns the identifier of the plate that this dataset belongs to.
     */
    public PlateIdentifier getPlate()
    {
        return plate;
    }

    /**
     * Returns the identifier of the experiment that this dataset belongs to.
     * 
     * @since 1.2
     */
    public ExperimentIdentifier getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    /**
     * Returns the data set properties.
     * 
     * @since 1.5
     */
    public Map<String, String> getProperties()
    {
        return properties;
    }

    // Special method for customizing Java deserialization.
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        // Kick-off the default serialization procedure.
        in.defaultReadObject();
        // V1.0 and V1.1 didn't have the experimentIdentifier, so it may be null here.
        if (experimentIdentifier == null)
        {
            experimentIdentifier = createFakeExperiment(plate);
        }
    }

    @Override
    public String toString()
    {
        return super.toString() + " (plate: " + plate + ")";
    }

    //
    // JSON-RPC
    //

    private DatasetReference()
    {
        super(null, null);
    }

    private void setPlate(PlateIdentifier plate)
    {
        this.plate = plate;
    }

    private void setExperimentIdentifier(ExperimentIdentifier experimentIdentifier)
    {
        this.experimentIdentifier = experimentIdentifier;
    }

    private void setPlateGeometry(Geometry plateGeometry)
    {
        this.plateGeometry = plateGeometry;
    }

    private void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    private void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    private void setDataSetType(String dataSetType)
    {
        this.dataSetType = dataSetType;
    }

}