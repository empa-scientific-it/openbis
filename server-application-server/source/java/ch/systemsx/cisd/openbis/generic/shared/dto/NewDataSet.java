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

package ch.systemsx.cisd.openbis.generic.shared.dto;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class NewDataSet extends NewExternalData
{
    private static final long serialVersionUID = 1L;

    // TODO KE: Sekhar, should we delete the dead code ?!
    // The following fields and methods should eventually be moved here, but it's too difficult to
    // do this now.

    // private String shareId;
    //
    // private String location;
    //
    // private int speedHint;
    //
    // private FileFormatType fileFormatType;
    //
    // private LocatorType locatorType;
    //
    // private BooleanOrUnknown complete = BooleanOrUnknown.U;
    //
    // public String getShareId()
    // {
    // return shareId;
    // }
    //
    // public void setShareId(String shareId)
    // {
    // this.shareId = shareId;
    // }
    //
    // public int getSpeedHint()
    // {
    // return speedHint;
    // }
    //
    // public void setSpeedHint(int speedHint)
    // {
    // this.speedHint = speedHint;
    // }
    //
    // /** Returns <code>locator</code>. */
    // public final String getLocation()
    // {
    // return location;
    // }
    //
    // /** Sets <code>locator</code>. */
    // public final void setLocation(final String locator)
    // {
    // this.location = locator;
    // }
    //
    // /** Returns <code>fileFormatType</code>. */
    // public final FileFormatType getFileFormatType()
    // {
    // return fileFormatType;
    // }
    //
    // /** Sets <code>fileFormatType</code>. */
    // public final void setFileFormatType(final FileFormatType fileFormatType)
    // {
    // this.fileFormatType = fileFormatType;
    // }
    //
    // /** Returns <code>locatorType</code>. */
    // public final LocatorType getLocatorType()
    // {
    // return locatorType;
    // }
    //
    // /** Sets <code>locatorType</code>. */
    // public final void setLocatorType(final LocatorType locatorType)
    // {
    // this.locatorType = locatorType;
    // }
    //
    // /**
    // * Returns {@link BooleanOrUnknown#T}, if the data set is complete in the data store and
    // * {@link BooleanOrUnknown#F}, if some parts of the data are missing. If the completeness is
    // not
    // * known (e.g. because the data set is stored in a format that does not allow to assess the
    // * completeness, {@link BooleanOrUnknown#U} is returned.
    // */
    // public final BooleanOrUnknown getComplete()
    // {
    // return complete;
    // }
    //
    // /**
    // * Sets whether this data set is complete in the data store or not. The default is
    // * {@link BooleanOrUnknown#U}, which corresponds to the case where the data are stored in a
    // * format that does not allow to assess completeness.
    // */
    // public final void setComplete(final BooleanOrUnknown complete)
    // {
    // this.complete = complete;
    // }
    //
    // public final boolean equals(final NewDataSet obj)
    // {
    // if (obj == this)
    // {
    // return true;
    // }
    // if (obj == null)
    // {
    // return false;
    // }
    // final NewDataSet that = obj;
    // final EqualsBuilder builder = new EqualsBuilder();
    // builder.append(that.location, location);
    // return builder.isEquals();
    // }
    //
    // @Override
    // public int hashCode()
    // {
    // final HashCodeBuilder builder = new HashCodeBuilder();
    // builder.append(location);
    // return builder.toHashCode();
    // }
    //
    // @Override
    // public String toString()
    // {
    // ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    // builder.append("code", getCode());
    // builder.append("type", getDataSetType());
    // builder.append("fileFormat", getFileFormatType());
    // builder.append("properties", getDataSetProperties());
    // return builder.toString();
    // }

}
