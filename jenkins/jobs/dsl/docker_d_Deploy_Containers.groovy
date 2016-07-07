freeStyleJob("${PROJECT_NAME}/Environment_Provisioning_Docker/Deploy_Docker_Containers") {
  logRotator(-1, 10)
  customWorkspace('$CUSTOM_WORKSPACE')
  label('ansible')
	wrappers {
	  sshAgent('ansible-user-key')
	  colorizeOutput('css')
	}
  scm {
    git {
      remote {
		    url("git@gitlab:${WORKSPACE_NAME}/fmw-docker-build.git")
        credentials('adop-jenkins-master')
        branch("*/master")
      }
      extensions {
        relativeTargetDirectory('fmw-docker-build')
      }
    }
  }
  
  steps {
		shell('''#!/bin/bash -e

export TIMEOUT=${TIMEOUT:-2400}
export DOCKER_HOST=tcp://${ENV_PRIVATE_IP}:2375

if [[ $(docker network ls | grep fmw-network | wc -l) == 0 ]]
then
 docker network create fmw-network
fi

cd fmw-docker-build
docker-compose up -d

## Insert command here to wait for fmw to be up and running
START_TIME=$(date +"%s")

docker logs -f soa-host | while read line
do

  echo $line
  ELAPSED_TIME=$(($(date +"%s") - $START_TIME))

  if [[ $(curl -I -s ${ENV_PRIVATE_IP}:7001/console | grep HTTP | awk '{print $2}') == 302 ]]
  then
    echo "Environment is up and running.. "
    exit 0
  fi
  
  if [[ "$ELAPSED_TIME" -gt $TIMEOUT ]]
  then
    echo "Elapsed time has exceeded the Deploy timeout - ${TIMEOUT}(s). Script will exit with error now.."
    exit 1
  fi   

done

cat > release_note.txt <<- EOF
===================================================================
Environment Details:
Environment Tag: ${ENVIRONMENT}
===================================================================
Oracle Fusion Middleware v12.1.3
===================================================================
Weblogic Application Public IP: ${ENV_PUBLIC_IP}
Weblogic Application Private IP: ${ENV_PRIVATE_IP}
Weblogic Application Port: 7001
Weblogic public url: http://${ENV_PUBLIC_IP}:7001/console
Weblogic private url: http://${ENV_PRIVATE_IP}:7001/console
Weblogic default admin user: weblogic
Weblogic default admin password: wlsAFPO#1
===================================================================
Oracle Database v12c
===================================================================
Database Public IP: ${ENV_PUBLIC_IP}
Database Private IP: ${ENV_PRIVATE_IP}
Database Port: 1521
Database default SYS user: SYS
Database default SYS password: rcuAFPO#1
===================================================================
EOF

 ''')
  }
  publishers {      
    extendedEmail {
      recipientList('$EMAIL_NOTIFICATION_LIST')
      defaultSubject('Oracle Fusion Middleware Environment Provisioning Successful!')
      defaultContent("Hi," +
				"\n\nThe creation of " + '${ENVIRONMENT}' + " environment has been completed. Please see the attached release note text file for more information on how to access the environment." +
				"\n\nRegards," +
				"\nADOP Jenkins")
      contentType('text/plain')
			attachmentPatterns('release_note.txt')
      triggers {
          success()
      }
    }
  }
}