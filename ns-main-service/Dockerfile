# Copyright 2017-present, Nam Seob Seo
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM openjdk:17-jdk-slim

ENV REFRESHED_AT=2018-11-22 \
    PORT=8080 \
    JVM_MAX_HEAP=1G \
    JVM_MIN_HEAP=1G \
    SERVICE_VERSION=0.1.0-SNAPSHOT \
    SERVICE_NAME=svg-converter \
    KEYSTORE_PATH=/opt/ns/svg-service/keystore.jks

RUN mkdir -p /opt/ns/svg-service

COPY build/libs/ns-main-service-0.1.0-SNAPSHOT.jar /opt/ns/svg-service/${SERVICE_NAME}-${SERVICE_VERSION}.jar
COPY docker-data/start.sh /opt/ns/svg-service/start.sh
RUN chmod 755  /opt/ns/svg-service/start.sh

WORKDIR /opt/ns/svg-service

EXPOSE ${PORT}

ENTRYPOINT ["/opt/ns/svg-service/start.sh"]
