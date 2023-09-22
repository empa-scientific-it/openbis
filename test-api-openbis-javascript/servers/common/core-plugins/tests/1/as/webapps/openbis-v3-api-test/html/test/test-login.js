define([ 'jquery', 'underscore', 'openbis', 'test/common', 'test/dtos' ], function($, _, openbis, common, dtos) {
	var executeModule = function(moduleName, facade, dtos) {
		QUnit.module(moduleName);

		QUnit.test("loginAs()", function(assert) {
			var c = new common(assert, dtos);
			c.start();

			var criteria = new dtos.SpaceSearchCriteria();
			var fetchOptions = new dtos.SpaceFetchOptions();

			facade.login("openbis_test_js", "password").then(function() {
				return facade.searchSpaces(criteria, fetchOptions).then(function(spacesForInstanceAdmin) {
					return facade.loginAs("openbis_test_js", "password", "test_space_admin").then(function() {
						return facade.searchSpaces(criteria, fetchOptions).then(function(spacesForSpaceAdmin) {
							c.assertTrue(spacesForInstanceAdmin.getTotalCount() > spacesForSpaceAdmin.getTotalCount());
							c.assertObjectsWithValues(spacesForSpaceAdmin.getObjects(), "code", [ "TEST" ]);
							c.finish();
						});
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("getSessionInformation()", function(assert) {
			var c = new common(assert, dtos);
			c.start();

			facade.login("openbis_test_js", "password").then(function() {
				return facade.getSessionInformation().then(function(sessionInformation) {
					c.assertTrue(sessionInformation != null);
					c.assertTrue(sessionInformation.getPerson() != null);
					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("loginAsAnonymousUser()", function(assert) {
			var c = new common(assert, dtos);
			c.start();

			var criteria = new dtos.SpaceSearchCriteria();
			var fetchOptions = new dtos.SpaceFetchOptions();

			facade.loginAsAnonymousUser().then(function() {
				return facade.searchSpaces(criteria, fetchOptions).then(function(spaces) {
					c.assertTrue(spaces.getTotalCount() == 1)
					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

	}

	return function(){
		executeModule("Login tests (RequireJS)", new openbis(), dtos);
		executeModule("Login tests (module VAR)", new window.openbis.openbis(), window.openbis);
		executeModule("Login tests (module ESM)", new window.openbisESM.openbis(), window.openbisESM);
	}
});
