echo "Launching $BUILD_NAME IN AMAZON ECS"
ecs-cli configure profile --profile-name "default" --access-key $AWS_ACCESS_KEY --secret-key $AWS_SECRET_KEY
ecs-cli configure --cluster --default-launch-type EC@ --region --config-name default
ecs-cli compose --file docker/aws-dev/docker-compose.yml up
rm -rf ~/.ecs
