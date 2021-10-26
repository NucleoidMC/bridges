package xyz.nucleoid.bridges.game;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import xyz.nucleoid.bridges.game.map.BridgesMap;
import xyz.nucleoid.bridges.game.map.BridgesMapGenerator;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamSelectionLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;

import java.util.HashMap;

public class BridgesWaiting {
    private final GameSpace gameSpace;
    private final BridgesMap map;
    private final BridgesConfig config;
    private final BridgesSpawnLogic spawnLogic;
    private final ServerWorld world;
    private final TeamSelectionLobby teamSelection;

    private BridgesWaiting(GameSpace gameSpace, BridgesMap map, BridgesConfig config, ServerWorld world, TeamSelectionLobby teamSelection) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.spawnLogic = new BridgesSpawnLogic(gameSpace, map, world);
        this.world = world;
        this.teamSelection = teamSelection;
    }

    public static GameOpenProcedure open(GameOpenContext<BridgesConfig> context) {
        BridgesConfig config = context.config();
        BridgesMapGenerator generator = new BridgesMapGenerator(config);
        MinecraftServer server = context.server();
        BridgesMap map = generator.build(server);

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setGenerator(map.asGenerator(server))
                .setGameRule(GameRules.KEEP_INVENTORY, false)
                .setDimensionType(RegistryKey.of(Registry.DIMENSION_TYPE_KEY, context.config().dimension()));

        return context.openWithWorld(worldConfig, (game, world) -> {
            GameWaitingLobby.addTo(game, config.playerConfig());
            TeamSelectionLobby teamSelection = TeamSelectionLobby.addTo(game, config.teams());
            BridgesWaiting waiting = new BridgesWaiting(game.getGameSpace(), map, context.config(), world, teamSelection);

            game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
            game.listen(GamePlayerEvents.OFFER, waiting::addPlayer);
            game.listen(GamePlayerEvents.REMOVE, waiting::onPlayerDeath);
        });
    }

    private GameResult requestStart() {
        HashMap<GameTeamKey, ServerPlayerEntity> players = new HashMap<>();
        this.teamSelection.allocate(this.gameSpace.getPlayers(), players::put);

        BridgesActive.open(this.gameSpace, this.map, this.config, world, players);
        return GameResult.ok();
    }

    private PlayerOfferResult addPlayer(PlayerOffer offer) {
        this.spawnPlayer(offer.player());
        return offer.accept(this.world, new Vec3d(0, 20, 0));
    }

    private void onPlayerDeath(ServerPlayerEntity player) {
        player.setHealth(20.0f);
        this.spawnPlayer(player);
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayerAtCenter(player);
    }
}
