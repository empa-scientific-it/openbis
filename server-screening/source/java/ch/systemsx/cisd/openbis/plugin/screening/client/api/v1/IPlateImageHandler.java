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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;

/**
 * Interface of classes handling plate images as byte arrays. The bytes are either in the original format of the image or PNG-encoded, depending on
 * the parameters given to the method that uses the handler.
 * 
 * @author Franz-Josef Elmer
 */
public interface IPlateImageHandler
{
    /**
     * Handles specified image file bytes for specified plate image reference.
     */
    public void handlePlateImage(PlateImageReference plateImageReference, byte[] imageFileBytes);
}
