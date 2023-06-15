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
package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

/**
 * A static class which holds the database version and the version of its indices.
 *
 * @author Christian Ribeaud
 */
public final class DatabaseVersionHolder
{
    /**
     * Current version of the database.
     */
    private static final String DATABASE_VERSION = "193";

    /** Current version of the database INDICES. */
    private static final String DATABASE_FULL_TEXT_SEARCH_DOCUMENT_VERSION = "002";

    private static final String RELEASE_PATCHES_VERSION = "000";

    private DatabaseVersionHolder()
    {
        // Can not be instantiated
    }

    /**
     * Returns the current version of the database.
     */
    public static String getDatabaseVersion()
    {
        return DATABASE_VERSION;
    }

    /**
     * Returns the current version of the database indices.
     */
    public static String getDatabaseFullTextSearchDocumentVersion()
    {
        return DATABASE_FULL_TEXT_SEARCH_DOCUMENT_VERSION;
    }

    /**
     * Returns the current version of the database.
     */
    public static String getReleasePatchesVersion()
    {
        return RELEASE_PATCHES_VERSION;
    }

}
