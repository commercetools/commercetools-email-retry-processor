#! /bin/bash

set -e

export REPO="commercetools/commercetools-email-retry-processor"
export COMMIT=${TRAVIS_COMMIT::8}
export BASE_TAG="${REPO}:${COMMIT}"
export DOCKER_TAG=`if [ "$TRAVIS_BRANCH" == "master" -a "$TRAVIS_PULL_REQUEST" = "false" ]; then echo "latest"; else echo ${TRAVIS_BRANCH//\//-} ; fi`

echo "Building Docker image using tag '${BASE_TAG}'."
docker build -t "${BASE_TAG}" .

docker login -u="${DOCKER_USERNAME}" -p="${DOCKER_PASSWORD}"

echo "Adding additional tag '${REPO}:${DOCKER_TAG}' to already built Docker image '${BASE_TAG}'."
docker tag $REPO:$COMMIT $REPO:$DOCKER_TAG
if [ "$TRAVIS_TAG" ]; then
  echo "Adding additional tag '${REPO}:${TRAVIS_TAG}' to already built Docker image '${BASE_TAG}'."
  docker tag $REPO:$COMMIT $REPO:${TRAVIS_TAG};
  echo "Adding additional tag '${REPO}:production' to already built Docker image '${BASE_TAG}'."
  docker tag $REPO:$COMMIT $REPO:production;
fi
echo "Pushing Docker images to repository '${REPO}' (all local tags are pushed)."
docker push $REPO
docker logout
