/**
 * Test searching and executing custom AS services.
 */
define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common', 'test/dtos' ], function($, _, openbis, openbisExecuteOperations, common, dtos) {
	var executeModule = function(moduleName, facade, dtos) {
		QUnit.module(moduleName);

		var testAction = function(c, fAction, fCheck) {
			c.start();

			c.login(facade).then(function() {
				c.ok("Login");
				return fAction(facade).then(function(result) {
					c.ok("Got results");
					fCheck(facade, result);
					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		QUnit.test("searchCustomASServices()", function(assert) {
			var c = new common(assert, dtos);

			var fAction = function(facade) {
				var criteria = new dtos.CustomASServiceSearchCriteria();
				criteria.withCode().thatStartsWith("simple");
				return facade.searchCustomASServices(criteria, new dtos.CustomASServiceFetchOptions());
			}

			var fCheck = function(facade, result) {
				var services = result.getObjects();
				c.assertEqual(services.length, 1);
				var service = services[0];
				c.assertEqual(service.getCode().getPermId(), "simple-service", "Code");
				c.assertEqual(service.getDescription(), "a simple service", "Description");
			}

			testAction(c, fAction, fCheck);
		});

		QUnit.test("executeCustomASService()", function(assert) {
			var c = new common(assert, dtos);

			var fAction = function(facade) {
				var id = new dtos.CustomASServiceCode("simple-service");
				var options = new dtos.CustomASServiceExecutionOptions().withParameter("a", "1").withParameter("space-code", "TEST");
				return facade.executeCustomASService(id, options);
			}

			var fCheck = function(facade, result) {
				c.assertEqual(1, result.getTotalCount());
				var space = result.getObjects()[0];
				c.assertEqual(space.getPermId(), "TEST", "PermId");
				c.assertEqual(space.getCode(), "TEST", "Code");
				c.assertEqual(space.getDescription(), null, "Description");
				c.assertDate(space.getRegistrationDate(), "Registration date", 2013, 4, 12, 12, 59);
			}

			testAction(c, fAction, fCheck);
		});
	}

	return function() {
		executeModule("Custom AS service tests (RequireJS)", new openbis(), dtos);
		executeModule("Custom AS service tests (RequireJS - executeOperations)", new openbisExecuteOperations(new openbis(), dtos), dtos);
		executeModule("Custom AS service tests (module VAR)", new window.openbis.openbis(), window.openbis);
		executeModule("Custom AS service tests (module VAR - executeOperations)", new openbisExecuteOperations(new window.openbis.openbis(), window.openbis), window.openbis);
		executeModule("Custom AS service tests (module ESM)", new window.openbisESM.openbis(), window.openbisESM);
		executeModule("Custom AS service tests (module ESM - executeOperations)", new openbisExecuteOperations(new window.openbisESM.openbis(), window.openbisESM), window.openbisESM);
	}
})