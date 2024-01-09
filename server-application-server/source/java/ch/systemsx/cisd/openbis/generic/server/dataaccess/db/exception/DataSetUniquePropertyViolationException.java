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

public class DataSetUniquePropertyViolationException extends UniquePropertyViolationExceptionAbstract
{
    public DataSetUniquePropertyViolationException(String code)
    {
        super(code, getMessage(code));
    }

    private static String getMessage(String value)
    {
        return String
                .format("Insert/Update of dataset failed because property contains value that is not unique! (value: %s)",
                        value);
    }
}
