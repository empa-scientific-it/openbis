/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client;

/**
 * A interface for getting the resources necessary for the help system. Online help is broken into two sections: Generic -- help for OpenBIS in
 * general, provided by CISD; and Specific -- help for the specific installation of OpenBIS, provided by the host of the installation.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IOnlineHelpResourceLocatorService
{
    /**
     * The generic root URL is the root (perhaps the index) of the generic online help system.
     */
    String getOnlineHelpGenericRootURL();

    /**
     * The generic page template is a URL that encodes how to reference the help pages. It takes one argument, title -- the title of the page, and it
     * should be constructed to automatically create the page if no page exists. Example:
     * https://wiki-bsse.ethz.ch/pages/createpage.action?spaceKey=CISDDoc&title=${title }&linkCreation=true&fromPageId=40633829
     */
    String getOnlineHelpGenericPageTemplate();

    /**
     * The specific root URL is the root (perhaps the index) of the specific online help system.
     */
    String getOnlineHelpSpecificRootURL();

    /**
     * The specific page template is a URL that encodes how to reference the help pages. It takes one argument, title -- the title of the page, and it
     * should be constructed to automatically create the page if no page exists. Example:
     * https://wiki-bsse.ethz.ch/pages/createpage.action?spaceKey=CISDDoc&title=${title }&linkCreation=true&fromPageId=40633829
     */
    String getOnlineHelpSpecificPageTemplate();
}
