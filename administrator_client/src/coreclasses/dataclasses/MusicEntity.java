package coreclasses.dataclasses;

public class MusicEntity {
    private int id;
    private final String title;
    private final String authors;
    private final String duration;
    private final Integer totalVotes;
    private final String percentage;


    public MusicEntity(int id, String title, String authors, String duration, Integer totalVotes, Integer positiveVotes) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.duration = duration;
        this.totalVotes = totalVotes;
        if(!(totalVotes == null || positiveVotes == null)){
            this.percentage = String.valueOf(Math.round(((double) ((float) positiveVotes / (float) totalVotes) * 100))).concat("%");
        } else {
            totalVotes = null;
            percentage = null;
        }
    }

    public MusicEntity(int id, MusicInfos infos){
        this(id, infos.getTitle(), infos.getAlbumName(), getStringDuration(infos.getDuration()), infos.getTotalVoteNumber(), infos.getPositiveVoteNumber());
    }

    private static String getStringDuration(int x){
        String r = String.valueOf((x / 60));
        int secs = x % 60;
        if (secs < 10)
            return r.concat(":0").concat(String.valueOf(secs));

        return r.concat(":").concat(String.valueOf(secs));
    }

    public int getId() { return id; }

    public String getTitle() { return title; }

    public String getAuthors() { return authors; }

    public String getDuration() { return duration; }

    public Integer getTotalVotes() { return totalVotes; }

    public String getPercentage() { return percentage; }

    public void setId(int id) { this.id = id; }
}
