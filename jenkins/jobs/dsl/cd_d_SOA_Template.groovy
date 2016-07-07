def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

freeStyleJob("${PROJECT_NAME}/Continuous_Delivery/Job_Templates/SOA_Job_Template") {
    logRotator(-1, 10)
    parameters {
	stringParam('dsl_soa_job_env', 'dev', '')
	stringParam('dsl_soa_job_env_root_dir', '/u01/jenkins/CI_Artifacts', '')
	stringParam('dsl_soa_job_platform', '12c', '')
	stringParam('dsl_soa_job_type', 'SOA', '')
	stringParam('dsl_soa_job_platform_version', '12.1.3.0.0', '')
	stringParam('dsl_soa_job_composite_name', 'CSFResubmissionBatchControlProc', '')
	stringParam('dsl_soa_job_composite_partition_name', 'CommonServices', '')
	stringParam('dsl_soa_job_composite_revision', '1.0', '')
	stringParam('dsl_soa_job_project_url', 'git@gitlab:${WORKSPACE_NAME}/soacsf.git', '')
    stringParam('dsl_soa_job_composite_application_name', 'ReSubmission', '')
    stringParam('dsl_soa_job_domain_name', 'ReusableCode', '')
    stringParam('dsl_soa_job_application_name', 'SOACSF', '')
	
	environmentVariables {
        env('WORKSPACE_NAME',workspaceFolderName)
        env('PROJECT_NAME',projectFolderName)
    }
    }
  
    label('fmw')
	
	scm {
        git {
            remote {
				url("git@gitlab:${WORKSPACE_NAME}/ci_artifacts.git")
                credentials('adop-jenkins-master')
                branch("*/master")
            }
            extensions {
              relativeTargetDirectory('/u01/jenkins/CI_Artifacts')
            }
        }
	  }    
  
  
	steps {
	
      dsl {
        
        text (''' 
 
		def dsl_job_name= dsl_soa_job_env + '_'+ dsl_soa_job_platform + '_'+ dsl_soa_job_type + '_' + dsl_soa_job_platform_version + '_' + dsl_soa_job_application_name + '_'+dsl_soa_job_composite_name
		def workspaceFolderName = "${WORKSPACE_NAME}"
		def projectFolderName = "${PROJECT_NAME}"
		
		freeStyleJob("${PROJECT_NAME}/Continuous_Delivery/$dsl_job_name") {
    		parameters {
			 	stringParam('afpo_env', dsl_soa_job_env, 'Enter the environment abbreviation')
        		stringParam('afpo_env_root_dir', dsl_soa_job_env_root_dir, 'Enter the path for ENV root directory')
				stringParam('afpo_platform', dsl_soa_job_platform, 'Enter the FMW Platform')
				stringParam('afpo_job_type', dsl_soa_job_type, 'my description')
				stringParam('afpo_platform_version', dsl_soa_job_platform_version, 'my description')
        		stringParam('afpo_application_name', dsl_soa_job_application_name, 'my description')
				stringParam('composite_name', dsl_soa_job_composite_name, 'my description')
        		stringParam('composite_partition_name', dsl_soa_job_composite_partition_name, 'my description')
				stringParam('composite_revision', dsl_soa_job_composite_revision, 'my description')
				stringParam('composite_application_name', dsl_soa_job_composite_application_name, 'my description')
				
				environmentVariables {
					env('WORKSPACE_NAME',workspaceFolderName)
					env('PROJECT_NAME',projectFolderName)
				}
    		}

	    label('fmw')

		scm {
        	git {
         		remote {
                	url(dsl_soa_job_project_url)
                	credentials('adop-jenkins-master')
                	branch("*/master")
            		}
          		
				extensions {
                	relativeTargetDirectory("$WORKSPACE")
            		}
        	}
    	}
	
		steps {
      
      		shell (''\'
				    #!/bin/bash 
                    cp -r /workspace/${PROJECT_NAME}/Continuous_Delivery/Job_Templates/SOA_Job_Template/$afpo_job_type/$afpo_platform_version/$composite_application_name/** $WORKSPACE
                    cp -r /workspace/${PROJECT_NAME}/Continuous_Delivery/Job_Templates/SOA_Job_Template/$afpo_job_type/$afpo_platform_version/$composite_application_name/.adf $WORKSPACE
				   '\'')
        
      
      		ant {
            	targets(['processTokens'])
            	props('afpo.env': '$afpo_env', 'afpo.platform': '$afpo_platform' ,'afpo.job.type': '$afpo_job_type','afpo.platform.version': '$afpo_platform_version','afpo.env.root.dir': '$afpo_env_root_dir','token.workspace.dir':'${WORKSPACE}','afpo.application.name' :'$afpo_application_name'  )
            	buildFile('$afpo_env_root_dir' + '/Build/'+ '$afpo_platform' +'/' +'$afpo_job_type' + '/' + '$afpo_platform_version' + '/pre-build.xml')
            	antInstallation('SOAant')
        		}
        
			ant {
            	targets(['checkbuild ', 'deploy'])
            	props('afpo.env': '$afpo_env', 'afpo.platform': '$afpo_platform' ,'afpo.job.type': '$afpo_job_type','afpo.platform.version': '$afpo_platform_version','afpo.env.root.dir': '$afpo_env_root_dir','composite.name': '$composite_name','composite.partition.name': '$composite_partition_name','composite.revision': '$composite_revision','composite.workspace':'${WORKSPACE}'  )
            	buildFile('$afpo_env_root_dir' + '/Build/'+ '$afpo_platform' +'/' +'$afpo_job_type' + '/' + '$afpo_platform_version' + '/build.xml')
            	antInstallation('SOAant')
        		}
    	}

	   }
      
	''')
     
      }
		
	}
}