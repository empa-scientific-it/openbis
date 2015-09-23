/**
 *  Sample identifier.
 *  
 *  @author pkupczyk
 */
define(["stjs", "dto/id/ObjectIdentifier", "dto/id/sample/ISampleId"], function (stjs, ObjectIdentifier, ISampleId) {
    var SampleIdentifier = /**
     *  @param identifier Sample identifier, e.g. "/MY_SPACE/MY_SAMPLE" (space sample) or "/MY_SAMPLE" (shared sample)
     */
    function(identifier) {
        ObjectIdentifier.call(this, identifier);
    };
    stjs.extend(SampleIdentifier, ObjectIdentifier, [ObjectIdentifier, ISampleId], function(constructor, prototype) {
        prototype['@type'] = 'dto.id.sample.SampleIdentifier';
        constructor.serialVersionUID = 1;
    }, {});
    return SampleIdentifier;
})