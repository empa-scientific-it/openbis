define(["stjs"], function (stjs) {
  var PluginEvaluationResult = function () {};
  stjs.extend(
    PluginEvaluationResult,
    null,
    [],
    function (constructor, prototype) {
      prototype["@type"] = "as.dto.plugin.evaluate.PluginEvaluationResult";
      constructor.serialVersionUID = 1;
    },
    {}
  );
  return PluginEvaluationResult;
});
