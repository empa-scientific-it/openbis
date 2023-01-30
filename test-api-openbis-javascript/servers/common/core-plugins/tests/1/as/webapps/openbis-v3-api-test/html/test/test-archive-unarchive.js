define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common' ], function($, _, openbis, openbisExecuteOperations, common) {
	var executeModule = function(moduleName, openbis) {
		QUnit.module(moduleName);

		var testAction = function(c, fAction, actionType) {
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
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
			var c = new common(assert, openbis);

			var fAction = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					var ids = [ permId1, permId2 ];
					return facade.archiveDataSets(ids, new c.DataSetArchiveOptions());
				});
			}

			testAction(c, fAction, "Archived");
		});

		QUnit.test("unarchiveDataSets()", function(assert) {
			var c = new common(assert, openbis);

			var fAction = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					var ids = [ permId1, permId2 ];
					return facade.archiveDataSets(ids, new c.DataSetArchiveOptions()).then(function() {
						return facade.unarchiveDataSets(ids, new c.DataSetUnarchiveOptions());
					});
				});
			}

			testAction(c, fAction, "Unarchived");
		});

		QUnit.test("lockDataSets()", function(assert) {
			var c = new common(assert, openbis);
			
			var fAction = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					var ids = [ permId1, permId2 ];
					return facade.lockDataSets(ids, new c.DataSetLockOptions());
				});
			}
			
			testAction(c, fAction, "Lock");
		});
		
		QUnit.test("unlockDataSets()", function(assert) {
			var c = new common(assert, openbis);
			
			var fAction = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					var ids = [ permId1, permId2 ];
					return facade.lockDataSets(ids, new c.DataSetLockOptions()).then(function() {
						return facade.unlockDataSets(ids, new c.DataSetUnlockOptions());
					});
				});
			}
			
			testAction(c, fAction, "Unlock");
		});
		
	}

	return function() {
		executeModule("Archive/Unarchive", openbis);
		executeModule("Archive/Unarchive (executeOperations)", openbisExecuteOperations);
	}
})