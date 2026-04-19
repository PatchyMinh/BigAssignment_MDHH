package model;
import java.time.LocalDate;

public class Arts extends Items{
    private String artistName;
    private LocalDate releaseDate;

    public Arts(User owner, double startingPrice, String description, String artistName, LocalDate releaseDate) {
        super(owner, startingPrice, description);
        this.artistName = artistName;
        this.releaseDate = releaseDate;
    }

    public String getArtistName() {
        return artistName;
    }
    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    @Override
    public String showInfo() {
        return "Current Item: \nType: Art\n Owner: " + getOwner() + "\n Artist Name: " + getArtistName() + "\nRelease Date: " + getReleaseDate() + "\nDescription: " + getDescription() + "\nStarting Price: " + getStartingPrice();
    }
}
