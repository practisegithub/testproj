def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

freeStyleJob("${PROJECT_NAME}/Continuous_Delivery/Job_Templates/ADF_Job_Template") {
    logRotator(-1, 10)
    parameters {
	stringParam('dsl_adf_job_env', 'dev', '')
	stringParam('dsl_adf_job_env_root_dir', '/u01/jenkins/CI_Artifacts', '')
	stringParam('dsl_adf_job_platform', '12c', '')
	stringParam('dsl_adf_job_type', 'ADF', '')
	stringParam('dsl_adf_job_platform_version', '12.1.3.0.0', '')
	stringParam('dsl_adf_job_application_name', 'SOACSF', '')
	stringParam('dsl_adf_job_deploy_profile_name', 'CsfViewer', '')
	stringParam('dsl_adf_job_jdeveloper_application_name', 'SoaCsf', '')
	stringParam('dsl_adf_application_url', 'http://gitlab.52.73.219.89.xip.io/root/SOACSF.git', '')
	
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
 
        text('''
        def dsl_job_name= dsl_adf_job_env + '_'+ dsl_adf_job_platform + '_'+ dsl_adf_job_type + '_' + dsl_adf_job_platform_version + '_' + dsl_adf_job_application_name + '_' + dsl_adf_job_deploy_profile_name
		def workspaceFolderName = "${WORKSPACE_NAME}"
		def projectFolderName = "${PROJECT_NAME}"
		
		freeStyleJob("${PROJECT_NAME}/Continuous_Delivery/$dsl_job_name") {
    	  parameters {
			stringParam('afpo_env', dsl_adf_job_env, 'Enter the environment abbreviation')
       		stringParam('afpo_env_root_dir', dsl_adf_job_env_root_dir, 'Enter the path for ENV root directory')
			stringParam('afpo_platform', dsl_adf_job_platform, 'Enter the FMW Platform')
			stringParam('afpo_job_type', dsl_adf_job_type, 'my description')
			stringParam('afpo_platform_version', dsl_adf_job_platform_version, 'my description')
       		stringParam('afpo_application_name', dsl_adf_job_application_name, 'my description')
			stringParam('adf_deploy_profile_name', dsl_adf_job_deploy_profile_name, 'my description')
			stringParam('adf_jdeveloper_application_name', dsl_adf_job_jdeveloper_application_name, 'my description')
			
			environmentVariables {
				env('WORKSPACE_NAME',workspaceFolderName)
				env('PROJECT_NAME',projectFolderName)
			}
    	}

	    label('fmw')

		scm {
       		git {
           		remote {
               		url(dsl_adf_application_url)
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
				  mkdir -p $WORKSPACE/$adf_jdeveloper_application_name
                  cp -r /workspace/${PROJECT_NAME}/Continuous_Delivery/Job_Templates/ADF_Job_Template/$afpo_job_type/$afpo_platform_version/$adf_jdeveloper_application_name/** $WORKSPACE/$adf_jdeveloper_application_name
               	  '\'')
      
			ant {
           	 targets(['processTokens'])
           	 props('afpo.env': '$afpo_env', 'afpo.platform': '$afpo_platform' ,'afpo.job.type': '$afpo_job_type','afpo.platform.version': '$afpo_platform_version','afpo.env.root.dir': '$afpo_env_root_dir','token.workspace.dir':'${WORKSPACE}','afpo.application.name' :'$afpo_application_name'  )
           	 buildFile('$afpo_env_root_dir' + '/Build/'+ '$afpo_platform' +'/' +'$afpo_job_type' + '/' + '$afpo_platform_version' + '/pre-build.xml')
           	 antInstallation('SOAant')
       		}      
        	
			ant {
           	 targets(['package','adf-deploy'])
           	 props('afpo.env': '$afpo_env', 'afpo.platform': '$afpo_platform' ,'afpo.job.type': '$afpo_job_type','afpo.platform.version': '$afpo_platform_version','afpo.env.root.dir': '$afpo_env_root_dir','adf.deploy.profile.name':'$adf_deploy_profile_name','adf.jdeveloper.application.name':'$adf_jdeveloper_application_name','afpo.application.name':'$afpo_application_name','adf.application.workspace':'${WORKSPACE}')
           	 buildFile('$afpo_env_root_dir' + '/Build/'+ '$afpo_platform' +'/' +'$afpo_job_type' + '/' + '$afpo_platform_version' + '/build.xml')
           	 antInstallation('SOAant')
        	}
    	    }
	        }
				
         ''')
		
    }
  }
}