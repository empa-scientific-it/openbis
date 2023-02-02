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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.image;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.reveregroup.gwt.imagepreloader.FitImage;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateStyleSetter;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.ImageDatasetChannel;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageChannelsReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.IntensityRange;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.ImageServletUrlParameters;

/**
 * @author pkupczyk
 */
public class Image extends LayoutContainer
{

    private ImageInitializer initializer;

    public Image(final ImageInitializer initializer)
    {
        this.initializer = initializer;

        // do not specify width to get correct aspect ratio of the original image
        PlateStyleSetter.setPointerCursor(this);
        setHeight(String.valueOf(getInitializer().getImageHeight()));
        add(createImage());
    }

    protected URLMethodWithParameters createUrl()
    {
        final URLMethodWithParameters url =
                createBasicImageURL(getInitializer().getSessionId(), getInitializer()
                        .getChannelReferences());

        if (getInitializer().getChannelReferences().tryGetImageTransformationCode() != null)
        {
            if (ScreeningConstants.USER_DEFINED_RESCALING_CODE.equalsIgnoreCase(getInitializer()
                    .getChannelReferences().tryGetImageTransformationCode()))
            {
                Map<String, IntensityRange> rangesOrNull =
                        getInitializer().getChannelReferences().tryGetIntensityRanges();
                if (rangesOrNull != null)
                {
                    for (Map.Entry<String, IntensityRange> range : rangesOrNull.entrySet())
                    {
                        String suffix = "";
                        if (range.getValue() != null)
                        {
                            suffix =
                                    "(" + range.getValue().getBlackPoint() + ","
                                            + range.getValue().getWhitePoint() + ")";
                        }

                        if (rangesOrNull.size() == 1)
                        {
                            url.addParameter(
                                    ImageServletUrlParameters.SINGLE_CHANNEL_TRANSFORMATION_CODE_PARAM,
                                    getInitializer().getChannelReferences()
                                            .tryGetImageTransformationCode() + suffix);
                        } else
                        {
                            url.addParameter(
                                    ImageServletUrlParameters.SINGLE_CHANNEL_TRANSFORMATION_CODE_PARAM
                                            + range.getKey(), getInitializer()
                                            .getChannelReferences().tryGetImageTransformationCode()
                                            + suffix);
                        }

                    }
                }
            } else
            {
                url.addParameter(
                        ImageServletUrlParameters.SINGLE_CHANNEL_TRANSFORMATION_CODE_PARAM,
                        getInitializer().getChannelReferences().tryGetImageTransformationCode());
            }
        }

        addImageTransformerSignature(url, getInitializer().getChannelReferences());
        return url;
    }

    protected URLMethodWithParameters createThumbnailImageUrl()
    {
        URLMethodWithParameters url = createUrl();
        addThumbnailSize(url, getInitializer().getImageWidth(), getInitializer().getImageHeight());
        return url;
    }

    protected URLMethodWithParameters createOriginalImageUrl()
    {
        return createUrl();
    }

    protected FitImage createImage()
    {
        final FitImage image = new FitImage();
        image.setFixedHeight(getInitializer().getImageHeight());

        if (getInitializer().getImageLoadHandler() != null)
        {
            image.addFitImageLoadHandler(getInitializer().getImageLoadHandler());
        }

        final String originalImageUrl = createOriginalImageUrl().toString();
        final String thumbnailImageUrl = createThumbnailImageUrl().toString();

        image.addClickHandler(new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    if (getInitializer().getImageClickHandler() != null)
                    {
                        getInitializer().getImageClickHandler().onClick(
                                getInitializer().getChannelReferences(),
                                getInitializer().getImageRow(), getInitializer().getImageColumn());
                    } else
                    {
                        Window.open(originalImageUrl, "_blank", "");
                    }
                }
            });

        // Set the url at the very end. This method triggers the image loading process
        // therefore it should be done after all image load handlers have been set.
        // Setting the url before defining the load handlers may result in handlers not
        // being notified about the finished loading (when loading is finished before
        // handlers are added). Moreover we are deferring setting the url to make sure that
        // all actions that attach the image to DOM finish first. Otherwise again images
        // are not displayed.
        GWTUtils.executeDelayed(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    image.setUrl(thumbnailImageUrl);
                }
            });

        return image;
    }

    private static void addThumbnailSize(URLMethodWithParameters url, int width, int height)
    {
        url.addParameter("mode", "thumbnail" + width + "x" + height);
    }

    private static void addImageTransformerSignature(URLMethodWithParameters url,
            LogicalImageChannelsReference channelReferences)
    {
        LogicalImageReference images = channelReferences.getBasicImage();

        List<String> channels = channelReferences.getChannelCodes();
        if (channels != null)
        {
            for (String channel : channels)
            {
                String channelOrNull = channel;
                if (channelOrNull.equalsIgnoreCase(ScreeningConstants.MERGED_CHANNELS))
                {
                    channelOrNull = null;
                }
                String signature =
                        images.tryGetTransformerFactorySignature(channelOrNull,
                                channelReferences.tryGetImageTransformationCode());
                if (signature != null)
                {
                    url.addParameter("transformerFactorySignature", signature);
                }

            }
        }
    }

    private static URLMethodWithParameters createBasicImageURL(String sessionID,
            LogicalImageChannelsReference channelReferences)
    {
        LogicalImageReference images = channelReferences.getBasicImage();
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(images.getDatastoreHostUrl() + "/"
                        + ScreeningConstants.DATASTORE_SCREENING_SERVLET_URL);
        methodWithParameters.addParameter("sessionID", sessionID);
        methodWithParameters.addParameter(ImageServletUrlParameters.DATASET_CODE_PARAM,
                images.getDatasetCode());

        List<String> channels = channelReferences.getChannelCodes();
        if (channels.contains(ScreeningConstants.MERGED_CHANNELS))
        {
            methodWithParameters.addParameter(ImageServletUrlParameters.MERGE_CHANNELS_PARAM,
                    "true");
        } else
        {
            for (String channel : channelReferences.getChannelCodes())
            {
                methodWithParameters.addParameter(ImageServletUrlParameters.CHANNEL_PARAM, channel);
            }
        }

        addOverlayParameters(channelReferences.getOverlayChannels(), methodWithParameters);
        return methodWithParameters;
    }

    private static void addOverlayParameters(Set<ImageDatasetChannel> overlayChannels,
            URLMethodWithParameters methodWithParameters)
    {
        for (ImageDatasetChannel overlayChannel : overlayChannels)
        {
            String paramName =
                    ImageServletUrlParameters.OVERLAY_CHANNEL_PREFIX_PARAM
                            + overlayChannel.getDatasetCode();
            methodWithParameters.addParameter(paramName, overlayChannel.getChannelCode());
        }
    }

    protected ImageInitializer getInitializer()
    {
        return initializer;
    }

}
