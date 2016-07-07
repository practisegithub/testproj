// Create all Folders inside Jenkins Workspace first

folder("${PROJECT_NAME}/Environment_Provisioning_Multi_Node") {
    configure { folder ->
        folder / icon(class: 'org.example.MyFolderIcon')
    }
}

folder("${PROJECT_NAME}/Environment_Provisioning_Single_Node") {
    configure { folder ->
        folder / icon(class: 'org.example.MyFolderIcon')
    }
}

folder("${PROJECT_NAME}/Environment_Provisioning_Docker") {
    configure { folder ->
        folder / icon(class: 'org.example.MyFolderIcon')
    }
}