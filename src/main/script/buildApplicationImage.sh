IMAGE_NAME_SPLIT=(${IMAGE//:/ })
IMAGE_VERSION="${IMAGE_NAME_SPLIT[1]}"
echo "Image: name [${IMAGE_NAME_SPLIT[0]}], tag: [$IMAGE_VERSION]"
sbt "set version := \"$IMAGE_VERSION\"" "Docker / publishLocal"