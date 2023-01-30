define(["stjs"], function (stjs) {
  var EvaluatePluginOperation = function (options) {
    this.options = options;
  };
  stjs.extend(
    EvaluatePluginOperation,
    null,
    [],
    function (constructor, prototype) {
      prototype["@type"] = "as.dto.plugin.evaluate.EvaluatePluginOperation";
      constructor.serialVersionUID = 1;
      prototype.options = null;

      prototype.getOptions = function () {
        return this.options;
      };
      prototype.getMessage = function () {
        return "EvaluatePluginOperation";
      };
    },
    {
      options: "PluginEvaluationOptions",
    }
  );
  return EvaluatePluginOperation;
});
