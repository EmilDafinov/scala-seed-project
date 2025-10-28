load('ext://helm_resource', 'helm_resource', 'helm_repo')

docker_build(
    ref = 'docker.io/library/scala-seed-project-flyway',
    context = '.',
    only=['./flyway/'],
    dockerfile = 'flyway/Dockerfile'
)

custom_build(
  ref = 'docker.io/library/scala-seed-project',
  env = {
    'SBT_NATIVE_CLIENT': 'true'
  },
  command = 'sbt "set version := \\"$EXPECTED_TAG\\";Docker/publishLocal" ',
  deps = ['./src/main/']
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
  port_forwards=['9000', '5005'],
  labels = ['app']
)

k8s_resource(
  workload='postgres-deployment',
  port_forwards=['5433:5432'],
  labels = ['infra']
)

local_resource(
    name = 'unit_tests',
    cmd = 'sbt test',
    env = {
        'SBT_NATIVE_CLIENT': 'true'
    },
    deps = ['./src/main/', './src/test/'],
    labels = ['testing']
)