/**
 * Holds information that uniquely identifies a custom AS service in openBIS.
 *
 * @author Franz-Josef Elmer
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
  var ICustomASServiceId = function() {
  };
  stjs.extend(ICustomASServiceId, null, [ IObjectId ], null, {});
  return ICustomASServiceId;
})