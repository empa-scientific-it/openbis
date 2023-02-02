/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.shared;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import javax.sql.DataSource;

import loci.formats.gui.Index16ColorModel;
import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.image.IntensityRescaling.IImageToPixelsConverter;
import ch.systemsx.cisd.common.image.IntensityRescaling.Pixels;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingTransformerDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.TransformerFactoryMapper;

/**
 * Utility methods for DSS.
 * 
 * @author Franz-Josef Elmer
 */
public class DssScreeningUtils
{
    public static final IImageToPixelsConverter CONVERTER = new IImageToPixelsConverter()
        {
            @Override
            public Pixels convert(BufferedImage image)
            {
                return createPixels(image);
            }
        };

    private static IImagingReadonlyQueryDAO query;

    static
    {
        QueryTool.getTypeMap().put(IImageTransformerFactory.class, new TransformerFactoryMapper());
    }

    /**
     * Returned query is reused and should NOT be closed.
     */
    public static IImagingReadonlyQueryDAO getQuery()
    {
        if (query == null)
        {
            query = createQuery();
        }
        return query;
    }

    /**
     * Creates a DAO based on imaging database specified in DSS service.properties by data source {@link ScreeningConstants#IMAGING_DATA_SOURCE}.
     * <p>
     * Returned query is reused and should not be closed.
     * </p>
     */
    private static IImagingReadonlyQueryDAO createQuery()
    {
        DataSource dataSource =
                ServiceProvider.getDataSourceProvider().getDataSource(
                        ScreeningConstants.IMAGING_DATA_SOURCE);
        return QueryTool.getQuery(dataSource, IImagingReadonlyQueryDAO.class);
    }

    /**
     * Creates a new query each time when it is called. Returned query should be closed after all operations are done.
     */
    public static IImagingTransformerDAO createImagingTransformerDAO()
    {
        DataSource dataSource =
                ServiceProvider.getDataSourceProvider().getDataSource(
                        ScreeningConstants.IMAGING_DATA_SOURCE);
        return QueryTool.getQuery(dataSource, IImagingTransformerDAO.class);
    }

    /**
     * Creates {@link Pixels} wrapper object for the specified image which can also handle 16bit index color models. Such color models are used when
     * reading Nikon microscopy image files with ND2Reader of BioFormats.
     */
    public static Pixels createPixels(BufferedImage image)
    {
        return new Pixels(image)
            {
                @Override
                protected int[][] tryCreateColorIndexMap(ColorModel colorModel)
                {
                    int[][] indexMap = super.tryCreateColorIndexMap(colorModel);
                    if (indexMap == null && colorModel instanceof Index16ColorModel)
                    {
                        Index16ColorModel indexColorModel = (Index16ColorModel) colorModel;
                        indexMap = new int[3][1 << 16];
                        copyTo(indexColorModel.getReds(), indexMap[0]);
                        copyTo(indexColorModel.getGreens(), indexMap[1]);
                        copyTo(indexColorModel.getBlues(), indexMap[2]);
                    }
                    return indexMap;
                }

                private void copyTo(short[] shorts, int[] integers)
                {
                    for (int i = 0; i < shorts.length; i++)
                    {
                        integers[i] = shorts[i] & 0xffff;
                    }
                }
            };
    }
}
