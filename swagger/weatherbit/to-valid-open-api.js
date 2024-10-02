var fs = require("fs");

// Function to rename a field in a Swagger model
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

    // Function to replace invalid union types ["number", "null"] by "number"
    // https://swagger.io/docs/specification/v3_0/data-models/data-types/
    let replaceUnionType = (key, value) => {
       if(key === "type" && JSON.stringify(value) === JSON.stringify(["number", "null"])) {
           return "number";
       }
       return value;
   }
   // Parse the Swagger file with custom type replacement
   let swagger = JSON.parse(buf.toString(), replaceUnionType);

    renameField(swagger, "CurrentObs", "wind_speed", "wind_spd");

    // Write the modified Swagger to a new file
    fs.writeFile('swagger.json', JSON.stringify(swagger, null, 2), err => {
      if (err) {
        console.error(err);
      }
    });
});