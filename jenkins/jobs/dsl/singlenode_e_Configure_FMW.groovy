freeStyleJob("${PROJECT_NAME}/Environment_Provisioning_Single_Node/Configure_FMW") {
  customWorkspace('$CUSTOM_WORKSPACE')
  logRotator(-1, 10)
  label('ansible')
  wrappers {
	  sshAgent('ansible-user-key')
	  colorizeOutput('css')
	}
  steps {
		shell('''#!/bin/bash
export ANSIBLE_FORCE_COLOR=true 

#oracle-fmw-12 fmw_install
ansible-playbook ${WORKSPACE}/oracle-fmw-12/fusionmiddleware.yml -i ${WORKSPACE}/ec2-create/jenkins-hosts -e "host_group=adminserver" -t "run_create_soa_domain,run_rcu_create"
ansible adminserver -i ${WORKSPACE}/ec2-create/jenkins-hosts -a "sudo su - oracle -c 'cd /software/prov/ && ./extend_soa_domain.sh /software/prov/domain.ini'"
#ansible-playbook ${WORKSPACE}/oracle-fmw-12/fusionmiddleware.yml -i ${WORKSPACE}/ec2-create/jenkins-hosts -t "run_wlst"

# Error handling
if [ $? -gt 0 ]
then
 rm -rf /u01/app
 exit 1
fi 
 			
			''')
 	
    }
  publishers {
	  downstreamParameterized {
			trigger('Smoke_Test') {
			  condition('SUCCESS')
          parameters {
            currentBuild()
						predefinedProp('CUSTOM_WORKSPACE', '$WORKSPACE')
          }
				}
			}
		}
  }