var pathsWithSlashes = [
    __FILES__
]

var dtos = []

pathsWithSlashes.forEach(pathWithSlashes => {
  dtos.push({
    name: pathWithSlashes.substring(pathWithSlashes.lastIndexOf('/') + 1),
    pathWithSlashes: pathWithSlashes
  })
})

// import all DTOs and facade

var imports = {}

dtos
  .sort((dto1, dto2) => dto1.name.localeCompare(dto2.name))
  .forEach(dto => {
    var isDuplicatedName = imports[dto.name]
    console.log(
       (isDuplicatedName ? "//" : "") + 'import ' + dto.name + " from '../../src/v3/" + dto.pathWithSlashes + "'"
    )
    imports[dto.name] = true
  })

console.log("import openbis from '../../src/v3/openbis'")

// use bundled DTOs when parsing JSON responses

console.log('\nvar modules = {')

dtos
  .sort((dto1, dto2) =>
    dto1.pathWithSlashes.localeCompare(dto2.pathWithSlashes)
  )
  .forEach(dto => {
    console.log('  "' + dto.pathWithSlashes + '" : ' + dto.name + ',')
  })

console.log('}')

console.log('\nJson.setRequireFn(function(moduleNames, callback){')
console.log('  callback.apply(')
console.log('    null,')
console.log('    moduleNames.map(function(moduleName){')
console.log('      return modules[moduleName]')
console.log('    })')
console.log('  )')
console.log('})')

// export all DTOs and facade

console.log('\nexport default {')

dtos
  .sort((dto1, dto2) => dto1.name.localeCompare(dto2.name))
  .forEach(dto => {
    console.log('  ' + dto.name + ',')
  })

console.log('  openbis,')

console.log('}')
