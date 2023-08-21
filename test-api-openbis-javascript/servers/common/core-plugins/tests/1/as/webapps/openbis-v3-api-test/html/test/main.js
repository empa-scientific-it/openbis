define([
         'test/test-login',
         'test/test-jsVSjava',
         'test/test-create',
         'test/test-update',
         'test/test-search',

         'test/test-freezing',
         'test/test-get',
         'test/test-delete',
         'test/test-execute',
         'test/test-evaluate',
         'test/test-json',

//         'test/test-dto',
         'test/test-dto-roundtrip',
         'test/test-custom-services',
         'test/test-dss-services',
         'test/test-archive-unarchive',

         'test/test-import-export'
         ], function() {
	var testSuites = arguments;
	return function() {
		for (var i = 0; i < testSuites.length; i++) {
			testSuites[i]();
		}
	};
});
