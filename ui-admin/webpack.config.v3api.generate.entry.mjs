//Module sources
import fs from "fs";
import path from "path";
import {globSync} from "glob";

function getModuleName(pathWithSlashes) {
    return path.basename(pathWithSlashes, ".js");
}


const allDtos =
    globSync(["./srcV3/as/dto/**/*.js","./srcV3/dss/dto/**/*.js", "./srcV3/openbis.js"]).map(pathWithSlashes => {
    console.log(pathWithSlashes)
    const relPath = pathWithSlashes.replace(".js", "")
    const moduleName = getModuleName(relPath);
    return {
        name: moduleName,
        pathWithSlashes: relPath
    }
}).sort((dto1, dto2) =>  dto1.pathWithSlashes.localeCompare(dto2.pathWithSlashes))

const dtos = [...new Map(allDtos.map(dto => [dto.name, dto])).values()];


// import all DTOs and facade

const imports = dtos.map(dto => {
     return `import ${dto.name}  from './${dto.pathWithSlashes}'`
  }).concat(["import openbis from './srcV3/openbis'", "import require from './srcV3/require'"])





const jsonFunction =`
import Json from './srcV3/util/Json'
Json.setRequireFn(function(moduleNames, callback){
    callback.apply(
        null,
        moduleNames.map(function(moduleName){
            return modules[moduleName]
        })
    )
})
`

const moduleExports = `export {\n ${dtos.map(dto => dto.name).join(",\n")} \n}`


const script = imports.join('\n') + jsonFunction + '\n' + moduleExports

fs.writeFileSync("./webpack.config.v3api.entry.js", script)