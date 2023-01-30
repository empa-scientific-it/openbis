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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Role grantee.
 * 
 * @author Izabela Adamczyk
 */
public class Grantee implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String code;

    private GranteeType type;

    public enum GranteeType implements IsSerializable
    {
        PERSON, AUTHORIZATION_GROUP;
    }

    public static final Grantee createPerson(String code)
    {
        return new Grantee(code, GranteeType.PERSON);
    }

    public static final Grantee createAuthorizationGroup(String code)
    {
        return new Grantee(code, GranteeType.AUTHORIZATION_GROUP);
    }

    private Grantee(String code, GranteeType type)
    {
        setCode(code);
        setType(type);
    }

    private Grantee()
    {
    }

    public GranteeType getType()
    {
        return type;
    }

    public void setType(GranteeType type)
    {
        this.type = type;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    @Override
    public final String toString()
    {
        return getType().name() + ":" + getCode();
    }
}
