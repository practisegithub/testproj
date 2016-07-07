buildPipelineView("${PROJECT_NAME}/Environment_Provisioning_Docker/ENVIRONMENT_BUILD") {
  title('Docker Environment Provisioning Pipeline')
  displayedBuilds(5)
  selectedJob('Set_Environment_Parameters')
	consoleOutputLinkStyle(OutputStyle.NewWindow)
  showPipelineDefinitionHeader()
  refreshFrequency(1)
}