package coreclasses;

public enum TreeViewNames {INSTRU("Instru", "intruview.fxml"), BACKTRACK("Fond sonore","backtrackview.fxml"),
    LIVE("Prendre l'antenne", "streamview.fxml"), PLAYLIST("Playlist", "playlistview.fxml"),
    HISTORIQUE("Historique", "historiqueview.fxml"), CHAT("Chat", "chatview.fxml"),
    EVENTS("Events", "eventsview.fxml"), DB("Utilisateurs", "userdbview.fxml"),
    SANCTIONS("Sanctions", "sanctionsview.fxml"), ADMINUSERS("Admins", "staffview.fxml");


    private final String name, fileName;

    TreeViewNames(String name, String fileName){
        this.name = name;
        this.fileName = "/gui/".concat(fileName);
    }

    public static TreeViewNames getObjectFromName(String name){
        if(name == null) return null;

        for(TreeViewNames tvn : TreeViewNames.values()){
            if(name.contains(tvn.name))
                return tvn;
        }

        return null;
    }

    public String getName() { return name; }

    public String getFileName() { return fileName; }
}
