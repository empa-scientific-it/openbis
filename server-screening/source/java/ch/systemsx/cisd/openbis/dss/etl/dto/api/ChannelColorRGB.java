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

package ch.systemsx.cisd.openbis.dss.etl.dto.api;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * RGB color components specify the color in which channel should be displayed.
 * 
 * @author Tomasz Pylak
 */
public class ChannelColorRGB implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private int r, g, b;

    @SuppressWarnings("unused")
    private ChannelColorRGB()
    {
    }

    public ChannelColorRGB(int r, int g, int b)
    {
        assert r >= 0 && r <= 255 : "invalid color " + r;
        assert g >= 0 && g <= 255 : "invalid color " + g;
        assert b >= 0 && b <= 255 : "invalid color " + b;

        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getR()
    {
        return r;
    }

    public int getG()
    {
        return g;
    }

    public int getB()
    {
        return b;
    }

    @Override
    public String toString()
    {
        return "Color(r=" + r + ", g=" + g + ", b=" + b + ")";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + b;
        result = prime * result + g;
        result = prime * result + r;
        return result;
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
        ChannelColorRGB other = (ChannelColorRGB) obj;
        if (b != other.b)
            return false;
        if (g != other.g)
            return false;
        if (r != other.r)
            return false;
        return true;
    }

}
