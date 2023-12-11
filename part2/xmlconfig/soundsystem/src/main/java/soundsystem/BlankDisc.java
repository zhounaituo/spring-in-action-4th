package soundsystem;

public class BlankDisc implements CompactDisc {

    private String titles;
    private String artist;

    public BlankDisc(String titles, String artist) {
        this.titles = titles;
        this.artist = artist;
    }

    public void play() {
        System.out.println("Playing " + titles + " by " + artist);
    }
}
