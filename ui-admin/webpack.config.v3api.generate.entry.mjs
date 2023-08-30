//Module sources
import fs from "fs";
import path from "path";
import {globSync} from "glob";

function getModuleName(pathWithSlashes) {
    return path.basename(pathWithSlashes, ".js");
}


function cleanIt(obj) {
//Found here https://stackoverflow.com/questions/11233498/json-stringify-without-quotes-on-properties
//and modified
    var cleaned = JSON.stringify(obj, null, 2);

   function removeQuotesFromValues(jsonString) {
     return jsonString.replace(/"([^"]+)": "([^"]+)"/g, '"$1": $2');
   }
   return removeQuotesFromValues(cleaned);
}

const allDtos =
    globSync(["./srcV3/as/**/**.js","./srcV3/dss/**/**.js", "./srcV3/openbis.js"]).map(pathWithSlashes => {
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
  }).concat(["import require from './srcV3/require'"])


//Make a map of all modules

const modules = dtos.map(dto => [dto.pathWithSlashes.replace("srcV3/", ""), dto.name]);

const moduleExpression = cleanIt(Object.fromEntries(modules))
console.log(moduleExpression)

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
const defaultExport = `export default {\n ${dtos.map(dto => dto.name).join(",\n")} \n}`

const script = imports.join('\n') + '\n' + `const modules = ${moduleExpression}` + jsonFunction + '\n' + moduleExports + '\n' + defaultExport

fs.writeFileSync("./webpack.config.v3api.entry.js", script)