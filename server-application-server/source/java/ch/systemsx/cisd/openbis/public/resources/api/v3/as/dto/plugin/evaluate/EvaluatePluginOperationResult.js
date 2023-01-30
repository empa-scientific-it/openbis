define(["stjs"], function (stjs) {
  var EvaluatePluginOperationResult = function (result) {
    this.result = result;
  };
  stjs.extend(
    EvaluatePluginOperationResult,
    null,
    [],
    function (constructor, prototype) {
      prototype["@type"] =
        "as.dto.plugin.evaluate.EvaluatePluginOperationResult";
      constructor.serialVersionUID = 1;
      prototype.result = null;

      prototype.getResult = function () {
        return this.result;
      };
      prototype.getMessage = function () {
        return "EvaluatePluginOperationResult";
      };
    },
    {
      result: "PluginEvaluationResult",
    }
  );
  return EvaluatePluginOperationResult;
});
