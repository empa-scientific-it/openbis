/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Query;

import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SamplePropertyAccessValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetOverlayImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IImageDatasetLoader;

/**
 * @author Tomasz Pylak
 */
public class LogicalImageLoader
{
    /**
     * Loads information about the logical image in the chosen image dataset (restricted to one well in HCS case).
     */
    public static LogicalImageInfo loadLogicalImageInfo(Session session,
            org.hibernate.Session hibernateSession,
            IScreeningBusinessObjectFactory businessObjectFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory, String datasetCode,
            String datastoreCode, WellLocation wellLocationOrNull)
    {
        LogicalImageInfo logicalImageInfo =
                new LogicalImageLoader(session, hibernateSession, businessObjectFactory,
                        managedPropertyEvaluatorFactory).tryLoadLogicalImageInfo(datasetCode,
                        datastoreCode, wellLocationOrNull);
        if (logicalImageInfo == null)
        {
            throw new IllegalStateException(String.format("Dataset '%s' is not an image dataset.",
                    datasetCode));
        }
        return logicalImageInfo;
    }

    public static ImageDatasetEnrichedReference getImageDatasetReference(Session session,
            org.hibernate.Session hibernateSession,
            IScreeningBusinessObjectFactory businessObjectFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory, String datasetCode,
            String datastoreCode)
    {
        return new LogicalImageLoader(session, hibernateSession, businessObjectFactory,
                managedPropertyEvaluatorFactory).getImageDatasetReference(datasetCode,
                datastoreCode);
    }

    private final Session session;

    private final org.hibernate.Session hibernateSession;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private final IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    public LogicalImageLoader(Session session,
            org.hibernate.Session hibernateSession, IScreeningBusinessObjectFactory businessObjectFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this.session = session;
        this.hibernateSession = hibernateSession;
        this.businessObjectFactory = businessObjectFactory;
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    private ImageDatasetEnrichedReference getImageDatasetReference(String datasetCode,
            String datastoreCode)
    {
        IImageDatasetLoader datasetLoader =
                businessObjectFactory.tryCreateImageDatasetLoader(datasetCode, datastoreCode);
        if (datasetLoader == null)
        {
            throw new IllegalStateException(String.format("Dataset '%s' not an image dataset.",
                    datasetCode));
        }
        return getImageDataset(datasetCode, datasetLoader);
    }

    LogicalImageInfo tryLoadLogicalImageInfo(String datasetCode, String datastoreCode,
            WellLocation wellLocationOrNull)
    {
        IImageDatasetLoader datasetLoader =
                businessObjectFactory.tryCreateImageDatasetLoader(datasetCode, datastoreCode);
        if (datasetLoader == null)
        {
            return null;
        }
        List<ImageChannelStack> stacks = datasetLoader.listImageChannelStacks(wellLocationOrNull);
        ImageDatasetEnrichedReference imageDataset = getImageDataset(datasetCode, datasetLoader);
        return new LogicalImageInfo(imageDataset, stacks);
    }

    private ImageDatasetEnrichedReference getImageDataset(String datasetCode,
            IImageDatasetLoader datasetLoader)
    {
        ImageDatasetParameters imageParameters = datasetLoader.getImageParameters();

        DataPE dataset = loadDatasetWithChildren(datasetCode);
        DatasetImagesReference datasetImagesReference =
                createDatasetImagesReference(translate(dataset), imageParameters);
        List<DatasetOverlayImagesReference> overlayDatasets = extractImageOverlays(dataset);
        return new ImageDatasetEnrichedReference(datasetImagesReference, overlayDatasets);
    }

    List<ImageDatasetEnrichedReference> loadImageDatasets(List<DataPE> datasets)
    {
        List<ImageDatasetEnrichedReference> refs = new ArrayList<ImageDatasetEnrichedReference>();
        List<DataPE> imageDatasets = ScreeningUtils.filterImageDatasets(datasets);
        for (DataPE imageDataset : imageDatasets)
        {
            DatasetImagesReference ref = tryLoadImageDatasetReference(imageDataset);
            if (ref != null)
            {
                List<DatasetOverlayImagesReference> overlays = extractImageOverlays(imageDataset);
                ImageDatasetEnrichedReference enrichedRef =
                        new ImageDatasetEnrichedReference(ref, overlays);
                refs.add(enrichedRef);
            }
        }
        return refs;
    }

    private List<DatasetOverlayImagesReference> extractImageOverlays(DataPE imageDataset)
    {
        List<AbstractExternalData> overlayDatasets = fetchOverlayDatasets(imageDataset);

        List<DatasetOverlayImagesReference> overlays =
                new ArrayList<DatasetOverlayImagesReference>();
        for (AbstractExternalData overlay : overlayDatasets)
        {
            DatasetOverlayImagesReference ref = tryLoadOverlayDatasetReference(overlay);
            if (ref != null)
            {
                overlays.add(ref);
            }
        }
        return overlays;
    }

    private List<AbstractExternalData> fetchOverlayDatasets(DataPE imageDataset)
    {
        Query query = hibernateSession.createQuery(
                "SELECT rel.childDataSet from DataSetRelationshipPE rel WHERE rel.parentDataSet = :parent")
                .setParameter("parent", imageDataset);
        @SuppressWarnings("unchecked")
        List<DataPE> children = query.list();
        List<DataPE> overlayPEs =
                ScreeningUtils.filterImageOverlayDatasets(children);
        Collection<Long> datasetIds = extractIds(overlayPEs);
        return businessObjectFactory.createDatasetLister(session).listByDatasetIds(datasetIds);
    }

    private DatasetOverlayImagesReference tryLoadOverlayDatasetReference(AbstractExternalData overlay)
    {
        DatasetImagesReference imageDatasetReference = tryLoadImageDatasetReference(overlay);
        if (imageDatasetReference == null)
        {
            return null;
        }
        String analysisProcedure = tryGetAnalysisProcedure(overlay);
        return DatasetOverlayImagesReference.create(imageDatasetReference.getDatasetReference(),
                imageDatasetReference.getImageParameters(), analysisProcedure);
    }

    private String tryGetAnalysisProcedure(AbstractExternalData dataset)
    {
        return EntityHelper.tryFindPropertyValue(dataset, ScreeningConstants.ANALYSIS_PROCEDURE);
    }

    private DatasetImagesReference tryLoadImageDatasetReference(AbstractExternalData dataset)
    {
        ImageDatasetParameters imageParameters =
                ScreeningUtils.tryLoadImageParameters(dataset, businessObjectFactory);
        if (imageParameters == null)
        {
            return null;
        }
        return createDatasetImagesReference(dataset, imageParameters);
    }

    private DatasetImagesReference createDatasetImagesReference(AbstractExternalData dataset,
            ImageDatasetParameters imageParameters)
    {
        return DatasetImagesReference.create(ScreeningUtils.createDatasetReference(dataset),
                imageParameters);
    }

    DatasetImagesReference tryLoadImageDatasetReference(DataPE imageDataset)
    {
        return tryLoadImageDatasetReference(translate(imageDataset));
    }

    private DataPE loadDatasetWithChildren(String datasetPermId)
    {
        IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.loadByCode(datasetPermId);
        dataBO.enrichWithChildren();
        DataPE dataSet = dataBO.getData();
        return dataSet;
    }

    private static Collection<Long> extractIds(List<DataPE> datasets)
    {
        List<Long> ids = new ArrayList<Long>();
        for (DataPE dataset : datasets)
        {
            ids.add(HibernateUtils.getId(dataset));
        }
        return ids;
    }

    private AbstractExternalData translate(DataPE dataSet)
    {
        HibernateUtils.initialize(dataSet.getProperties());
        return DataSetTranslator.translate(dataSet, session.getBaseIndexURL(), false, null,
                managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, businessObjectFactory.getDAOFactory()));
    }
}
