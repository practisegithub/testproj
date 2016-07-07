freeStyleJob("${PROJECT_NAME}/Environment_Provisioning_Docker/Launch_EC2_Instance") {
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
export INSTANCE_NAME=${ENVIRONMENT}_fmw_docker_host
cd ec2-create

ansible-playbook provision.yml --extra-vars "instance_name=${INSTANCE_NAME} aws_region=${AWS_REGION} key_name=${AWS_KEY_PAIR} vpc_subnet_id=${AWS_SUBNET_ID} ami_id=${AWS_AMI_ID} instance_type=${INSTANCE_TYPE} volume_size=${VOLUME_SIZE} vpc_id=${AWS_VPC_ID} volume_device_name='/dev/xvda' multinode=false type=docker env=${ENVIRONMENT}"

if [ $? -gt 0 ]
then
  ansible-playbook terminate_instances.yml
  exit 1
fi

rm -f env.props

if [ -f ${INSTANCE_NAME}_public.ip ]
then
  echo "ENV_PUBLIC_IP=$(cat ${INSTANCE_NAME}_public.ip)" >> ${WORKSPACE}/env.props
fi
echo "ENV_PRIVATE_IP=$(cat ${INSTANCE_NAME}.ip)" >> ${WORKSPACE}/env.props

    ''')
  }
  
  publishers {
    downstreamParameterized {
	  	trigger('Install_Docker') {
		 	  condition('SUCCESS')
          parameters {
            currentBuild()
					  propertiesFile('env.props')
          }
		  }
	  }
  }

}