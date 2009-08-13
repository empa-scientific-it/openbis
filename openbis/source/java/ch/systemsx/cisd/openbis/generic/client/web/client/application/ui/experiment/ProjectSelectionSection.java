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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import com.extjs.gxt.ui.client.Style.Scroll;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.SectionPanel;

/**
 * {@link SectionPanel} containing {@link ProjectSelectionTreeWidget}.
 * 
 * @author Piotr Buczek
 */
public class ProjectSelectionSection extends SectionPanel
{
    public ProjectSelectionSection(final ProjectSelectionTreeWidget tree)
    {
        super("Project Selection");
        setScrollMode(Scroll.AUTOY);
        add(tree);
    }
}