package xyz.nucleoid.bridges.game;

import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.widget.SidebarWidget;

import java.util.Collection;

public class BridgesScoreboard {
    private final SidebarWidget widget;

    public BridgesScoreboard(GlobalWidgets widgets) {
        widget = widgets.addSidebar(Text.translatable("gameType.bridges.bridges"));
    }

    public void updateScoreboard(Collection<BridgesTeamState> states) {
        widget.clearLines();
        states.forEach(bridgesTeamState -> widget.addLines(Text.translatable("text.bridges.team_score", bridgesTeamState.team.config().name(), bridgesTeamState.score)));
    }
}
