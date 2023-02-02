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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.TypeMapper;
import net.lemnik.eodsql.Update;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;

/**
 * Write operations on {@link IImageTransformerFactory} + all inherited read operations.
 * 
 * @author Franz-Josef Elmer
 */
public interface IImagingTransformerDAO extends TransactionQuery, IImagingReadonlyQueryDAO
{
    @Update(sql = "update experiments set image_transformer_factory = ?{2} where id = ?{1}", parameterBindings =
    { TransformerFactoryMapper.class, TypeMapper.class /* default */})
    public void saveTransformerFactoryForExperiment(long experimentId,
            IImageTransformerFactory factory);

    @Update(sql = "update image_data_sets set image_transformer_factory = ?{2} where id = ?{1}", parameterBindings =
    { TransformerFactoryMapper.class, TypeMapper.class /* default */})
    public void saveTransformerFactoryForImageDataset(long datasetId,
            IImageTransformerFactory factory);

    @Update(sql = "update acquired_images set image_transformer_factory = ?{2} where id = ?{1}", parameterBindings =
    { TransformerFactoryMapper.class, TypeMapper.class /* default */})
    public void saveTransformerFactoryForImage(long acquiredImageId,
            IImageTransformerFactory factory);

    @Update(sql = "insert into IMAGE_TRANSFORMATIONS(CODE, LABEL, DESCRIPTION, IS_DEFAULT, IMAGE_TRANSFORMER_FACTORY, IS_EDITABLE, CHANNEL_ID) values "
            + "(?{1.code}, ?{1.label}, ?{1.description}, ?{1.isDefault}, ?{1.serializedImageTransformerFactory}, ?{1.isEditable}, ?{1.channelId})")
    public void addImageTransformation(ImgImageTransformationDTO imageTransformation);

    @Update(sql = "update IMAGE_TRANSFORMATIONS set IMAGE_TRANSFORMER_FACTORY = ?{2} where id = ?{1}", parameterBindings =
    { TransformerFactoryMapper.class, TypeMapper.class /* default */})
    public void updateImageTransformerFactory(long imageTransformationId,
            IImageTransformerFactory factory);

    @Update(sql = "delete from IMAGE_TRANSFORMATIONS where id = ?{1}", parameterBindings =
    { TransformerFactoryMapper.class })
    public void removeImageTransformation(long imageTransformationId);
}
