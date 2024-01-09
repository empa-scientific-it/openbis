/*
 *  Copyright ETH 2024 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.exception;

import org.springframework.dao.DataIntegrityViolationException;

public abstract class UniquePropertyViolationExceptionAbstract extends
        DataIntegrityViolationException
{
    private static final long serialVersionUID = 1L;

    private String code;

    public UniquePropertyViolationExceptionAbstract(String code, String msg)
    {
        super(msg);

        if (code == null)
        {
            throw new IllegalArgumentException("Code cannot be null");
        }

        this.code = code;
    }

    public String getCode()
    {
        return code;
    }
}
