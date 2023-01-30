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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * Utility class to be used both on client and server side for permlink management.
 * 
 * @author Piotr Buczek
 */
public class PermlinkUtilities
{

    /** The HTTP URL parameter used to specify the entity identifier. */
    public static final String PERM_ID_PARAMETER_KEY = "permId";

    /** The HTTP URL parameter used to specify the entity kind. */
    public static final String ENTITY_KIND_PARAMETER_KEY = "entity";

    /** The optional HTTP URL parameter used to specify the subtab that should be opened. */
    public static final String SUBTAB_PARAMETER_KEY = "ui-subtab";

    public final static String createPermlinkURL(final String baseIndexURL,
            final EntityKind entityKind, final String permId)
    {
        URLMethodWithParameters ulrWithParameters = new URLMethodWithParameters(baseIndexURL);
        ulrWithParameters.addParameter(BasicConstant.VIEW_MODE_KEY, ViewMode.SIMPLE.name());
        ulrWithParameters.startHistoryToken();
        ulrWithParameters.addParameter(ENTITY_KIND_PARAMETER_KEY, entityKind.name());
        ulrWithParameters.addParameter(PERM_ID_PARAMETER_KEY, permId);
        return ulrWithParameters.toString();
    }

    public static final String DOWNLOAD_ATTACHMENT_ACTION = "DOWNLOAD_ATTACHMENT";

    public static final String FILE_NAME_KEY = "file";

    public static final String VERSION_KEY = "version";

    private final static URLMethodWithParameters createAttachmentParameters(
            final String baseIndexURL, final String fileName, final Integer version)
    {
        URLMethodWithParameters ulrWithParameters = new URLMethodWithParameters(baseIndexURL);
        ulrWithParameters.addParameter(BasicConstant.VIEW_MODE_KEY, ViewMode.SIMPLE.name());
        ulrWithParameters.startHistoryToken();
        ulrWithParameters.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER,
                DOWNLOAD_ATTACHMENT_ACTION);
        ulrWithParameters.addParameter(FILE_NAME_KEY, fileName);
        if (version != null)
        {
            ulrWithParameters.addParameter(VERSION_KEY, version);
        }
        return ulrWithParameters;
    }

    public final static String createAttachmentPermlinkURL(final String baseIndexURL,
            final String fileName, final Integer version, final AttachmentHolderKind entityKind,
            final String permId)
    {
        URLMethodWithParameters ulrWithParameters =
                createAttachmentParameters(baseIndexURL, fileName, version);
        ulrWithParameters.addParameter(ENTITY_KIND_PARAMETER_KEY, entityKind.name());
        ulrWithParameters.addParameter(PERM_ID_PARAMETER_KEY, permId);
        return ulrWithParameters.toString();
    }

    public final static String createProjectAttachmentPermlinkURL(final String baseIndexURL,
            final String fileName, final Integer version, final String projectCode,
            final String space)
    {
        URLMethodWithParameters ulrWithParameters =
                createAttachmentParameters(baseIndexURL, fileName, version);
        ulrWithParameters.addParameter(ENTITY_KIND_PARAMETER_KEY, AttachmentHolderKind.PROJECT);
        ulrWithParameters.addParameter(PermlinkUtilities.CODE_PARAMETER_KEY, projectCode);
        ulrWithParameters.addParameter(PermlinkUtilities.SPACE_PARAMETER_KEY, space);
        return ulrWithParameters.toString();
    }

    public final static String createProjectPermlinkURL(final String baseIndexURL,
            final String projectCode, final String spaceCode)
    {
        URLMethodWithParameters ulrWithParameters = new URLMethodWithParameters(baseIndexURL);
        ulrWithParameters.addParameter(BasicConstant.VIEW_MODE_KEY, ViewMode.SIMPLE.name());
        ulrWithParameters.startHistoryToken();
        ulrWithParameters.addParameter(ENTITY_KIND_PARAMETER_KEY, PermlinkUtilities.PROJECT);
        ulrWithParameters.addParameter(PermlinkUtilities.CODE_PARAMETER_KEY, projectCode);
        ulrWithParameters.addParameter(PermlinkUtilities.SPACE_PARAMETER_KEY, spaceCode);
        return ulrWithParameters.toString();
    }

    public final static String PROJECT = "PROJECT";

    public final static String CODE_PARAMETER_KEY = "code";

    public final static String SPACE_PARAMETER_KEY = "space";

    public final static String TYPE_PARAMETER_KEY = "type";

    public static final String PERMLINK_ACTION = "VIEW";

    public final static String METAPROJECT = "METAPROJECT";

    public final static String NAME_PARAMETER_KEY = "name";

    public final static String BROWSE_ACTION = "BROWSE";

}
