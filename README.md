1. 윈도우 환경에서 실습 시 run.sh 스크립트의 line separator 가 달라서 도커에서 스크립트가 실행되지 않을 수 있다. 

다음 사이트를 참조하면 해결하도록 하자

- https://seokr.tistory.com/670
- https://smallgiant.tistory.com/19

2. postgres DB의 경우 
docker/common 의 docker-compose 를 통해서 실행한다

3. 개별 application 테스트를 위해서 
local machine hosts 파일에 127.0.0.1 database host를 추가한다
