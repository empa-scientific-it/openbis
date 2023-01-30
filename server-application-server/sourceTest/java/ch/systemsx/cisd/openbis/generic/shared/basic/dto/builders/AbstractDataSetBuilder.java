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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractDataSetBuilder<T extends AbstractDataSetBuilder<?>>
{

    protected final AbstractExternalData dataSet;

    /**
     * Return this object typed to the concrete class. This is a subclass responsibility.
     */
    abstract protected T asConcreteSubclass();

    /**
     * Constructor that takes a concrete data set class as an argument.
     */
    protected AbstractDataSetBuilder(AbstractExternalData concreteDataSet)
    {
        super();
        this.dataSet = concreteDataSet;
        dataSet.setDataSetProperties(new ArrayList<IEntityProperty>());
    }

    public T code(String code)
    {
        dataSet.setCode(code);
        return asConcreteSubclass();
    }

    public T type(String dataSetTypeCode)
    {
        dataSet.setDataSetType(new DataSetType(dataSetTypeCode));
        return asConcreteSubclass();
    }

    public T container(ContainerDataSet container)
    {
        dataSet.addContainer(container, 0);
        return asConcreteSubclass();
    }

    public T experiment(Experiment experiment)
    {
        dataSet.setExperiment(experiment);
        return asConcreteSubclass();
    }

    public T sample(Sample sample)
    {
        dataSet.setSample(sample);
        return asConcreteSubclass();
    }

    public T store(DataStore dataStore)
    {
        dataSet.setDataStore(dataStore);
        return asConcreteSubclass();
    }

    public T size(long size)
    {
        dataSet.setSize(size);
        return asConcreteSubclass();
    }

    public T version(int version)
    {
        dataSet.setVersion(version);
        return asConcreteSubclass();
    }

    public PropertyBuilder property(String key)
    {
        List<IEntityProperty> properties = dataSet.getProperties();
        PropertyBuilder propertyBuilder = new PropertyBuilder(key);
        properties.add(propertyBuilder.getProperty());
        return propertyBuilder;
    }

    public T property(String key, String value)
    {
        property(key).value(value);
        return asConcreteSubclass();
    }

    public T registrationDate(Date registrationDate)
    {
        dataSet.setRegistrationDate(registrationDate);
        return asConcreteSubclass();
    }

    public T modificationDate(Date modificationDate)
    {
        dataSet.setModificationDate(modificationDate);
        return asConcreteSubclass();
    }

    public T parent(PhysicalDataSet parent)
    {
        Collection<AbstractExternalData> parents = dataSet.getParents();
        if (parents == null)
        {
            parents = new ArrayList<AbstractExternalData>();
            dataSet.setParents(parents);
        }
        parents.add(parent);
        return asConcreteSubclass();
    }

    public T child(PhysicalDataSet child)
    {
        Collection<AbstractExternalData> children = dataSet.getChildren();
        if (children == null)
        {
            children = new ArrayList<AbstractExternalData>();
            dataSet.setChildren(children);
        }
        children.add(child);
        return asConcreteSubclass();
    }

}