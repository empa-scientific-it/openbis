define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common', 'test/dtos' ], function($, _, openbis, openbisExecuteOperations, common, dtos) {
	var executeModule = function(moduleName, facade, dtos) {
		QUnit.module(moduleName);

		var testAction = function(c, fAction, actionType) {
			c.start();

			c.login(facade).then(function() {
				c.ok("Login");
				return fAction(facade).then(function(result) {
					c.ok(actionType);
					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		QUnit.test("archiveDataSets()", function(assert) {
			var c = new common(assert, dtos);

			var fAction = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					var ids = [ permId1, permId2 ];
					return facade.archiveDataSets(ids, new dtos.DataSetArchiveOptions());
				});
			}

			testAction(c, fAction, "Archived");
		});

		QUnit.test("unarchiveDataSets()", function(assert) {
			var c = new common(assert, dtos);

			var fAction = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					var ids = [ permId1, permId2 ];
					return facade.archiveDataSets(ids, new dtos.DataSetArchiveOptions()).then(function() {
						return facade.unarchiveDataSets(ids, new dtos.DataSetUnarchiveOptions());
					});
				});
			}

			testAction(c, fAction, "Unarchived");
		});

		QUnit.test("lockDataSets()", function(assert) {
			var c = new common(assert, dtos);
			
			var fAction = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					var ids = [ permId1, permId2 ];
					return facade.lockDataSets(ids, new dtos.DataSetLockOptions());
				});
			}
			
			testAction(c, fAction, "Lock");
		});
		
		QUnit.test("unlockDataSets()", function(assert) {
			var c = new common(assert, dtos);
			
			var fAction = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					var ids = [ permId1, permId2 ];
					return facade.lockDataSets(ids, new dtos.DataSetLockOptions()).then(function() {
						return facade.unlockDataSets(ids, new dtos.DataSetUnlockOptions());
					});
				});
			}
			
			testAction(c, fAction, "Unlock");
		});
		
	}

	return function() {
		executeModule("Archive/Unarchive (RequireJS)", new openbis(), dtos);
		executeModule("Archive/Unarchive (RequireJS - executeOperations)", new openbisExecuteOperations(new openbis(), dtos), dtos);
		executeModule("Archive/Unarchive (module VAR)", new window.openbis.openbis(), window.openbis);
		executeModule("Archive/Unarchive (module VAR - executeOperations)", new openbisExecuteOperations(new window.openbis.openbis(), window.openbis), window.openbis);
		executeModule("Archive/Unarchive (module ESM)", new window.openbisESM.openbis(), window.openbisESM);
		executeModule("Archive/Unarchive (module ESM - executeOperations)", new openbisExecuteOperations(new window.openbisESM.openbis(), window.openbisESM), window.openbisESM);
	}
})