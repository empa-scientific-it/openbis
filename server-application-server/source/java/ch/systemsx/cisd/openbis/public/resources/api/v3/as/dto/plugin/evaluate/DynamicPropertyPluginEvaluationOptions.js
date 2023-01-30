define(["stjs", "as/dto/plugin/evaluate/PluginEvaluationOptions"], function (
  stjs,
  PluginEvaluationOptions
) {
  var DynamicPropertyPluginEvaluationOptions = function () {};
  stjs.extend(
    DynamicPropertyPluginEvaluationOptions,
    PluginEvaluationOptions,
    [PluginEvaluationOptions],
    function (constructor, prototype) {
      prototype["@type"] =
        "as.dto.plugin.evaluate.DynamicPropertyPluginEvaluationOptions";
      constructor.serialVersionUID = 1;
      prototype.objectId = null;

      prototype.getObjectId = function () {
        return this.objectId;
      };
      prototype.setObjectId = function (objectId) {
        this.objectId = objectId;
      };
    },
    {
      objectId: "IObjectId",
    }
  );
  return DynamicPropertyPluginEvaluationOptions;
});
