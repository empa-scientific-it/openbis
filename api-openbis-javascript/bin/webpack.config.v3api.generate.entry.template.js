var pathsWithSlashes = [
    __FILES__
]

var dtos = []

pathsWithSlashes.forEach(pathWithSlashes => {
  dtos.push({
    name: pathWithSlashes.substring(pathWithSlashes.lastIndexOf('/') + 1),
    pathWithSlashes: pathWithSlashes,
    pathWithUnderscores: pathWithSlashes.replaceAll('/', '_'),
  })
})

// import all DTOs and facade

dtos
  .sort((dto1, dto2) =>
    dto1.pathWithSlashes.localeCompare(dto2.pathWithSlashes)
  )
  .forEach(dto => {
    console.log('import ' + dto.pathWithUnderscores + " from '../../src/v3/" + dto.pathWithSlashes + "'")
  })

console.log("import stjs from '../../src/v3/lib/stjs/js/stjs'")
console.log("import underscore from '../../src/v3/lib/underscore/js/underscore'")
console.log("import openbis from '../../src/v3/openbis'")

// use bundled DTOs when parsing JSON responses (i.e. overwrite the default Json.requireFn - it would load DTOs from the server using RequireJS)

console.log('\nvar modules = {')

dtos
  .sort((dto1, dto2) =>
    dto1.pathWithSlashes.localeCompare(dto2.pathWithSlashes)
  )
  .forEach(dto => {
    console.log('  "' + dto.pathWithSlashes + '" : ' + dto.pathWithUnderscores + ',')
  })

console.log('}')

console.log('\nutil_Json.setRequireFn(function(moduleNames, callback){')
console.log('  callback.apply(')
console.log('    null,')
console.log('    moduleNames.map(function(moduleName){')
console.log('      return modules[moduleName]')
console.log('    })')
console.log('  )')
console.log('})')

// stjs uses "underscore" library and expects it to be available in the global context under "_" - let's make it use the bundled "underscore" instead

console.log('\nstjs._ = underscore')

// export all DTOs and facade

// Some details:
// DTOs are exported using their simple name as well as their full name.
// For instance, "as.dto.sample.Sample" will be available in the final exported "openbis" object
// as both "openbis.Sample" and "openbis.as.dto.sample.Sample".
// This way any DTOs with duplicated simple names can still be accessed via their full names.

var exported = {
  "openbis" : "$$openbis$$",
  "noConflict" : "$$noConflict$$"
}

// define noConflict function that reverts (if needed) window.openbis field back to its original value (similar to jquery.noConflict)
console.log('\nvar originalOpenbisValue = window && window.openbis')
console.log('\nvar noConflict = function(){')
console.log('  if(window && window.openbis === exported){')
console.log('    window.openbis = originalOpenbisValue')
console.log('  }')
console.log('  return exported')
console.log('}')

dtos.forEach(dto => {
  if(exported[dto.name] === undefined){
    exported[dto.name] = "$$" + dto.pathWithUnderscores + "$$"
  }else{
    // for duplicated simple names do not export anything via a simple name to avoid accidental mistakes where one DTOs is used instead of another
    delete exported[dto.name]
  }
  exported[dto.pathWithUnderscores] = "$$" + dto.pathWithUnderscores + "$$"
})

// remove quotes after JSON.stringify, the quotes to remove have $$ (e.g. "$$as_dto_sample_Sample$$" => as_dto_sample_Sample)
console.log('\n var exported = ' + JSON.stringify(exported, null, 4).replaceAll("$$\"", "").replaceAll("\"$$", ""))
console.log('\nexport default exported')
