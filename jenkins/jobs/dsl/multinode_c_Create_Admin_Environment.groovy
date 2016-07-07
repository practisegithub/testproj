freeStyleJob("${PROJECT_NAME}/Environment_Provisioning_Multi_Node/Create_Admin_Environment") {
    customWorkspace('$CUSTOM_WORKSPACE')
    logRotator(-1, 10)
    label('ansible')
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

# Create a backup of jenkins-hosts file
cp jenkins-hosts jenkins-hosts.bak

#create admin server
ansible-playbook provision.yml -e "instance_type=${INSTANCE_TYPE} aws_region=${AWS_REGION} key_name=${AWS_KEY_PAIR} vpc_subnet_id=${AWS_SUBNET_ID} volume_size=100 env=${ENVIRONMENT} ami_id=${AWS_DB_AMI_ID} multinode=true env=${ENVIRONMENT} instance_name=${ENVIRONMENT}_AdminServer type=admin volume_device_name='/dev/sda1' vpc_id=${AWS_VPC_ID}"

# Error handling
if [ $? -gt 0 ]
then
 ansible-playbook terminate_instances.yml
 exit 1
fi 

#oracle-fmw-12 fmw_host
ansible-playbook ${WORKSPACE}/oracle-fmw-12/fusionmiddleware.yml -i ${WORKSPACE}/ec2-create/jenkins-hosts -e "host_group=adminserver" -t "create_swapfile,prepare,download,extract"

#oracle-fmw-12 fmw_install
ansible-playbook ${WORKSPACE}/oracle-fmw-12/fusionmiddleware.yml -i ${WORKSPACE}/ec2-create/jenkins-hosts -e "host_group=adminserver soa_flag=${SOAFlag} bpm_flag=${BPMFlag} b2b_flag=${B2BFlag} ess_flag=${ESSFlag} bam_flag=${BAMFlag} mft_flag=${MFTFlag}"  -t "create_templates_prov,create_templates_conf,create_templates_tmp,run_install"

#oracle-fmw-12 fmw_config	
ansible-playbook ${WORKSPACE}/oracle-fmw-12/fusionmiddleware.yml -i ${WORKSPACE}/ec2-create/jenkins-hosts -e "host_group=adminserver" -t "run_create_soa_domain,run_rcu_create"

ansible adminserver -i jenkins-hosts -m fetch -a "src=/u01/app/oracle/config/templates/soa_domain_template.jar dest=${WORKSPACE}/templates/soa_domain_template.jar flat=yes" --become-user root --become

#ansible adminserver -i ${WORKSPACE}/ec2-create/jenkins-hosts -a "sudo su - oracle -c 'cd /software/prov/ && ./extend_soa_domain.sh /software/prov/domain.ini'"

			''')
 	
    }
	wrappers {
	sshAgent('ansible-user-key')
	credentialsBinding{
        usernamePassword("EC2_ACCESS_KEY","EC2_SECRET_KEY","aws-environment-provisioning")
					} 
	colorizeOutput('css')
	}
    publishers {
			downstreamParameterized {
					trigger('Create_Soa_Host1_Environment') {
					condition('SUCCESS')
          parameters {
            currentBuild()
						predefinedProp('CUSTOM_WORKSPACE', '$WORKSPACE')				        	
          }
				}
			}
		}
  }
