1. 윈도우 환경에서 실습 시 run.sh 스크립트의 line separator 가 달라서 도커에서 스크립트가 실행되지 않을 수 있다. 

다음 사이트를 참조하면 해결하도록 하자

- https://seokr.tistory.com/670
- https://smallgiant.tistory.com/19

2. postgres DB의 경우 
docker/common 의 docker-compose 를 통해서 실행한다

3. 개별 application 테스트 시 database를 찾기 위해서 
C:\Windows\System32\drivers\etc\hosts 파일에 127.0.0.1 database 를 추가한다
(docker 사용 시 service name 기반으로 찾아감)

4. zuul route filter 테스트 시 organization-new 를 찾기 위해서 
C:\Windows\System32\drivers\etc\hosts 파일에 127.0.0.1 orgservice-new 를 추가한다

5. OAuth2RestTemplate 사용 시 loadbalalced 되지 않으므로 
license service 에서 zuul service 찾기 위해서 
C:\Windows\System32\drivers\etc\hosts 파일에 127.0.0.1 zuulserver 를 추가한다

6. redis server 실행 시 hostname 이 redis 로 설정되어 있으므로 
C:\Windows\System32\drivers\etc\hosts 파일에 127.0.0.1 redis 를 추가한다

7. 개별 application 테스트 시 papertrail logging을 위해
NXLog 설치한다.

8. log 수집 위해 logging.file 프로퍼티 추가한다.
(logback 설정을 사용하는 편이 낫다)

9. zipkin server 실행 시 hostname 이 zipkin 으로 설정되어 있으므로 
C:\Windows\System32\drivers\etc\hosts 파일에 127.0.0.1 zipkin 을 추가한다


