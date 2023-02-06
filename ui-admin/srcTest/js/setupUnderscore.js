import _ from 'underscore'

/*
Provide underscore globally in tests under '_' for stjs (used in V3 dtos) to work. 
Unfortunately Jest does not have a shim mechanism similar to the one used in RequireJS (see RequireJS config.js file)
*/

// eslint-disable-next-line no-undef
global._ = _
