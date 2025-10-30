# URL shortener service:

## Running locally:
The service is configured to be run in your local kubernetes instance, using Tilt
* Install docker and kubernetes locally (on Mac I use [Docker Desktop](https://www.docker.com/products/docker-desktop/))
* Install tilt using Homebrew: `brew install tilt`
* You can start the application by running `tilt up` in the root directory
  * This would deploy the application and all dependencies (applicatoin at port 9000, postgresql at port 5433)
* See the [included Postman collection](dev_env_demo%20Copy.postman_collection.json) for sample requests
  * When sending a request for shortening, make sure your URL is valid and URL encoded

## Design notes
* A relational database is used for storage to guarantee that we don't create duplicate short URLs for the same full URL
* A Redis instance configured for LRU is used for caching

## Possible improvements:
* The short url creation and resolution can be split in separate (independentky deployed) services:
  * That way the shortener can be down without affecting the resolution
* For the sake of simplicity, the example is using a setuf that is less sophisticated than what we'd need in prod
  * The large amount of data that we need to store would require sharding of the database and possibly a cluster of Redis instances

