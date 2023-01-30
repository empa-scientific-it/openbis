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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.gui.CreateExperimentGui;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateExperimentRmi;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class ExperimentBuilder implements Builder<Experiment>
{

    private ExperimentType type;

    private String code;

    private Project project;

    private Collection<Sample> samples;

    private UidGenerator uid;

    public ExperimentBuilder(UidGenerator uid)
    {
        this.uid = uid;
        this.type = null;
        this.code = uid.uid();
        this.project = null;
        this.samples = new ArrayList<Sample>();
    }

    public ExperimentBuilder ofType(ExperimentType type)
    {
        this.type = type;
        return this;
    }

    public ExperimentBuilder in(Project project)
    {
        this.project = project;
        return this;
    }

    public ExperimentBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    public ExperimentBuilder withSamples(Sample... samples)
    {
        this.samples = new HashSet<Sample>();
        for (Sample sample : samples)
        {
            this.samples.add(sample);
        }
        return this;
    }

    @Override
    public Experiment build(Application openbis, Ui ui)
    {
        if (type == null)
        {
            type = new ExperimentTypeBuilder(uid).build(openbis, ui);
        }
        if (project == null)
        {
            project = new ProjectBuilder(uid).build(openbis, ui);
        }

        Experiment experiment =
                new ExperimentDsl(type, code, project, samples, new HashSet<MetaProject>());

        if (Ui.WEB.equals(ui))
        {
            return openbis.execute(new CreateExperimentGui(experiment));
        } else if (Ui.PUBLIC_API.equals(ui))
        {
            return openbis.execute(new CreateExperimentRmi(experiment));
        } else
        {
            return experiment;
        }
    }
}
