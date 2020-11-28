define(["stjs"], function (stjs) {
  var PluginEvaluationOptions = function () {};
  stjs.extend(
    PluginEvaluationOptions,
    null,
    [],
    function (constructor, prototype) {
      prototype["@type"] = "as.dto.plugin.evaluate.PluginEvaluationOptions";
      constructor.serialVersionUID = 1;
      prototype.pluginId = null;
      prototype.pluginScript = null;

      prototype.getPluginId = function () {
        return this.pluginId;
      };
      prototype.setPluginId = function (pluginId) {
        this.pluginId = pluginId;
      };
      prototype.getPluginScript = function () {
        return this.pluginScript;
      };
      prototype.setPluginScript = function (pluginScript) {
        this.pluginScript = pluginScript;
      };
    },
    {
      pluginId: "IPluginId",
    }
  );
  return PluginEvaluationOptions;
});
