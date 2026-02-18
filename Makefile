CLUSTER_NAME=enron-cluster
IMAGE_NAME=quarkus/enron-ingestion-service-jvm

.PHONY: port-forward

.PHONY: build
build:
	mvn package
	docker build -f Dockerfile.jvm -t $(IMAGE_NAME) .

.PHONY: create-cluster
create-cluster:
	kind create cluster --config k8s/kind-data.yaml --name $(CLUSTER_NAME)

.PHONY: load-image
load-image:
	kind --name $(CLUSTER_NAME) load docker-image $(IMAGE_NAME)

.PHONY: deploy
deploy:
	kubectl apply -f k8s/local-data-mount/
	kubectl apply -f k8s/postgres/
	kubectl apply -f k8s/enron-ingestion-service/

.PHONY: port-forward
port-forward:
	kubectl port-forward svc/enron-ingestion-service 8080:8080

.PHONY: logs
logs:
	kubectl logs -f deployment/enron-ingestion-service

.PHONY: delete
delete:
	kind delete cluster --name $(CLUSTER_NAME)

.PHONY: build-start
build-start: build create-cluster load-image deploy

.PHONY: start
start: create-cluster load-image deploy