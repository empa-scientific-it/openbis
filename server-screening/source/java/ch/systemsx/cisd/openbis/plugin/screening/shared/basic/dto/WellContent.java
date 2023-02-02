/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes the well and its location on the plate. Contains pointers to well and plate samples, a material inside the well and the images from one
 * dataset (if available) acquired for the well.
 * 
 * @author Tomasz Pylak
 */
public class WellContent extends WellImage implements Serializable, IEntityPropertiesHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // ------------ Metadata -----------

    /**
     * well properties also contain the referenced materials (if any) enriched with material properties.
     */
    private List<IEntityProperty> wellProperties = new ArrayList<IEntityProperty>(0);

    /**
     * this is a lazy-initialized sublist of ({@link #wellProperties}), containing only the well properties which refer to a material.
     */
    private List<IEntityProperty> wellPropertiesOfMaterialType;

    private EntityReference plate;

    // ------------ Dataset Data -------------

    // dataset which contains feature vectors for this well, null if images have not been analyzed
    // or the information has not been loaded
    private DatasetReference featureVectorDatasetOrNull;

    // Feature vector values, null if images have not been analyzed or the information has not been
    // loaded
    private NamedFeatureVector featureVectorOrNull;

    // GWT only
    @SuppressWarnings("unused")
    private WellContent()
    {
    }

    public WellContent(WellLocation locationOrNull, EntityReference well, EntityReference plate,
            ExperimentReference experiment)
    {
        this(locationOrNull, well, plate, experiment, null, null, null, null);
    }

    private WellContent(WellLocation locationOrNull, EntityReference well, EntityReference plate,
            ExperimentReference experiment, List<IEntityProperty> wellProperties,
            DatasetImagesReference imagesDatasetOrNull,
            DatasetReference featureVectorDatasetOrNull, NamedFeatureVector featureVectorOrNull)
    {
        super(locationOrNull, well, experiment, imagesDatasetOrNull);
        this.plate = plate;
        this.featureVectorDatasetOrNull = featureVectorDatasetOrNull;
        this.featureVectorOrNull = featureVectorOrNull;
        this.wellProperties = wellProperties;
    }

    public EntityReference getPlate()
    {
        return plate;
    }

    public List<IEntityProperty> getMaterialTypeProperties()
    {
        if (wellPropertiesOfMaterialType == null)
        {
            wellPropertiesOfMaterialType = filterWellPropertiesOfMaterialType();
        }
        return Collections.unmodifiableList(wellPropertiesOfMaterialType);
    }

    private List<IEntityProperty> filterWellPropertiesOfMaterialType()
    {
        ArrayList<IEntityProperty> materialProps = new ArrayList<IEntityProperty>();
        for (IEntityProperty property : getWellProperties())
        {
            DataTypeCode propertyDataTypeCode = property.getPropertyType().getDataType().getCode();
            if (propertyDataTypeCode == DataTypeCode.MATERIAL)
            {
                materialProps.add(property);
            }
        }
        return materialProps;
    }

    public List<Material> getMaterialContents()
    {
        ArrayList<Material> materials = new ArrayList<Material>();
        for (IEntityProperty property : getMaterialTypeProperties())
        {
            Material materialOrNull = property.getMaterial();
            if (materialOrNull != null)
            {
                materials.add(materialOrNull);
            }
        }
        return materials;
    }

    public DatasetReference tryGetFeatureVectorDataset()
    {
        return featureVectorDatasetOrNull;
    }

    public NamedFeatureVector tryGetFeatureVectorValues()
    {
        return featureVectorOrNull;
    }

    public WellContent cloneWithImageDatasets(DatasetImagesReference newImagesDatasetOrNull,
            DatasetReference newFeatureVectorDatasetOrNull)
    {
        return new WellContent(this.locationOrNull, this.well, this.plate, this.experiment,
                this.wellProperties, newImagesDatasetOrNull, newFeatureVectorDatasetOrNull,
                this.featureVectorOrNull);
    }

    public WellContent cloneWithFeatureVector(NamedFeatureVector newFeatureVectorOrNull)
    {
        return new WellContent(this.locationOrNull, this.well, this.plate, this.experiment,
                this.wellProperties, this.imagesDatasetOrNull, this.featureVectorDatasetOrNull,
                newFeatureVectorOrNull);
    }

    @Override
    public String toString()
    {
        return "location = " + locationOrNull + ", experiment = " + experiment + ", plate = "
                + plate + ", well = " + well;
    }

    public List<IEntityProperty> getWellProperties()
    {
        if (wellProperties == null)
        {
            return Collections.emptyList();
        } else
        {
            return Collections.unmodifiableList(wellProperties);
        }
    }

    public void setWellProperties(List<IEntityProperty> properties)
    {
        this.wellProperties = properties;
    }

    /** id of the well */
    @Override
    public Long getId()
    {
        return well.getId();
    }

    /** properties of the well */
    @Override
    public List<IEntityProperty> getProperties()
    {
        return getWellProperties();
    }

}
