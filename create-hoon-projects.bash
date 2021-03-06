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