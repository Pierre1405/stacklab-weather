The weatherbit open api documentation is not updated according to the latest version of the Weatherbit API.
i.e: CurrentObs wind_spd field is named wind_spd in the documentation but wind_speed is used in the api.

it also uses a not valid union type ["number", "null"] instead of "number"
https://swagger.io/docs/specification/v3_0/data-models/data-types/
```
"gust": {
  "type": ["number", "null"],
  "description": "Wind gust speed - Default (m/s)",
  "example": 9.0,
  "nullable": true
}
```

According to this, we have to use a custom open api swagger file.

the source-swagger.json contains the latest version of https://www.weatherbit.io/static/swagger.json
If you run `node to-valid-open-api.js`, it will generate a new swagger.json with fixes to generate
the weatherbit client.

