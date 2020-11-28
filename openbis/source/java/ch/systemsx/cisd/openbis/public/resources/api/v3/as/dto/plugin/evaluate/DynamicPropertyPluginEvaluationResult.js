define(["stjs", "as/dto/plugin/evaluate/PluginEvaluationResult"], function (
  stjs,
  PluginEvaluationResult
) {
  var DynamicPropertyPluginEvaluationResult = function (value) {
    this.value = value;
  };
  stjs.extend(
    DynamicPropertyPluginEvaluationResult,
    PluginEvaluationResult,
    [PluginEvaluationResult],
    function (constructor, prototype) {
      prototype["@type"] =
        "as.dto.plugin.evaluate.DynamicPropertyPluginEvaluationResult";
      constructor.serialVersionUID = 1;
      prototype.value = null;

      prototype.getValue = function () {
        return this.value;
      };
    },
    {}
  );
  return DynamicPropertyPluginEvaluationResult;
});
