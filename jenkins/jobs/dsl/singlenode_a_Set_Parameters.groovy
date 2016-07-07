// Kickoff Job for Single node environment provisioning
def env = System.getenv()

def keyPair = env['KEY_PAIR'] ?: "your_aws_key_pair_here"
def vpcId = env['VPC_ID'] ?: "your_vpc_id_here"
def subnetId = env['DEFAULT_PUBLIC_SUBNET_ID'] ?: "your_vpc_subnet_id_here"
def amiId = env['DEFAULT_RHEL_AMI'] ?: "region_redhat_ami_id_here"
def awsRegion = env['AWS_REGION'] ?: "your_aws_region_id_here"

freeStyleJob("${PROJECT_NAME}/Environment_Provisioning_Single_Node/Set_Environment_Parameters") {
  logRotator(-1, 10)
  label('ansible')
	wrappers {
    preBuildCleanup() 
	  sshAgent('ansible-user-key')
	  credentialsBinding {
	  	usernamePassword("EC2_ACCESS_KEY","EC2_SECRET_KEY","aws-environment-provisioning")
    } 
	  colorizeOutput('css')
	}
  parameters {
	  stringParam('ENVIRONMENT','sample', '')
	  stringParam('AWS_SUBNET_ID', "$subnetId", '')
	  stringParam('AWS_DB_AMI_ID', "$amiId", '')
	  stringParam('AWS_RHEL_AMI_ID', "$amiId", '')
	  stringParam('AWS_VPC_ID', "$vpcId", '')
	  stringParam('AWS_REGION', "$awsRegion", '')
	  stringParam('AWS_KEY_PAIR', "$keyPair", '')
	  booleanParam('SOAFlag', true, 'uncheck to disable')
	  booleanParam('BPMFlag', true, 'uncheck to disable')
	  booleanParam('B2BFlag', true, 'uncheck to disable')
	  booleanParam('ESSFlag', true, 'uncheck to disable')
	  booleanParam('BAMFlag', true, 'uncheck to disable')
	  booleanParam('MFTFlag', true, 'uncheck to disable')
  }
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
			  url("git@gitlab:${WORKSPACE_NAME}/oracle-db-12c.git")
        credentials('adop-jenkins-master')
        branch("*/master")
      }
      extensions {
        relativeTargetDirectory('$WORKSPACE/oracle-db-12c')
      }
    }
	  git {    
      remote {
			  url("git@gitlab:${WORKSPACE_NAME}/oracle-fmw-12.git")
        credentials('adop-jenkins-master')
        branch("*/master")
      }
      extensions {
        relativeTargetDirectory('$WORKSPACE/oracle-fmw-12')
      }
    }
  }
  publishers {
	  downstreamParameterized {
		  trigger('Create_Oracle_Database_12c') {
		    condition('SUCCESS')
        parameters {
          currentBuild()
		  	  predefinedProp('CUSTOM_WORKSPACE', '$WORKSPACE')
        }
		  }
	  }
	}
}