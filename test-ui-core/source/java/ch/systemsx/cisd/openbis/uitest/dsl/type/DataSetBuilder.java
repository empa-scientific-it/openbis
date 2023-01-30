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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateDataSetRmi;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.DataSetType;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class DataSetBuilder implements Builder<DataSet>
{
    private DataSetType type;

    private Sample sample;

    private Experiment experiment;

    private UidGenerator uid;

    private boolean external;

    private Collection<DataSet> parents;

    public DataSetBuilder(UidGenerator uid)
    {
        this.uid = uid;
        this.sample = null;
        this.experiment = null;
        this.external = false;
        this.parents = new HashSet<DataSet>();
    }

    public DataSetBuilder ofType(DataSetType type)
    {
        this.type = type;
        return this;
    }

    public DataSetBuilder in(Experiment experiment)
    {
        this.experiment = experiment;
        return this;
    }

    public DataSetBuilder in(Sample sample)
    {
        this.sample = sample;
        return this;
    }

    public DataSetBuilder inExternalDss()
    {
        this.external = true;
        return this;
    }

    public DataSetBuilder withParents(DataSet... dataSets)
    {
        parents.addAll(Arrays.asList(dataSets));
        return this;
    }

    @Override
    public DataSet build(Application openbis, Ui ui)
    {
        if (type == null)
        {
            type = new DataSetTypeBuilder(uid).build(openbis, ui);
        }

        if (sample == null && experiment == null)
        {
            Experiment experiment = new ExperimentBuilder(uid).build(openbis, ui);
            sample = new SampleBuilder(uid).in(experiment).build(openbis, ui);
        }

        return openbis.execute(new CreateDataSetRmi(new DataSetDsl(type, sample, experiment, parents,
                new HashSet<MetaProject>()), external));
    }
}
