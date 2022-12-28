package xyz.nucleoid.bridges.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import xyz.nucleoid.bridges.Bridges;
import xyz.nucleoid.bridges.game.map.BridgesMap;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.team.*;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.util.ColoredBlocks;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class BridgesActive {
    private final BridgesConfig config;

    public final GameSpace gameSpace;

    private final Object2ObjectMap<PlayerRef, BridgesPlayer> participants;
    private final Map<GameTeamKey, BridgesTeamState> teamStates = new Reference2ObjectOpenHashMap<>();

    private final TeamManager teams;
    private final BridgesMap map;

    private final BridgesSpawnLogic spawnLogic;
    private final BridgesStateManager stageManager;
    private final BridgesTimerBar timerBar;
    private final BridgesScoreboard scoreboard;
    private final ServerWorld world;

    private BridgesActive(GameSpace gameSpace, BridgesMap map, GlobalWidgets widgets, BridgesConfig config, ServerWorld world, TeamManager manager) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.spawnLogic = new BridgesSpawnLogic(gameSpace, map, world);
        this.participants = new Object2ObjectOpenHashMap<>();
        this.teams = manager;
        this.map = map;

        this.stageManager = new BridgesStateManager();
        this.timerBar = new BridgesTimerBar(widgets);
        this.scoreboard = new BridgesScoreboard(widgets);
        this.world = world;
    }

    public static void open(GameSpace gameSpace, BridgesMap map, BridgesConfig config, ServerWorld world, HashMap<GameTeamKey, ServerPlayerEntity> players) {
        gameSpace.setActivity(game -> {
            GlobalWidgets widgets = GlobalWidgets.addTo(game);
            TeamManager manager = TeamManager.addTo(game);
            BridgesActive active = new BridgesActive(gameSpace, map, widgets, config, world, manager);
            active.addTeams(config.teams());
            active.initPlayers(players);

            TeamChat.addTo(game, manager);

            game.setRule(GameRuleType.CRAFTING, ActionResult.FAIL);
            game.setRule(GameRuleType.PORTALS, ActionResult.FAIL);
            game.setRule(GameRuleType.PVP, ActionResult.PASS);
            game.setRule(GameRuleType.HUNGER, ActionResult.PASS);
            game.setRule(GameRuleType.FALL_DAMAGE, ActionResult.PASS);
            game.setRule(GameRuleType.BREAK_BLOCKS, ActionResult.PASS);
            game.setRule(GameRuleType.PLACE_BLOCKS, ActionResult.PASS);
            game.setRule(GameRuleType.BLOCK_DROPS, ActionResult.PASS);
            game.setRule(GameRuleType.THROW_ITEMS, ActionResult.FAIL);
            game.setRule(GameRuleType.UNSTABLE_TNT, ActionResult.FAIL);

            game.listen(GameActivityEvents.CREATE, active::onOpen);
            game.listen(GameActivityEvents.DESTROY, active::onClose);

            game.listen(GamePlayerEvents.JOIN, (active::spawnSpectator));
            game.listen(GamePlayerEvents.ADD, (active::addPlayer));
            game.listen(GamePlayerEvents.LEAVE, active::removePlayer);

            game.listen(GameActivityEvents.TICK, active::tick);

            game.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
            game.listen(BlockBreakEvent.EVENT, active::onBlockBreak);
            game.listen(BlockPlaceEvent.BEFORE, active::onBlockPlace);
            Bridges.GAMES.put(gameSpace, active);
        });
    }

    private void onOpen() {
        ServerWorld world = this.world;
        for (PlayerRef ref : this.participants.keySet()) {
            ref.ifOnline(world, this::spawnParticipant);
        }
        this.stageManager.onOpen(world.getTime(), this.config);
    }

    private void onClose(GameCloseReason reason) {
        this.participants.forEach(((playerRef, bridgesPlayer) -> {
            bridgesPlayer.player().getInventory().clear();
            bridgesPlayer.player().setHealth(bridgesPlayer.player().getMaxHealth());
        }));
        Bridges.GAMES.remove(this.gameSpace);
    }

    private void initPlayers(Map<GameTeamKey, ServerPlayerEntity> players) {
        players.forEach((teamKey, player) -> {
            var teamConfig = this.teams.getTeamConfig(teamKey);
            var participant = new BridgesPlayer(player, new GameTeam(teamKey, teamConfig));
            this.participants.put(PlayerRef.of(player), participant);
            this.teams.addPlayerTo(player, teamKey);
        });
    }

    private void addPlayer(ServerPlayerEntity player) {
        player.getInventory().clear();
        if (!this.participants.containsKey(PlayerRef.of(player))) {
            this.spawnSpectator(player);
        } else {
            spawnParticipant(player);
        }
    }

    private void removePlayer(ServerPlayerEntity player) {
        this.participants.remove(PlayerRef.of(player));
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.spawnParticipant(player);
        return ActionResult.FAIL;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SURVIVAL);

        var bridgesPlayer = this.participants.get(PlayerRef.of(player));

        player.getInventory().clear();
        for (ItemStack stack : config.items()) {
            if (stack.getItem() instanceof ArmorItem item) {
                player.equipStack(item.getSlotType(), stack);
            } else {
                player.getInventory().insertStack(stack.copy());
            }
        }

        var terracotta = bridgesPlayer.team().config().applyDye(new ItemStack(ColoredBlocks.terracotta(bridgesPlayer.team().config().blockDyeColor()), 64));
        for (int i = 0; i < 3; i++) {
            bridgesPlayer.player().getInventory().insertStack(terracotta.copy());
        }
        this.spawnLogic.spawnPlayer(bridgesPlayer);
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayer(this.participants.get(PlayerRef.of(player)));
    }

    private void tick() {
        ServerWorld world = this.world;
        long time = world.getTime();

        BridgesStateManager.IdleTickResult result = this.stageManager.tick(time, gameSpace);

        switch (result) {
            case CONTINUE_TICK:
                break;
            case TICK_FINISHED:
                return;
            case GAME_FINISHED:
                this.broadcastWin(this.checkWinResult());
                return;
            case GAME_CLOSED:
                this.gameSpace.close(GameCloseReason.CANCELED);
                return;
        }

        this.timerBar.update(this.stageManager.finishTime - time, this.config.timeLimitSecs() * 20L);
        this.scoreboard.updateScoreboard(teamStates.values());

        if (this.checkWinResult().team() != null) {
            broadcastWin(this.checkWinResult());
            gameSpace.close(GameCloseReason.FINISHED);
        }
    }

    public void tickPlayerInGoal(ServerPlayerEntity player) {
        var playerTeam = this.teams.teamFor(player);
        if (this.map.getRegions(playerTeam).goal().contains(player.getBlockPos())) {
            // Player is in own goal
            this.spawnParticipant(player);
        } else {
            teams.forEach(team -> {
                if (this.map.getRegions(team).goal().contains(player.getBlockPos())) {
                    // Player is in goal but not in own goal
                    this.spawnParticipant(player);
                    teamStates.get(playerTeam).score++;
                    player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
            });
        }
    }

    private void broadcastWin(WinResult result) {
        var team = result.team;

        Text message;
        if (team != null) {
            message = Text.translatable("text.bridges.win", team.config().name()).formatted(Formatting.GOLD);
        } else {
            message = Text.translatable("text.bridges.no_win").formatted(Formatting.GOLD);
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
    }

    private WinResult checkWinResult() {
        var winner = this.teamStates.values().stream().filter(state -> state.score >= this.config.pointWinThreshold()).findFirst();
        return winner.map(bridgesTeamState -> WinResult.win(bridgesTeamState.team)).orElseGet(WinResult::no);
    }

    private ActionResult onBlockBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (map.allowBlockInteractions(pos)) {
            return ActionResult.PASS;
        }
        return ActionResult.FAIL;
    }

    private ActionResult onBlockPlace(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState blockState, ItemUsageContext itemUsageContext) {
        if (map.allowBlockInteractions(pos)) {
            return ActionResult.PASS;
        }
        return ActionResult.FAIL;
    }

    private void addTeams(GameTeamList teams) {
        for (var team : teams) {
            var config = GameTeamConfig.builder(team.config())
                    .setCollision(AbstractTeam.CollisionRule.NEVER)
                    .setFriendlyFire(false)
                    .build();

            var newTeam = new GameTeam(team.key(), config);
            this.teams.addTeam(newTeam);

            this.teamStates.put(team.key(), new BridgesTeamState(newTeam));
        }
        map.updateTeams(teams, this);
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    record WinResult(GameTeam team) {

        static WinResult no() {
            return new WinResult(null);
        }

        static WinResult win(GameTeam team) {
            return new WinResult(team);
        }
    }
}
