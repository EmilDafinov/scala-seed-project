IMAGE_NAME_SPLIT=(${1//:/ })
IMAGE_VERSION="${IMAGE_NAME_SPLIT[1]}"
echo "Image: name [${IMAGE_NAME_SPLIT[0]}], tag: [$IMAGE_VERSION] EXPECTED_TAG: [$EXPECTED_TAG]"
sbt "set version := \"$IMAGE_VERSION\"" "Docker / publishLocal"