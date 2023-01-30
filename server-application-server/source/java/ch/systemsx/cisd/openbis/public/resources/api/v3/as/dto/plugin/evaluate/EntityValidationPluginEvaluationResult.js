define(["stjs", "as/dto/plugin/evaluate/PluginEvaluationResult"], function (
  stjs,
  PluginEvaluationResult
) {
  var EntityValidationPluginEvaluationResult = function (
    error,
    requestedValidations
  ) {
    this.error = error;
    this.requestedValidations = requestedValidations;
  };
  stjs.extend(
    EntityValidationPluginEvaluationResult,
    PluginEvaluationResult,
    [PluginEvaluationResult],
    function (constructor, prototype) {
      prototype["@type"] =
        "as.dto.plugin.evaluate.EntityValidationPluginEvaluationResult";
      constructor.serialVersionUID = 1;
      prototype.error = null;
      prototype.requestedValidations = null;

      prototype.getError = function () {
        return this.error;
      };
      prototype.getRequestedValidations = function () {
        return this.requestedValidations;
      };
    },
    {
      requestedValidations: {
        name: "Collection",
        arguments: ["IObjectId"],
      },
    }
  );
  return EntityValidationPluginEvaluationResult;
});
