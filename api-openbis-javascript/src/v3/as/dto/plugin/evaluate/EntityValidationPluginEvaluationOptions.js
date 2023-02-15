define(["stjs", "as/dto/plugin/evaluate/PluginEvaluationOptions"], function (
  stjs,
  PluginEvaluationOptions
) {
  var EntityValidationPluginEvaluationOptions = function () {};
  stjs.extend(
    EntityValidationPluginEvaluationOptions,
    PluginEvaluationOptions,
    [PluginEvaluationOptions],
    function (constructor, prototype) {
      prototype["@type"] =
        "as.dto.plugin.evaluate.EntityValidationPluginEvaluationOptions";
      constructor.serialVersionUID = 1;
      prototype.objectId = null;
      prototype.isNew = null;

      prototype.getObjectId = function () {
        return this.objectId;
      };
      prototype.setObjectId = function (objectId) {
        this.objectId = objectId;
      };
      prototype.isNew = function () {
        return this.isNew;
      };
      prototype.setNew = function (isNew) {
        this.isNew = isNew;
      };
      prototype.getIsNew = function () {
        return this.isNew;
      };
      prototype.setIsNew = function (isNew) {
        this.isNew = isNew;
      };
    },
    {
      objectId: "IObjectId",
    }
  );
  return EntityValidationPluginEvaluationOptions;
});
