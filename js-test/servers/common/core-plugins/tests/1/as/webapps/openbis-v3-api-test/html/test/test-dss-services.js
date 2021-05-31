/**
 * Test searching and executing DSS services.
 */
define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common' ], function($, _, openbis, openbisExecuteOperations, common) {
	var executeModule = function(moduleName, openbis) {
		QUnit.module(moduleName);

		var testAction = function(c, fAction, fCheck) {
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				c.ok("Login");
				return fAction(facade).then(function(result) {
					c.ok("Got results");
					var token = fCheck(facade, result);
					if (token) {
						token.then(function() {
							c.finish()
						});
					} else {
						c.finish();
					}
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		var testActionWhichShouldFail = function(c, fAction, errorMessage) {
			c.start();
			
			c.createFacadeAndLogin().then(function(facade) {
				c.ok("Login");
				return fAction(facade).then(function(result) {
					c.fail("Action supposed to fail");
					c.finish();
				});
			}).fail(function(error) {
				c.ok("Action failed as expected");
				c.assertEqual(error.message, errorMessage, "Error message");
				c.finish();
			});
		}
		
		QUnit.test("searchSearchDomainService()", function(assert) {
			var c = new common(assert, openbis);

			var fAction = function(facade) {
				var criteria = new c.SearchDomainServiceSearchCriteria();
				var fetchOptions = new c.SearchDomainServiceFetchOptions();
				return facade.searchSearchDomainServices(criteria, fetchOptions);
			}

			var fCheck = function(facade, result) {
				c.assertEqual(result.getTotalCount(), 4, "Number of results");
				c.assertEqual(result.getObjects().length, 4, "Number of results");
				var objects = result.getObjects();
				objects.sort(function(o1, o2) { return o1.getPermId().toString().localeCompare(o2.getPermId().toString())});
				c.assertEqual(objects[1].getPermId().toString(), "DSS1:echo-database", "Perm id");
				c.assertEqual(objects[1].getName(), "echo-database", "Name");
				c.assertEqual(objects[1].getLabel(), "Echo database", "Label");
				c.assertEqual(objects[1].getPossibleSearchOptionsKey(), "optionsKey", "Possible searcg option keys");
				c.assertEqual(objects[1].getPossibleSearchOptions().toString(), "Alpha [alpha],beta [beta]", "Possible search options");
			}

			testAction(c, fAction, fCheck);
		});
		
		QUnit.test("executeSearchDomainService()", function(assert) {
			var c = new common(assert, openbis);
			
			var fAction = function(facade) {
				var options = new c.SearchDomainServiceExecutionOptions();
				options.withPreferredSearchDomain("echo-database");
				options.withSearchString("key").withParameter("key", 
						JSON.stringify({
							"searchDomain" : "Echo database",
							"dataSetCode" : "20130415093804724-403",
							"pathInDataSet" : "PATH-2",
							"sequenceIdentifier" : "ID-2",
							"positionInSequence" : "2"
						}));
				return facade.executeSearchDomainService(options);
			}
			
			var fCheck = function(facade, result) {
				c.assertEqual(result.getTotalCount(), 2, "Number of results");
				c.assertEqual(result.getObjects().length, 2, "Number of results");
				var objects = result.getObjects();
				objects.sort(function(o1, o2) { return o1.getServicePermId().toString().localeCompare(o2.getServicePermId().toString())});
				c.assertEqual(objects[0].getServicePermId().toString(), "DSS1:echo-database", "Service perm id");
				c.assertEqual(objects[0].getSearchDomainName(), "echo-database", "Search domain name");
				c.assertEqual(objects[0].getSearchDomainLabel(), "Echo database", "Search domain label");
				c.assertEqual(objects[0].getEntityIdentifier(), "20130415093804724-403", "Entity identifier");
				c.assertEqual(objects[0].getEntityKind(), "DATA_SET", "Entity kind");
				c.assertEqual(objects[0].getEntityType(), "UNKNOWN", "Entity type");
				c.assertEqual(objects[0].getEntityPermId(), "20130415093804724-403", "Entity perm id");
				c.assertEqual(JSON.stringify(objects[0].getResultDetails()), JSON.stringify({"identifier": "ID-2",
					"path_in_data_set": "PATH-2", "position": "2"}), "Result details");
			}
			
			testAction(c, fAction, fCheck);
		});
		
		QUnit.test("searchAggregationService()", function(assert) {
			var c = new common(assert, openbis);

			var fAction = function(facade) {
				var criteria = new c.AggregationServiceSearchCriteria();
				var id = new c.DssServicePermId("js-test", new c.DataStorePermId("DSS1"));
				criteria.withId().thatEquals(id);
				var fetchOptions = new c.AggregationServiceFetchOptions();
				return facade.searchAggregationServices(criteria, fetchOptions);
			}

			var fCheck = function(facade, result) {
				c.assertEqual(result.getTotalCount(), 1, "Number of results");
				c.assertEqual(result.getObjects().length, 1, "Number of results");
				var objects = result.getObjects();
				c.assertEqual(objects[0].getPermId().toString(), "DSS1:js-test", "Perm id");
				c.assertEqual(objects[0].getName(), "js-test", "Name");
				c.assertEqual(objects[0].getLabel(), "js-test", "Label");
			}

			testAction(c, fAction, fCheck);
		});
		
		QUnit.test("executeAggregationService()", function(assert) {
			var c = new common(assert, openbis);
			
			var fAction = function(facade) {
				var id = new c.DssServicePermId("js-test", new c.DataStorePermId("DSS1"));
				var options = new c.AggregationServiceExecutionOptions();
				options.withParameter("method", "test");
				options.withParameter("answer", 42).withParameter("pi", 3.1415926);
				return facade.executeAggregationService(id, options);
			}
			
			var fCheck = function(facade, tableModel) {
				c.assertEqual(tableModel.getColumns().toString(), "key,value", "Table columns");
				c.assertEqual(tableModel.getRows().toString(), "method,test,answer,42,pi,3.1415926", "Table rows");
			}
			
			testAction(c, fAction, fCheck);
		});
		
		QUnit.test("executeAggregationService() with data store code is null", function(assert) {
			var c = new common(assert, openbis);
			
			var fAction = function(facade) {
				var id = new c.DssServicePermId("js-test", new c.DataStorePermId(null));
				var options = new c.AggregationServiceExecutionOptions();
				return facade.executeAggregationService(id, options);
			}
			
			testActionWhichShouldFail(c, fAction, "Data store code cannot be empty. (Context: [])");
		});
		
		QUnit.test("executeAggregationService() with data store id is null", function(assert) {
			var c = new common(assert, openbis);
			
			var fAction = function(facade) {
				var id = new c.DssServicePermId("js-test", null);
				var options = new c.AggregationServiceExecutionOptions();
				return facade.executeAggregationService(id, options);
			}
			
			testActionWhichShouldFail(c, fAction, "Data store id cannot be null. (Context: [])");
		});
		
		QUnit.test("executeAggregationService() with key is null", function(assert) {
			var c = new common(assert, openbis);
			
			var fAction = function(facade) {
				var id = new c.DssServicePermId(null, new c.DataStorePermId("DSS1"));
				var options = new c.AggregationServiceExecutionOptions();
				return facade.executeAggregationService(id, options);
			}
			
			testActionWhichShouldFail(c, fAction, "Service key cannot be empty. (Context: [])");
		});
		
		QUnit.test("searchReportingService()", function(assert) {
			var c = new common(assert, openbis);

			var fAction = function(facade) {
				var criteria = new c.ReportingServiceSearchCriteria();
				var id = new c.DssServicePermId("test-reporting-service", new c.DataStorePermId("DSS1"));
				criteria.withId().thatEquals(id);
				var fetchOptions = new c.ReportingServiceFetchOptions();
				return facade.searchReportingServices(criteria, fetchOptions);
			}

			var fCheck = function(facade, result) {
				c.assertEqual(result.getTotalCount(), 1, "Number of results");
				c.assertEqual(result.getObjects().length, 1, "Number of results");
				var objects = result.getObjects();
				c.assertEqual(objects[0].getPermId().toString(), "DSS1:test-reporting-service", "Perm id");
				c.assertEqual(objects[0].getName(), "test-reporting-service", "Name");
				c.assertEqual(objects[0].getLabel(), "Test Jython Reporting", "Label");
			}

			testAction(c, fAction, fCheck);
		});
		
		QUnit.test("executeReportingService()", function(assert) {
			var c = new common(assert, openbis);
			var dataSetCode;
			
			var fAction = function(facade) {
				return $.when(c.createDataSet(facade)).then(function(permId) {
					dataSetCode = permId.getPermId();
					var serviceId = new c.DssServicePermId("test-reporting-service", new c.DataStorePermId("DSS1"));
					var options = new c.ReportingServiceExecutionOptions();
					options.withDataSets(dataSetCode);
					return facade.executeReportingService(serviceId, options);
				});
			}
			
			var fCheck = function(facade, tableModel) {
				c.assertEqual(tableModel.getColumns().toString(), "Data Set,Data Set Type", "Table columns");
				c.assertEqual(tableModel.getRows().toString(), dataSetCode + ",ALIGNMENT", "Table rows");
			}
			
			testAction(c, fAction, fCheck);
		});
		
		QUnit.test("searchProcessingService()", function(assert) {
			var c = new common(assert, openbis);

			var fAction = function(facade) {
				var criteria = new c.ProcessingServiceSearchCriteria();
				var id = new c.DssServicePermId("test-processing-service", new c.DataStorePermId("DSS1"));
				criteria.withId().thatEquals(id);
				var fetchOptions = new c.ProcessingServiceFetchOptions();
				return facade.searchProcessingServices(criteria, fetchOptions);
			}

			var fCheck = function(facade, result) {
				c.assertEqual(result.getTotalCount(), 1, "Number of results");
				c.assertEqual(result.getObjects().length, 1, "Number of results");
				var objects = result.getObjects();
				c.assertEqual(objects[0].getPermId().toString(), "DSS1:test-processing-service", "Perm id");
				c.assertEqual(objects[0].getName(), "test-processing-service", "Name");
				c.assertEqual(objects[0].getLabel(), "Test Jython Processing", "Label");
			}

			testAction(c, fAction, fCheck);
		});
		
		QUnit.test("executeProcessingService()", function(assert) {
			var c = new common(assert, openbis);
			var dataSetCode;
			
			var fAction = function(facade) {
				return $.when(c.createDataSet(facade)).then(function(permId) {
					dataSetCode = permId.getPermId();
					var serviceId = new c.DssServicePermId("test-processing-service", new c.DataStorePermId("DSS1"));
					var options = new c.ProcessingServiceExecutionOptions();
					options.withDataSets([dataSetCode]);
					return facade.executeProcessingService(serviceId, options);
				});
			}
			
			var fCheck = function(facade) {
				return $.when(c.waitUntilEmailWith(facade, dataSetCode, 10000).then(function(emails) {
					c.assertEqual(emails[0][0].value, "franz-josef.elmer@systemsx.ch", "Email To");
					c.assertEqual(emails[0][1].value, "'Test Jython Processing' [test-processing-service] processing\n finished", "Email Subject");
					c.assertContains(emails[0][2].value, dataSetCode, "Email Content with data set " + dataSetCode);
				}));
			}
			
			testAction(c, fAction, fCheck);
		});
		
	}

	return function() {
		executeModule("DSS service tests", openbis);
		executeModule("DSS service tests (executeOperations)", openbisExecuteOperations);
	}
})