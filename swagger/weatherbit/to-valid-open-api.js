var fs = require("fs");

// fix divergence on attribute names between swagger and api
// I tried using openapi-generator parameterNameMappings configuration
// But there is no way to target ModelName.attributeName
// it will change attributeName in all models
var renameField = (swagger, modelName, parameterName, newParameterName) => {
    let modelProps = swagger.definitions[modelName].properties
    modelProps[newParameterName] = modelProps[parameterName]
    console.log(`renamed ${parameterName} to ${newParameterName}`, modelProps)
    delete modelProps[parameterName]
}

fs.readFile("source-swagger.json", function(err, buf) {
    if (err) {
        console.log(err);
        return;
    }

    // Parse swagger file and replace not valid union type ["number", "null"] by "number"
    // https://swagger.io/docs/specification/v3_0/data-models/data-types/
    let replaceUnionType = (key, value) => {
       if(key === "type" && JSON.stringify(value) === JSON.stringify(["number", "null"])) {
           return "number";
       }
       return value;
   }
   let swagger = JSON.parse(buf.toString(), replaceUnionType);

    renameField(swagger, "CurrentObs", "wind_speed", "wind_spd");

    fs.writeFile('swagger.json', JSON.stringify(swagger, null, 2), err => {
      if (err) {
        console.error(err);
      }
    });
});