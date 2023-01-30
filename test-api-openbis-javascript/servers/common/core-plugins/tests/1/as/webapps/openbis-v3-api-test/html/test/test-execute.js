define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common' ], function($, _, openbis, openbisExecuteOperations, common) {
	var executeModule = function(moduleName, openbis) {
		QUnit.module(moduleName);

		QUnit.test("executeQuery()", function(assert) {
			var c = new common(assert, openbis);
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				var creation = new c.QueryCreation();
				creation.setName(c.generateId("query"));
				creation.setDatabaseId(new c.QueryDatabaseName("openbisDB"));
				creation.setQueryType(c.QueryType.GENERIC);
				creation.setSql("select perm_id, code from projects where perm_id = ${perm_id}");

				return facade.createQueries([ creation ]).then(function(techIds) {
					var options = new c.QueryExecutionOptions();
					options.withParameter("perm_id", "20130412150031345-203");

					return facade.executeQuery(techIds[0], options).then(function(table) {
						c.assertEqual(table.getColumns().length, 2, "Columns count");
						c.assertEqual(table.getColumns()[0].title, "perm_id", "Column[0] title");
						c.assertEqual(table.getColumns()[1].title, "code", "Column[1] title");
						c.assertEqual(table.getRows().length, 1, "Rows count");
						c.assertEqual(table.getRows()[0][0].value, "20130412150031345-203", "Value[0][0]");
						c.assertEqual(table.getRows()[0][1].value, "TEST-PROJECT", "Value[0][1]");

						c.finish();
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("executeSql()", function(assert) {
			var c = new common(assert, openbis);
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				var options = new c.SqlExecutionOptions();
				options.withDatabaseId(new c.QueryDatabaseName("openbisDB"));
				options.withParameter("perm_id", "20130412150031345-203");

				return facade.executeSql("select perm_id, code from projects where perm_id = ${perm_id}", options).then(function(table) {
					c.assertEqual(table.getColumns().length, 2, "Columns count");
					c.assertEqual(table.getColumns()[0].title, "perm_id", "Column[0] title");
					c.assertEqual(table.getColumns()[1].title, "code", "Column[1] title");
					c.assertEqual(table.getRows().length, 1, "Rows count");
					c.assertEqual(table.getRows()[0][0].value, "20130412150031345-203", "Value[0][0]");
					c.assertEqual(table.getRows()[0][1].value, "TEST-PROJECT", "Value[0][1]");

					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

	}

	return function() {
		executeModule("Execute tests", openbis);
		executeModule("Execute tests (executeOperations)", openbisExecuteOperations);
	}
});
