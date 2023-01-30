/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.managed_property.api;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IPerson;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityPropertyAdaptor;

/**
 * @author Jakub Straszewski
 */
public interface IManagedPropertyEvaluator
{
    public void configureUI(final IManagedProperty managedProperty,
            final IEntityPropertyAdaptor entityProperty);

    public void updateFromUI(final IManagedProperty managedProperty, final IPerson person,
            final IManagedUiAction action);

    public List<String> getBatchColumnNames();

    public List<IManagedInputWidgetDescription> getInputWidgetDescriptions();

    public void updateFromBatchInput(final IManagedProperty managedProperty, final IPerson person,
            final Map<String, String> bindings);

    public void updateFromRegistrationForm(final IManagedProperty managedProperty,
            final IPerson person, final List<Map<String, String>> bindings);

}