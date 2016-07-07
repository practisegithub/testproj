folder("${PROJECT_NAME}/Continuous_Delivery") {
    configure { folder ->
        folder / icon(class: 'org.example.MyFolderIcon')
    }
}

folder("${PROJECT_NAME}/Continuous_Delivery/Job_Templates") {
    configure { folder ->
        folder / icon(class: 'org.example.MyFolderIcon')
    }
}
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"


freeStyleJob("${PROJECT_NAME}/Continuous_Delivery/Job_Templates/DB_Job_Template") {
    logRotator(-1, 10)
    parameters {
	stringParam('dsl_db_job_env', 'dev', '')
	stringParam('dsl_db_job_env_root_dir', '/u01/jenkins/CI_Artifacts', '')
	stringParam('dsl_db_job_platform', '12c', '')
	stringParam('dsl_db_job_type', 'DB', '')
	stringParam('dsl_db_job_platform_version', '12.1.0.2.0', '')
	stringParam('dsl_db_scripts_url', 'git@gitlab:${WORKSPACE_NAME}/soacsf.git', '')
	stringParam('dsl_db_job_domain_name', 'ReusableCode', '')
	stringParam('dsl_db_job_application_name', 'SOACSF', '')
	stringParam('dsl_db_job_db_name', 'fmwdb', '')
	
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
            def dsl_job_name= dsl_db_job_env + '_'+ dsl_db_job_platform + '_'+ dsl_db_job_type + '_' + dsl_db_job_platform_version + '_' + dsl_db_job_application_name
            
			def workspaceFolderName = "${WORKSPACE_NAME}"
			def projectFolderName = "${PROJECT_NAME}"
			
			freeStyleJob("${PROJECT_NAME}/Continuous_Delivery/$dsl_job_name") {
			  parameters {
				stringParam('afpo_env', dsl_db_job_env, 'Enter the environment abbreviation')
				stringParam('afpo_env_root_dir', dsl_db_job_env_root_dir, 'Enter the path for ENV root directory')
				stringParam('afpo_platform', dsl_db_job_platform, 'Enter the FMW Platform')
				stringParam('afpo_job_type', dsl_db_job_type, 'my description')
				stringParam('afpo_platform_version', dsl_db_job_platform_version, 'my description')
				stringParam('afpo_application_name', dsl_db_job_application_name, 'my description')
				stringParam('afpo_db_name', dsl_db_job_db_name, 'my description')
				
			environmentVariables {
				env('WORKSPACE_NAME',workspaceFolderName)
				env('PROJECT_NAME',projectFolderName)
			}
			}
	
      		label('fmw')

			multiscm {
				git {
				
					remote {
						url(dsl_db_scripts_url)
						credentials('adop-jenkins-master')
						branch("*/master")
					}
				extensions {
					relativeTargetDirectory("$WORKSPACE")
					}
					
				}
				git {
				
					remote {
						url("git@gitlab:${WORKSPACE_NAME}/ci_artifacts.git")
						credentials('adop-jenkins-master')
						branch("*/master")
					}
				extensions {
					relativeTargetDirectory("/u01/jenkins/CI_Artifacts")
					}
					
				}
			}
	
			steps {
				
				shell (''\' #!/bin/bash 
					chown -R jenkins. /u01
					mkdir -p $WORKSPACE/SOACSF
					cp -r /workspace/${PROJECT_NAME}/Continuous_Delivery/Job_Templates/DB_Job_Template/$afpo_job_type/$afpo_platform_version/** $WORKSPACE/SOACSF/ '\'')
				
				ant {
					targets(['processTokens'])
					props('afpo.env': '$afpo_env', 'afpo.platform': '$afpo_platform' ,'afpo.job.type': '$afpo_job_type','afpo.platform.version': '$afpo_platform_version','afpo.env.root.dir': '$afpo_env_root_dir','token.workspace.dir':'${WORKSPACE}','afpo.application.name' :'$afpo_application_name','afpo.db.name':'$afpo_db_name' )
					buildFile('$afpo_env_root_dir' + '/Build/'+ '$afpo_platform' +'/' +'$afpo_job_type' + '/' + '$afpo_platform_version' + '/pre-build.xml')
					antInstallation('SOAant')
				    }
				ant {
					targets(['create-db-artifacts'])
					props('afpo.env': '$afpo_env', 'afpo.platform': '$afpo_platform' ,'afpo.job.type': '$afpo_job_type','afpo.platform.version': '$afpo_platform_version','afpo.env.root.dir': '$afpo_env_root_dir','sql.scripts.dir':'${WORKSPACE}' + '/' + dsl_db_job_application_name,'afpo.db.name':'$afpo_db_name','afpo.application.name' :'$afpo_application_name')
					buildFile('$afpo_env_root_dir' + '/Build/'+ '$afpo_platform' +'/' +'$afpo_job_type' + '/' + '$afpo_platform_version' + '/build.xml')
					antInstallation('SOAant')
				}
			}
			wrappers {
				preBuildCleanup() 
			}

			}
  			 
		    ''')
		}
		
	}
	wrappers {
        preBuildCleanup() 
	}
}