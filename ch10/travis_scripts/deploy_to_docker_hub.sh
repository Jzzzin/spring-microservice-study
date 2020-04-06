echo "Pushing service docker images to docker hub ..."
docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
docker push jzzzin/bn-authentication-service:$BUILD_NAME
docker push jzzzin/bn-licensing-service:$BUILD_NAME
docker push jzzzin/bn-organization-service:$BUILD_NAME
docker push jzzzin/bn-cofsvr:$BUILD_NAME
docker push jzzzin/bn-eurekasvr:$BUILD_NAME
docker push jzzzin/bn-zuulsvr:$BUILD_NAME
