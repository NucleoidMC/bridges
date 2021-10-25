package xyz.nucleoid.bridges.game;

import xyz.nucleoid.plasmid.game.common.team.GameTeam;

public class BridgesTeamState {
    public GameTeam team;
    public int score;
    public BridgesTeamState(GameTeam team) {
        this.team = team;
        this.score = 0;
    }
}
