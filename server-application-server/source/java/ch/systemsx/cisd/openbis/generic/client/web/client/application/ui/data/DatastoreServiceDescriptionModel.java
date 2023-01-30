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