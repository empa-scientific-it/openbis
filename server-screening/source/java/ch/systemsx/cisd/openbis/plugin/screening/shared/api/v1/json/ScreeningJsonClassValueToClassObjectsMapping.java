/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.json;

import java.util.List;

import ch.systemsx.cisd.openbis.common.api.server.json.mapping.JsonStaticClassValueToClassObjectsMapping;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.json.GenericJsonClassValueToClassObjectsMapping;

/**
 * @author pkupczyk
 */
public class ScreeningJsonClassValueToClassObjectsMapping extends
        JsonStaticClassValueToClassObjectsMapping
{

    private static ScreeningJsonClassValueToClassObjectsMapping instance;

    private ScreeningJsonClassValueToClassObjectsMapping()
    {
        addClass(".FeatureVector",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector.class);
        addClass(
                ".FeatureVectorDataset",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset.class);
        addClass(
                ".FeatureVectorWithDescription",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription.class);
        addClass(
                ".MaterialIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier.class);
        addClass(
                ".PermanentIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PermanentIdentifier.class);
        addClass(
                ".ExperimentIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier.class);
        addClass(".PlateIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier.class);
        addClass(".Plate", ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate.class);
        addClass(".PlateMetadata",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateMetadata.class);
        addClass(".WellIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier.class);
        addClass(".WellMetadata",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellMetadata.class);
        addClass(".DatasetIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetIdentifier.class);
        addClass(".DatasetReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetReference.class);
        addClass(
                ".FeatureVectorDatasetReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference.class);
        addClass(
                ".FeatureVectorDatasetWellReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference.class);
        addClass(
                ".ImageDatasetReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference.class);
        addClass(
                ".MicroscopyImageReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MicroscopyImageReference.class);
        addClass(
                ".PlateImageReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference.class);
    }

    @Override
    public List<Class<?>> getClasses(String classValue)
    {
        List<Class<?>> classes =
                GenericJsonClassValueToClassObjectsMapping.getInstance().getClasses(classValue);

        if (classes != null && !classes.isEmpty())
        {
            return classes;
        } else
        {
            return super.getClasses(classValue);
        }
    }

    public static ScreeningJsonClassValueToClassObjectsMapping getInstance()
    {
        synchronized (ScreeningJsonClassValueToClassObjectsMapping.class)
        {
            if (instance == null)
            {
                instance = new ScreeningJsonClassValueToClassObjectsMapping();
            }
            return instance;
        }
    }

}
