# Akka Cluster Bootstrap
* [Deployment Consideration](https://doc.akka.io/docs/akka-management/current/bootstrap/index.html)
    - Observe `ClusterSingleton` moving during rolling update
    - Recommended configuration
      `akka.cluster.shutdown-after-unsuccessful-join-seed-nodes = 30s
      akka.coordinated-shutdown.exit-jvm = on`
    - Initial deployment vs following deployment `Experiment: watch cluster formation`
        - `akka.management.cluster.bootstrap.new-cluster-enabled=on` - initial
        - `akka.management.cluster.bootstrap.new-cluster-enabled=off` - following