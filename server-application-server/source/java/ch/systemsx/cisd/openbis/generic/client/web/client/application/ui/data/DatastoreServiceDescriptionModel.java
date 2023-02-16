/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModelData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * {@link ModelData} for {@link DatastoreServiceDescription}.
 * 
 * @author Piotr Buczek
 */
public class DatastoreServiceDescriptionModel extends SimplifiedBaseModelData
{

    private static final long serialVersionUID = 1L;

    public static DatastoreServiceDescriptionModel createFakeReportingServiceModel(String label)
    {
        final DatastoreServiceDescription service =
                DatastoreServiceDescription.reporting(label, label, null, null, null);
        return new DatastoreServiceDescriptionModel(service);
    }

    public DatastoreServiceDescriptionModel(final DatastoreServiceDescription description)
    {
        set(ModelDataPropertyNames.OBJECT, description);
        set(ModelDataPropertyNames.LABEL, description.getLabel());
        set(ModelDataPropertyNames.DESCRIPTION, description.getServiceKind().getDescription()
                + ": " + description.getLabel());
    }

    /**
     * @param datasetOrNull if not null only services that match the data set's type will be converted
     */
    public final static List<DatastoreServiceDescriptionModel> convert(
            final List<DatastoreServiceDescription> services, final AbstractExternalData datasetOrNull)
    {
        final List<DatastoreServiceDescriptionModel> result =
                new ArrayList<DatastoreServiceDescriptionModel>();
        for (final DatastoreServiceDescription service : services)
        {
            if (datasetOrNull == null
                    || DatastoreServiceDescription.isMatching(service, datasetOrNull))
            {
                result.add(new DatastoreServiceDescriptionModel(service));
            }
        }
        return result;
    }

    public final DatastoreServiceDescription getBaseObject()
    {
        return get(ModelDataPropertyNames.OBJECT);
    }

}