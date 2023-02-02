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

package ch.systemsx.cisd.openbis.dss.etl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.etlserver.plugins.DeleteFromExternalDBMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;

/**
 * @author Pawel Glyzewski
 */
public class DeleteFromImagingDBMaintenanceTask extends DeleteFromExternalDBMaintenanceTask
{
    private static Set<TechId> getContainers(Connection c, List<DeletedDataSet> deletedDataSets)
            throws SQLException
    {
        ResultSet result =
                c.createStatement()
                        .executeQuery(
                                String.format(
                                        "SELECT DISTINCT CONTAINER_DATASET_ID FROM IMAGE_ZOOM_LEVELS where PHYSICAL_DATASET_PERM_ID in (%s)",
                                        joinIds(deletedDataSets)));
        return getContainersSet(result);
    }

    private static Set<TechId> getContainersSet(ResultSet result) throws SQLException
    {
        Set<TechId> containers = new HashSet<TechId>();
        while (result.next())
        {
            long techId = result.getLong("CONTAINER_DATASET_ID");
            containers.add(new TechId(techId));
        }
        return containers;
    }

    private static Set<TechId> getRemainingZoomLevels(Connection c, Set<TechId> modifiedContaiers,
            boolean thumbnails) throws SQLException
    {
        ResultSet result =
                c.createStatement()
                        .executeQuery(
                                String.format(
                                        "SELECT CONTAINER_DATASET_ID FROM IMAGE_ZOOM_LEVELS where CONTAINER_DATASET_ID in (%s) AND IS_ORIGINAL = "
                                                + (thumbnails ? "FALSE" : "TRUE"),
                                        joinIds(modifiedContaiers)));

        return getContainersSet(result);
    }

    private static String joinIds(Set<TechId> codes)
    {
        StringBuilder sb = new StringBuilder();
        for (IIdHolder dds : codes)
        {
            if (sb.length() != 0)
            {
                sb.append(", ");
            }
            sb.append("'" + StringUtils.replace(dds.getId().toString(), "'", "''") + "'");
        }
        String ids = sb.toString();
        return ids;
    }

    private static Set<TechId> getMissingIds(Set<TechId> allIds, Set<TechId> selectedIds)
    {
        Set<TechId> missingIds = new HashSet<TechId>();
        for (TechId techId : allIds)
        {
            if (false == selectedIds.contains(techId))
            {
                missingIds.add(techId);
            }
        }

        return missingIds;
    }

    private static void clearAcquiredImages(Connection c, Set<TechId> ids, boolean isThumbnail)
            throws SQLException
    {
        final String joinedIds = joinIds(ids);

        final String statement = String.format(
                "UPDATE ACQUIRED_IMAGES SET %s = NULL "
                        + "  WHERE CHANNEL_STACK_ID IN (SELECT ID FROM CHANNEL_STACKS WHERE DS_ID IN (%s)) "
                        + "    OR CHANNEL_ID IN (SELECT ID FROM CHANNELS WHERE DS_ID IN (%s))",
                (isThumbnail ? "THUMBNAIL_ID" : "IMG_ID"), joinedIds, joinedIds);

        c.createStatement().execute(statement);
    }

    @Override
    protected void deleteDatasets(List<DeletedDataSet> deletedDataSets) throws SQLException
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "Synchronizing deletions of %d datasets with the database.",
                    deletedDataSets.size()));
        }

        Set<TechId> containers = getContainers(connection, deletedDataSets);
        if (containers.size() > 0)
        {
            connection.createStatement().execute(
                    String.format(
                            "DELETE FROM IMAGE_ZOOM_LEVELS WHERE PHYSICAL_DATASET_PERM_ID IN (%s)",
                            joinIds(deletedDataSets)));

            Set<TechId> thumbnailsToDelete =
                    getMissingIds(containers, getRemainingZoomLevels(connection, containers, true));
            Set<TechId> originalImagesToDelete =
                    getMissingIds(containers, getRemainingZoomLevels(connection, containers, false));
            if (thumbnailsToDelete.size() > 0)
            {
                clearAcquiredImages(connection, thumbnailsToDelete, true);
            }
            if (originalImagesToDelete.size() > 0)
            {
                clearAcquiredImages(connection, originalImagesToDelete, false);
            }
        }

        for (String dataSetTableName : dataSetTableNames)
        {
            connection.createStatement().execute(
                    String.format("DELETE FROM " + dataSetTableName.trim() + " WHERE "
                            + permIDColumn + " IN (%s)", joinIds(deletedDataSets)));
        }
    }
}
