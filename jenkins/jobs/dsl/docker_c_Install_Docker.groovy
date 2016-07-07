freeStyleJob("${PROJECT_NAME}/Environment_Provisioning_Docker/Install_Docker") {
  logRotator(-1, 10)
  customWorkspace('$CUSTOM_WORKSPACE')
  label('ansible')
	wrappers {
	  sshAgent('ansible-user-key')
	  colorizeOutput('css')
	}
	
  steps {
		shell('''#!/bin/bash -e

export ANSIBLE_FORCE_COLOR=true

cd docker-oracle-fmw

export TARGET_SERVER_IP=$(cat ../ec2-create/${ENVIRONMENT}_fmw_docker_host.ip)

cat > hosts <<- EOF
[docker_host]
${TARGET_SERVER_IP}
EOF

ansible-playbook site.yml -i hosts -u ${REMOTE_SUDO_USER} ''')
  }
  
  publishers {
    downstreamParameterized {
		trigger('Deploy_Docker_Containers') {
			condition('SUCCESS')
            parameters {
              currentBuild()
            }
		}
	}
  }
}