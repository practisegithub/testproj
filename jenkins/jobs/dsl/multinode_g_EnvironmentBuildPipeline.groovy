buildPipelineView("${PROJECT_NAME}/Environment_Provisioning_Multi_Node/ENVIRONMENT_BUILD") {
  title('Multi node Environment Provisioning Pipeline')
  displayedBuilds(5)
  selectedJob('Set_Environment_Parameters')
	consoleOutputLinkStyle(OutputStyle.NewWindow)
  showPipelineDefinitionHeader()
  refreshFrequency(1)
}