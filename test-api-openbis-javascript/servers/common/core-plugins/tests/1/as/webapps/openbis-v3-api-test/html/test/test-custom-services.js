/**
 * Test searching and executing custom AS services.
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
					fCheck(facade, result);
					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		QUnit.test("searchCustomASServices()", function(assert) {
			var c = new common(assert, openbis);

			var fAction = function(facade) {
				var criteria = new c.CustomASServiceSearchCriteria();
				criteria.withCode().thatStartsWith("simple");
				return facade.searchCustomASServices(criteria, new c.CustomASServiceFetchOptions());
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
			var c = new common(assert, openbis);

			var fAction = function(facade) {
				var id = new c.CustomASServiceCode("simple-service");
				var options = new c.CustomASServiceExecutionOptions().withParameter("a", "1").withParameter("space-code", "TEST");
				return facade.executeCustomASService(id, options);
			}

			var fCheck = function(facade, result) {
				c.assertEqual(1, result.getTotalCount());
				var space = result.getObjects()[0];
				c.assertEqual(space.getPermId(), "TEST", "PermId");
				c.assertEqual(space.getCode(), "TEST", "Code");
				c.assertEqual(space.getDescription(), null, "Description");
				c.assertDate(space.getRegistrationDate(), "Registration date", 2013, 04, 12, 12, 59);
			}

			testAction(c, fAction, fCheck);
		});
	}

	return function() {
		executeModule("Custom AS service tests", openbis);
		executeModule("Custom AS service tests (executeOperations)", openbisExecuteOperations);
	}
})