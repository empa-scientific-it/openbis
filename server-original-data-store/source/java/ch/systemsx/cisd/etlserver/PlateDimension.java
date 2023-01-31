/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.Serializable;

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * @author Tomasz Pylak
 */
// TODO 2010-07-28, Tomasz Pylak: remove this class together with BDS
@Deprecated
public class PlateDimension extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private int rowsNum;

    private int colsNum;

    // for internal use only
    public PlateDimension()
    {
        this(0, 0);
    }

    public PlateDimension(int rowsNum, int colsNum)
    {
        super();
        this.rowsNum = rowsNum;
        this.colsNum = colsNum;
    }

    public int getRowsNum()
    {
        return rowsNum;
    }

    public void setRowsNum(int rowsNum)
    {
        this.rowsNum = rowsNum;
    }

    public int getColsNum()
    {
        return colsNum;
    }

    public void setColsNum(int colsNum)
    {
        this.colsNum = colsNum;
    }

    @Override
    public String toString()
    {
        return "(" + rowsNum + ", " + colsNum + ")";
    }
}
