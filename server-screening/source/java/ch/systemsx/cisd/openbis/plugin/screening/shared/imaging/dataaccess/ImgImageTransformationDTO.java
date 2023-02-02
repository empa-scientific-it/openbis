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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.ResultColumn;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;

/**
 * @author Tomasz Pylak
 */
public class ImgImageTransformationDTO extends AbstractImageTransformerFactoryHolder
{
    @ResultColumn("CODE")
    private String code;

    @ResultColumn("LABEL")
    private String label;

    @ResultColumn("DESCRIPTION")
    private String descriptionOrNull;

    @ResultColumn("IS_DEFAULT")
    private boolean isDefault;

    @ResultColumn("IS_EDITABLE")
    private boolean isEditable;

    @ResultColumn("CHANNEL_ID")
    private long channelId;

    // EODSQL only
    @SuppressWarnings("unused")
    private ImgImageTransformationDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public ImgImageTransformationDTO(String code, String label, String descriptionOrNull,
            boolean isDefault, long channelId, IImageTransformerFactory imageTransformationFactory,
            boolean isEditable)
    {
        assert code != null : "code is null";
        assert label != null : "label is null";
        assert imageTransformationFactory != null : "transformationFactory is null";

        this.code = CodeNormalizer.normalize(code);
        this.label = label;
        this.descriptionOrNull = descriptionOrNull;
        this.isDefault = isDefault;
        this.channelId = channelId;
        setImageTransformerFactory(imageTransformationFactory);
        this.isEditable = isEditable;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    /** Can be null. */
    public String getDescription()
    {
        return descriptionOrNull;
    }

    public void setDescription(String descriptionOrNull)
    {
        this.descriptionOrNull = descriptionOrNull;
    }

    public boolean getIsDefault()
    {
        return isDefault;
    }

    public void setDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }

    public long getChannelId()
    {
        return channelId;
    }

    public void setChannelId(long channelId)
    {
        this.channelId = channelId;
    }

    public final IImageTransformerFactory getImageTransformerFactory()
    {
        IImageTransformerFactory factory = super.tryGetImageTransformerFactory();
        assert factory != null : "image factory is null";
        return factory;
    }

    public boolean getIsEditable()
    {
        return isEditable;
    }

    public void setEditable(boolean isEditable)
    {
        this.isEditable = isEditable;
    }

}
