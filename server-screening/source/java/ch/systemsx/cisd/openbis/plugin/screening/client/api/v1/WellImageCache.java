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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import ch.systemsx.cisd.base.annotation.JsonObject;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * A cache for images on a per-well base. The general assumption is that all images for a well should be fetched in one go from the server, but the
 * client using the API may not be able to do so due to a limited programming model. Thus this class caches the data and synchronizes filling of the
 * cache by different threads in a thread-safe way.
 * 
 * @author Bernd Rinn
 */
final class WellImageCache
{
    @JsonObject("InternalPlateWellImageReference")
    private static final class InternalPlateWellImageReference extends DatasetIdentifier
    {
        private static final long serialVersionUID = 1L;

        private final WellPosition wellPosition;

        private final ImageSize size;

        private InternalPlateWellImageReference(WellPosition wellPosition, ImageSize size,
                IDatasetIdentifier dataset)
        {
            super(dataset.getDatasetCode(), dataset.getDatastoreServerUrl());
            this.wellPosition = wellPosition;
            this.size = size;
        }

        static InternalPlateWellImageReference fromPlateImageReference(PlateImageReference ref,
                ImageSize size)
        {
            return new InternalPlateWellImageReference(ref.getWellPosition(), size, ref);
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + super.hashCode();
            result = prime * result + wellPosition.hashCode();
            result = prime * result + size.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            if (super.equals(obj) == false)
            {
                return false;
            }

            final InternalPlateWellImageReference other = (InternalPlateWellImageReference) obj;
            return wellPosition.equals(other.wellPosition) && size.equals(other.size);
        }
    }

    private static final class InternalTileChannelReference
    {
        private final int tile;

        private final String channel;

        private InternalTileChannelReference(int tile, String channel)
        {
            this.tile = tile;
            this.channel = channel;
        }

        static InternalTileChannelReference fromPlateImageReference(PlateImageReference ref)
        {
            return new InternalTileChannelReference(ref.getTile(), ref.getChannel());
        }

        @Override
        public String toString()
        {
            return "InternalTileChannelReference [tile=" + tile + ", channel=" + channel + "]";
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((channel == null) ? 0 : channel.hashCode());
            result = prime * result + tile;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            InternalTileChannelReference other = (InternalTileChannelReference) obj;
            if (channel == null)
            {
                if (other.channel != null)
                    return false;
            } else if (!channel.equals(other.channel))
                return false;
            if (tile != other.tile)
                return false;
            return true;
        }
    }

    /**
     * A cache for one image. It may be loaded asynchronously in another thread and thus the call to {@link #getImageData} may block until the data
     * have been loaded.
     */
    static final class CachedImage
    {
        private final CountDownLatch ready = new CountDownLatch(1);

        private byte[] imageData;

        private IOException ioe;

        private RuntimeException rex;

        void set(byte[] imageData)
        {
            this.imageData = imageData;
            ready.countDown();
        }

        byte[] getImageData() throws IOException
        {
            try
            {
                ready.await();
                if (ioe != null)
                {
                    throw ioe;
                }
                if (rex != null)
                {
                    throw rex;
                }
                return imageData;
            } catch (InterruptedException ex)
            {
                throw new RuntimeException("Image fetching interrupted.");
            }
        }

        void release(IOException ex)
        {
            this.ioe = ex;
            ready.countDown();
        }

        void release(RuntimeException ex)
        {
            if (ready.getCount() > 0)
            {
                this.rex = ex;
                ready.countDown();
            }
        }
    }

    static final class WellImages
    {
        private final Map<InternalTileChannelReference, CachedImage> imageMap;

        private final boolean loaderCall;

        WellImages(Map<InternalTileChannelReference, CachedImage> imageMap, boolean loaderCall)
        {
            this.imageMap = imageMap;
            this.loaderCall = loaderCall;
        }

        void putImage(PlateImageReference ref, byte[] imageData)
        {
            final CachedImage image =
                    imageMap.get(InternalTileChannelReference.fromPlateImageReference(ref));
            image.set(imageData);
        }

        CachedImage getImage(PlateImageReference ref)
        {
            return imageMap.get(InternalTileChannelReference.fromPlateImageReference(ref));
        }

        boolean isLoaderCall()
        {
            return loaderCall;
        }

        void cancel(IOException ex)
        {
            for (CachedImage image : imageMap.values())
            {
                image.release(ex);
            }
        }

        void cancel(RuntimeException ex)
        {
            for (CachedImage image : imageMap.values())
            {
                image.release(ex);
            }
        }

    }

    private final Map<InternalPlateWellImageReference, SoftReference<Map<InternalTileChannelReference, CachedImage>>> cacheMap =
            new HashMap<InternalPlateWellImageReference, SoftReference<Map<InternalTileChannelReference, CachedImage>>>();

    synchronized WellImages getWellImages(PlateImageReference imageRef, ImageSize size,
            ImageDatasetMetadata imageMetadata)
    {
        final InternalPlateWellImageReference plateWellRef =
                InternalPlateWellImageReference.fromPlateImageReference(imageRef, size);
        final SoftReference<Map<InternalTileChannelReference, CachedImage>> cachedImageRefOrNull =
                cacheMap.get(plateWellRef);
        final Map<InternalTileChannelReference, CachedImage> cachedImageOrNull =
                (cachedImageRefOrNull) == null ? null : cachedImageRefOrNull.get();
        if (cachedImageOrNull == null)
        {
            final Map<InternalTileChannelReference, CachedImage> emptyImages =
                    new HashMap<InternalTileChannelReference, CachedImage>();
            for (int tile = 0; tile < imageMetadata.getNumberOfTiles(); ++tile)
            {
                for (String channel : imageMetadata.getChannelCodes())
                {
                    emptyImages.put(new InternalTileChannelReference(tile, channel),
                            new CachedImage());
                }
            }
            cacheMap.put(plateWellRef,
                    new SoftReference<Map<InternalTileChannelReference, CachedImage>>(emptyImages));
            return new WellImages(emptyImages, true);
        } else
        {
            return new WellImages(cachedImageOrNull, false);
        }
    }

    void clear()
    {
        cacheMap.clear();
    }

}