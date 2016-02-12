define([ 'jquery', 'underscore', 'openbis', 'test/common', 'test/naturalsort' ], function($, _, openbis, common, naturalsort) {
	return function() {
		QUnit.module("Search tests");

		var testSearch = function(c, fSearch, fCheck) {
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				c.ok("Login");
				return fSearch(facade).then(function(results) {
					c.ok("Got results");
					fCheck(facade, results.getObjects());
					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		var testSearchWithPagingAndSortingByAll = function(c, fSearch, fetchOptions) {
			testSearchWithPagingAndSorting(c, fSearch, fetchOptions, "code").then(function() {
				testSearchWithPagingAndSorting(c, fSearch, fetchOptions, "registrationDate").then(function() {
					testSearchWithPagingAndSorting(c, fSearch, fetchOptions, "modificationDate");
				});
			});
		}

		var testSearchWithPagingAndSorting = function(c, fSearch, fetchOptions, fieldName, fieldParameters) {
			c.start();

			fetchOptions.from(null).count(null);
			fetchOptions.sort = null;

			return c.createFacadeAndLogin().then(function(facade) {
				c.ok("Login");

				return fSearch(facade).then(function(results) {
					var objects = results.getObjects();

					c.assertTrue(objects.length > 1, "Got at least 2 objects");

					c.ok("Sorting by " + fieldName);

					var fieldGetterName = "get" + fieldName.substr(0, 1).toUpperCase() + fieldName.substr(1);

					var comparatorAsc = function(o1, o2) {
						var v1 = o1[fieldGetterName](fieldParameters);
						var v2 = o2[fieldGetterName](fieldParameters);
						return naturalsort(v1, v2);
					};

					var comparatorDesc = function(o1, o2) {
						return comparatorAsc(o2, o1);
					};

					objects.sort(comparatorAsc);
					var codesAsc = objects.map(function(object) {
						return object.code;
					});

					objects.sort(comparatorDesc);
					var codesDesc = objects.map(function(object) {
						return object.code;
					});

					fetchOptions.from(1).count(1);
					fetchOptions.sortBy()[fieldName](fieldParameters);

					return fSearch(facade).then(function(results) {
						c.ok("Got results ASC");
						c.assertObjectsWithValues(results.getObjects(), "code", [ codesAsc[1] ]);
						fetchOptions.sortBy()[fieldName](fieldParameters).desc();

						return fSearch(facade).then(function(results) {
							c.ok("Got results DESC");
							c.assertObjectsWithValues(results.getObjects(), "code", [ codesDesc[1] ]);
							c.finish();
						});
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		QUnit.test("searchSpaces()", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.SpaceSearchCriteria();
				criteria.withCode().thatEquals("TEST");
				return facade.searchSpaces(criteria, c.createSpaceFetchOptions());
			}

			var fCheck = function(facade, spaces) {
				c.assertEqual(spaces.length, 1);
				var space = spaces[0];
				c.assertEqual(space.getPermId(), "TEST", "PermId");
				c.assertEqual(space.getCode(), "TEST", "Code");
				c.assertEqual(space.getDescription(), null, "Description");
				c.assertDate(space.getRegistrationDate(), "Registration date", 2013, 04, 12, 12, 59);
				c.assertEqual(space.getRegistrator().getUserId(), "admin", "Registrator userId");
				c.assertObjectsWithCollections(space, function(object) {
					return object.getSamples()
				});
				c.assertObjectsWithCollections(space, function(object) {
					return object.getProjects()
				});
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSpaces() with paging and sorting", function(assert) {
			var c = new common(assert);

			var criteria = new c.SpaceSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("TEST");
			criteria.withCode().thatEquals("PLATONIC");

			var fo = c.createSpaceFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchSpaces(criteria, fo);
			}, fo);
		});

		QUnit.test("searchProjects()", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.ProjectSearchCriteria();
				criteria.withSpace().withCode().thatEquals("PLATONIC");
				criteria.withCode().thatEquals("SCREENING-EXAMPLES");
				return facade.searchProjects(criteria, c.createProjectFetchOptions());
			}

			var fCheck = function(facade, projects) {
				c.assertEqual(projects.length, 1);
				var project = projects[0];
				c.assertEqual(project.getPermId().getPermId(), "20130412103942912-1", "PermId");
				c.assertEqual(project.getIdentifier().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES", "Identifier");
				c.assertEqual(project.getCode(), "SCREENING-EXAMPLES", "Code");
				c.assertEqual(project.getDescription(), null, "Description");
				c.assertDate(project.getRegistrationDate(), "Registration date", 2013, 4, 12, 8, 39);
				c.assertObjectsWithCollections(project, function(object) {
					return object.getExperiments()
				});
				c.assertEqual(project.getSpace().getCode(), "PLATONIC", "Space code");
				c.assertEqual(project.getRegistrator().getUserId(), "admin", "Registrator userId");
				c.assertEqual(project.getLeader(), null, "Leader");
				c.assertObjectsWithoutCollections(project, function(object) {
					return object.getAttachments()
				});
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchProjects() with paging and sorting", function(assert) {
			var c = new common(assert);

			var criteria = new c.ProjectSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("TEST-PROJECT");
			criteria.withCode().thatEquals("SCREENING-EXAMPLES");

			var fo = c.createProjectFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchProjects(criteria, fo);
			}, fo);
		});

		QUnit.test("searchExperiments()", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.ExperimentSearchCriteria();
				criteria.withPermId().thatEquals("20130412105232616-2");
				return facade.searchExperiments(criteria, c.createExperimentFetchOptions());
			}

			var fCheck = function(facade, experiments) {
				c.assertEqual(experiments.length, 1);
				var experiment = experiments[0];
				c.assertEqual(experiment.getCode(), "EXP-1", "Experiment code");
				c.assertEqual(experiment.getType().getCode(), "HCS_PLATONIC", "Type code");
				c.assertEqual(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
				c.assertEqual(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchExperiments() with paging and sorting", function(assert) {
			var c = new common(assert);

			var criteria = new c.ExperimentSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("EXP-1");
			criteria.withCode().thatEquals("EXP-2");

			var fo = c.createExperimentFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchExperiments(criteria, fo);
			}, fo);
		});

		QUnit.test("searchSamples()", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.SampleSearchCriteria();
				criteria.withPermId().thatEquals("20130415095748527-404");
				return facade.searchSamples(criteria, c.createSampleFetchOptions());
			}

			var fCheck = function(facade, samples) {
				c.assertEqual(samples.length, 1);
				var sample = samples[0];
				c.assertEqual(sample.code, "TEST-SAMPLE-2-PARENT", "Sample code");
				c.assertEqual(sample.type.code, "UNKNOWN", "Type code");
				c.assertEqual(sample.experiment.code, "TEST-EXPERIMENT-2", "Experiment code");
				c.assertEqual(sample.experiment.project.code, "TEST-PROJECT", "Project code");
				c.assertEqual(sample.space.code, "TEST", "Space code");
				c.assertNotEqual(sample.children, null, "Children expected");
				if (sample.children !== null) {
					console.log("Children %s", sample.children);
					var child = sample.children[0];
					c.assertEqual(sample.children.length, 1, "Number of children");
					c.assertEqual(child.code, "TEST-SAMPLE-2", "Child sample code");
					c.assertEqual(child.type.code, "UNKNOWN", "Child type code");
					c.assertEqual(child.experiment.code, "TEST-EXPERIMENT-2", "Child experiment code");
					c.assertNotEqual(child.children, null, "Grand children expected");
					if (child.children !== null) {
						c.assertEqual(child.children.length, 2, "Number of grand children");
					}
				}
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSamples() with paging and sorting", function(assert) {
			var c = new common(assert);

			var criteria = new c.SampleSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("TEST-SAMPLE-1");
			criteria.withCode().thatEquals("TEST-SAMPLE-2");

			var fo = c.createSampleFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchSamples(criteria, fo);
			}, fo);
		});

		QUnit.test("searchSamples() withoutExperiment", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.SampleSearchCriteria();
				criteria.withCode().thatStartsWith("TEST-SAMPLE");
				criteria.withoutExperiment();
				return facade.searchSamples(criteria, c.createSampleFetchOptions());
			}

			var fCheck = function(facade, samples) {
				c.assertObjectsWithValues(samples, "code", [ "TEST-SAMPLE-1-CONTAINED-1", "TEST-SAMPLE-1-CONTAINED-2", "TEST-SAMPLE-1" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSamples() withExperiment", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.SampleSearchCriteria();
				criteria.withCode().thatStartsWith("TEST-SAMPLE");
				criteria.withExperiment();
				return facade.searchSamples(criteria, c.createSampleFetchOptions());
			}

			var fCheck = function(facade, samples) {
				c.assertObjectsWithValues(samples, "code", [ "TEST-SAMPLE-2-CHILD-2", "TEST-SAMPLE-2-CHILD-1", "TEST-SAMPLE-2-PARENT", "TEST-SAMPLE-2" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSamples() withoutContainer", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.SampleSearchCriteria();
				criteria.withCode().thatStartsWith("TEST-SAMPLE");
				criteria.withoutContainer();
				return facade.searchSamples(criteria, c.createSampleFetchOptions());
			}

			var fCheck = function(facade, samples) {
				c.assertObjectsWithValues(samples, "code", [ "TEST-SAMPLE-2", "TEST-SAMPLE-1", "TEST-SAMPLE-2-PARENT", "TEST-SAMPLE-2-CHILD-1", "TEST-SAMPLE-2-CHILD-2" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSamples() withContainer", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.SampleSearchCriteria();
				criteria.withCode().thatStartsWith("TEST-SAMPLE");
				criteria.withContainer();
				return facade.searchSamples(criteria, c.createSampleFetchOptions());
			}

			var fCheck = function(facade, samples) {
				c.assertObjectsWithValues(samples, "code", [ "TEST-SAMPLE-1-CONTAINED-1", "TEST-SAMPLE-1-CONTAINED-2" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets()", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withPermId().thatEquals("20130415093804724-403");
				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertEqual(dataSets.length, 1);
				var dataSet = dataSets[0];
				c.assertEqual(dataSet.getCode(), "20130415093804724-403", "Code");
				c.assertEqual(dataSet.getType().getCode(), "UNKNOWN", "Type code");
				c.assertEqual(dataSet.getExperiment().getCode(), "TEST-EXPERIMENT-2", "Experiment code");
				c.assertEqual(dataSet.getSample().getCode(), "TEST-SAMPLE-2", "Sample code");
				c.assertEqual(dataSet.getProperties()["DESCRIPTION"], "403 description", "Property DESCRIPTION");
				c.assertEqual(dataSet.isPostRegistered(), true, "post registered");

				var physicalData = dataSet.getPhysicalData();
				c.assertEqual(physicalData.getShareId(), "1", "Share id");
				c.assertEqual(physicalData.getLocation(), "1FD3FF61-1576-4908-AE3D-296E60B4CE06/06/e5/ad/20130415093804724-403", "Location");
				c.assertEqual(physicalData.getStatus(), "AVAILABLE", "Status");
				c.assertEqual(physicalData.getFileFormatType().getCode(), "PROPRIETARY", "File format type");
				c.assertEqual(physicalData.getLocatorType().getCode(), "RELATIVE_LOCATION", "Locator type");

				c.assertObjectsWithValues(dataSet.getParents(), "code", [ "20130415100158230-407" ]);
				c.assertObjectsWithValues(dataSet.getChildren(), "code", [ "20130415100238098-408", "20130415100308111-409" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets() with paging and sorting", function(assert) {
			var c = new common(assert);

			var criteria = new c.DataSetSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("20130412142205843-196");
			criteria.withCode().thatEquals("20130412142543232-197");

			var fo = c.createDataSetFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchDataSets(criteria, fo);
			}, fo);
		});

		QUnit.test("searchDataSets() with sorting by property", function(assert) {
			var c = new common(assert);

			var criteria = new c.DataSetSearchCriteria();
			criteria.withOrOperator();
			criteria.withPermId().thatEquals("20130412142543232-197");
			criteria.withPermId().thatEquals("20130412142205843-196");
			criteria.withPermId().thatEquals("20130412142942295-198");

			var fo = c.createDataSetFetchOptions();

			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchDataSets(criteria, fo);
			}, fo, "property", "RESOLUTION");
		});

		QUnit.test("searchDataSets() withoutSample", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withCode().thatContains("-40");
				criteria.withoutSample();
				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20130415100158230-407", "20130415100308111-409" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets() withSample", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withCode().thatContains("-40");
				criteria.withSample();
				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20130415093804724-403", "20130415100238098-408" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets() withoutExperiment", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withCode().thatContains("-40");
				criteria.withoutExperiment();
				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20130415100238098-408" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets() withExperiment", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withCode().thatContains("-40");
				criteria.withExperiment();
				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20130415093804724-403", "20130415100158230-407", "20130415100308111-409" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchMaterials()", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.MaterialSearchCriteria();
				criteria.withCode().thatEquals("H2O");
				return facade.searchMaterials(criteria, c.createMaterialFetchOptions());
			}

			var fCheck = function(facade, materials) {
				c.assertEqual(materials.length, 1);
				var material = materials[0];
				c.assertEqual(material.getCode(), "H2O", "Code");
				c.assertEqual(material.getType().getCode(), "COMPOUND", "Type code");
				var properties = material.getProperties();
				c.assertEqual(Object.keys(properties), "DESCRIPTION", "Water");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchMaterials() with paging and sorting", function(assert) {
			var c = new common(assert);

			var criteria = new c.MaterialSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("ABC");
			criteria.withCode().thatEquals("SIRNA-2");

			var fo = c.createMaterialFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchMaterials(criteria, fo);
			}, fo);
		});

		QUnit.test("searchGlobally() withText thatContains", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.GlobalSearchCriteria();
				criteria.withText().thatContains("20130412150049446-204 20130412140147735-20 20130417094936021-428 H2O");
				return facade.searchGlobally(criteria, c.createGlobalSearchObjectFetchOptions());
			}

			var fCheck = function(facade, objects) {
				c.assertEqual(objects.length, 4);

				var object0 = objects[0];
				c.assertEqual(object0.getObjectKind(), "EXPERIMENT", "ObjectKind");
				c.assertEqual(object0.getObjectPermId().getPermId(), "20130412150049446-204", "ObjectPermId");
				c.assertEqual(object0.getObjectIdentifier().getIdentifier(), "/TEST/TEST-PROJECT/TEST-EXPERIMENT", "ObjectIdentifier");
				c.assertEqual(object0.getMatch(), "Perm ID: 20130412150049446-204", "Match");
				c.assertNotNull(object0.getScore(), "Score");
				c.assertEqual(object0.getExperiment().getCode(), "TEST-EXPERIMENT", "Experiment");
				c.assertNull(object0.getSample(), "Sample");
				c.assertNull(object0.getDataSet(), "DataSet");
				c.assertNull(object0.getMaterial(), "Material");

				var object1 = objects[1];
				c.assertEqual(object1.getObjectKind(), "SAMPLE", "ObjectKind");
				c.assertEqual(object1.getObjectPermId().getPermId(), "20130412140147735-20", "ObjectPermId");
				c.assertEqual(object1.getObjectIdentifier().getIdentifier(), "/PLATONIC/PLATE-1", "ObjectIdentifier");
				c.assertEqual(object1.getMatch(), "Perm ID: 20130412140147735-20", "Match");
				c.assertNotNull(object1.getScore(), "Score");
				c.assertNull(object1.getExperiment(), "Experiment");
				c.assertEqual(object1.getSample().getCode(), "PLATE-1", "Sample");
				c.assertNull(object1.getDataSet(), "DataSet");
				c.assertNull(object1.getMaterial(), "Material");

				var object2 = objects[2];
				c.assertEqual(object2.getObjectKind(), "DATA_SET", "ObjectKind");
				c.assertEqual(object2.getObjectPermId().getPermId(), "20130417094936021-428", "ObjectPermId");
				c.assertEqual(object2.getObjectIdentifier().getPermId(), "20130417094936021-428", "ObjectIdentifier");
				c.assertEqual(object2.getMatch(), "Perm ID: 20130417094936021-428", "Match");
				c.assertNotNull(object2.getScore(), "Score");
				c.assertNull(object2.getExperiment(), "Experiment");
				c.assertNull(object2.getSample(), "Sample");
				c.assertEqual(object2.getDataSet().getCode(), "20130417094936021-428", "DataSet");
				c.assertNull(object2.getMaterial(), "Material");

				var object3 = objects[3];
				c.assertEqual(object3.getObjectKind(), "MATERIAL", "ObjectKind");
				c.assertEqual(object3.getObjectPermId().getCode(), "H2O", "ObjectPermId 1");
				c.assertEqual(object3.getObjectPermId().getTypeCode(), "COMPOUND", "ObjectPermId 2");
				c.assertEqual(object3.getObjectIdentifier().getCode(), "H2O", "ObjectIdentifier 1");
				c.assertEqual(object3.getObjectIdentifier().getTypeCode(), "COMPOUND", "ObjectIdentifier 2");
				c.assertEqual(object3.getMatch(), "Identifier: H2O", "Match");
				c.assertNotNull(object3.getScore(), "Score");
				c.assertNull(object3.getExperiment(), "Experiment");
				c.assertNull(object3.getSample(), "Sample");
				c.assertNull(object3.getDataSet(), "DataSet");
				c.assertEqual(object3.getMaterial().getCode(), "H2O", "Material");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchGlobally() withText thatContainsExactly", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.GlobalSearchCriteria();
				criteria.withText().thatContainsExactly("407 description");
				return facade.searchGlobally(criteria, c.createGlobalSearchObjectFetchOptions());
			}

			var fCheck = function(facade, objects) {
				c.assertEqual(objects.length, 1);

				var object0 = objects[0];
				c.assertEqual(object0.getObjectKind(), "DATA_SET", "ObjectKind");
				c.assertEqual(object0.getObjectPermId().getPermId(), "20130415100158230-407", "ObjectPermId");
				c.assertEqual(object0.getObjectIdentifier().getPermId(), "20130415100158230-407", "ObjectIdentifier");
				c.assertEqual(object0.getMatch(), "Property 'Description': 407 description", "Match");
				c.assertNotNull(object0.getScore(), "Score");
				c.assertNull(object0.getExperiment(), "Experiment");
				c.assertNull(object0.getSample(), "Sample");
				c.assertEqual(object0.getDataSet().getCode(), "20130415100158230-407", "DataSet");
				c.assertNull(object0.getMaterial(), "Material");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchGlobally() withObjectKind thatIn", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.GlobalSearchCriteria();
				criteria.withText().thatContains("20130412150049446-204 20130412140147735-20 20130417094936021-428 H2O");
				criteria.withObjectKind().thatIn(["EXPERIMENT"]);
				return facade.searchGlobally(criteria, c.createGlobalSearchObjectFetchOptions());
			}

			var fCheck = function(facade, objects) {
				c.assertEqual(objects.length, 1);

				var object0 = objects[0];
				c.assertEqual(object0.getObjectKind(), "EXPERIMENT", "ObjectKind");
				c.assertEqual(object0.getObjectPermId().getPermId(), "20130412150049446-204", "ObjectPermId");
				c.assertEqual(object0.getObjectIdentifier().getIdentifier(), "/TEST/TEST-PROJECT/TEST-EXPERIMENT", "ObjectIdentifier");
				c.assertEqual(object0.getMatch(), "Perm ID: 20130412150049446-204", "Match");
				c.assertNotNull(object0.getScore(), "Score");
				c.assertEqual(object0.getExperiment().getCode(), "TEST-EXPERIMENT", "Experiment");
				c.assertNull(object0.getSample(), "Sample");
				c.assertNull(object0.getDataSet(), "DataSet");
				c.assertNull(object0.getMaterial(), "Material");
			}

			testSearch(c, fSearch, fCheck);
		});

	}
});
