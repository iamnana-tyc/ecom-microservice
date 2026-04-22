#!/bin/bash

cd ../..

set -e

services=(
  configserver
  eureka
  order
  user
  product
  gateway
  notification
)

for s in "${services[@]}"; do
  echo "Building $s"

  cd "$s"

  ./mvnw spring-boot:build-image -DskipTests || {
    echo "Retrying $s..."
    ./mvnw spring-boot:build-image -DskipTests
  }

  cd ..
done


##!/bin/bash
#
#cd ../..
#
#### BUILD ALL SERVICES
#cd configserver && ./mvnw spring-boot:build-image -DskipTests && cd ..
#cd eureka && ./mvnw spring-boot:build-image -DskipTests && cd ..
#cd order && ./mvnw spring-boot:build-image -DskipTests && cd ..
#cd user && ./mvnw spring-boot:build-image -DskipTests && cd ..
#cd product && ./mvnw spring-boot:build-image -DskipTests && cd ..
#cd gateway && ./mvnw spring-boot:build-image -DskipTests && cd ..
#cd notification && ./mvnw spring-boot:build-image -DskipTests && cd ..