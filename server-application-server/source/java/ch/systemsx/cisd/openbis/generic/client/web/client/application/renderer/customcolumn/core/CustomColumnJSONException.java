/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.customcolumn.core;

import java.io.IOException;

/**
 * @author pkupczyk
 */
public class CustomColumnJSONException extends Exception
{

    private static final long serialVersionUID = 1L;

    public CustomColumnJSONException(String msg)
    {
        super(msg);
    }

    public CustomColumnJSONException(String msg, IOException e)
    {
        super(msg, e);
    }

}
