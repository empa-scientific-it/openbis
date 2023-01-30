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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Piotr Buczek
 */
public enum ViewMode implements IsSerializable
{
    SIMPLE,

    /**
     * Embedded mode inherits most of the SIMPLE mode behaviors. But it has less widgets (e.g. top toolbar and footer are invisible, grids have only
     * "Export" button) to allow embedding the application on other web sites.
     */
    EMBEDDED,

    /**
     * Grid mode is similar to embedded, but it has full-featured grid functionality.
     */
    GRID,

    NORMAL;
}
