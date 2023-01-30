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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api;

import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;

/**
 * @author Jakub Straszewski
 */
public interface IDataAdaptor extends IEntityAdaptor
{
    /**
     * Returns the experiment of this data set, or null if not exists.
     */
    public IExperimentAdaptor experiment();

    /**
     * Returns the sample of this data set, or null if not exists.
     */
    public ISampleAdaptor sample();

    /**
     * Returns parent data sets of this data set.
     */
    public Iterable<IDataAdaptor> parents();

    /**
     * Returns parent data sets of this data set. Types of the returned parent data sets must match the specified regular expression.
     */
    public Iterable<IDataAdaptor> parentsOfType(String typeCodeRegexp);

    /**
     * Returns child data sets of this data set.
     */
    public Iterable<IDataAdaptor> children();

    /**
     * Returns child data sets of this data set. Types of the returned child data sets must match the specified regular expression.
     */
    public Iterable<IDataAdaptor> childrenOfType(String typeCodeRegexp);

    /**
     * Returns the container data set, or null if not exists.
     * 
     * @deprecated Use {@link #containers()}
     */
    @Deprecated
    public IDataAdaptor container();

    /**
     * Returns the containers data sets of this data set.
     */
    public Iterable<IDataAdaptor> containers();

    /**
     * Returns contained data sets of this data set.
     */
    public Iterable<IDataAdaptor> contained();

    /**
     * Returns contained data sets of this data set. Types of the returned contained data sets must match the specified regular expression.
     */
    public Iterable<IDataAdaptor> containedOfType(String typeCodeRegexp);

}
