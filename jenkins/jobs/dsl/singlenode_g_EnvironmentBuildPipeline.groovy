buildPipelineView("${PROJECT_NAME}/Environment_Provisioning_Single_Node/ENVIRONMENT_BUILD") {
  title('Single node Environment Provisioning Pipeline')
  displayedBuilds(5)
  selectedJob('Set_Environment_Parameters')
	consoleOutputLinkStyle(OutputStyle.NewWindow)
  showPipelineDefinitionHeader()
  refreshFrequency(1)
}