MSA 프로젝트 생성
1. create-hoon-projects.bash 실행
2. 각 마이크로서비스를 따로 빌드 할 수 있다.
```
cd microservices/product-composite-service; ./gradlew build; cd -; \ 
cd microservices/product-service;           ./gradlew build; cd -; \
cd microservices/recommendation-service;    ./gradlew build; cd -; \
cd microservices/review-service;            ./gradlew build; cd -;
```
3. 그래들에 멀티 프로젝트 빌드 설정
- settings.gradle 파일을 생성하고 그래들이 빌드할 프로젝트를 입력한다.
- settings.gradle 파일
```aidl
include ':microservices:product-service'
include ':microservices:review-service'
include ':microservices:recommendation-service'
include ':microservices:product-composite-service'
```
4. product-service 프로젝트에서 그래들 실행 파일을 복사한다. 복사한 파일은 멀티 프로젝트 빌드에서 재사용한다.
```aidl
cp -r microservices/product-service/gradle .
cp microservices/product-service/gradlew .
cp microservices/product-service/gradlew.bat .
cp microservices/product-service/.gitignore . 
```
5. 이제 하나의 커맨드로 전체 마이크로서비스를 빌드할 수 있다.
```aidl
hoon@namgunghun-ui-Macmini study % ls
create-hoon-projects.bash	gradlew				microservices
gradle				gradlew.bat			settings.gradle

cd /Users/hoon/dev/study
./gradlew build
```


