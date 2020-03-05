# Micronaut / OPA demo application

This showcase demonstrates policy enforcement with OPA (Open Policy Agent) in Java.

- Application Runtime: [OpenJDK 8](http://openjdk.java.net/)
- Application Framework: [Micronaut](http://micronaut.io/)
- Containerization: [JIB](https://github.com/GoogleContainerTools/jib)
- Orchestration: [Kubernetes](https://kubernetes.io/)
- Policy Enforcement: [OPA](https://www.openpolicyagent.org/)

## Build & Deployment

### Java Build

```bash
./gradlew build
```

### Run The Application locally

```bash
./gradlew run
```

will start the application locally, listening on port 8080. Without further configuration, the application expects an
OPA instance at localhost:8181.
You can access the application using *curl*: 

```bash
curl http://localhost:8080/free
```

### Container Build

Preconditions: 
- Local Docker environment is set up.

```bash
./gradlew jibDockerBuild
```


### Kubernetes Deployment

Preconditions:
- Kubernetes CLI is installed
- Image is available in the Kubernetes cluster's Docker registry, either by directly installing it or by allowing the 
registry to pull the image from Docker Hub.

```bash
kubectl apply -f k8s-deployment.yml 
```

## Using The Showcase

This guide assumes that the application is deployed in a Kubernetes cluster and $SERVICE_URL points to the application 
service.

The following command will output a Chuck Norris Fact (and the HTTP headers):

```bash
curl -i $SERVICE_URL/free
```

The output will look like this:

```
HTTP/1.1 200 OK
Date: Sun, 15 Jul 2018 21:12:33 GMT
content-type: text/plain
content-length: 71

Chuck Norris can juggle 12 bar stools when drunk but only 8 when sober.   
```

The endpoint is unprotected, hence `/free`. You can repeat this ad nauseam.

The `/protected` endpoint will serve facts that require protection. Try to call this endpoint:

```bash
curl -i $SERVICE_URL/protected
```

Computer says no:

```
HTTP/1.1 401 Unauthorized
Content-Type: text/plain
Date: Sun, 15 Jul 2018 21:12:37 GMT
content-length: 54

You are not authorized. Use /free, you filthy peasant!
```

To access the protected endpoint, have a look at what's required in the Rego policy in [k8s.yml](/k8s.yml):
A JWT containing `micronaut-opa-demo` in the *payload.sub* field.

Let's try this again with a matching token.

```
curl -i -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtaWNyb25hdXQtb3BhLWRlbW8iLCJuYW1lIjoiTWljcm9uYXV0IE9QQSBEZW1vIiwiaWF0IjoxNTE2MjM5MDIyfQ.2sOzCwb9777B4yAP-nU5PQPFIjulRJxS9nKDNgHOvqA" -i $SERVICE_URL/protected
```

And lo and behold, Chuck Norris' real name is exposed.

```
HTTP/1.1 200 OK
Date: Sun, 15 Jul 2018 21:33:31 GMT
content-type: text/plain
content-length: 147

Carlos Ray Norris once had a head on collision with the sun. Luckily, the sun is so far away that the shift of its position had no effect on Earth.
```

The application sends all headers, request path and method to OPA. OPA can make decisions on that data. It's easy to 
see that almost any policy decision can be offloaded to OPA. 

## LICENSE

This repository may be used under the terms and conditions of the [Apache License 2.0](/LICENSE).