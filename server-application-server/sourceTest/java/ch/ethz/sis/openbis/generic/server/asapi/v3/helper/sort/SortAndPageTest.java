package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort;

import static org.testng.AssertJUnit.assertEquals;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;

public class SortAndPageTest
{
	
	@Test
    public void testFetchedFieldsScore_Sample_CodeScore()
    {
		SampleType sampleTypeA = new SampleType();
		sampleTypeA.setCode("DUMMY_CODE_A");
		
		SampleType sampleTypeB = new SampleType();
		sampleTypeB.setCode("DUMMY_CODE_B");
		
		String propertyCode = "DUMMY_PROPERTY";
		
		SampleSearchCriteria c = new SampleSearchCriteria();
		c.withOrOperator();
        c.withCode().thatEquals("S2");
        
		SampleFetchOptions fo = new SampleFetchOptions();
		fo.withType();
		fo.withProperties();
		fo.sortBy().fetchedFieldsScore();
		
        Sample sample1 = new Sample();
        sample1.setType(sampleTypeA);
        sample1.setCode("S1");
        sample1.setProperty(propertyCode, "DUMMY_S1");
        sample1.setFetchOptions(fo);

        Sample sample2 = new Sample();
        sample2.setType(sampleTypeB);
        sample2.setCode("S2");
        sample2.setProperty(propertyCode, "DUMMY_S2");
        sample2.setFetchOptions(fo);

        Sample sample3 = new Sample();
        sample3.setType(sampleTypeA);
        sample3.setCode("S3");
        sample3.setProperty(propertyCode, "DUMMY_S3");
        sample3.setFetchOptions(fo);

        List<Sample> samples = new ArrayList<Sample>();
        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);

        Collection<Sample> results = new SortAndPage().sortAndPage(samples, c, fo);

        assertEquals(results, list(sample2, sample1, sample3));
    }
	
	@Test
    public void testFetchedFieldsScore_Sample_PropertyScore()
    {
		SampleType sampleTypeA = new SampleType();
		sampleTypeA.setCode("DUMMY_CODE_A");
		
		SampleType sampleTypeB = new SampleType();
		sampleTypeB.setCode("DUMMY_CODE_B");
		
		String propertyCode = "DUMMY_PROPERTY";
		
		SampleSearchCriteria c = new SampleSearchCriteria();
		c.withOrOperator();
        c.withProperty(propertyCode).thatEquals("DUMMY_S3");
        
		SampleFetchOptions fo = new SampleFetchOptions();
		fo.withType();
		fo.withProperties();
		fo.sortBy().fetchedFieldsScore();
		
        Sample sample1 = new Sample();
        sample1.setType(sampleTypeA);
        sample1.setCode("S1");
        sample1.setProperty(propertyCode, "DUMMY_S1");
        sample1.setFetchOptions(fo);

        Sample sample2 = new Sample();
        sample2.setType(sampleTypeB);
        sample2.setCode("S2");
        sample2.setProperty(propertyCode, "DUMMY_S2");
        sample2.setFetchOptions(fo);

        Sample sample3 = new Sample();
        sample3.setType(sampleTypeA);
        sample3.setCode("S3");
        sample3.setProperty(propertyCode, "DUMMY_S3");
        sample3.setFetchOptions(fo);

        List<Sample> samples = new ArrayList<Sample>();
        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);

        Collection<Sample> results = new SortAndPage().sortAndPage(samples, c, fo);

        assertEquals(results, list(sample3, sample1, sample2));
    }
	
	@Test
    public void testFetchedFieldsScore_Sample_PropertyScore_MissingProperties()
    {
		SampleType sampleTypeA = new SampleType();
		sampleTypeA.setCode("DUMMY_CODE_A");
		
		SampleType sampleTypeB = new SampleType();
		sampleTypeB.setCode("DUMMY_CODE_B");
		
		String propertyCode = "DUMMY_PROPERTY";
		
		SampleSearchCriteria c = new SampleSearchCriteria();
		c.withOrOperator();
        c.withProperty(propertyCode).thatEquals("DUMMY_S3");
        
		SampleFetchOptions fo = new SampleFetchOptions();
		fo.withType();
		fo.sortBy().fetchedFieldsScore();
		
        Sample sample1 = new Sample();
        sample1.setType(sampleTypeA);
        sample1.setCode("S1");
        sample1.setFetchOptions(fo);

        Sample sample2 = new Sample();
        sample2.setType(sampleTypeB);
        sample2.setCode("S2");
        sample2.setFetchOptions(fo);

        Sample sample3 = new Sample();
        sample3.setType(sampleTypeA);
        sample3.setCode("S3");
        sample3.setFetchOptions(fo);

        List<Sample> samples = new ArrayList<Sample>();
        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);

        Collection<Sample> results = new SortAndPage().sortAndPage(samples, c, fo);

        assertEquals(results, list(sample1, sample2, sample3));
    }
	
	@Test
    public void testFetchedFieldsScore_Sample_TypeScore()
    {
		SampleType sampleTypeA = new SampleType();
		sampleTypeA.setCode("DUMMY_CODE_A");
		
		SampleType sampleTypeB = new SampleType();
		sampleTypeB.setCode("DUMMY_CODE_B");
		
		String propertyCode = "DUMMY_PROPERTY";
		
		SampleSearchCriteria c = new SampleSearchCriteria();
		c.withOrOperator();
        c.withType().withCode().thatEquals(sampleTypeA.getCode());
        
		SampleFetchOptions fo = new SampleFetchOptions();
		fo.withType();
		fo.withProperties();
		fo.sortBy().fetchedFieldsScore();
		
        Sample sample1 = new Sample();
        sample1.setType(sampleTypeA);
        sample1.setCode("S1");
        sample1.setProperty(propertyCode, "DUMMY_S1");
        sample1.setFetchOptions(fo);

        Sample sample2 = new Sample();
        sample2.setType(sampleTypeB);
        sample2.setCode("S2");
        sample2.setProperty(propertyCode, "DUMMY_S2");
        sample2.setFetchOptions(fo);

        Sample sample3 = new Sample();
        sample3.setType(sampleTypeA);
        sample3.setCode("S3");
        sample3.setProperty(propertyCode, "DUMMY_S3");
        sample3.setFetchOptions(fo);

        List<Sample> samples = new ArrayList<Sample>();
        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);

        Collection<Sample> results = new SortAndPage().sortAndPage(samples, c, fo);

        assertEquals(results, list(sample1, sample3, sample2));
    }
	
	@Test
    public void testFetchedFieldsScore_Sample_TypeScore_MissingType()
    {
		SampleType sampleTypeA = new SampleType();
		sampleTypeA.setCode("DUMMY_CODE_A");
		
		String propertyCode = "DUMMY_PROPERTY";
		
		SampleSearchCriteria c = new SampleSearchCriteria();
		c.withOrOperator();
        c.withType().withCode().thatEquals(sampleTypeA.getCode());
        
		SampleFetchOptions fo = new SampleFetchOptions();
		fo.withProperties();
		fo.sortBy().fetchedFieldsScore();
		
        Sample sample1 = new Sample();
        sample1.setCode("S1");
        sample1.setProperty(propertyCode, "DUMMY_S1");
        sample1.setFetchOptions(fo);

        Sample sample2 = new Sample();
        sample2.setCode("S2");
        sample2.setProperty(propertyCode, "DUMMY_S2");
        sample2.setFetchOptions(fo);

        Sample sample3 = new Sample();
        sample3.setCode("S3");
        sample3.setProperty(propertyCode, "DUMMY_S3");
        sample3.setFetchOptions(fo);

        List<Sample> samples = new ArrayList<Sample>();
        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);

        Collection<Sample> results = new SortAndPage().sortAndPage(samples, c, fo);

        assertEquals(results, list(sample1, sample2, sample3));
    }
	
	@Test
    public void testFetchedFieldsScore_Experiment_CodeScore()
    {
		ExperimentType experimentTypeA = new ExperimentType();
		experimentTypeA.setCode("DUMMY_CODE_A");
		
		ExperimentType experimentTypeB = new ExperimentType();
		experimentTypeB.setCode("DUMMY_CODE_B");
		
		String propertyCode = "DUMMY_PROPERTY";
		
		ExperimentSearchCriteria c = new ExperimentSearchCriteria();
		c.withOrOperator();
        c.withCode().thatEquals("S2");
        
		ExperimentFetchOptions fo = new ExperimentFetchOptions();
		fo.withType();
		fo.withProperties();
		fo.sortBy().fetchedFieldsScore();
		
        Experiment experiment1 = new Experiment();
        experiment1.setType(experimentTypeA);
        experiment1.setCode("S1");
        experiment1.setProperty(propertyCode, "DUMMY_S1");
        experiment1.setFetchOptions(fo);

        Experiment experiment2 = new Experiment();
        experiment2.setType(experimentTypeB);
        experiment2.setCode("S2");
        experiment2.setProperty(propertyCode, "DUMMY_S2");
        experiment2.setFetchOptions(fo);

        Experiment experiment3 = new Experiment();
        experiment3.setType(experimentTypeA);
        experiment3.setCode("S3");
        experiment3.setProperty(propertyCode, "DUMMY_S3");
        experiment3.setFetchOptions(fo);

        List<Experiment> experiments = new ArrayList<Experiment>();
        experiments.add(experiment1);
        experiments.add(experiment2);
        experiments.add(experiment3);

        Collection<Experiment> results = new SortAndPage().sortAndPage(experiments, c, fo);

        assertEquals(results, list(experiment2, experiment1, experiment3));
    }
	
	@Test
    public void testFetchedFieldsScore_Experiment_PropertyScore()
    {
		ExperimentType experimentTypeA = new ExperimentType();
		experimentTypeA.setCode("DUMMY_CODE_A");
		
		ExperimentType experimentTypeB = new ExperimentType();
		experimentTypeB.setCode("DUMMY_CODE_B");
		
		String propertyCode = "DUMMY_PROPERTY";
		
		ExperimentSearchCriteria c = new ExperimentSearchCriteria();
		c.withOrOperator();
        c.withProperty(propertyCode).thatEquals("DUMMY_S3");
        
		ExperimentFetchOptions fo = new ExperimentFetchOptions();
		fo.withType();
		fo.withProperties();
		fo.sortBy().fetchedFieldsScore();
		
        Experiment experiment1 = new Experiment();
        experiment1.setType(experimentTypeA);
        experiment1.setCode("S1");
        experiment1.setProperty(propertyCode, "DUMMY_S1");
        experiment1.setFetchOptions(fo);

        Experiment experiment2 = new Experiment();
        experiment2.setType(experimentTypeB);
        experiment2.setCode("S2");
        experiment2.setProperty(propertyCode, "DUMMY_S2");
        experiment2.setFetchOptions(fo);

        Experiment experiment3 = new Experiment();
        experiment3.setType(experimentTypeA);
        experiment3.setCode("S3");
        experiment3.setProperty(propertyCode, "DUMMY_S3");
        experiment3.setFetchOptions(fo);

        List<Experiment> experiments = new ArrayList<Experiment>();
        experiments.add(experiment1);
        experiments.add(experiment2);
        experiments.add(experiment3);

        Collection<Experiment> results = new SortAndPage().sortAndPage(experiments, c, fo);

        assertEquals(results, list(experiment3, experiment1, experiment2));
    }
	
	@Test
    public void testFetchedFieldsScore_Experiment_PropertyScore_MissingProperties()
    {
		ExperimentType experimentTypeA = new ExperimentType();
		experimentTypeA.setCode("DUMMY_CODE_A");
		
		ExperimentType experimentTypeB = new ExperimentType();
		experimentTypeB.setCode("DUMMY_CODE_B");
		
		String propertyCode = "DUMMY_PROPERTY";
		
		ExperimentSearchCriteria c = new ExperimentSearchCriteria();
		c.withOrOperator();
        c.withProperty(propertyCode).thatEquals("DUMMY_S3");
        
		ExperimentFetchOptions fo = new ExperimentFetchOptions();
		fo.withType();
		fo.sortBy().fetchedFieldsScore();
		
        Experiment experiment1 = new Experiment();
        experiment1.setType(experimentTypeA);
        experiment1.setCode("S1");
        experiment1.setFetchOptions(fo);

        Experiment experiment2 = new Experiment();
        experiment2.setType(experimentTypeB);
        experiment2.setCode("S2");
        experiment2.setFetchOptions(fo);

        Experiment experiment3 = new Experiment();
        experiment3.setType(experimentTypeA);
        experiment3.setCode("S3");
        experiment3.setFetchOptions(fo);

        List<Experiment> experiments = new ArrayList<Experiment>();
        experiments.add(experiment1);
        experiments.add(experiment2);
        experiments.add(experiment3);

        Collection<Experiment> results = new SortAndPage().sortAndPage(experiments, c, fo);

        assertEquals(results, list(experiment1, experiment2, experiment3));
    }
	
	@Test
    public void testFetchedFieldsScore_Experiment_TypeScore()
    {
		ExperimentType experimentTypeA = new ExperimentType();
		experimentTypeA.setCode("DUMMY_CODE_A");
		
		ExperimentType experimentTypeB = new ExperimentType();
		experimentTypeB.setCode("DUMMY_CODE_B");
		
		String propertyCode = "DUMMY_PROPERTY";
		
		ExperimentSearchCriteria c = new ExperimentSearchCriteria();
		c.withOrOperator();
        c.withType().withCode().thatEquals(experimentTypeA.getCode());
        
		ExperimentFetchOptions fo = new ExperimentFetchOptions();
		fo.withType();
		fo.withProperties();
		fo.sortBy().fetchedFieldsScore();
		
        Experiment experiment1 = new Experiment();
        experiment1.setType(experimentTypeA);
        experiment1.setCode("S1");
        experiment1.setProperty(propertyCode, "DUMMY_S1");
        experiment1.setFetchOptions(fo);

        Experiment experiment2 = new Experiment();
        experiment2.setType(experimentTypeB);
        experiment2.setCode("S2");
        experiment2.setProperty(propertyCode, "DUMMY_S2");
        experiment2.setFetchOptions(fo);

        Experiment experiment3 = new Experiment();
        experiment3.setType(experimentTypeA);
        experiment3.setCode("S3");
        experiment3.setProperty(propertyCode, "DUMMY_S3");
        experiment3.setFetchOptions(fo);

        List<Experiment> experiments = new ArrayList<Experiment>();
        experiments.add(experiment1);
        experiments.add(experiment2);
        experiments.add(experiment3);

        Collection<Experiment> results = new SortAndPage().sortAndPage(experiments, c, fo);

        assertEquals(results, list(experiment1, experiment3, experiment2));
    }
	
	@Test
    public void testFetchedFieldsScore_Experiment_TypeScore_MissingType()
    {
		ExperimentType experimentTypeA = new ExperimentType();
		experimentTypeA.setCode("DUMMY_CODE_A");
		
		String propertyCode = "DUMMY_PROPERTY";
		
		ExperimentSearchCriteria c = new ExperimentSearchCriteria();
		c.withOrOperator();
        c.withType().withCode().thatEquals(experimentTypeA.getCode());
        
		ExperimentFetchOptions fo = new ExperimentFetchOptions();
		fo.withProperties();
		fo.sortBy().fetchedFieldsScore();
		
        Experiment experiment1 = new Experiment();
        experiment1.setCode("S1");
        experiment1.setProperty(propertyCode, "DUMMY_S1");
        experiment1.setFetchOptions(fo);

        Experiment experiment2 = new Experiment();
        experiment2.setCode("S2");
        experiment2.setProperty(propertyCode, "DUMMY_S2");
        experiment2.setFetchOptions(fo);

        Experiment experiment3 = new Experiment();
        experiment3.setCode("S3");
        experiment3.setProperty(propertyCode, "DUMMY_S3");
        experiment3.setFetchOptions(fo);

        List<Experiment> experiments = new ArrayList<Experiment>();
        experiments.add(experiment1);
        experiments.add(experiment2);
        experiments.add(experiment3);

        Collection<Experiment> results = new SortAndPage().sortAndPage(experiments, c, fo);

        assertEquals(results, list(experiment1, experiment2, experiment3));
    }
	
	@Test
    public void testFetchedFieldsScore_DataSet_CodeScore()
    {
        DataSetType dataSetTypeA = new DataSetType();
        dataSetTypeA.setCode("DUMMY_CODE_A");
        
        DataSetType dataSetTypeB = new DataSetType();
        dataSetTypeB.setCode("DUMMY_CODE_B");
        
        String propertyCode = "DUMMY_PROPERTY";
        
        DataSetSearchCriteria c = new DataSetSearchCriteria();
        c.withOrOperator();
        c.withCode().thatEquals("S2");
        
        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withType();
        fo.withProperties();
        fo.sortBy().fetchedFieldsScore();
        
        DataSet dataset1 = new DataSet();
        dataset1.setType(dataSetTypeA);
        dataset1.setCode("S1");
        dataset1.setProperty(propertyCode, "DUMMY_S1");
        dataset1.setFetchOptions(fo);

        DataSet dataset2 = new DataSet();
        dataset2.setType(dataSetTypeB);
        dataset2.setCode("S2");
        dataset2.setProperty(propertyCode, "DUMMY_S2");
        dataset2.setFetchOptions(fo);

        DataSet dataset3 = new DataSet();
        dataset3.setType(dataSetTypeA);
        dataset3.setCode("S3");
        dataset3.setProperty(propertyCode, "DUMMY_S3");
        dataset3.setFetchOptions(fo);

        List<DataSet> datasets = new ArrayList<DataSet>();
        datasets.add(dataset1);
        datasets.add(dataset2);
        datasets.add(dataset3);

        Collection<DataSet> results = new SortAndPage().sortAndPage(datasets, c, fo);

        assertEquals(results, list(dataset2, dataset1, dataset3));
    }
    
    @Test
    public void testFetchedFieldsScore_DataSet_PropertyScore()
    {
        DataSetType dataSetTypeA = new DataSetType();
        dataSetTypeA.setCode("DUMMY_CODE_A");
        
        DataSetType dataSetTypeB = new DataSetType();
        dataSetTypeB.setCode("DUMMY_CODE_B");
        
        String propertyCode = "DUMMY_PROPERTY";
        
        DataSetSearchCriteria c = new DataSetSearchCriteria();
        c.withOrOperator();
        c.withProperty(propertyCode).thatEquals("DUMMY_S3");
        
        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withType();
        fo.withProperties();
        fo.sortBy().fetchedFieldsScore();
        
        DataSet dataset1 = new DataSet();
        dataset1.setType(dataSetTypeA);
        dataset1.setCode("S1");
        dataset1.setProperty(propertyCode, "DUMMY_S1");
        dataset1.setFetchOptions(fo);

        DataSet dataset2 = new DataSet();
        dataset2.setType(dataSetTypeB);
        dataset2.setCode("S2");
        dataset2.setProperty(propertyCode, "DUMMY_S2");
        dataset2.setFetchOptions(fo);

        DataSet dataset3 = new DataSet();
        dataset3.setType(dataSetTypeA);
        dataset3.setCode("S3");
        dataset3.setProperty(propertyCode, "DUMMY_S3");
        dataset3.setFetchOptions(fo);

        List<DataSet> datasets = new ArrayList<DataSet>();
        datasets.add(dataset1);
        datasets.add(dataset2);
        datasets.add(dataset3);

        Collection<DataSet> results = new SortAndPage().sortAndPage(datasets, c, fo);

        assertEquals(results, list(dataset3, dataset1, dataset2));
    }
    
    @Test
    public void testFetchedFieldsScore_DataSet_PropertyScore_MissingProperties()
    {
        DataSetType dataSetTypeA = new DataSetType();
        dataSetTypeA.setCode("DUMMY_CODE_A");
        
        DataSetType dataSetTypeB = new DataSetType();
        dataSetTypeB.setCode("DUMMY_CODE_B");
        
        String propertyCode = "DUMMY_PROPERTY";
        
        DataSetSearchCriteria c = new DataSetSearchCriteria();
        c.withOrOperator();
        c.withProperty(propertyCode).thatEquals("DUMMY_S3");
        
        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withType();
        fo.sortBy().fetchedFieldsScore();
        
        DataSet dataset1 = new DataSet();
        dataset1.setType(dataSetTypeA);
        dataset1.setCode("S1");
        dataset1.setFetchOptions(fo);

        DataSet dataset2 = new DataSet();
        dataset2.setType(dataSetTypeB);
        dataset2.setCode("S2");
        dataset2.setFetchOptions(fo);

        DataSet dataset3 = new DataSet();
        dataset3.setType(dataSetTypeA);
        dataset3.setCode("S3");
        dataset3.setFetchOptions(fo);

        List<DataSet> datasets = new ArrayList<DataSet>();
        datasets.add(dataset1);
        datasets.add(dataset2);
        datasets.add(dataset3);

        Collection<DataSet> results = new SortAndPage().sortAndPage(datasets, c, fo);

        assertEquals(results, list(dataset1, dataset2, dataset3));
    }
    
    @Test
    public void testFetchedFieldsScore_DataSet_TypeScore()
    {
        DataSetType dataSetTypeA = new DataSetType();
        dataSetTypeA.setCode("DUMMY_CODE_A");
        
        DataSetType dataSetTypeB = new DataSetType();
        dataSetTypeB.setCode("DUMMY_CODE_B");
        
        String propertyCode = "DUMMY_PROPERTY";
        
        DataSetSearchCriteria c = new DataSetSearchCriteria();
        c.withOrOperator();
        c.withType().withCode().thatEquals(dataSetTypeA.getCode());
        
        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withType();
        fo.withProperties();
        fo.sortBy().fetchedFieldsScore();
        
        DataSet dataset1 = new DataSet();
        dataset1.setType(dataSetTypeA);
        dataset1.setCode("S1");
        dataset1.setProperty(propertyCode, "DUMMY_S1");
        dataset1.setFetchOptions(fo);

        DataSet dataset2 = new DataSet();
        dataset2.setType(dataSetTypeB);
        dataset2.setCode("S2");
        dataset2.setProperty(propertyCode, "DUMMY_S2");
        dataset2.setFetchOptions(fo);

        DataSet dataset3 = new DataSet();
        dataset3.setType(dataSetTypeA);
        dataset3.setCode("S3");
        dataset3.setProperty(propertyCode, "DUMMY_S3");
        dataset3.setFetchOptions(fo);

        List<DataSet> datasets = new ArrayList<DataSet>();
        datasets.add(dataset1);
        datasets.add(dataset2);
        datasets.add(dataset3);

        Collection<DataSet> results = new SortAndPage().sortAndPage(datasets, c, fo);

        assertEquals(results, list(dataset1, dataset3, dataset2));
    }
    
    @Test
    public void testFetchedFieldsScore_DataSet_TypeScore_MissingType()
    {
        DataSetType dataSetTypeA = new DataSetType();
        dataSetTypeA.setCode("DUMMY_CODE_A");
        
        String propertyCode = "DUMMY_PROPERTY";
        
        DataSetSearchCriteria c = new DataSetSearchCriteria();
        c.withOrOperator();
        c.withType().withCode().thatEquals(dataSetTypeA.getCode());
        
        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withProperties();
        fo.sortBy().fetchedFieldsScore();
        
        DataSet dataset1 = new DataSet();
        dataset1.setCode("S1");
        dataset1.setProperty(propertyCode, "DUMMY_S1");
        dataset1.setFetchOptions(fo);

        DataSet dataset2 = new DataSet();
        dataset2.setCode("S2");
        dataset2.setProperty(propertyCode, "DUMMY_S2");
        dataset2.setFetchOptions(fo);

        DataSet dataset3 = new DataSet();
        dataset3.setCode("S3");
        dataset3.setProperty(propertyCode, "DUMMY_S3");
        dataset3.setFetchOptions(fo);

        List<DataSet> datasets = new ArrayList<DataSet>();
        datasets.add(dataset1);
        datasets.add(dataset2);
        datasets.add(dataset3);

        Collection<DataSet> results = new SortAndPage().sortAndPage(datasets, c, fo);

        assertEquals(results, list(dataset1, dataset2, dataset3));
    }
    

    @Test
    public void testFetchedFieldsScore_Material_CodeScore()
    {
        MaterialType materialTypeA = new MaterialType();
        materialTypeA.setCode("DUMMY_CODE_A");
        
        MaterialType materialTypeB = new MaterialType();
        materialTypeB.setCode("DUMMY_CODE_B");
        
        String propertyCode = "DUMMY_PROPERTY";
        
        MaterialSearchCriteria c = new MaterialSearchCriteria();
        c.withOrOperator();
        c.withCode().thatEquals("S2");
        
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.withType();
        fo.withProperties();
        fo.sortBy().fetchedFieldsScore();
        
        Material sample1 = new Material();
        sample1.setType(materialTypeA);
        sample1.setCode("S1");
        sample1.setProperty(propertyCode, "DUMMY_S1");
        sample1.setFetchOptions(fo);

        Material sample2 = new Material();
        sample2.setType(materialTypeB);
        sample2.setCode("S2");
        sample2.setProperty(propertyCode, "DUMMY_S2");
        sample2.setFetchOptions(fo);

        Material sample3 = new Material();
        sample3.setType(materialTypeA);
        sample3.setCode("S3");
        sample3.setProperty(propertyCode, "DUMMY_S3");
        sample3.setFetchOptions(fo);

        List<Material> samples = new ArrayList<Material>();
        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);

        Collection<Material> results = new SortAndPage().sortAndPage(samples, c, fo);

        assertEquals(results, list(sample2, sample1, sample3));
    }
    
    @Test
    public void testFetchedFieldsScore_Material_PropertyScore()
    {
        MaterialType materialTypeA = new MaterialType();
        materialTypeA.setCode("DUMMY_CODE_A");
        
        MaterialType materialTypeB = new MaterialType();
        materialTypeB.setCode("DUMMY_CODE_B");
        
        String propertyCode = "DUMMY_PROPERTY";
        
        MaterialSearchCriteria c = new MaterialSearchCriteria();
        c.withOrOperator();
        c.withProperty(propertyCode).thatEquals("DUMMY_S3");
        
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.withType();
        fo.withProperties();
        fo.sortBy().fetchedFieldsScore();
        
        Material sample1 = new Material();
        sample1.setType(materialTypeA);
        sample1.setCode("S1");
        sample1.setProperty(propertyCode, "DUMMY_S1");
        sample1.setFetchOptions(fo);

        Material sample2 = new Material();
        sample2.setType(materialTypeB);
        sample2.setCode("S2");
        sample2.setProperty(propertyCode, "DUMMY_S2");
        sample2.setFetchOptions(fo);

        Material sample3 = new Material();
        sample3.setType(materialTypeA);
        sample3.setCode("S3");
        sample3.setProperty(propertyCode, "DUMMY_S3");
        sample3.setFetchOptions(fo);

        List<Material> samples = new ArrayList<Material>();
        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);

        Collection<Material> results = new SortAndPage().sortAndPage(samples, c, fo);

        assertEquals(results, list(sample3, sample1, sample2));
    }
    
    @Test
    public void testFetchedFieldsScore_Material_PropertyScore_MissingProperties()
    {
        MaterialType materialTypeA = new MaterialType();
        materialTypeA.setCode("DUMMY_CODE_A");
        
        MaterialType materialTypeB = new MaterialType();
        materialTypeB.setCode("DUMMY_CODE_B");
        
        String propertyCode = "DUMMY_PROPERTY";
        
        MaterialSearchCriteria c = new MaterialSearchCriteria();
        c.withOrOperator();
        c.withProperty(propertyCode).thatEquals("DUMMY_S3");
        
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.withType();
        fo.sortBy().fetchedFieldsScore();
        
        Material sample1 = new Material();
        sample1.setType(materialTypeA);
        sample1.setCode("S1");
        sample1.setFetchOptions(fo);

        Material sample2 = new Material();
        sample2.setType(materialTypeB);
        sample2.setCode("S2");
        sample2.setFetchOptions(fo);

        Material sample3 = new Material();
        sample3.setType(materialTypeA);
        sample3.setCode("S3");
        sample3.setFetchOptions(fo);

        List<Material> samples = new ArrayList<Material>();
        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);

        Collection<Material> results = new SortAndPage().sortAndPage(samples, c, fo);

        assertEquals(results, list(sample1, sample2, sample3));
    }
    
    @Test
    public void testFetchedFieldsScore_Material_TypeScore()
    {
        MaterialType materialTypeA = new MaterialType();
        materialTypeA.setCode("DUMMY_CODE_A");
        
        MaterialType materialTypeB = new MaterialType();
        materialTypeB.setCode("DUMMY_CODE_B");
        
        String propertyCode = "DUMMY_PROPERTY";
        
        MaterialSearchCriteria c = new MaterialSearchCriteria();
        c.withOrOperator();
        c.withType().withCode().thatEquals(materialTypeA.getCode());
        
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.withType();
        fo.withProperties();
        fo.sortBy().fetchedFieldsScore();
        
        Material sample1 = new Material();
        sample1.setType(materialTypeA);
        sample1.setCode("S1");
        sample1.setProperty(propertyCode, "DUMMY_S1");
        sample1.setFetchOptions(fo);

        Material sample2 = new Material();
        sample2.setType(materialTypeB);
        sample2.setCode("S2");
        sample2.setProperty(propertyCode, "DUMMY_S2");
        sample2.setFetchOptions(fo);

        Material sample3 = new Material();
        sample3.setType(materialTypeA);
        sample3.setCode("S3");
        sample3.setProperty(propertyCode, "DUMMY_S3");
        sample3.setFetchOptions(fo);

        List<Material> samples = new ArrayList<Material>();
        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);

        Collection<Material> results = new SortAndPage().sortAndPage(samples, c, fo);

        assertEquals(results, list(sample1, sample3, sample2));
    }
    
    @Test
    public void testFetchedFieldsScore_Material_TypeScore_MissingType()
    {
        MaterialType materialTypeA = new MaterialType();
        materialTypeA.setCode("DUMMY_CODE_A");
        
        String propertyCode = "DUMMY_PROPERTY";
        
        MaterialSearchCriteria c = new MaterialSearchCriteria();
        c.withOrOperator();
        c.withType().withCode().thatEquals(materialTypeA.getCode());
        
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.withProperties();
        fo.sortBy().fetchedFieldsScore();
        
        Material sample1 = new Material();
        sample1.setCode("S1");
        sample1.setProperty(propertyCode, "DUMMY_S1");
        sample1.setFetchOptions(fo);

        Material sample2 = new Material();
        sample2.setCode("S2");
        sample2.setProperty(propertyCode, "DUMMY_S2");
        sample2.setFetchOptions(fo);

        Material sample3 = new Material();
        sample3.setCode("S3");
        sample3.setProperty(propertyCode, "DUMMY_S3");
        sample3.setFetchOptions(fo);

        List<Material> samples = new ArrayList<Material>();
        samples.add(sample1);
        samples.add(sample2);
        samples.add(sample3);

        Collection<Material> results = new SortAndPage().sortAndPage(samples, c, fo);

        assertEquals(results, list(sample1, sample2, sample3));
    }
    
    @Test
    public void testTopLevel()
    {
    		MaterialSearchCriteria c = new MaterialSearchCriteria();
    		
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.sortBy().code().desc();
        fo.from(1).count(2);

        Material material1 = new Material();
        material1.setCode("M1");
        material1.setFetchOptions(fo);

        Material material2 = new Material();
        material2.setCode("M2");
        material2.setFetchOptions(fo);

        Material material3 = new Material();
        material3.setCode("M3");
        material3.setFetchOptions(fo);

        List<Material> materials = new ArrayList<Material>();
        materials.add(material1);
        materials.add(material2);
        materials.add(material3);

        Collection<Material> results = new SortAndPage().sortAndPage(materials, c, fo);

        assertEquals(results, list(material2, material1));
    }

    @Test
    public void testSubLevel()
    {
    		MaterialSearchCriteria c = new MaterialSearchCriteria();
    	
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.sortBy().code().desc();
        fo.withTags().from(1).count(1);

        Tag tag1 = new Tag();
        tag1.setCode("T1");
        Tag tag2 = new Tag();
        tag2.setCode("T2");
        Tag tag3 = new Tag();
        tag3.setCode("T3");

        Material material1 = new Material();
        material1.setCode("M1");
        material1.setTags(set(tag1, tag2));
        material1.setFetchOptions(fo);

        Material material2 = new Material();
        material2.setCode("M2");
        material2.setTags(set(tag2, tag3));
        material2.setFetchOptions(fo);

        List<Material> materials = new ArrayList<Material>();
        materials.add(material1);
        materials.add(material2);

        List<Material> results = new SortAndPage().sortAndPage(materials, c, fo);

        assertEquals(results, list(material2, material1));
        assertEquals(results.get(0).getTags(), set(tag3));
        assertEquals(results.get(1).getTags(), set(tag2));
    }

    @Test
    public void testSubLevelThroughSingleRelation()
    {
    		MaterialSearchCriteria c = new MaterialSearchCriteria();
    	
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.sortBy().code().desc();
        fo.withTags().from(1).count(1);
        fo.withRegistrator().withSpace().withProjects().from(1).count(1);
        fo.withMaterialProperties().withTags();

        Project project1 = new Project();
        project1.setFetchOptions(fo.withRegistrator().withSpace().withProjects());

        Project project2 = new Project();
        project2.setFetchOptions(fo.withRegistrator().withSpace().withProjects());

        Space space1 = new Space();
        space1.setProjects(Arrays.asList(project1, project2));
        space1.setFetchOptions(fo.withRegistrator().withSpace());

        Person person1 = new Person();
        person1.setSpace(space1);
        person1.setFetchOptions(fo.withRegistrator());

        Tag tag1 = new Tag();
        tag1.setCode("T1");
        Tag tag2 = new Tag();
        tag2.setCode("T2");
        Tag tag3 = new Tag();
        tag3.setCode("T3");

        Material material1 = new Material();
        material1.setCode("M1");
        material1.setTags(set(tag1, tag2));
        material1.setRegistrator(person1);
        material1.setFetchOptions(fo);

        Material material2 = new Material();
        material2.setCode("M2");
        material2.setTags(set(tag2, tag3));
        material2.setRegistrator(person1);
        material2.setFetchOptions(fo);

        Material material3 = new Material();
        material3.setCode("M3");
        material3.setTags(set(tag1, tag3));
        material3.setFetchOptions(fo);

        Map<String, Material> properties1 = new HashMap<String, Material>();
        properties1.put("PROPERTY_1", material1);
        properties1.put("PROPERTY_3", material3);
        material1.setMaterialProperties(properties1);

        Map<String, Material> properties2 = new HashMap<String, Material>();
        properties2.put("PROPERTY_2", material2);
        properties2.put("PROPERTY_3", material3);
        material2.setMaterialProperties(properties2);

        List<Material> materials = new ArrayList<Material>();
        materials.add(material1);
        materials.add(material2);

        List<Material> results = new SortAndPage().sortAndPage(materials, c, fo);

        assertEquals(results, list(material2, material1));

        Material material1Result = results.get(1);
        Material material2Result = results.get(0);

        assertEquals(material1Result.getTags(), set(tag2));
        assertEquals(material2Result.getTags(), set(tag3));

        assertEquals(material1Result.getRegistrator().getSpace().getProjects(), list(project2));
        assertEquals(material2Result.getRegistrator().getSpace().getProjects(), list(project2));

        assertEquals(material2Result.getMaterialProperties().get("PROPERTY_3").getTags(), set(tag1, tag3));
    }

    @Test
    public void testSortByMultipleFields()
    {
    		MaterialSearchCriteria c = new MaterialSearchCriteria();
    	
        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.sortBy().code().desc();
        fo.sortBy().registrationDate().asc();

        Material material1 = new Material();
        material1.setCode("M1");
        material1.setRegistrationDate(new Date(3));
        material1.setFetchOptions(fo);

        Material material2 = new Material();
        material2.setCode("M2");
        material2.setRegistrationDate(new Date(2));
        material2.setFetchOptions(fo);

        Material material3 = new Material();
        material3.setCode("M3");
        material3.setRegistrationDate(new Date(1));
        material3.setFetchOptions(fo);

        List<Material> materials = new ArrayList<Material>();
        materials.add(material1);
        materials.add(material2);
        materials.add(material3);

        List<Material> results = new SortAndPage().sortAndPage(materials, c, fo);

        assertEquals(results.get(0), material3);
        assertEquals(results.get(1), material2);
        assertEquals(results.get(2), material1);
    }

    @Test
    public void testSamePageMultipleTimes()
    {
    		MaterialSearchCriteria c = new MaterialSearchCriteria();
    	
        Tag tag1 = new Tag();
        tag1.setCode("T1");
        Tag tag2 = new Tag();
        tag2.setCode("T2");
        Tag tag3 = new Tag();
        tag3.setCode("T3");

        MaterialFetchOptions fo = new MaterialFetchOptions();
        fo.from(1).count(1);
        fo.withTags().from(1).count(1);

        Material material1 = new Material();
        material1.setCode("M1");
        material1.setTags(set(tag1, tag3));
        material1.setFetchOptions(fo);

        Material material2 = new Material();
        material2.setCode("M2");
        material2.setTags(set(tag2, tag3));
        material2.setFetchOptions(fo);

        List<Material> materials = new ArrayList<Material>();
        materials.add(material1);
        materials.add(material2);

        List<Material> results = new SortAndPage().sortAndPage(materials, c, fo);

        assertEquals(results, list(material2));
        assertEquals(results.get(0).getTags(), set(tag3));
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> list(T... items)
    {
        return Arrays.asList(items);
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> set(T... items)
    {
        return new LinkedHashSet<T>(Arrays.asList(items));
    }

}
