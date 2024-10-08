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
- [X] create a service for weather evaluation
- [X] add abstraction for weatherbit api client?

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

- you can also use one of the image deploy on docker hub https://hub.docker.com/r/pierre1405/personal/tags
  (sorry for the name, free accounts are limites to a single repo)
```
docker run -p 8080:8080 pierre1405/personal:latest
```

# CI/CD
There is 2 github actions pipeline defined,
- a CD pipeline launch for each commit pushed on the main and develop branches and for each pull request
  The pipeline build the app and run the tests.
- a push docker image pipeline launch for each commit pushed on the main and develop branches.

check `.github/workflows/docker-image.yml` and `.github/workflows/gradle.yml` for more details 

# Code life cyle

- we use github issue to create the feature branch.
- we use github pull request to merge the feature branch into the develop branch.
- we use github pull request to merge the develop branch into the main branch.
- we use the net.researchgate.release plugin to release the main branch.
- I give a try to io.github.robwin.jgitflow plugin to manage the git flow branch, but it not so relevant 
while using github.


# Issues

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


## Incoherent city not found error

When the city is not found, the weatherbit api current weather endpoint return a 400 error with body
```json
{
    "error": "No Location Found. Try lat/lon."
}
```
When the city is not found, the weatherbit api forecast daily endpoint return a 204 error without body

Not easy to do a proper error management

# Axes to improve

- better kotlin null management