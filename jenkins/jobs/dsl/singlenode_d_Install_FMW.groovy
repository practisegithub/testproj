freeStyleJob("${PROJECT_NAME}/Environment_Provisioning_Single_Node/Install_FMW") {
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
ansible-playbook ${WORKSPACE}/oracle-fmw-12/fusionmiddleware.yml -i ${WORKSPACE}/ec2-create/jenkins-hosts -e "host_group=adminserver soa_flag=${SOAFlag} bpm_flag=${BPMFlag} b2b_flag=${B2BFlag} ess_flag=${ESSFlag} bam_flag=${BAMFlag} osb_flag=${OSBFlag} mft_flag=${MFTFlag}" -t "create_templates_prov,create_templates_conf,create_templates_tmp,run_install"

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
	    trigger('Configure_FMW') {
	  		condition('SUCCESS')
        parameters {
          currentBuild()
	  			predefinedProp('CUSTOM_WORKSPACE', '$WORKSPACE')
        }
			}
		}
	}
}