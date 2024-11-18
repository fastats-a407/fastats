**build 환경**

BACKEND           gradle:7.6-jdk17        포트 8080
FRONTEND          node:20-alpine          포트 3000
ELASTICSEARCH     8.7.1                   포트 9200
KIBANA            8.7.1                   포트 5601
OLLAMA            icodo/llama3-ko         포트 11434
MYSQL                                     포트 3306
REDIS                                     포트 6379


**이미지 build**

1. spring image build
    + back/fastats 폴더로 이동 후, `docker build -t thundercloud3/fastats:latest -f Dockerfile .` 명령어 실행

2. react image build
    + front/fast 폴더로 이동 후, `docker build -t thundercloud3/front:latest -f Dockerfile .` 명령어 실행

**elasticsearch, kibana, logstash 비밀번호 설정**

1. elasticsearch 비밀번호 설정
    + elasticsearch 컨테이너에 접속하여 `/usr/share/elasticsearch/bin/elasticsearch-reset-password -u elastic -i` 명령어 실행 후, 원하는 비밀번호로 설정

2. kibana 비밀번호 설정
    + elasticsearch 컨테이너에 접속하여 `/usr/share/elasticsearch/bin/elasticsearch-reset-password -u kibana_system -i` 명령어 실행 후, 원하는 비밀번호로 설정

3. logstash 비밀번호 설정
    + elasticsearch 컨테이너에 접속하여 `/usr/share/elasticsearch/bin/elasticsearch-reset-password -u logstash_system -i` 명령어 실행 후, 원하는 비밀번호로 설정

**필수 설정 파일 마운트**

+ elasticsearch.yml 
+ kibana.yml
+ logstash.yml
+ application-dev.yml
+ userdict_ko.txt
+ elastic-stack-ca.p12
+ http.p12
+ elasticsearch-node1.p12
+ elasticsearch.keystore
+ mysql-connector-j-8.0.33.jar
+ pipelines.yml
+ default.conf
+ logstash.p12
+ elasticsearch-ca.pem
+ init.sql
+ kibana.p12
+ elasticsearch-cert.crt

해당 설정 및 인증 파일들이 docker-compose.yml 파일과 같은 폴더에 존재해야 함.


**docker compose up**

1. exec 폴더에 존재하는 docker-compose.yml 파일을 대상으로 exec 폴더에서 docker compose up -d 명령어를 실행

2. spring 컨테이너는 elasticsearch 컨테이너가 완전히 동작한 이후에야 실행이 될 수 있기 때문에, 실패 이후의 자동 실행 3~4번 이후 정상 작동할 것이다.

