# stacklab-weather

## Todo
- [X] basic helloworld rest api
- [X] gradle release
- [X] gradle gitflow
- [X] jenkins pipeline
- [X] github pipeline
- [X] stacklabsDto with swagger
- [X] weatherbit swagger to dto
- [X] ~~mapstruct weatherbit to stacklabs~~ (kapt not working with java 21)
- [X] Inject configuration
- [X] weather service
- [X] get current weather in service
- [X] get forecast in service
- [X] dockerized
- [X] rest route api
- [X] mocked http integration test
- [ ] http error management 
- [X] create a service for weather evaluation
- [X] add abstraction for weatherbit api client?
- [ ] github pipeline publish docker image?
- [ ] terraform?

# Startup

- launch `./gradlew openApiGenerate` command in order to generate weatherbit api client.
- launch `./gradlew clean bootJar` command in order to build weather application.
- launch `./gradlew bootRun` command in order to build weather application.
- launch the following commands in order to build a docker image for weather application.
```
cd weather
docker build -t stacklabs/weather .
```
- launch `docker run -p 8080:8080 stacklabs/weather` command in order to run weather application.
- you can check http://localhost:8080/api/v1/swagger-ui/index.html for swagger ui documentation.

# Platanes

## Weather bit non-functional swagger file
In order to avoid information duplication I choose to generate a weather bit client from the 
swagger file provide by weatherbit. Unfortunately the swagger file is not fully functional:
- There is divergence between the api and the documentation (i.e. different field name like wind_speed and wind_spd).
- The swagger use a non-conform union type `type: ["number", "null"]` instead of `oneOf: ["number", "null"]` 
(https://swagger.io/docs/specification/v3_0/data-models/data-types/).

I had to choose between 2 solutions:

1. generate a client from a custom swagger file.
2. write a minimalist client.

I finally chose the first solution, which is more interesting to discuss during an interview. I wrote the script 
`to-valid-open-api.js` to keep a track of the applied customisations.

In real life I may have done a different choice depending if it's a short or long life project and if I have the 
possibility to contact and work with the team in charge of the api and the documentation. 