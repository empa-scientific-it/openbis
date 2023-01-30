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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ColumnDistinctValues;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ContainerDataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.VocabularyTermBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class TableForUpdateExporterTest extends AssertJUnit
{
    @Test
    public void testExperimentExport()
    {

        Experiment e1 = new ExperimentBuilder().identifier("/S/P/E1").getExperiment();
        Experiment e2 =
                new ExperimentBuilder().identifier("/S/P/E2").property("P1", "hello")
                        .getExperiment();
        ExperimentBuilder e3 = new ExperimentBuilder().identifier("/S/P/E3");
        e3.property("P2").internal().label("Property 2")
                .value(new MaterialBuilder().code("ALPHA").type("GENE"));
        e3.property("P3").managed().value("hello");
        e3.property("P3").dynamic().value("hello");
        GridRowModels<TableModelRowWithObject<Experiment>> rows =
                createGridRowModels(e1, e2, e3.getExperiment());

        Mockery context = new Mockery();
        final ICommonServer commonServer = context.mock(ICommonServer.class);
        context.checking(new Expectations()
            {
                {
                    allowing(commonServer).listExperimentTypes(with(any(String.class)));
                    will(returnValue(new ArrayList<ExperimentType>()));
                }
            });

        String fileContent =
                TableForUpdateExporter.getExportTableForUpdate(rows, EntityKind.EXPERIMENT, "\n",
                        commonServer, "");

        assertEquals("identifier\tproject\tP1\t$P2\n" + "/S/P/E1\t\t\t\n" + "/S/P/E2\t\thello\t\n"
                + "/S/P/E3\t\t\tALPHA (GENE)\n", fileContent);
    }

    @Test
    public void testSampleExportOnlyOneSampleType()
    {
        Sample s1 =
                new SampleBuilder("/S/S1").type("T1")
                        .partOf(new SampleBuilder("/S/C1").getSample())
                        .experiment(new ExperimentBuilder().identifier("/S/P/E1").getExperiment())
                        .getSample();
        SampleBuilder s2 = new SampleBuilder("/S/S2").type("T1");
        s2.parents(new SampleBuilder("/S/S1").getSample(), new SampleBuilder("/S/S3").getSample());
        s2.property("P1").label("p1")
                .value(new VocabularyTermBuilder("A").label("alpha").getTerm());
        GridRowModels<TableModelRowWithObject<Sample>> rows =
                createGridRowModels(s1, s2.getSample());

        Mockery context = new Mockery();
        final ICommonServer commonServer = context.mock(ICommonServer.class);
        context.checking(new Expectations()
            {
                {
                    allowing(commonServer).listSampleTypes(with(any(String.class)));
                    will(returnValue(new ArrayList<SampleType>()));
                }
            });

        String fileContent =
                TableForUpdateExporter.getExportTableForUpdate(rows, EntityKind.SAMPLE, "\n",
                        commonServer,
                        "");

        assertEquals("identifier\tcontainer\tparents\texperiment\tP1\n"
                + "/S/S1\t/S/C1\t\t/S/P/E1\t\n" + "/S/S2\t\t/S/S1,/S/S3\t\tA\n", fileContent);
    }

    @Test
    public void testSampleExportForMixedSampleTypes()
    {
        Sample s1 = new SampleBuilder("/S/S1").type("T1").property("P1", "hello").getSample();
        Sample s2 = new SampleBuilder("/A/S2").type("T1").getSample();
        SampleBuilder s3 = new SampleBuilder("/S/S3").type("T2");
        s3.parents(new SampleBuilder("/S/S1").getSample());
        s3.property("P2").label("p2").value(new Date(1234567L));
        GridRowModels<TableModelRowWithObject<Sample>> rows =
                createGridRowModels(s1, s2, s3.getSample());

        Mockery context = new Mockery();
        final ICommonServer commonServer = context.mock(ICommonServer.class);
        context.checking(new Expectations()
            {
                {
                    allowing(commonServer).listSampleTypes(with(any(String.class)));
                    will(returnValue(new ArrayList<SampleType>()));
                }
            });

        String fileContent =
                TableForUpdateExporter.getExportTableForUpdate(rows, EntityKind.SAMPLE, "\n",
                        commonServer,
                        "");

        assertEquals("[T1]\n" + "identifier\tcontainer\tparents\texperiment\tP1\n"
                + "/A/S2\t\t\t\t\n" + "/S/S1\t\t\t\thello\n" + "[T2]\n"
                + "identifier\tcontainer\tparents\texperiment\tP2\n"
                + "/S/S3\t\t/S/S1\t\t1970-01-01 01:20:34 +0100\n", fileContent);
    }

    @Test
    public void testDataSetExport()
    {
        Experiment experiment = new ExperimentBuilder().identifier("/S/P/E").getExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code("ds1").experiment(experiment).property("P1", "hello")
                        .parent(new DataSetBuilder().code("ds3").getDataSet())
                        .parent(new DataSetBuilder().code("ds4").getDataSet()).getDataSet();
        PhysicalDataSet ds2 =
                new DataSetBuilder().code("ds2").experiment(experiment)
                        .container(new ContainerDataSetBuilder(1).code("ds1").getContainerDataSet())
                        .sample(new SampleBuilder("/S/S1").getSample()).getDataSet();
        GridRowModels<TableModelRowWithObject<PhysicalDataSet>> rows = createGridRowModels(ds1, ds2);

        Mockery context = new Mockery();
        final ICommonServer commonServer = context.mock(ICommonServer.class);
        context.checking(new Expectations()
            {
                {
                    allowing(commonServer).listDataSetTypes(with(any(String.class)));
                    will(returnValue(new ArrayList<DataSetType>()));
                }
            });

        String fileContent =
                TableForUpdateExporter.getExportTableForUpdate(rows, EntityKind.DATA_SET, "\n",
                        commonServer, "");

        assertEquals("code\tcontainer\tparents\texperiment\tsample\tP1\n"
                + "ds1\t\tds3,ds4\t/S/P/E\t\thello\n" + "ds2\tds1\t\t/S/P/E\t/S/S1\t\n",
                fileContent);
    }

    private <T extends Serializable> GridRowModels<TableModelRowWithObject<T>> createGridRowModels(
            T... entities)
    {
        List<GridRowModel<TableModelRowWithObject<T>>> rows =
                new ArrayList<GridRowModel<TableModelRowWithObject<T>>>();
        for (T entity : entities)
        {
            rows.add(new GridRowModel<TableModelRowWithObject<T>>(new TableModelRowWithObject<T>(
                    entity, Collections.<ISerializableComparable> emptyList()), null));
        }
        return new GridRowModels<TableModelRowWithObject<T>>(rows,
                Collections.<TableModelColumnHeader> emptyList(),
                Collections.<GridCustomColumnInfo> emptyList(),
                Collections.<ColumnDistinctValues> emptyList());
    }
}
