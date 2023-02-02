package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * A helper class for retrieving data sets associated with plates.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class PlateDatasetLoader
{
    // TODO 2010-05-27, CR : This class and its subclasses should be refactored. The subclasses
    // should become independent and use the dataset loader, instead of being dataset loaders.

    // Infrastructure state
    protected final Session session;

    protected final IScreeningBusinessObjectFactory businessObjectFactory;

    // Parameter state
    private final Set<? extends PlateIdentifier> plates;

    private final String[] datasetTypeCodePatterns;

    private String homeSpaceOrNull;

    // Running state
    private List<Sample> samples;

    private List<AbstractExternalData> datasets;

    private HashMap<SampleIdentifier, Sample> samplesByIdentifier;

    private HashMap<Long, Sample> samplesById;

    private Set<Long> initialSampleIds;

    private boolean loaded = false;

    PlateDatasetLoader(Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            String homeSpaceOrNull, Set<? extends PlateIdentifier> plates,
            String... datasetTypeCodes)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.homeSpaceOrNull = (homeSpaceOrNull != null) ? ("/" + homeSpaceOrNull + "/") : null;
        this.plates = plates;
        this.datasetTypeCodePatterns = datasetTypeCodes;
    }

    protected List<AbstractExternalData> getDatasets()
    {
        return datasets;
    }

    /**
     * Loads the samples and datasets. Call the method before calling {@link #getDatasets()}.
     */
    protected void load()
    {
        if (loaded == false)
        {
            loadSamples();
            loadDatasets();
            loaded = true;
        }
    }

    private void loadSamples()
    {
        // NOTE: plate identifier can use augmented codes or/and permIds. That is why we have to use
        // both when fetching plates.
        // It can result in fetching the same sample twice, but it does not hurt.
        String[] sampleCodesArray = extractSampleCodes();
        String[] samplePermIdArray = extractPermIds();
        ListOrSearchSampleCriteria criteria =
                new ListOrSearchSampleCriteria(sampleCodesArray, samplePermIdArray);
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        samples = sampleLister.list(criteria);
        // After loading the samples, we need to initialize the maps because other methods rely
        // on this.
        initializeSampleMaps();
        filterSamplesByPlateIdentifiers();
    }

    private void loadDatasets()
    {
        IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        datasets = datasetLister.listBySampleIds(initialSampleIds);
        datasets =
                ScreeningUtils.filterExternalDataByTypePattern(datasets, datasetTypeCodePatterns);
    }

    private void initializeSampleMaps()
    {
        samplesByIdentifier = new HashMap<SampleIdentifier, Sample>();
        samplesById = new HashMap<Long, Sample>();
        for (Sample sample : samples)
        {
            samplesByIdentifier.put(createSampleIdentifier(sample), sample);
            samplesById.put(sample.getId(), sample);
        }
        initialSampleIds = new HashSet<Long>(samplesById.keySet());
    }

    /**
     * Filter the list of samples, which were selected by code, to those that exactly match the identifiers in the plates
     */
    private void filterSamplesByPlateIdentifiers()
    {
        final Set<String> augmentedCodeSet = new HashSet<String>(plates.size());
        final Set<String> permIdSet = new HashSet<String>(plates.size());
        for (PlateIdentifier plate : plates)
        {
            if (plate.getPermId() != null)
            {
                permIdSet.add(plate.getPermId());
            } else
            {
                if (SpaceCodeHelper.isHomeSpace(plate.tryGetSpaceCode()))
                {
                    if (homeSpaceOrNull == null)
                    {
                        throw UserFailureException.fromTemplate(
                                "Plate '%s' is in home space, but user has no home space defined.",
                                plate);
                    }
                    augmentedCodeSet.add(homeSpaceOrNull + plate.getAugmentedCode());
                } else
                {
                    augmentedCodeSet.add(plate.getAugmentedCode());
                }
            }
        }
        for (Iterator<Sample> it = samples.iterator(); it.hasNext(); /**/)
        {
            final Sample sample = it.next();
            final String sampleSpaceCodeOrNull =
                    (null == sample.getSpace()) ? null : sample.getSpace().getCode();
            final String augmentedCode =
                    (sampleSpaceCodeOrNull == null) ? ("/" + sample.getCode()) : ("/"
                            + sampleSpaceCodeOrNull + "/" + sample.getCode());
            if (permIdSet.contains(sample.getPermId()) == false
                    && augmentedCodeSet.contains(augmentedCode) == false)
            {
                it.remove();
                samplesByIdentifier.remove(createSampleIdentifier(sample));
                samplesById.remove(sample.getId());
            }
        }
    }

    protected PlateIdentifier createPlateIdentifier(AbstractExternalData dataSet)
    {
        return ScreeningUtils.createPlateIdentifier(getSample(dataSet));
    }

    protected static ExperimentIdentifier createExperimentIdentifier(AbstractExternalData parentDataset)
    {
        Experiment experiment = parentDataset.getExperiment();
        if (experiment == null)
        {
            return null;
        }
        String code = experiment.getCode();
        String permId = experiment.getPermId();
        String projectCode = null;
        String spaceCode = null;
        Project project = experiment.getProject();
        if (project != null)
        {
            projectCode = project.getCode();
            Space space = project.getSpace();
            if (space != null)
            {
                spaceCode = space.getCode();
            }
        }
        if (spaceCode == null)
        {
            Sample sample = parentDataset.getSample();
            if (sample != null)
            {
                Space space = sample.getSpace();
                if (space != null)
                {
                    spaceCode = space.getCode();
                }
            }
        }
        return new ExperimentIdentifier(code, projectCode, spaceCode, permId);
    }

    protected Geometry extractPlateGeometry(AbstractExternalData dataSet)
    {
        Sample sample = getSample(dataSet);
        List<IEntityProperty> properties = sample.getProperties();
        for (IEntityProperty property : properties)
        {
            PropertyType propertyType = property.getPropertyType();
            if (propertyType.getCode().equals(ScreeningConstants.PLATE_GEOMETRY))
            {
                final String code = property.getVocabularyTerm().getCode();
                try
                {
                    return Geometry.createFromPlateGeometryString(code);
                } catch (IllegalArgumentException ex)
                {
                    throw new UserFailureException("Invalid property "
                            + ScreeningConstants.PLATE_GEOMETRY + ": " + code);
                }

            }
        }
        throw new UserFailureException("Sample '" + sample.getIdentifier() + "' has no property "
                + ScreeningConstants.PLATE_GEOMETRY);
    }

    protected Map<String, String> extractProperties(AbstractExternalData dataSet)
    {
        final Map<String, String> properties = new HashMap<String, String>();
        if (dataSet.getProperties() != null)
        {
            for (IEntityProperty prop : dataSet.getProperties())
            {
                final String value = prop.tryGetAsString();
                if (value != null)
                {
                    properties.put(prop.getPropertyType().getCode(), value);
                }
            }
        }
        return properties;
    }

    private Sample getSample(AbstractExternalData dataset)
    {
        // Sample may be NULL even though the selector does not begin with try
        Sample sample = dataset.getSample();
        if (sample == null)
        {
            DataSetType dataSetType = dataset.getDataSetType();
            throw new IllegalStateException("dataset not connected to a sample: " + dataset.getCode()
                    + (dataSetType == null ? "" : " [" + dataSetType.getCode() + "]"));
        }
        // The dataset's reference to the sample is not complete, get the one from the map
        Sample result = samplesById.get(sample.getId());

        // if the sample is not found in cache - it means it is a sample related via parent dataset
        // we can fetch and cache it now
        if (result == null)
        {
            ListOrSearchSampleCriteria criteria =
                    new ListOrSearchSampleCriteria(Collections.singletonList(sample.getId()));
            ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
            samples = sampleLister.list(criteria);
            assert samples.size() == 1;
            result = samples.get(0);
            samplesById.put(sample.getId(), result);
        }
        return result;
    }

    private String[] extractSampleCodes()
    {
        ArrayList<String> sampleCodes = new ArrayList<String>();
        for (PlateIdentifier plate : plates)
        {
            if (plate.getPlateCode() != null && plate.getPermId() == null)
            {
                sampleCodes.add(plate.getPlateCode());
            }
        }

        String[] sampleCodesArray = new String[sampleCodes.size()];
        sampleCodes.toArray(sampleCodesArray);

        return sampleCodesArray;
    }

    private String[] extractPermIds()
    {
        ArrayList<String> samplePermIds = new ArrayList<String>();
        for (PlateIdentifier plate : plates)
        {
            if (plate.getPermId() != null)
            {
                samplePermIds.add(plate.getPermId());
            }
        }

        String[] sampleCodesArray = new String[samplePermIds.size()];
        samplePermIds.toArray(sampleCodesArray);

        return sampleCodesArray;
    }

    protected static String getDataStoreUrlFromDataStore(DataStore dataStore)
    {
        String datastoreUrl = dataStore.getHostUrl();
        if (datastoreUrl.endsWith("/"))
        {
            datastoreUrl = datastoreUrl.substring(0, datastoreUrl.length() - 1);
        }

        return datastoreUrl;
    }

    protected static SampleIdentifier createSampleIdentifier(Sample sample)
    {
        SampleOwnerIdentifier owner;
        Space spaceOrNull = sample.getSpace();
        Project project = sample.getProject();
        if (project != null)
        {
            Space projectSpace = project.getSpace();
            if (projectSpace == null)
            {
                throw new IllegalArgumentException("Missing project space of sample " + sample.getIdentifier());
            }
            owner = new SampleOwnerIdentifier(new ProjectIdentifier(projectSpace.getCode(), project.getCode()));
        } else if (spaceOrNull != null)
        {
            SpaceIdentifier space = new SpaceIdentifier(spaceOrNull.getCode());
            owner = new SampleOwnerIdentifier(space);
        } else
        {
            owner = new SampleOwnerIdentifier();
        }
        return SampleIdentifier.createOwnedBy(owner, sample.getCode());
    }
}
