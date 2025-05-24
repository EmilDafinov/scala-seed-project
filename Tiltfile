docker_build(
    ref = 'docker.io/library/scala-seed-project-flyway',
    context = '.',
    only=['./flyway/'],
    dockerfile = 'flyway/Dockerfile'
)
custom_build(
  'docker.io/library/scala-seed-project',
  'sh ./src/main/script/buildApplicationImage.sh $EXPECTED_REF',
  ['./src/main/']
)


load('ext://helm_resource', 'helm_resource', 'helm_repo')
helm_repo('strimzi', 'https://strimzi.io/charts')
helm_repo('kafka-ui', 'https://ui.charts.kafbat.io/')

helm_resource(
    'strimzi-kafka-operator',
    'strimzi/strimzi-kafka-operator',
    resource_deps=['strimzi'],
    flags=['--version=0.46.0']
)

helm_resource(
    'kafka-ui-res',
    'kafka-ui/kafka-ui',
    resource_deps=['kafka-ui'],
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
  workload='kafka-ui-res',
  port_forwards=['8080']
)
