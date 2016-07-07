freeStyleJob("${PROJECT_NAME}/Environment_Provisioning_Single_Node/Create_Oracle_Database_12c") {
  logRotator(-1, 10)
	customWorkspace('$CUSTOM_WORKSPACE')
  label('ansible')
	wrappers {
	  sshAgent('ansible-user-key')
	  credentialsBinding {
		  usernamePassword("EC2_ACCESS_KEY","EC2_SECRET_KEY","aws-environment-provisioning")
		} 
	  colorizeOutput('css')
	}
  steps {
		shell('''#!/bin/bash
export ANSIBLE_FORCE_COLOR=true 

#Seoul Region Fix for not having m3 type instances
if [[ $AWS_REGION == 'ap-northeast-2' ]]; then
  INSTANCE_TYPE="m4.xlarge"
else
  INSTANCE_TYPE="m3.xlarge"
fi

cd ec2-create

rm -rf jenkins-hosts
#Ec2-create
ansible-playbook provision.yml -e "instance_type=${INSTANCE_TYPE} instance_name=${ENVIRONMENT}_DatabaseServer aws_region=${AWS_REGION} key_name=${AWS_KEY_PAIR} vpc_subnet_id=${AWS_SUBNET_ID} ami_id=${AWS_DB_AMI_ID} multinode=false volume_size=100 env=${ENVIRONMENT} type=db volume_device_name='/dev/sda1' vpc_id=${AWS_VPC_ID}" 

# Error handling
if [ $? -gt 0 ]
then
 ansible-playbook terminate_instances.yml
 exit 1
fi 

#oracle-db-12c
ansible-playbook ${WORKSPACE}/oracle-db-12c/database.yml -i ${WORKSPACE}/ec2-create/jenkins-hosts -e "host_group=dbserver"

# Error handling
if [ $? -gt 0 ]
then
 ansible-playbook ${WORKSPACE}/ec2-create/terminate_instances.yml
 exit 1
fi 
 			
			''')
 	
  }
  publishers {
    downstreamParameterized {
		  trigger('Create_FMW_Environment') {
			  condition('SUCCESS')
        parameters {
            currentBuild()
						predefinedProp('CUSTOM_WORKSPACE', '$WORKSPACE')
        }
		  }
	  }
  }
}