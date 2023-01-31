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

package ch.systemsx.cisd.etlserver.validation;

/**
 * Factory for table cell validators.
 *
 * @author Franz-Josef Elmer
 */
public interface IValidatorFactory
{
    /**
     * Creates a validator for the specified column header. The same instance can be returned as in a previous call for the same argument if the
     * validator object is immutable and the .
     */
    public IValidator createValidator(String columnHeader);
}
