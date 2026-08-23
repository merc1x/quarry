package ch.iselaj.quarry.position;

import jakarta.persistence.*;

@Entity
@Table(name = "positions")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(nullable = false)
    private int ply;

    @Column(nullable = false, columnDefinition = "text")
    private String fen;

    @Column(nullable = false)
    private String san;

    @Column(nullable = false)
    private String uci;

    protected Position() {
    }

    public Position(Long gameId, int ply, String fen, String san, String uci) {
        this.gameId = gameId;
        this.ply = ply;
        this.fen = fen;
        this.san = san;
        this.uci = uci;
    }

    public Long getId() {
        return id;
    }

    public String getFen() {
        return fen;
    }

    public int getPly() {
        return ply;
    }
}