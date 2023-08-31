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

console.log("import openbis from '../../src/v3/openbis'")

// use bundled DTOs when parsing JSON responses

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

// export all DTOs and facade

// Some details:
// DTOs are exported using their simple name as well as their full name.
// For instance, "as.dto.sample.Sample" will be available in the final exported "openbis" object
// as both "openbis.Sample" and "openbis.as.dto.sample.Sample".
// This way any DTOs with duplicated simple names can still be accessed via their full names.

var exported = {
  "openbis" : "$$openbis$$"
}

dtos.forEach(dto => {
  var package = exported

  dto.pathWithSlashes.split('/').slice(0,-1).forEach(pathPart => {
    if(!package[pathPart]){
      package[pathPart] = {}
    }
    package = package[pathPart]
  })

  if(exported[dto.name] === undefined){
    exported[dto.name] = "$$" + dto.pathWithUnderscores + "$$"
  }else{
    // for duplicated simple names use null to avoid accidental mistakes where one DTOs is used instead of another
    exported[dto.name] = null
  }

  package[dto.name] = "$$" + dto.pathWithUnderscores + "$$"
})

// remove quotes after JSON.stringify, the quotes to remove have $$ (e.g. "$$as_dto_sample_Sample$$" => as_dto_sample_Sample)
console.log('\nexport default ' + JSON.stringify(exported, null, 4).replaceAll("$$\"", "").replaceAll("\"$$", ""))
