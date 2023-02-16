/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * @author pkupczyk
 */
public class DeletedDataSetLocation implements IDeletedDataSetLocation, Serializable
{

    private static final long serialVersionUID = 1L;

    private static final String SEPARATOR_BETWEEN_LOCATIONS = ", ";

    private static final String SEPARATOR_BETWEEN_PARTS = "/";

    private String datastoreCode;

    private String shareId;

    private String location;

    public DeletedDataSetLocation()
    {
    }

    @Override
    public String getDatastoreCode()
    {
        return datastoreCode;
    }

    public void setDatastoreCode(String datastoreCode)
    {
        this.datastoreCode = datastoreCode;
    }

    @Override
    public String getShareId()
    {
        return shareId;
    }

    public void setShareId(String shareId)
    {
        this.shareId = shareId;
    }

    @Override
    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public boolean isEmpty()
    {
        return getDatastoreCode() == null && getShareId() == null && getLocation() == null;
    }

    public static String format(List<DeletedDataSetLocation> locationObjects)
    {
        if (locationObjects == null || locationObjects.isEmpty())
        {
            return StringUtils.EMPTY_STRING;
        }

        StringBuilder locationString = new StringBuilder();
        Iterator<DeletedDataSetLocation> iter = locationObjects.iterator();

        while (iter.hasNext())
        {
            locationString.append(format(iter.next()));

            if (iter.hasNext())
            {
                locationString.append(SEPARATOR_BETWEEN_LOCATIONS);
            }
        }

        return locationString.toString();
    }

    private static String format(DeletedDataSetLocation locationObject)
    {
        if (locationObject.isEmpty())
        {
            return StringUtils.EMPTY_STRING;
        } else
        {
            String datastoreCode = StringUtils.emptyIfNull(locationObject.getDatastoreCode());
            String shareId = StringUtils.emptyIfNull(locationObject.getShareId());
            String location = StringUtils.emptyIfNull(locationObject.getLocation());

            StringBuilder locationString = new StringBuilder();
            locationString.append(SEPARATOR_BETWEEN_PARTS + datastoreCode);
            locationString.append(SEPARATOR_BETWEEN_PARTS + shareId);
            locationString.append(SEPARATOR_BETWEEN_PARTS + location);
            return locationString.toString();
        }
    }

    public static List<DeletedDataSetLocation> parse(String str)
    {
        if (str == null)
        {
            return Collections.emptyList();
        }
        List<DeletedDataSetLocation> locationObjects = new ArrayList<DeletedDataSetLocation>();
        String[] locationStrings = str.split(SEPARATOR_BETWEEN_LOCATIONS, -1);

        for (String locationString : locationStrings)
        {
            DeletedDataSetLocation locationObject = new DeletedDataSetLocation();

            if (locationString.startsWith(SEPARATOR_BETWEEN_PARTS))
            {
                String[] locationParts = locationString.split(SEPARATOR_BETWEEN_PARTS);
                Iterator<String> locationPartsIter = Arrays.asList(locationParts).iterator();

                locationPartsIter.next();

                if (locationPartsIter.hasNext())
                {
                    locationObject.setDatastoreCode(StringUtils.nullIfBlank(locationPartsIter
                            .next()));
                }
                if (locationPartsIter.hasNext())
                {
                    locationObject.setShareId(StringUtils.nullIfBlank(locationPartsIter.next()));
                }
                if (locationPartsIter.hasNext())
                {
                    StringBuilder location = new StringBuilder();
                    while (locationPartsIter.hasNext())
                    {
                        location.append(locationPartsIter.next());
                        if (locationPartsIter.hasNext())
                        {
                            location.append(SEPARATOR_BETWEEN_PARTS);
                        }
                    }
                    locationObject.setLocation(StringUtils.nullIfBlank(location.toString()));
                }

            } else
            {
                locationObject.setLocation(StringUtils.nullIfBlank(locationString));
            }

            locationObjects.add(locationObject);
        }

        return locationObjects;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getDatastoreCode());
        builder.append(getShareId());
        builder.append(getLocation());
        return builder.toHashCode();
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

        DeletedDataSetLocation other = (DeletedDataSetLocation) obj;

        EqualsBuilder builder = new EqualsBuilder();
        builder.append(getDatastoreCode(), other.getDatastoreCode());
        builder.append(getShareId(), other.getShareId());
        builder.append(getLocation(), other.getLocation());

        return builder.isEquals();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("datastoreCode", getDatastoreCode());
        builder.append("shareId", getShareId());
        builder.append("location", getLocation());
        return builder.toString();
    }
}
