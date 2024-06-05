docker build --pull --rm -f "./Dockerfile" -t gestiondeprojet-loginapp:latest "."

docker network create -d bridge lb-net

docker run -d -p 8081:8081 --net=lb-net --name loadBalancer gestiondeprojet-loginapp

docker run -d -p--net=lb-net --name registry -e HOSTNAME=registry gestiondeprojet-loginapp
docker run -d --net=lb-net --name worker1 -e HOSTNAME=worker1 gestiondeprojet-loginapp
docker run -d --net=lb-net --name worker2 -e HOSTNAME=worker2 gestiondeprojet-loginapp
docker run -d --net=lb-net --name worker3 -e HOSTNAME=worker3 gestiondeprojet-loginapp
docker run -d --net=lb-net --name worker4 -e HOSTNAME=worker4 gestiondeprojet-loginapp