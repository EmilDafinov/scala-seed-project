load('ext://helm_resource', 'helm_resource', 'helm_repo')
docker_build(
    ref = 'docker.io/library/scala-seed-project-flyway',
    context = '.',
    only=['./flyway/'],
    dockerfile = 'flyway/Dockerfile'
)
custom_build(
  'docker.io/library/scala-seed-project',
  'sh ./src/main/script/buildApplicationImage_tilt.sh $EXPECTED_TAG',
  ['./src/main/']
)


helm_repo('strimzi-repo', 'https://strimzi.io/charts')
helm_repo('kafka-ui-repo', 'https://ui.charts.kafbat.io/')

helm_resource(
    'strimzi-kafka-operator',
    'strimzi-repo/strimzi-kafka-operator',
    resource_deps=['strimzi-repo'],
    flags=['--version=0.46.0']
)

helm_resource(
    'kafka-ui',
    'kafka-ui-repo/kafka-ui',
    resource_deps=['kafka-ui-repo'],
    deps=[
        './kafka-ui-values.yaml'
    ],
    flags=[
        '--version=1.5.0',
        '--values=./kafka-ui-values.yaml',
    ]
)

k8s_yaml(
    helm(
        './scala-seed-project',
        set = [
            'project.rootdir=' + os.environ.get('PWD', ''),
            'scaffold=false'
        ]
    )
)
k8s_resource(
  workload='scala-seed-project',
  port_forwards=['9000', '5005']
)
k8s_resource(
  workload='postgres-deployment',
  port_forwards=['5433:5432']
)
k8s_resource(
  workload='kafka-ui',
  port_forwards=['8080']
)
local_resource(
    name = 'unit_tests',
    cmd = 'sbt test',
    deps = ['./src/main/', './src/test/']
)