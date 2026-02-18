# Enron ingestion service

Data ingestion service built with **Quarkus (Kotlin)** that processes the **Enron Email 2015 dataset**
and exposes metrics over HTTP.

---

## API endpoints

* `POST /api/v1/ingestion/start`: Start the ingestion
* `GET  /api/v1/ingestion/status`: Provide ingestion status info
* `GET  /api/v1/ingestion/top-senders`: Return top 10 senders
* `POST /api/v1/ingestion/reset`: Clear processed data and resets ingestion state

---

## Running locally

You can run this service in two ways:

* **In development mode**
* **With Kind**

> **_[Kind](https://kind.sigs.k8s.io/)_** is a tool for running Kubernetes cluster locally by using Docker

### Downloading the dataset

Download the Enron dataset: </br>
https://www.cs.cmu.edu/~enron/enron_mail_20150507.tar.gz

> **_NOTE:_** Depending on the mode, the dataset must be placed in a specific location.

---

### 1. In development mode

In development mode Quarkus uses [Dev Services](https://quarkus.io/guides/dev-services) to automatically provision
required infrastructure
such as in this case the postgres database

#### 1.0 Prerequisites

* Java (JDK 25+)
* Maven
* Docker

#### 1.1 Compile and package

In project root:

```
./mvnw clean install
```

or, if using Maven directly

```
mvn clean install
```

#### 1.2 Place dataset

Place the downloaded dataset in:

```
src/main/resources/META-INF/resources
```

#### 1.3 Run the service

Start the application using Quarkus:

```
./mvnw quarkus:dev
```

or, if using Maven directly

```
mvn quarkus:dev
```

---

### 2. With Kind

#### 2.0 Prerequisites

* Docker
* Kind
* kubectl
* Maven
* Make

#### 2.1 Place dataset

Place dataset locally inside the folder: `/tmp/enron_data`, for example:

```
mkdir /tmp/enron_data
mv enron_mail_20150507.tar.gz /tmp/enron_data
```

#### 2.2 Quick start

In project root:

```
make build-start
```

This will:

1. Build the Quarkus application
2. Build Docker image
3. Create Kind cluster
4. Load Docker image into cluster
5. Deploy PostgreSQL
6. Deploy enron ingestion service
7. Configure persistent volume used to mount local data

Check and wait until pods are ready:

```
kubectl get pods
```

Then:

```
make port-forward
```

Service will then be available at:

```
http://localhost:8080
```

## Makefile commands

| Command               | Description                      |
|-----------------------|----------------------------------|
| `make build`          | Build application + Docker image |
| `make create-cluster` | Create Kind cluster              |
| `make load-image`     | Load Docker image into cluster   |
| `make deploy`         | Deploy all Kubernetes resources  |
| `make port-forward`   | Expose service locally           |
| `make logs`           | Tail service logs                |
| `make delete`         | Delete cluster                   |
| `make build-start`    | Full setup from scratch          |
| `make start`          | Full setup without 'build' step  |