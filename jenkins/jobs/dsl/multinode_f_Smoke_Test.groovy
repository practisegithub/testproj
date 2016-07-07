freeStyleJob("${PROJECT_NAME}/Environment_Provisioning_Multi_Node/Smoke_Test") {
  customWorkspace('$CUSTOM_WORKSPACE')
  logRotator(-1, 10)
  label('ansible')
  steps { 
    shell('''#!/bin/bash
	
WORK_NAME=$(echo "${WORKSPACE}" | awk -F'/' '{print $3}')
PROJECT_NAME=$(echo "${WORKSPACE}" | awk -F'/' '{print $4}')
ENVIRONMENT_NAME=$(echo "${WORKSPACE}" | awk -F'/' '{print $5}')
                
SERVER_INSTANCEIP=$(cat ${WORKSPACE}/ec2-create/${ENVIRONMENT}_AdminServer.ip)
echo ${PUBLIC_IP}
echo "BASEURL=http://${SERVER_INSTANCEIP}:7001/console" > /tmp/fmwConfig.properties
echo "USER=Uname" >> /tmp/fmwConfig.properties
echo "PASSWORD=Pword" >> /tmp/fmwConfig.properties
echo "NODEURL=http://selenium-hub:4444/wd/hub" >> /tmp/fmwConfig.properties
echo "REPORTLOCATION=${WORKSPACE}/" >> /tmp/fmwConfig.properties
echo "IMAGELOCATION=${WORKSPACE}/" >> /tmp/fmwConfig.properties
echo "IMAGEMAP=http://${PUBLIC_IP}/jenkins/job/${WORK_NAME}/job/${PROJECT_NAME}/job/${ENVIRONMENT_NAME}/view/ENVIRONMENT_BUILD/job/Smoke_Test/ws/" >> /tmp/fmwConfig.properties

cd ${WORKSPACE}/oracle-fmw-12
java -jar FMW_test.jar
			
mv $WORKSPACE/Report.html $WORKSPACE/index.html
rm -fr /tmp/fmwConfig.properties

		''')
    }
  publishers {
		publishHtml {
      report('${WORKSPACE}') {
        reportName('Report.html')
      }
    }
  }
}