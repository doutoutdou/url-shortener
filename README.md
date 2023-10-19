# Url shortener project

## Purpose
- Generate a short URL from a complete one.  
- Be able to retrieve the original url from the shortened url.

## Constraints
- Shorten url size must not exceed 10 characters (after the domain)  
- A shortened url that is not found should return an error  
- Two identical urls must return the same shortened url  

## How to run it

### Technical requirements
You need a jdk 21 to be able to run the project  
There is no need of docker installation, an embedded h2 database is used and it's configured to be saved on disk before exiting

### Start it
You can run it through intellij or with command line `./mvnw spring-boot:run`

### Use the API
If you are using Intellij, you can open the file requests.http and use some examples provided (You may need to install the plugin HTTP client).  

Otherwise, you can use your preferred http client.

## API Documentation
The API documentation is available here :
- http://localhost:8080/api/swagger-ui/index.html
- http://localhost:8080/api/api-docs
- http://localhost:8080/api-docs.yaml (for downloading the yaml file)

## H2 Console
When the application is running, you can access to the `H2` database console with the url `http://localhost:8080/api/h2-console`.  
All information for url and authentication can be found in the `application.yml` file

### More information
To know more about my choices, read the `CHOICES.md` file



