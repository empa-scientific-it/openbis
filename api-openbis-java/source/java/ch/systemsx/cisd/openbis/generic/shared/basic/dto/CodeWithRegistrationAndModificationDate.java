/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.basic.IRegistratorAndModifierHolder;

/**
 * @author Franz-Josef Elmer
 */
public class CodeWithRegistrationAndModificationDate<T extends CodeWithRegistration<T>> extends
        CodeWithRegistration<T> implements IRegistratorAndModifierHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Date modificationDate;

    private int version;

    private Person modifier;

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    @Override
    public final Person getModifier()
    {
        return modifier;
    }

    public final void setModifier(final Person modifier)
    {
        this.modifier = modifier;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }
}
