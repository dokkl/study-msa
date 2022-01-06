#!/usr/bin/env bash

mkdir microservices
cd microservices

spring init \
--boot-version=2.6.2.RELEASE \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=product-service \
--package-name=com.hoon.msa.core.product \
--groupId=com.hoon.msa.core.product \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
product-service

spring init \
--boot-version=2.6.2.RELEASE \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=review-service \
--package-name=com.hoon.msa.core.review \
--groupId=com.hoon.msa.core.review \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
review-service

spring init \
--boot-version=2.6.2.RELEASE \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=recommendation-service \
--package-name=com.hoon.msa.core.recommendation \
--groupId=com.hoon.msa.core.recommendation \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
recommendation-service

spring init \
--boot-version=2.6.2.RELEASE \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=product-composite-service \
--package-name=com.hoon.msa.composite.product \
--groupId=com.hoon.msa.composite.product \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
product-composite-service

cd ..

mkdir spring-cloud
cd spring-cloud

spring init \
--boot-version=2.6.2.RELEASE \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=eureka-server \
--package-name=com.hoon.springcloud \
--groupId=com.hoon.springcloud \
--dependencies=cloud-eureka-server \
--version=1.0.0-SNAPSHOT \
eureka-server

spring init \
--boot-version=2.6.2.RELEASE \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=gateway \
--package-name=com.hoon.springcloud.gateway \
--groupId=com.hoon.springcloud.gateway \
--dependencies=cloud-gateway \
--version=1.0.0-SNAPSHOT \
gateway

spring init \
--boot-version=2.6.2.RELEASE \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=authorization-server \
--package-name=com.hoon.springcloud \
--groupId=com.hoon.springcloud \
--dependencies=web,actuator,security \
--version=1.0.0-SNAPSHOT \
authorization-server