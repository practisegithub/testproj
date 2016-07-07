def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

freeStyleJob("${PROJECT_NAME}/Continuous_Delivery/Job_Templates/WLS_Job_Template") {
    logRotator(-1, 10)
    parameters {
	stringParam('dsl_wls_job_env', 'dev', '')
	stringParam('dsl_wls_job_env_root_dir', '/u01/jenkins/CI_Artifacts', '')
	stringParam('dsl_wls_job_platform', '12c', '')
	stringParam('dsl_wls_job_type', 'WLS', '')
	stringParam('dsl_wls_job_platform_version', '12.1.3.0.0', '')
	stringParam('dsl_wls_job_application_name', 'SOACSF', '')
	stringParam('dsl_wls_resource_url', 'git@gitlab:${WORKSPACE_NAME}/soacsf.git', '')
	stringParam('dsl_wls_resource_type', 'Datasource', '')
	stringParam('dsl_datasource_db_name', 'fmwdb', '')
	
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
 
		def dsl_job_name= dsl_wls_job_env + '_'+ dsl_wls_job_platform + '_'+ dsl_wls_job_type + '_' + dsl_wls_job_platform_version + '_' + dsl_wls_job_application_name + '_' + dsl_wls_resource_type
		
		def workspaceFolderName = "${WORKSPACE_NAME}"
		def projectFolderName = "${PROJECT_NAME}"
		
		freeStyleJob("${PROJECT_NAME}/Continuous_Delivery/$dsl_job_name") {
    		parameters {
				stringParam('afpo_env', dsl_wls_job_env, 'Enter the environment abbreviation')
        		stringParam('afpo_env_root_dir', dsl_wls_job_env_root_dir, 'Enter the path for ENV root directory')
				stringParam('afpo_platform', dsl_wls_job_platform, 'Enter the FMW Platform')
				stringParam('afpo_job_type', dsl_wls_job_type, 'my description')
				stringParam('afpo_platform_version', dsl_wls_job_platform_version, 'my description')
        		stringParam('afpo_application_name', dsl_wls_job_application_name, 'my description')
				stringParam('afpo_db_name', dsl_datasource_db_name, 'Enter the CI DB alias name for datasource creation only')
				stringParam('wls_resource_type', dsl_wls_resource_type, 'my description')
				
				environmentVariables {
					env('WORKSPACE_NAME',workspaceFolderName)
					env('PROJECT_NAME',projectFolderName)
				}
    		}

	    label('fmw')

		scm {
        
			git {
         
            	remote {
                	url(dsl_wls_resource_url)
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
                    # mv /var/lib/jenkins/workspace/WLS_Job_Template/WLS/12.1.3/JmsAdapter/** $WORKSPACE
					chown -R jenkins. /u01
					cp -r /workspace/${PROJECT_NAME}/Continuous_Delivery/Job_Templates/WLS_Job_Template/$afpo_job_type/12.1.3/$wls_resource_type/** $WORKSPACE
		  	  	  '\'')
              
        	ant {
            	targets(['deploy'])
            	props('afpo.env': '$afpo_env', 'afpo.platform': '$afpo_platform' ,'afpo.job.type': '$afpo_job_type','afpo.platform.version': '$afpo_platform_version','afpo.env.root.dir': '$afpo_env_root_dir','afpo.application.name':'$afpo_application_name','afpo.wls.resource':'${WORKSPACE}/wls.properties')
	            buildFile('$afpo_env_root_dir' + '/Build/'+ '$afpo_platform' +'/' +'$afpo_job_type' + '/' + '$afpo_platform_version' + '/' + '$wls_resource_type' +'/build.xml')
		   	    antInstallation('SOAant')
        		}
    	}
	
	   }
     
	''')
		
	}
}
}