ECR_TARGET := 089941056973.dkr.ecr.us-east-1.amazonaws.com/app-store-service-v2
UUID := $(shell uuidgen)
LABEL := $(shell git rev-parse HEAD)-$(UUID)

# Uses sudo for docker commands to run from Linux machines (like Jenkins)
manual-deploy-sudo:
	sudo $(shell aws ecr get-login --region us-east-1 --no-include-email)
	sudo docker tag apptentive/app-store-service-v2:latest "$(ECR_TARGET):$(UUID)"
	sudo docker push "$(ECR_TARGET):$(UUID)"
	kubectl patch -f k8s/app-store-service-k8s-deployment.yaml -p '{"spec": {"template": {"spec": { "containers": [{"name": "app-store-service-v2", "image": "089941056973.dkr.ecr.us-east-1.amazonaws.com/app-store-service-v2:latest"}] } } } }' --local -o yaml | kubectl apply -f -
