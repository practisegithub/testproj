// Kickoff Job for Docker provisioning
def env = System.getenv()

def keyPair = env['KEY_PAIR'] ?: "your_aws_key_pair_here"
def vpcId = env['VPC_ID'] ?: "your_vpc_id_here"
def subnetId = env['DEFAULT_PUBLIC_SUBNET_ID'] ?: "your_vpc_subnet_id_here"
def amiId = env['DEFAULT_AWS_LINUX_AMI'] ?: "region_aws_linux_ami_id_here"
def awsRegion = env['AWS_REGION'] ?: "your_aws_region_id_here"

freeStyleJob("${PROJECT_NAME}/Environment_Provisioning_Docker/Set_Environment_Parameters") {
  logRotator(-1, 10)
  parameters {
	  stringParam('ENVIRONMENT','demo', '')
	  stringParam('AWS_REGION', "$awsRegion", '')
	  stringParam('AWS_KEY_PAIR', "$keyPair", '')
	  stringParam('AWS_SUBNET_ID', "$subnetId", '')
	  stringParam('AWS_AMI_ID', "$amiId", '')
	  stringParam('AWS_VPC_ID', "$vpcId", '')
	  stringParam('VOLUME_SIZE', '200', '')
	  stringParam('INSTANCE_TYPE', 'm4.xlarge', '')
	  stringParam('REMOTE_SUDO_USER', 'ec2-user', '')
    stringParam('EMAIL_NOTIFICATION_LIST', "", 'Email address list separated by comma to be notified when environment provisioning is completed')
  }
  label('ansible')
	wrappers {
    preBuildCleanup() 
	colorizeOutput('css')
	}
//download ec2-create and docker-oracle-fmw
  multiscm {
    git {
      remote {
		    url("git@gitlab:${WORKSPACE_NAME}/ec2-create.git")
        credentials('adop-jenkins-master')
        branch("*/master")
      }
      extensions {
        relativeTargetDirectory('$WORKSPACE/ec2-create')
      }
    }
	  git {
      remote {
			  url("git@gitlab:${WORKSPACE_NAME}/docker-oracle-fmw.git")
        credentials('adop-jenkins-master')
        branch("*/master")
      }
      extensions {
        relativeTargetDirectory('$WORKSPACE/docker-oracle-fmw')
      }
    }
  }
  
  steps {
     shell ('''#!/bin/bash

## Temporary fix - make sure that ansible version is 2.1 
if [[ $(rpm -qa ansible | grep 2.1) == 1 ]]
then
  yum -y update
fi ''')
  }
  publishers {
    downstreamParameterized {
	    trigger('Launch_EC2_Instance') {
		    condition('SUCCESS')
        parameters {
         currentBuild()
				 predefinedProp('CUSTOM_WORKSPACE', '$WORKSPACE')
       }
		 }
	  }
	}
}