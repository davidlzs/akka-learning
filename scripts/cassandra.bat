docker network create cassandra-network
docker run --name cassandra --network cassandra-network -p 9042:9042 -d cassandra