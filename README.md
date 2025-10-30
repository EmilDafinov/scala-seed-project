# URL shortener service:

## Running locally:
The service is configured to be run in your local kubernetes instance, using Tilt

### Dependencies
* Install docker and kubernetes locally (on Mac I use [Docker Desktop](https://www.docker.com/products/docker-desktop/))
* Install sbt using Homebrew: `brew install sbt`
* Install tilt using Homebrew: `brew install tilt`
* Install helm using Homebrew: `brew install helm`
### Running with Tilt
* You can start the application by running `tilt up` in the root directory
  * This would deploy the application and all dependencies (application at port 9000, postgresql at port 5433)
  * You'll get an output similar to  ```shell
        Tilt started on http://localhost:10350/
        v0.34.3, built 2025-05-14
        
        (space) to open the browser
        (s) to stream logs (--stream=true)
        (t) to open legacy terminal mode (--legacy=true)
        (ctrl-c) to exit
    ```
  * you might need to open the link in your browser , and cancel the `unit_tests` resource 
    * it runs before the DB is ready, but some tests require the DB. However, it holds up the rest of the deployment
### Running with helm
You can build the app and deploy it to k8s using its helm chart. 
Navigate to the root directory of the project, then do the following:


1. First publish a tagged docker image for the application and for flyway: (we use `app_version` as the image tag for this example)
```shell
docker build -t docker.io/library/scala-seed-project-flyway:app_version . --file flyway/Dockerfile
sbt "set version := \"app_version\";Docker/publishLocal"
```
2. Once the images are built, deploy the helm chart with helm:
```shell
helm install -f ./scala-seed-project/values.yaml scala-seed-project ./scala-seed-project --set project.rootdir="${pwd}" --set image.tag="app_version"
```
3. Then, you can port forward in k8s to access the application:
```shell
kubectl port-forward deployments/scala-seed-project 9000:9000
```
4. See the [included Postman collection](dev_env_demo%20Copy.postman_collection.json) for sample requests
  * When sending a request for shortening, make sure your URL is valid and URL encoded

## Design notes
* A relational database is used for storage to guarantee that we don't create duplicate short URLs for the same full URL
* A Redis instance configured for LRU is used for caching

## Possible improvements:
* The short url creation and resolution can be split in separate (independentky deployed) services:
  * That way the shortener can be down without affecting the resolution
* For the sake of simplicity, the example is using a setuf that is less sophisticated than what we'd need in prod
  * The large amount of data that we need to store would require sharding of the database and possibly a cluster of Redis instances

