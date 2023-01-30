/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * A generic {@link ClientBundle} implementation.
 * <p>
 * Note that this approach does not render correctly animated <i>gif</i> (no animation!).
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericImageBundle extends ClientBundle
{
    /**
     * Returns CISD logo.
     */
    @Source("cisd.jpg")
    public ImageResource getCISDLogo();

    /**
     * Returns openBIS logo.
     */
    @Source("openBIS_logo_229x100.png")
    public ImageResource getOpenBISLogo();

    /**
     * Returns info icon.
     */
    @Source("info.png")
    public ImageResource getInfoIcon();

    /**
     * Returns home icon.
     */
    @Source("home.png")
    public ImageResource getHomeIcon();

    @Source("editable.gif")
    public ImageResource getEditableIcon();

    @Source("confirm.gif")
    public ImageResource getConfirmIcon();

    @Source("cancel.gif")
    public ImageResource getCancelIcon();

    @Source("trash.gif")
    public ImageResource getTrashIcon();

}
