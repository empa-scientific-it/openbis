define([
  "jquery",
  "underscore",
  "openbis",
  "test/openbis-execute-operations",
  "test/common",
], function ($, _, openbis, openbisExecuteOperations, common) {
  var executeModule = function (moduleName, openbis) {
    QUnit.module(moduleName);

    var testDynamicPropertyPlugin = function (assert, databasePlugin) {
      var c = new common(assert, openbis);
      c.start();

      c.createFacadeAndLogin()
        .then(function (facade) {
          var creation = new c.PluginCreation();
          creation.setName(c.generateId("plugin"));
          creation.setPluginType(c.PluginType.DYNAMIC_PROPERTY);
          creation.setScript("def calculate():\n  return 'testValue'");

          return $.when(
            facade.createPlugins([creation]),
            c.createSample(facade)
          ).then(function (pluginIds, sampleId) {
            var options = new c.DynamicPropertyPluginEvaluationOptions();
            if (databasePlugin) {
              options.setPluginId(pluginIds[0]);
            } else {
              options.setPluginScript(creation.getScript());
            }
            options.setObjectId(sampleId);

            return facade.evaluatePlugin(options).then(function (result) {
              c.assertEqual(
                result.getValue(),
                "testValue",
                "Evaluation result value"
              );
              c.finish();
            });
          });
        })
        .fail(function (error) {
          c.fail(error.message);
          c.finish();
        });
    };

    var testEntityValidationPlugin = function (assert, databasePlugin) {
      var c = new common(assert, openbis);
      c.start();

      c.createFacadeAndLogin()
        .then(function (facade) {
          var creation = new c.PluginCreation();
          creation.setName(c.generateId("plugin"));
          creation.setPluginType(c.PluginType.ENTITY_VALIDATION);
          creation.setScript(
            "def validate(entity, isNew):\n  requestValidation(entity)\n  if isNew:\n    return 'testError'\n  else:\n    return None"
          );

          return $.when(
            facade.createPlugins([creation]),
            c.createSample(facade)
          ).then(function (pluginIds, sampleId) {
            var options = new c.EntityValidationPluginEvaluationOptions();
            if (databasePlugin) {
              options.setPluginId(pluginIds[0]);
            } else {
              options.setPluginScript(creation.getScript());
            }
            options.setNew(true);
            options.setObjectId(sampleId);

            return $.when(
              facade.evaluatePlugin(options),
              c.findSample(facade, sampleId)
            ).then(function (result, sample) {
              c.assertEqual(
                result.getError(),
                "testError",
                "Evaluation result error"
              );
              c.assertObjectsWithValues(
                result.getRequestedValidations(),
                "identifier",
                [sample.getIdentifier().getIdentifier()]
              );

              c.finish();
            });
          });
        })
        .fail(function (error) {
          c.fail(error.message);
          c.finish();
        });
    };

    QUnit.test(
      "evaluatePlugin() dynamic property plugin from database",
      function (assert) {
        return testDynamicPropertyPlugin(assert, true);
      }
    );

    QUnit.test(
      "evaluatePlugin() dynamic property plugin from script",
      function (assert) {
        return testDynamicPropertyPlugin(assert, false);
      }
    );

    QUnit.test(
      "evaluatePlugin() entity validation plugin from database",
      function (assert) {
        return testEntityValidationPlugin(assert, true);
      }
    );

    QUnit.test(
      "evaluatePlugin() entity validation plugin from script",
      function (assert) {
        return testEntityValidationPlugin(assert, false);
      }
    );
  };

  return function () {
    executeModule("Evaluate tests", openbis);
    executeModule(
      "Evaluate tests (executeOperations)",
      openbisExecuteOperations
    );
  };
});
