package ch.iselaj.quarry.game;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String source;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "played_at")
    private Instant playedAt;

    private String white;
    private String black;
    private String result;
    private String eco;

    @Column(name = "time_control")
    private String timeControl;

    @Column(nullable = false, columnDefinition = "text")
    private String pgn;

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt = Instant.now();

    protected Game() {
    }

    public Game(String source, String externalId, String pgn) {
        this.source = source;
        this.externalId = externalId;
        this.pgn = pgn;
    }

    public Long getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getPgn() {
        return pgn;
    }

    public void setPlayedAt(Instant playedAt) {
        this.playedAt = playedAt;
    }

    public void setWhite(String white) {
        this.white = white;
    }

    public void setBlack(String black) {
        this.black = black;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setEco(String eco) {
        this.eco = eco;
    }

    public void setTimeControl(String timeControl) {
        this.timeControl = timeControl;
    }
}