package net.minecraft.server;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.SystemUtils;
import net.minecraft.commands.CommandDispatcher;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICommandListener;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.gametest.framework.GameTestHarnessTicker;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutServerDifficulty;
import net.minecraft.network.protocol.game.PacketPlayOutUpdateTime;
import net.minecraft.network.protocol.status.ServerPing;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.bossevents.BossBattleCustomData;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.DemoPlayerInteractManager;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerInteractManager;
import net.minecraft.server.level.WorldProviderNormal;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.level.progress.WorldLoadListener;
import net.minecraft.server.level.progress.WorldLoadListenerFactory;
import net.minecraft.server.network.ITextFilter;
import net.minecraft.server.network.ServerConnection;
import net.minecraft.server.packs.EnumResourcePackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ResourcePackLoader;
import net.minecraft.server.packs.repository.ResourcePackRepository;
import net.minecraft.server.packs.resources.IReloadableResourceManager;
import net.minecraft.server.packs.resources.IResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.OpListEntry;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserCache;
import net.minecraft.server.players.WhiteList;
import net.minecraft.util.CryptographyException;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MinecraftEncryption;
import net.minecraft.util.ModCheck;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.TimeRange;
import net.minecraft.util.debugchart.RemoteDebugSampleType;
import net.minecraft.util.debugchart.SampleLogger;
import net.minecraft.util.debugchart.TpsDebugDimensions;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.GameProfilerTick;
import net.minecraft.util.profiling.MethodProfilerResults;
import net.minecraft.util.profiling.MethodProfilerResultsEmpty;
import net.minecraft.util.profiling.MethodProfilerResultsField;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.ServerMetricsSamplersProvider;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.IAsyncTaskHandlerReentrant;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.MobSpawnerCat;
import net.minecraft.world.entity.npc.MobSpawnerTrader;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.PotionBrewer;
import net.minecraft.world.item.crafting.CraftingManager;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.DataPackConfiguration;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.ForcedChunk;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.MobSpawner;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.WorldSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.border.IWorldBorderListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.WorldDimension;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.MobSpawnerPatrol;
import net.minecraft.world.level.levelgen.MobSpawnerPhantom;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.feature.WorldGenFeatureConfigured;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.Convertable;
import net.minecraft.world.level.storage.IWorldDataServer;
import net.minecraft.world.level.storage.PersistentCommandStorage;
import net.minecraft.world.level.storage.SaveData;
import net.minecraft.world.level.storage.SavedFile;
import net.minecraft.world.level.storage.SecondaryWorldData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.WorldNBTStorage;
import net.minecraft.world.level.storage.WorldPersistentData;
import net.minecraft.world.phys.Vec2F;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public abstract class MinecraftServer extends IAsyncTaskHandlerReentrant<TickTask> implements ServerInfo, ICommandListener, AutoCloseable {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String VANILLA_BRAND = "vanilla";
    private static final float AVERAGE_TICK_TIME_SMOOTHING = 0.8F;
    private static final int TICK_STATS_SPAN = 100;
    private static final long OVERLOADED_THRESHOLD_NANOS = 20L * TimeRange.NANOSECONDS_PER_SECOND / 20L;
    private static final int OVERLOADED_TICKS_THRESHOLD = 20;
    private static final long OVERLOADED_WARNING_INTERVAL_NANOS = 10L * TimeRange.NANOSECONDS_PER_SECOND;
    private static final int OVERLOADED_TICKS_WARNING_INTERVAL = 100;
    private static final long STATUS_EXPIRE_TIME_NANOS = 5L * TimeRange.NANOSECONDS_PER_SECOND;
    private static final long PREPARE_LEVELS_DEFAULT_DELAY_NANOS = 10L * TimeRange.NANOSECONDS_PER_MILLISECOND;
    private static final int MAX_STATUS_PLAYER_SAMPLE = 12;
    private static final int SPAWN_POSITION_SEARCH_RADIUS = 5;
    private static final int AUTOSAVE_INTERVAL = 6000;
    private static final int MIMINUM_AUTOSAVE_TICKS = 100;
    private static final int MAX_TICK_LATENCY = 3;
    public static final int ABSOLUTE_MAX_WORLD_SIZE = 29999984;
    public static final WorldSettings DEMO_SETTINGS = new WorldSettings("Demo World", EnumGamemode.SURVIVAL, false, EnumDifficulty.NORMAL, false, new GameRules(), WorldDataConfiguration.DEFAULT);
    public static final GameProfile ANONYMOUS_PLAYER_PROFILE = new GameProfile(SystemUtils.NIL_UUID, "Anonymous Player");
    public Convertable.ConversionSession storageSource;
    public final WorldNBTStorage playerDataStorage;
    private final List<Runnable> tickables = Lists.newArrayList();
    private MetricsRecorder metricsRecorder;
    private GameProfilerFiller profiler;
    private Consumer<MethodProfilerResults> onMetricsRecordingStopped;
    private Consumer<Path> onMetricsRecordingFinished;
    private boolean willStartRecordingMetrics;
    @Nullable
    private MinecraftServer.TimeProfiler debugCommandProfiler;
    private boolean debugCommandProfilerDelayStart;
    private ServerConnection connection;
    public final WorldLoadListenerFactory progressListenerFactory;
    @Nullable
    private ServerPing status;
    @Nullable
    private ServerPing.a statusIcon;
    private final RandomSource random;
    public final DataFixer fixerUpper;
    private String localIp;
    private int port;
    private final LayeredRegistryAccess<RegistryLayer> registries;
    private Map<ResourceKey<World>, WorldServer> levels;
    private PlayerList playerList;
    private volatile boolean running;
    private boolean stopped;
    private int tickCount;
    private int ticksUntilAutosave;
    protected final Proxy proxy;
    private boolean onlineMode;
    private boolean preventProxyConnections;
    private boolean pvp;
    private boolean allowFlight;
    @Nullable
    private String motd;
    private int playerIdleTimeout;
    private final long[] tickTimesNanos;
    private long aggregatedTickTimesNanos;
    @Nullable
    private KeyPair keyPair;
    @Nullable
    private GameProfile singleplayerProfile;
    private boolean isDemo;
    private volatile boolean isReady;
    private long lastOverloadWarningNanos;
    protected final Services services;
    private long lastServerStatus;
    public final Thread serverThread;
    private long lastTickNanos;
    private long taskExecutionStartNanos;
    private long idleTimeNanos;
    private long nextTickTimeNanos;
    private long delayedTasksMaxNextTickTimeNanos;
    private boolean mayHaveDelayedTasks;
    private final ResourcePackRepository packRepository;
    private final ScoreboardServer scoreboard;
    @Nullable
    private PersistentCommandStorage commandStorage;
    private final BossBattleCustomData customBossEvents;
    private final CustomFunctionData functionManager;
    private boolean enforceWhitelist;
    private float smoothedTickTimeMillis;
    public final Executor executor;
    @Nullable
    private String serverId;
    public MinecraftServer.ReloadableResources resources;
    private final StructureTemplateManager structureTemplateManager;
    private final ServerTickRateManager tickRateManager;
    protected SaveData worldData;
    private final PotionBrewer potionBrewing;
    private volatile boolean isSaving;

    public static <S extends MinecraftServer> S spin(Function<Thread, S> function) {
        AtomicReference<S> atomicreference = new AtomicReference();
        Thread thread = new Thread(() -> {
            ((MinecraftServer) atomicreference.get()).runServer();
        }, "Server thread");

        thread.setUncaughtExceptionHandler((thread1, throwable) -> {
            MinecraftServer.LOGGER.error("Uncaught exception in server thread", throwable);
        });
        if (Runtime.getRuntime().availableProcessors() > 4) {
            thread.setPriority(8);
        }

        S s0 = (MinecraftServer) function.apply(thread);

        atomicreference.set(s0);
        thread.start();
        return s0;
    }

    public MinecraftServer(Thread thread, Convertable.ConversionSession convertable_conversionsession, ResourcePackRepository resourcepackrepository, WorldStem worldstem, Proxy proxy, DataFixer datafixer, Services services, WorldLoadListenerFactory worldloadlistenerfactory) {
        super("Server");
        this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
        this.profiler = this.metricsRecorder.getProfiler();
        this.onMetricsRecordingStopped = (methodprofilerresults) -> {
            this.stopRecordingMetrics();
        };
        this.onMetricsRecordingFinished = (path) -> {
        };
        this.random = RandomSource.create();
        this.port = -1;
        this.levels = Maps.newLinkedHashMap();
        this.running = true;
        this.ticksUntilAutosave = 6000;
        this.tickTimesNanos = new long[100];
        this.aggregatedTickTimesNanos = 0L;
        this.lastTickNanos = SystemUtils.getNanos();
        this.taskExecutionStartNanos = SystemUtils.getNanos();
        this.nextTickTimeNanos = SystemUtils.getNanos();
        this.scoreboard = new ScoreboardServer(this);
        this.customBossEvents = new BossBattleCustomData();
        this.registries = worldstem.registries();
        this.worldData = worldstem.worldData();
        if (!this.registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM).containsKey(WorldDimension.OVERWORLD)) {
            throw new IllegalStateException("Missing Overworld dimension data");
        } else {
            this.proxy = proxy;
            this.packRepository = resourcepackrepository;
            this.resources = new MinecraftServer.ReloadableResources(worldstem.resourceManager(), worldstem.dataPackResources());
            this.services = services;
            if (services.profileCache() != null) {
                services.profileCache().setExecutor(this);
            }

            this.connection = new ServerConnection(this);
            this.tickRateManager = new ServerTickRateManager(this);
            this.progressListenerFactory = worldloadlistenerfactory;
            this.storageSource = convertable_conversionsession;
            this.playerDataStorage = convertable_conversionsession.createPlayerStorage();
            this.fixerUpper = datafixer;
            this.functionManager = new CustomFunctionData(this, this.resources.managers.getFunctionLibrary());
            HolderGetter<Block> holdergetter = this.registries.compositeAccess().registryOrThrow(Registries.BLOCK).asLookup().filterFeatures(this.worldData.enabledFeatures());

            this.structureTemplateManager = new StructureTemplateManager(worldstem.resourceManager(), convertable_conversionsession, datafixer, holdergetter);
            this.serverThread = thread;
            this.executor = SystemUtils.backgroundExecutor();
            this.potionBrewing = PotionBrewer.bootstrap(this.worldData.enabledFeatures());
        }
    }

    private void readScoreboard(WorldPersistentData worldpersistentdata) {
        worldpersistentdata.computeIfAbsent(this.getScoreboard().dataFactory(), "scoreboard");
    }

    protected abstract boolean initServer() throws IOException;

    protected void loadLevel() {
        if (!JvmProfiler.INSTANCE.isRunning()) {
            ;
        }

        boolean flag = false;
        ProfiledDuration profiledduration = JvmProfiler.INSTANCE.onWorldLoadedStarted();

        this.worldData.setModdedInfo(this.getServerModName(), this.getModdedStatus().shouldReportAsModified());
        WorldLoadListener worldloadlistener = this.progressListenerFactory.create(this.worldData.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS));

        this.createLevels(worldloadlistener);
        this.forceDifficulty();
        this.prepareLevels(worldloadlistener);
        if (profiledduration != null) {
            profiledduration.finish();
        }

        if (flag) {
            try {
                JvmProfiler.INSTANCE.stop();
            } catch (Throwable throwable) {
                MinecraftServer.LOGGER.warn("Failed to stop JFR profiling", throwable);
            }
        }

    }

    protected void forceDifficulty() {}

    protected void createLevels(WorldLoadListener worldloadlistener) {
        IWorldDataServer iworlddataserver = this.worldData.overworldData();
        boolean flag = this.worldData.isDebugWorld();
        IRegistry<WorldDimension> iregistry = this.registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM);
        WorldOptions worldoptions = this.worldData.worldGenOptions();
        long i = worldoptions.seed();
        long j = BiomeManager.obfuscateSeed(i);
        List<MobSpawner> list = ImmutableList.of(new MobSpawnerPhantom(), new MobSpawnerPatrol(), new MobSpawnerCat(), new VillageSiege(), new MobSpawnerTrader(iworlddataserver));
        WorldDimension worlddimension = (WorldDimension) iregistry.get(WorldDimension.OVERWORLD);
        WorldServer worldserver = new WorldServer(this, this.executor, this.storageSource, iworlddataserver, World.OVERWORLD, worlddimension, worldloadlistener, flag, j, list, true, (RandomSequences) null);

        this.levels.put(World.OVERWORLD, worldserver);
        WorldPersistentData worldpersistentdata = worldserver.getDataStorage();

        this.readScoreboard(worldpersistentdata);
        this.commandStorage = new PersistentCommandStorage(worldpersistentdata);
        WorldBorder worldborder = worldserver.getWorldBorder();

        if (!iworlddataserver.isInitialized()) {
            try {
                setInitialSpawn(worldserver, iworlddataserver, worldoptions.generateBonusChest(), flag);
                iworlddataserver.setInitialized(true);
                if (flag) {
                    this.setupDebugLevel(this.worldData);
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception initializing level");

                try {
                    worldserver.fillReportDetails(crashreport);
                } catch (Throwable throwable1) {
                    ;
                }

                throw new ReportedException(crashreport);
            }

            iworlddataserver.setInitialized(true);
        }

        this.getPlayerList().addWorldborderListener(worldserver);
        if (this.worldData.getCustomBossEvents() != null) {
            this.getCustomBossEvents().load(this.worldData.getCustomBossEvents(), this.registryAccess());
        }

        RandomSequences randomsequences = worldserver.getRandomSequences();
        Iterator iterator = iregistry.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<ResourceKey<WorldDimension>, WorldDimension> entry = (Entry) iterator.next();
            ResourceKey<WorldDimension> resourcekey = (ResourceKey) entry.getKey();

            if (resourcekey != WorldDimension.OVERWORLD) {
                ResourceKey<World> resourcekey1 = ResourceKey.create(Registries.DIMENSION, resourcekey.location());
                SecondaryWorldData secondaryworlddata = new SecondaryWorldData(this.worldData, iworlddataserver);
                WorldServer worldserver1 = new WorldServer(this, this.executor, this.storageSource, secondaryworlddata, resourcekey1, (WorldDimension) entry.getValue(), worldloadlistener, flag, j, ImmutableList.of(), false, randomsequences);

                worldborder.addListener(new IWorldBorderListener.a(worldserver1.getWorldBorder()));
                this.levels.put(resourcekey1, worldserver1);
            }
        }

        worldborder.applySettings(iworlddataserver.getWorldBorder());
    }

    private static void setInitialSpawn(WorldServer worldserver, IWorldDataServer iworlddataserver, boolean flag, boolean flag1) {
        if (flag1) {
            iworlddataserver.setSpawn(BlockPosition.ZERO.above(80), 0.0F);
        } else {
            ChunkProviderServer chunkproviderserver = worldserver.getChunkSource();
            ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(chunkproviderserver.randomState().sampler().findSpawnPosition());
            int i = chunkproviderserver.getGenerator().getSpawnHeight(worldserver);

            if (i < worldserver.getMinBuildHeight()) {
                BlockPosition blockposition = chunkcoordintpair.getWorldPosition();

                i = worldserver.getHeight(HeightMap.Type.WORLD_SURFACE, blockposition.getX() + 8, blockposition.getZ() + 8);
            }

            iworlddataserver.setSpawn(chunkcoordintpair.getWorldPosition().offset(8, i, 8), 0.0F);
            int j = 0;
            int k = 0;
            int l = 0;
            int i1 = -1;

            for (int j1 = 0; j1 < MathHelper.square(11); ++j1) {
                if (j >= -5 && j <= 5 && k >= -5 && k <= 5) {
                    BlockPosition blockposition1 = WorldProviderNormal.getSpawnPosInChunk(worldserver, new ChunkCoordIntPair(chunkcoordintpair.x + j, chunkcoordintpair.z + k));

                    if (blockposition1 != null) {
                        iworlddataserver.setSpawn(blockposition1, 0.0F);
                        break;
                    }
                }

                if (j == k || j < 0 && j == -k || j > 0 && j == 1 - k) {
                    int k1 = l;

                    l = -i1;
                    i1 = k1;
                }

                j += l;
                k += i1;
            }

            if (flag) {
                worldserver.registryAccess().registry(Registries.CONFIGURED_FEATURE).flatMap((iregistry) -> {
                    return iregistry.getHolder(MiscOverworldFeatures.BONUS_CHEST);
                }).ifPresent((holder_c) -> {
                    ((WorldGenFeatureConfigured) holder_c.value()).place(worldserver, chunkproviderserver.getGenerator(), worldserver.random, iworlddataserver.getSpawnPos());
                });
            }

        }
    }

    private void setupDebugLevel(SaveData savedata) {
        savedata.setDifficulty(EnumDifficulty.PEACEFUL);
        savedata.setDifficultyLocked(true);
        IWorldDataServer iworlddataserver = savedata.overworldData();

        iworlddataserver.setRaining(false);
        iworlddataserver.setThundering(false);
        iworlddataserver.setClearWeatherTime(1000000000);
        iworlddataserver.setDayTime(6000L);
        iworlddataserver.setGameType(EnumGamemode.SPECTATOR);
    }

    public void prepareLevels(WorldLoadListener worldloadlistener) {
        WorldServer worldserver = this.overworld();

        MinecraftServer.LOGGER.info("Preparing start region for dimension {}", worldserver.dimension().location());
        BlockPosition blockposition = worldserver.getSharedSpawnPos();

        worldloadlistener.updateSpawnPos(new ChunkCoordIntPair(blockposition));
        ChunkProviderServer chunkproviderserver = worldserver.getChunkSource();

        this.nextTickTimeNanos = SystemUtils.getNanos();
        worldserver.setDefaultSpawnPos(blockposition, worldserver.getSharedSpawnAngle());
        int i = this.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS);
        int j = i > 0 ? MathHelper.square(WorldLoadListener.calculateDiameter(i)) : 0;

        while (chunkproviderserver.getTickingGenerated() < j) {
            this.nextTickTimeNanos = SystemUtils.getNanos() + MinecraftServer.PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
            this.waitUntilNextTick();
        }

        this.nextTickTimeNanos = SystemUtils.getNanos() + MinecraftServer.PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
        this.waitUntilNextTick();
        Iterator iterator = this.levels.values().iterator();

        while (iterator.hasNext()) {
            WorldServer worldserver1 = (WorldServer) iterator.next();
            ForcedChunk forcedchunk = (ForcedChunk) worldserver1.getDataStorage().get(ForcedChunk.factory(), "chunks");

            if (forcedchunk != null) {
                LongIterator longiterator = forcedchunk.getChunks().iterator();

                while (longiterator.hasNext()) {
                    long k = longiterator.nextLong();
                    ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(k);

                    worldserver1.getChunkSource().updateChunkForced(chunkcoordintpair, true);
                }
            }
        }

        this.nextTickTimeNanos = SystemUtils.getNanos() + MinecraftServer.PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
        this.waitUntilNextTick();
        worldloadlistener.stop();
        this.updateMobSpawningFlags();
    }

    public EnumGamemode getDefaultGameType() {
        return this.worldData.getGameType();
    }

    public boolean isHardcore() {
        return this.worldData.isHardcore();
    }

    public abstract int getOperatorUserPermissionLevel();

    public abstract int getFunctionCompilationLevel();

    public abstract boolean shouldRconBroadcast();

    public boolean saveAllChunks(boolean flag, boolean flag1, boolean flag2) {
        boolean flag3 = false;

        for (Iterator iterator = this.getAllLevels().iterator(); iterator.hasNext(); flag3 = true) {
            WorldServer worldserver = (WorldServer) iterator.next();

            if (!flag) {
                MinecraftServer.LOGGER.info("Saving chunks for level '{}'/{}", worldserver, worldserver.dimension().location());
            }

            worldserver.save((IProgressUpdate) null, flag1, worldserver.noSave && !flag2);
        }

        WorldServer worldserver1 = this.overworld();
        IWorldDataServer iworlddataserver = this.worldData.overworldData();

        iworlddataserver.setWorldBorder(worldserver1.getWorldBorder().createSettings());
        this.worldData.setCustomBossEvents(this.getCustomBossEvents().save(this.registryAccess()));
        this.storageSource.saveDataTag(this.registryAccess(), this.worldData, this.getPlayerList().getSingleplayerData());
        if (flag1) {
            Iterator iterator1 = this.getAllLevels().iterator();

            while (iterator1.hasNext()) {
                WorldServer worldserver2 = (WorldServer) iterator1.next();

                MinecraftServer.LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", worldserver2.getChunkSource().chunkMap.getStorageName());
            }

            MinecraftServer.LOGGER.info("ThreadedAnvilChunkStorage: All dimensions are saved");
        }

        return flag3;
    }

    public boolean saveEverything(boolean flag, boolean flag1, boolean flag2) {
        boolean flag3;

        try {
            this.isSaving = true;
            this.getPlayerList().saveAll();
            flag3 = this.saveAllChunks(flag, flag1, flag2);
        } finally {
            this.isSaving = false;
        }

        return flag3;
    }

    @Override
    public void close() {
        this.stopServer();
    }

    public void stopServer() {
        if (this.metricsRecorder.isRecording()) {
            this.cancelRecordingMetrics();
        }

        MinecraftServer.LOGGER.info("Stopping server");
        this.getConnection().stop();
        this.isSaving = true;
        if (this.playerList != null) {
            MinecraftServer.LOGGER.info("Saving players");
            this.playerList.saveAll();
            this.playerList.removeAll();
        }

        MinecraftServer.LOGGER.info("Saving worlds");
        Iterator iterator = this.getAllLevels().iterator();

        WorldServer worldserver;

        while (iterator.hasNext()) {
            worldserver = (WorldServer) iterator.next();
            if (worldserver != null) {
                worldserver.noSave = false;
            }
        }

        while (this.levels.values().stream().anyMatch((worldserver1) -> {
            return worldserver1.getChunkSource().chunkMap.hasWork();
        })) {
            this.nextTickTimeNanos = SystemUtils.getNanos() + TimeRange.NANOSECONDS_PER_MILLISECOND;
            iterator = this.getAllLevels().iterator();

            while (iterator.hasNext()) {
                worldserver = (WorldServer) iterator.next();
                worldserver.getChunkSource().removeTicketsOnClosing();
                worldserver.getChunkSource().tick(() -> {
                    return true;
                }, false);
            }

            this.waitUntilNextTick();
        }

        this.saveAllChunks(false, true, false);
        iterator = this.getAllLevels().iterator();

        while (iterator.hasNext()) {
            worldserver = (WorldServer) iterator.next();
            if (worldserver != null) {
                try {
                    worldserver.close();
                } catch (IOException ioexception) {
                    MinecraftServer.LOGGER.error("Exception closing the level", ioexception);
                }
            }
        }

        this.isSaving = false;
        this.resources.close();

        try {
            this.storageSource.close();
        } catch (IOException ioexception1) {
            MinecraftServer.LOGGER.error("Failed to unlock level {}", this.storageSource.getLevelId(), ioexception1);
        }

    }

    public String getLocalIp() {
        return this.localIp;
    }

    public void setLocalIp(String s) {
        this.localIp = s;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void halt(boolean flag) {
        this.running = false;
        if (flag) {
            try {
                this.serverThread.join();
            } catch (InterruptedException interruptedexception) {
                MinecraftServer.LOGGER.error("Error while shutting down", interruptedexception);
            }
        }

    }

    protected void runServer() {
        try {
            if (!this.initServer()) {
                throw new IllegalStateException("Failed to initialize server");
            }

            this.nextTickTimeNanos = SystemUtils.getNanos();
            this.statusIcon = (ServerPing.a) this.loadStatusIcon().orElse((Object) null);
            this.status = this.buildServerStatus();

            while (this.running) {
                long i;

                if (!this.isPaused() && this.tickRateManager.isSprinting() && this.tickRateManager.checkShouldSprintThisTick()) {
                    i = 0L;
                    this.nextTickTimeNanos = SystemUtils.getNanos();
                    this.lastOverloadWarningNanos = this.nextTickTimeNanos;
                } else {
                    i = this.tickRateManager.nanosecondsPerTick();
                    long j = SystemUtils.getNanos() - this.nextTickTimeNanos;

                    if (j > MinecraftServer.OVERLOADED_THRESHOLD_NANOS + 20L * i && this.nextTickTimeNanos - this.lastOverloadWarningNanos >= MinecraftServer.OVERLOADED_WARNING_INTERVAL_NANOS + 100L * i) {
                        long k = j / i;

                        MinecraftServer.LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", j / TimeRange.NANOSECONDS_PER_MILLISECOND, k);
                        this.nextTickTimeNanos += k * i;
                        this.lastOverloadWarningNanos = this.nextTickTimeNanos;
                    }
                }

                boolean flag = i == 0L;

                if (this.debugCommandProfilerDelayStart) {
                    this.debugCommandProfilerDelayStart = false;
                    this.debugCommandProfiler = new MinecraftServer.TimeProfiler(SystemUtils.getNanos(), this.tickCount);
                }

                this.nextTickTimeNanos += i;
                this.startMetricsRecordingTick();
                this.profiler.push("tick");
                this.tickServer(flag ? () -> {
                    return false;
                } : this::haveTime);
                this.profiler.popPush("nextTickWait");
                this.mayHaveDelayedTasks = true;
                this.delayedTasksMaxNextTickTimeNanos = Math.max(SystemUtils.getNanos() + i, this.nextTickTimeNanos);
                this.startMeasuringTaskExecutionTime();
                this.waitUntilNextTick();
                this.finishMeasuringTaskExecutionTime();
                if (flag) {
                    this.tickRateManager.endTickWork();
                }

                this.profiler.pop();
                this.logFullTickTime();
                this.endMetricsRecordingTick();
                this.isReady = true;
                JvmProfiler.INSTANCE.onServerTick(this.smoothedTickTimeMillis);
            }
        } catch (Throwable throwable) {
            MinecraftServer.LOGGER.error("Encountered an unexpected exception", throwable);
            CrashReport crashreport = constructOrExtractCrashReport(throwable);

            this.fillSystemReport(crashreport.getSystemReport());
            File file = new File(new File(this.getServerDirectory(), "crash-reports"), "crash-" + SystemUtils.getFilenameFormattedDateTime() + "-server.txt");

            if (crashreport.saveToFile(file)) {
                MinecraftServer.LOGGER.error("This crash report has been saved to: {}", file.getAbsolutePath());
            } else {
                MinecraftServer.LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.onServerCrash(crashreport);
        } finally {
            try {
                this.stopped = true;
                this.stopServer();
            } catch (Throwable throwable1) {
                MinecraftServer.LOGGER.error("Exception stopping the server", throwable1);
            } finally {
                if (this.services.profileCache() != null) {
                    this.services.profileCache().clearExecutor();
                }

                this.onServerExit();
            }

        }

    }

    private void logFullTickTime() {
        long i = SystemUtils.getNanos();

        if (this.isTickTimeLoggingEnabled()) {
            this.getTickTimeLogger().logSample(i - this.lastTickNanos);
        }

        this.lastTickNanos = i;
    }

    private void startMeasuringTaskExecutionTime() {
        if (this.isTickTimeLoggingEnabled()) {
            this.taskExecutionStartNanos = SystemUtils.getNanos();
            this.idleTimeNanos = 0L;
        }

    }

    private void finishMeasuringTaskExecutionTime() {
        if (this.isTickTimeLoggingEnabled()) {
            SampleLogger samplelogger = this.getTickTimeLogger();

            samplelogger.logPartialSample(SystemUtils.getNanos() - this.taskExecutionStartNanos - this.idleTimeNanos, TpsDebugDimensions.SCHEDULED_TASKS.ordinal());
            samplelogger.logPartialSample(this.idleTimeNanos, TpsDebugDimensions.IDLE.ordinal());
        }

    }

    private static CrashReport constructOrExtractCrashReport(Throwable throwable) {
        ReportedException reportedexception = null;

        for (Throwable throwable1 = throwable; throwable1 != null; throwable1 = throwable1.getCause()) {
            if (throwable1 instanceof ReportedException reportedexception1) {
                reportedexception = reportedexception1;
            }
        }

        CrashReport crashreport;

        if (reportedexception != null) {
            crashreport = reportedexception.getReport();
            if (reportedexception != throwable) {
                crashreport.addCategory("Wrapped in").setDetailError("Wrapping exception", throwable);
            }
        } else {
            crashreport = new CrashReport("Exception in server tick loop", throwable);
        }

        return crashreport;
    }

    private boolean haveTime() {
        return this.runningTask() || SystemUtils.getNanos() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTimeNanos : this.nextTickTimeNanos);
    }

    protected void waitUntilNextTick() {
        this.runAllTasks();
        this.managedBlock(() -> {
            return !this.haveTime();
        });
    }

    @Override
    public void waitForTasks() {
        boolean flag = this.isTickTimeLoggingEnabled();
        long i = flag ? SystemUtils.getNanos() : 0L;

        super.waitForTasks();
        if (flag) {
            this.idleTimeNanos += SystemUtils.getNanos() - i;
        }

    }

    @Override
    public TickTask wrapRunnable(Runnable runnable) {
        return new TickTask(this.tickCount, runnable);
    }

    protected boolean shouldRun(TickTask ticktask) {
        return ticktask.getTick() + 3 < this.tickCount || this.haveTime();
    }

    @Override
    public boolean pollTask() {
        boolean flag = this.pollTaskInternal();

        this.mayHaveDelayedTasks = flag;
        return flag;
    }

    private boolean pollTaskInternal() {
        if (super.pollTask()) {
            return true;
        } else {
            if (this.tickRateManager.isSprinting() || this.haveTime()) {
                Iterator iterator = this.getAllLevels().iterator();

                while (iterator.hasNext()) {
                    WorldServer worldserver = (WorldServer) iterator.next();

                    if (worldserver.getChunkSource().pollTask()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    protected void doRunTask(TickTask ticktask) {
        this.getProfiler().incrementCounter("runTask");
        super.doRunTask(ticktask);
    }

    private Optional<ServerPing.a> loadStatusIcon() {
        Optional<Path> optional = Optional.of(this.getFile("server-icon.png").toPath()).filter((path) -> {
            return Files.isRegularFile(path, new LinkOption[0]);
        }).or(() -> {
            return this.storageSource.getIconFile().filter((path) -> {
                return Files.isRegularFile(path, new LinkOption[0]);
            });
        });

        return optional.flatMap((path) -> {
            try {
                BufferedImage bufferedimage = ImageIO.read(path.toFile());

                Preconditions.checkState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                Preconditions.checkState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();

                ImageIO.write(bufferedimage, "PNG", bytearrayoutputstream);
                return Optional.of(new ServerPing.a(bytearrayoutputstream.toByteArray()));
            } catch (Exception exception) {
                MinecraftServer.LOGGER.error("Couldn't load server icon", exception);
                return Optional.empty();
            }
        });
    }

    public Optional<Path> getWorldScreenshotFile() {
        return this.storageSource.getIconFile();
    }

    public File getServerDirectory() {
        return new File(".");
    }

    public void onServerCrash(CrashReport crashreport) {}

    public void onServerExit() {}

    public boolean isPaused() {
        return false;
    }

    public void tickServer(BooleanSupplier booleansupplier) {
        long i = SystemUtils.getNanos();

        ++this.tickCount;
        this.tickRateManager.tick();
        this.tickChildren(booleansupplier);
        if (i - this.lastServerStatus >= MinecraftServer.STATUS_EXPIRE_TIME_NANOS) {
            this.lastServerStatus = i;
            this.status = this.buildServerStatus();
        }

        --this.ticksUntilAutosave;
        if (this.ticksUntilAutosave <= 0) {
            this.ticksUntilAutosave = this.computeNextAutosaveInterval();
            MinecraftServer.LOGGER.debug("Autosave started");
            this.profiler.push("save");
            this.saveEverything(true, false, false);
            this.profiler.pop();
            MinecraftServer.LOGGER.debug("Autosave finished");
        }

        this.profiler.push("tallying");
        long j = SystemUtils.getNanos() - i;
        int k = this.tickCount % 100;

        this.aggregatedTickTimesNanos -= this.tickTimesNanos[k];
        this.aggregatedTickTimesNanos += j;
        this.tickTimesNanos[k] = j;
        this.smoothedTickTimeMillis = this.smoothedTickTimeMillis * 0.8F + (float) j / (float) TimeRange.NANOSECONDS_PER_MILLISECOND * 0.19999999F;
        this.logTickMethodTime(i);
        this.profiler.pop();
    }

    private void logTickMethodTime(long i) {
        if (this.isTickTimeLoggingEnabled()) {
            this.getTickTimeLogger().logPartialSample(SystemUtils.getNanos() - i, TpsDebugDimensions.TICK_SERVER_METHOD.ordinal());
        }

    }

    private int computeNextAutosaveInterval() {
        float f;

        if (this.tickRateManager.isSprinting()) {
            long i = this.getAverageTickTimeNanos() + 1L;

            f = (float) TimeRange.NANOSECONDS_PER_SECOND / (float) i;
        } else {
            f = this.tickRateManager.tickrate();
        }

        boolean flag = true;

        return Math.max(100, (int) (f * 300.0F));
    }

    public void onTickRateChanged() {
        int i = this.computeNextAutosaveInterval();

        if (i < this.ticksUntilAutosave) {
            this.ticksUntilAutosave = i;
        }

    }

    protected abstract SampleLogger getTickTimeLogger();

    public abstract boolean isTickTimeLoggingEnabled();

    private ServerPing buildServerStatus() {
        ServerPing.ServerPingPlayerSample serverping_serverpingplayersample = this.buildPlayerStatus();

        return new ServerPing(IChatBaseComponent.nullToEmpty(this.motd), Optional.of(serverping_serverpingplayersample), Optional.of(ServerPing.ServerData.current()), Optional.ofNullable(this.statusIcon), this.enforceSecureProfile());
    }

    private ServerPing.ServerPingPlayerSample buildPlayerStatus() {
        List<EntityPlayer> list = this.playerList.getPlayers();
        int i = this.getMaxPlayers();

        if (this.hidesOnlinePlayers()) {
            return new ServerPing.ServerPingPlayerSample(i, list.size(), List.of());
        } else {
            int j = Math.min(list.size(), 12);
            ObjectArrayList<GameProfile> objectarraylist = new ObjectArrayList(j);
            int k = MathHelper.nextInt(this.random, 0, list.size() - j);

            for (int l = 0; l < j; ++l) {
                EntityPlayer entityplayer = (EntityPlayer) list.get(k + l);

                objectarraylist.add(entityplayer.allowsListing() ? entityplayer.getGameProfile() : MinecraftServer.ANONYMOUS_PLAYER_PROFILE);
            }

            SystemUtils.shuffle(objectarraylist, this.random);
            return new ServerPing.ServerPingPlayerSample(i, list.size(), objectarraylist);
        }
    }

    public void tickChildren(BooleanSupplier booleansupplier) {
        this.getPlayerList().getPlayers().forEach((entityplayer) -> {
            entityplayer.connection.suspendFlushing();
        });
        this.profiler.push("commandFunctions");
        this.getFunctions().tick();
        this.profiler.popPush("levels");
        Iterator iterator = this.getAllLevels().iterator();

        while (iterator.hasNext()) {
            WorldServer worldserver = (WorldServer) iterator.next();

            this.profiler.push(() -> {
                String s = String.valueOf(worldserver);

                return s + " " + String.valueOf(worldserver.dimension().location());
            });
            if (this.tickCount % 20 == 0) {
                this.profiler.push("timeSync");
                this.synchronizeTime(worldserver);
                this.profiler.pop();
            }

            this.profiler.push("tick");

            try {
                worldserver.tick(booleansupplier);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception ticking world");

                worldserver.fillReportDetails(crashreport);
                throw new ReportedException(crashreport);
            }

            this.profiler.pop();
            this.profiler.pop();
        }

        this.profiler.popPush("connection");
        this.getConnection().tick();
        this.profiler.popPush("players");
        this.playerList.tick();
        if (SharedConstants.IS_RUNNING_IN_IDE && this.tickRateManager.runsNormally()) {
            GameTestHarnessTicker.SINGLETON.tick();
        }

        this.profiler.popPush("server gui refresh");

        for (int i = 0; i < this.tickables.size(); ++i) {
            ((Runnable) this.tickables.get(i)).run();
        }

        this.profiler.popPush("send chunks");
        iterator = this.playerList.getPlayers().iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            entityplayer.connection.chunkSender.sendNextChunks(entityplayer);
            entityplayer.connection.resumeFlushing();
        }

        this.profiler.pop();
    }

    private void synchronizeTime(WorldServer worldserver) {
        this.playerList.broadcastAll(new PacketPlayOutUpdateTime(worldserver.getGameTime(), worldserver.getDayTime(), worldserver.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)), worldserver.dimension());
    }

    public void forceTimeSynchronization() {
        this.profiler.push("timeSync");
        Iterator iterator = this.getAllLevels().iterator();

        while (iterator.hasNext()) {
            WorldServer worldserver = (WorldServer) iterator.next();

            this.synchronizeTime(worldserver);
        }

        this.profiler.pop();
    }

    public boolean isNetherEnabled() {
        return true;
    }

    public void addTickable(Runnable runnable) {
        this.tickables.add(runnable);
    }

    protected void setId(String s) {
        this.serverId = s;
    }

    public boolean isShutdown() {
        return !this.serverThread.isAlive();
    }

    public File getFile(String s) {
        return new File(this.getServerDirectory(), s);
    }

    public final WorldServer overworld() {
        return (WorldServer) this.levels.get(World.OVERWORLD);
    }

    @Nullable
    public WorldServer getLevel(ResourceKey<World> resourcekey) {
        return (WorldServer) this.levels.get(resourcekey);
    }

    public Set<ResourceKey<World>> levelKeys() {
        return this.levels.keySet();
    }

    public Iterable<WorldServer> getAllLevels() {
        return this.levels.values();
    }

    @Override
    public String getServerVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    @Override
    public int getPlayerCount() {
        return this.playerList.getPlayerCount();
    }

    @Override
    public int getMaxPlayers() {
        return this.playerList.getMaxPlayers();
    }

    public String[] getPlayerNames() {
        return this.playerList.getPlayerNamesArray();
    }

    @DontObfuscate
    public String getServerModName() {
        return "vanilla";
    }

    public SystemReport fillSystemReport(SystemReport systemreport) {
        systemreport.setDetail("Server Running", () -> {
            return Boolean.toString(this.running);
        });
        if (this.playerList != null) {
            systemreport.setDetail("Player Count", () -> {
                int i = this.playerList.getPlayerCount();

                return "" + i + " / " + this.playerList.getMaxPlayers() + "; " + String.valueOf(this.playerList.getPlayers());
            });
        }

        systemreport.setDetail("Active Data Packs", () -> {
            return ResourcePackRepository.displayPackList(this.packRepository.getSelectedPacks());
        });
        systemreport.setDetail("Available Data Packs", () -> {
            return ResourcePackRepository.displayPackList(this.packRepository.getAvailablePacks());
        });
        systemreport.setDetail("Enabled Feature Flags", () -> {
            return (String) FeatureFlags.REGISTRY.toNames(this.worldData.enabledFeatures()).stream().map(MinecraftKey::toString).collect(Collectors.joining(", "));
        });
        systemreport.setDetail("World Generation", () -> {
            return this.worldData.worldGenSettingsLifecycle().toString();
        });
        systemreport.setDetail("World Seed", () -> {
            return String.valueOf(this.worldData.worldGenOptions().seed());
        });
        if (this.serverId != null) {
            systemreport.setDetail("Server Id", () -> {
                return this.serverId;
            });
        }

        return this.fillServerSystemReport(systemreport);
    }

    public abstract SystemReport fillServerSystemReport(SystemReport systemreport);

    public ModCheck getModdedStatus() {
        return ModCheck.identify("vanilla", this::getServerModName, "Server", MinecraftServer.class);
    }

    @Override
    public void sendSystemMessage(IChatBaseComponent ichatbasecomponent) {
        MinecraftServer.LOGGER.info(ichatbasecomponent.getString());
    }

    public KeyPair getKeyPair() {
        return this.keyPair;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int i) {
        this.port = i;
    }

    @Nullable
    public GameProfile getSingleplayerProfile() {
        return this.singleplayerProfile;
    }

    public void setSingleplayerProfile(@Nullable GameProfile gameprofile) {
        this.singleplayerProfile = gameprofile;
    }

    public boolean isSingleplayer() {
        return this.singleplayerProfile != null;
    }

    protected void initializeKeyPair() {
        MinecraftServer.LOGGER.info("Generating keypair");

        try {
            this.keyPair = MinecraftEncryption.generateKeyPair();
        } catch (CryptographyException cryptographyexception) {
            throw new IllegalStateException("Failed to generate key pair", cryptographyexception);
        }
    }

    public void setDifficulty(EnumDifficulty enumdifficulty, boolean flag) {
        if (flag || !this.worldData.isDifficultyLocked()) {
            this.worldData.setDifficulty(this.worldData.isHardcore() ? EnumDifficulty.HARD : enumdifficulty);
            this.updateMobSpawningFlags();
            this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
        }
    }

    public int getScaledTrackingDistance(int i) {
        return i;
    }

    private void updateMobSpawningFlags() {
        Iterator iterator = this.getAllLevels().iterator();

        while (iterator.hasNext()) {
            WorldServer worldserver = (WorldServer) iterator.next();

            worldserver.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());
        }

    }

    public void setDifficultyLocked(boolean flag) {
        this.worldData.setDifficultyLocked(flag);
        this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
    }

    private void sendDifficultyUpdate(EntityPlayer entityplayer) {
        WorldData worlddata = entityplayer.level().getLevelData();

        entityplayer.connection.send(new PacketPlayOutServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
    }

    public boolean isSpawningMonsters() {
        return this.worldData.getDifficulty() != EnumDifficulty.PEACEFUL;
    }

    public boolean isDemo() {
        return this.isDemo;
    }

    public void setDemo(boolean flag) {
        this.isDemo = flag;
    }

    public Optional<MinecraftServer.ServerResourcePackInfo> getServerResourcePack() {
        return Optional.empty();
    }

    public boolean isResourcePackRequired() {
        return this.getServerResourcePack().filter(MinecraftServer.ServerResourcePackInfo::isRequired).isPresent();
    }

    public abstract boolean isDedicatedServer();

    public abstract int getRateLimitPacketsPerSecond();

    public boolean usesAuthentication() {
        return this.onlineMode;
    }

    public void setUsesAuthentication(boolean flag) {
        this.onlineMode = flag;
    }

    public boolean getPreventProxyConnections() {
        return this.preventProxyConnections;
    }

    public void setPreventProxyConnections(boolean flag) {
        this.preventProxyConnections = flag;
    }

    public boolean isSpawningAnimals() {
        return true;
    }

    public boolean areNpcsEnabled() {
        return true;
    }

    public abstract boolean isEpollEnabled();

    public boolean isPvpAllowed() {
        return this.pvp;
    }

    public void setPvpAllowed(boolean flag) {
        this.pvp = flag;
    }

    public boolean isFlightAllowed() {
        return this.allowFlight;
    }

    public void setFlightAllowed(boolean flag) {
        this.allowFlight = flag;
    }

    public abstract boolean isCommandBlockEnabled();

    @Override
    public String getMotd() {
        return this.motd;
    }

    public void setMotd(String s) {
        this.motd = s;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public PlayerList getPlayerList() {
        return this.playerList;
    }

    public void setPlayerList(PlayerList playerlist) {
        this.playerList = playerlist;
    }

    public abstract boolean isPublished();

    public void setDefaultGameType(EnumGamemode enumgamemode) {
        this.worldData.setGameType(enumgamemode);
    }

    public ServerConnection getConnection() {
        return this.connection;
    }

    public boolean isReady() {
        return this.isReady;
    }

    public boolean hasGui() {
        return false;
    }

    public boolean publishServer(@Nullable EnumGamemode enumgamemode, boolean flag, int i) {
        return false;
    }

    public int getTickCount() {
        return this.tickCount;
    }

    public int getSpawnProtectionRadius() {
        return 16;
    }

    public boolean isUnderSpawnProtection(WorldServer worldserver, BlockPosition blockposition, EntityHuman entityhuman) {
        return false;
    }

    public boolean repliesToStatus() {
        return true;
    }

    public boolean hidesOnlinePlayers() {
        return false;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public int getPlayerIdleTimeout() {
        return this.playerIdleTimeout;
    }

    public void setPlayerIdleTimeout(int i) {
        this.playerIdleTimeout = i;
    }

    public MinecraftSessionService getSessionService() {
        return this.services.sessionService();
    }

    @Nullable
    public SignatureValidator getProfileKeySignatureValidator() {
        return this.services.profileKeySignatureValidator();
    }

    public GameProfileRepository getProfileRepository() {
        return this.services.profileRepository();
    }

    @Nullable
    public UserCache getProfileCache() {
        return this.services.profileCache();
    }

    @Nullable
    public ServerPing getStatus() {
        return this.status;
    }

    public void invalidateStatus() {
        this.lastServerStatus = 0L;
    }

    public int getAbsoluteMaxWorldSize() {
        return 29999984;
    }

    @Override
    public boolean scheduleExecutables() {
        return super.scheduleExecutables() && !this.isStopped();
    }

    @Override
    public void executeIfPossible(Runnable runnable) {
        if (this.isStopped()) {
            throw new RejectedExecutionException("Server already shutting down");
        } else {
            super.executeIfPossible(runnable);
        }
    }

    @Override
    public Thread getRunningThread() {
        return this.serverThread;
    }

    public int getCompressionThreshold() {
        return 256;
    }

    public boolean enforceSecureProfile() {
        return false;
    }

    public long getNextTickTime() {
        return this.nextTickTimeNanos;
    }

    public DataFixer getFixerUpper() {
        return this.fixerUpper;
    }

    public int getSpawnRadius(@Nullable WorldServer worldserver) {
        return worldserver != null ? worldserver.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS) : 10;
    }

    public AdvancementDataWorld getAdvancements() {
        return this.resources.managers.getAdvancements();
    }

    public CustomFunctionData getFunctions() {
        return this.functionManager;
    }

    public CompletableFuture<Void> reloadResources(Collection<String> collection) {
        CompletableFuture<Void> completablefuture = CompletableFuture.supplyAsync(() -> {
            Stream stream = collection.stream();
            ResourcePackRepository resourcepackrepository = this.packRepository;

            Objects.requireNonNull(this.packRepository);
            return (ImmutableList) stream.map(resourcepackrepository::getPack).filter(Objects::nonNull).map(ResourcePackLoader::open).collect(ImmutableList.toImmutableList());
        }, this).thenCompose((immutablelist) -> {
            ResourceManager resourcemanager = new ResourceManager(EnumResourcePackType.SERVER_DATA, immutablelist);

            return DataPackResources.loadResources(resourcemanager, this.registries, this.worldData.enabledFeatures(), this.isDedicatedServer() ? CommandDispatcher.ServerType.DEDICATED : CommandDispatcher.ServerType.INTEGRATED, this.getFunctionCompilationLevel(), this.executor, this).whenComplete((datapackresources, throwable) -> {
                if (throwable != null) {
                    resourcemanager.close();
                }

            }).thenApply((datapackresources) -> {
                return new MinecraftServer.ReloadableResources(resourcemanager, datapackresources);
            });
        }).thenAcceptAsync((minecraftserver_reloadableresources) -> {
            this.resources.close();
            this.resources = minecraftserver_reloadableresources;
            this.packRepository.setSelected(collection);
            WorldDataConfiguration worlddataconfiguration = new WorldDataConfiguration(getSelectedPacks(this.packRepository, true), this.worldData.enabledFeatures());

            this.worldData.setDataConfiguration(worlddataconfiguration);
            this.resources.managers.updateRegistryTags();
            this.getPlayerList().saveAll();
            this.getPlayerList().reloadResources();
            this.functionManager.replaceLibrary(this.resources.managers.getFunctionLibrary());
            this.structureTemplateManager.onResourceManagerReload(this.resources.resourceManager);
        }, this);

        if (this.isSameThread()) {
            Objects.requireNonNull(completablefuture);
            this.managedBlock(completablefuture::isDone);
        }

        return completablefuture;
    }

    public static WorldDataConfiguration configurePackRepository(ResourcePackRepository resourcepackrepository, WorldDataConfiguration worlddataconfiguration, boolean flag, boolean flag1) {
        DataPackConfiguration datapackconfiguration = worlddataconfiguration.dataPacks();
        FeatureFlagSet featureflagset = flag ? FeatureFlagSet.of() : worlddataconfiguration.enabledFeatures();
        FeatureFlagSet featureflagset1 = flag ? FeatureFlags.REGISTRY.allFlags() : worlddataconfiguration.enabledFeatures();

        resourcepackrepository.reload();
        if (flag1) {
            return configureRepositoryWithSelection(resourcepackrepository, List.of("vanilla"), featureflagset, false);
        } else {
            Set<String> set = Sets.newLinkedHashSet();
            Iterator iterator = datapackconfiguration.getEnabled().iterator();

            while (iterator.hasNext()) {
                String s = (String) iterator.next();

                if (resourcepackrepository.isAvailable(s)) {
                    set.add(s);
                } else {
                    MinecraftServer.LOGGER.warn("Missing data pack {}", s);
                }
            }

            iterator = resourcepackrepository.getAvailablePacks().iterator();

            while (iterator.hasNext()) {
                ResourcePackLoader resourcepackloader = (ResourcePackLoader) iterator.next();
                String s1 = resourcepackloader.getId();

                if (!datapackconfiguration.getDisabled().contains(s1)) {
                    FeatureFlagSet featureflagset2 = resourcepackloader.getRequestedFeatures();
                    boolean flag2 = set.contains(s1);

                    if (!flag2 && resourcepackloader.getPackSource().shouldAddAutomatically()) {
                        if (featureflagset2.isSubsetOf(featureflagset1)) {
                            MinecraftServer.LOGGER.info("Found new data pack {}, loading it automatically", s1);
                            set.add(s1);
                        } else {
                            MinecraftServer.LOGGER.info("Found new data pack {}, but can't load it due to missing features {}", s1, FeatureFlags.printMissingFlags(featureflagset1, featureflagset2));
                        }
                    }

                    if (flag2 && !featureflagset2.isSubsetOf(featureflagset1)) {
                        MinecraftServer.LOGGER.warn("Pack {} requires features {} that are not enabled for this world, disabling pack.", s1, FeatureFlags.printMissingFlags(featureflagset1, featureflagset2));
                        set.remove(s1);
                    }
                }
            }

            if (set.isEmpty()) {
                MinecraftServer.LOGGER.info("No datapacks selected, forcing vanilla");
                set.add("vanilla");
            }

            return configureRepositoryWithSelection(resourcepackrepository, set, featureflagset, true);
        }
    }

    private static WorldDataConfiguration configureRepositoryWithSelection(ResourcePackRepository resourcepackrepository, Collection<String> collection, FeatureFlagSet featureflagset, boolean flag) {
        resourcepackrepository.setSelected(collection);
        enableForcedFeaturePacks(resourcepackrepository, featureflagset);
        DataPackConfiguration datapackconfiguration = getSelectedPacks(resourcepackrepository, flag);
        FeatureFlagSet featureflagset1 = resourcepackrepository.getRequestedFeatureFlags().join(featureflagset);

        return new WorldDataConfiguration(datapackconfiguration, featureflagset1);
    }

    private static void enableForcedFeaturePacks(ResourcePackRepository resourcepackrepository, FeatureFlagSet featureflagset) {
        FeatureFlagSet featureflagset1 = resourcepackrepository.getRequestedFeatureFlags();
        FeatureFlagSet featureflagset2 = featureflagset.subtract(featureflagset1);

        if (!featureflagset2.isEmpty()) {
            Set<String> set = new ObjectArraySet(resourcepackrepository.getSelectedIds());
            Iterator iterator = resourcepackrepository.getAvailablePacks().iterator();

            while (iterator.hasNext()) {
                ResourcePackLoader resourcepackloader = (ResourcePackLoader) iterator.next();

                if (featureflagset2.isEmpty()) {
                    break;
                }

                if (resourcepackloader.getPackSource() == PackSource.FEATURE) {
                    String s = resourcepackloader.getId();
                    FeatureFlagSet featureflagset3 = resourcepackloader.getRequestedFeatures();

                    if (!featureflagset3.isEmpty() && featureflagset3.intersects(featureflagset2) && featureflagset3.isSubsetOf(featureflagset)) {
                        if (!set.add(s)) {
                            throw new IllegalStateException("Tried to force '" + s + "', but it was already enabled");
                        }

                        MinecraftServer.LOGGER.info("Found feature pack ('{}') for requested feature, forcing to enabled", s);
                        featureflagset2 = featureflagset2.subtract(featureflagset3);
                    }
                }
            }

            resourcepackrepository.setSelected(set);
        }
    }

    private static DataPackConfiguration getSelectedPacks(ResourcePackRepository resourcepackrepository, boolean flag) {
        Collection<String> collection = resourcepackrepository.getSelectedIds();
        List<String> list = ImmutableList.copyOf(collection);
        List<String> list1 = flag ? resourcepackrepository.getAvailableIds().stream().filter((s) -> {
            return !collection.contains(s);
        }).toList() : List.of();

        return new DataPackConfiguration(list, list1);
    }

    public void kickUnlistedPlayers(CommandListenerWrapper commandlistenerwrapper) {
        if (this.isEnforceWhitelist()) {
            PlayerList playerlist = commandlistenerwrapper.getServer().getPlayerList();
            WhiteList whitelist = playerlist.getWhiteList();
            List<EntityPlayer> list = Lists.newArrayList(playerlist.getPlayers());
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                if (!whitelist.isWhiteListed(entityplayer.getGameProfile())) {
                    entityplayer.connection.disconnect(IChatBaseComponent.translatable("multiplayer.disconnect.not_whitelisted"));
                }
            }

        }
    }

    public ResourcePackRepository getPackRepository() {
        return this.packRepository;
    }

    public CommandDispatcher getCommands() {
        return this.resources.managers.getCommands();
    }

    public CommandListenerWrapper createCommandSourceStack() {
        WorldServer worldserver = this.overworld();

        return new CommandListenerWrapper(this, worldserver == null ? Vec3D.ZERO : Vec3D.atLowerCornerOf(worldserver.getSharedSpawnPos()), Vec2F.ZERO, worldserver, 4, "Server", IChatBaseComponent.literal("Server"), this, (Entity) null);
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public abstract boolean shouldInformAdmins();

    public CraftingManager getRecipeManager() {
        return this.resources.managers.getRecipeManager();
    }

    public ScoreboardServer getScoreboard() {
        return this.scoreboard;
    }

    public PersistentCommandStorage getCommandStorage() {
        if (this.commandStorage == null) {
            throw new NullPointerException("Called before server init");
        } else {
            return this.commandStorage;
        }
    }

    public GameRules getGameRules() {
        return this.overworld().getGameRules();
    }

    public BossBattleCustomData getCustomBossEvents() {
        return this.customBossEvents;
    }

    public boolean isEnforceWhitelist() {
        return this.enforceWhitelist;
    }

    public void setEnforceWhitelist(boolean flag) {
        this.enforceWhitelist = flag;
    }

    public float getCurrentSmoothedTickTime() {
        return this.smoothedTickTimeMillis;
    }

    public ServerTickRateManager tickRateManager() {
        return this.tickRateManager;
    }

    public long getAverageTickTimeNanos() {
        return this.aggregatedTickTimesNanos / (long) Math.min(100, Math.max(this.tickCount, 1));
    }

    public long[] getTickTimesNanos() {
        return this.tickTimesNanos;
    }

    public int getProfilePermissions(GameProfile gameprofile) {
        if (this.getPlayerList().isOp(gameprofile)) {
            OpListEntry oplistentry = (OpListEntry) this.getPlayerList().getOps().get(gameprofile);

            return oplistentry != null ? oplistentry.getLevel() : (this.isSingleplayerOwner(gameprofile) ? 4 : (this.isSingleplayer() ? (this.getPlayerList().isAllowCommandsForAllPlayers() ? 4 : 0) : this.getOperatorUserPermissionLevel()));
        } else {
            return 0;
        }
    }

    public GameProfilerFiller getProfiler() {
        return this.profiler;
    }

    public abstract boolean isSingleplayerOwner(GameProfile gameprofile);

    public void dumpServerProperties(Path path) throws IOException {}

    private void saveDebugReport(Path path) {
        Path path1 = path.resolve("levels");

        try {
            Iterator iterator = this.levels.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<ResourceKey<World>, WorldServer> entry = (Entry) iterator.next();
                MinecraftKey minecraftkey = ((ResourceKey) entry.getKey()).location();
                Path path2 = path1.resolve(minecraftkey.getNamespace()).resolve(minecraftkey.getPath());

                Files.createDirectories(path2);
                ((WorldServer) entry.getValue()).saveDebugReport(path2);
            }

            this.dumpGameRules(path.resolve("gamerules.txt"));
            this.dumpClasspath(path.resolve("classpath.txt"));
            this.dumpMiscStats(path.resolve("stats.txt"));
            this.dumpThreads(path.resolve("threads.txt"));
            this.dumpServerProperties(path.resolve("server.properties.txt"));
            this.dumpNativeModules(path.resolve("modules.txt"));
        } catch (IOException ioexception) {
            MinecraftServer.LOGGER.warn("Failed to save debug report", ioexception);
        }

    }

    private void dumpMiscStats(Path path) throws IOException {
        BufferedWriter bufferedwriter = Files.newBufferedWriter(path);

        try {
            bufferedwriter.write(String.format(Locale.ROOT, "pending_tasks: %d\n", this.getPendingTasksCount()));
            bufferedwriter.write(String.format(Locale.ROOT, "average_tick_time: %f\n", this.getCurrentSmoothedTickTime()));
            bufferedwriter.write(String.format(Locale.ROOT, "tick_times: %s\n", Arrays.toString(this.tickTimesNanos)));
            bufferedwriter.write(String.format(Locale.ROOT, "queue: %s\n", SystemUtils.backgroundExecutor()));
        } catch (Throwable throwable) {
            if (bufferedwriter != null) {
                try {
                    bufferedwriter.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            }

            throw throwable;
        }

        if (bufferedwriter != null) {
            bufferedwriter.close();
        }

    }

    private void dumpGameRules(Path path) throws IOException {
        BufferedWriter bufferedwriter = Files.newBufferedWriter(path);

        try {
            final List<String> list = Lists.newArrayList();
            final GameRules gamerules = this.getGameRules();

            GameRules.visitGameRuleTypes(new GameRules.GameRuleVisitor(this) {
                @Override
                public <T extends GameRules.GameRuleValue<T>> void visit(GameRules.GameRuleKey<T> gamerules_gamerulekey, GameRules.GameRuleDefinition<T> gamerules_gameruledefinition) {
                    list.add(String.format(Locale.ROOT, "%s=%s\n", gamerules_gamerulekey.getId(), gamerules.getRule(gamerules_gamerulekey)));
                }
            });
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                String s = (String) iterator.next();

                bufferedwriter.write(s);
            }
        } catch (Throwable throwable) {
            if (bufferedwriter != null) {
                try {
                    bufferedwriter.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            }

            throw throwable;
        }

        if (bufferedwriter != null) {
            bufferedwriter.close();
        }

    }

    private void dumpClasspath(Path path) throws IOException {
        BufferedWriter bufferedwriter = Files.newBufferedWriter(path);

        try {
            String s = System.getProperty("java.class.path");
            String s1 = System.getProperty("path.separator");
            Iterator iterator = Splitter.on(s1).split(s).iterator();

            while (iterator.hasNext()) {
                String s2 = (String) iterator.next();

                bufferedwriter.write(s2);
                bufferedwriter.write("\n");
            }
        } catch (Throwable throwable) {
            if (bufferedwriter != null) {
                try {
                    bufferedwriter.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            }

            throw throwable;
        }

        if (bufferedwriter != null) {
            bufferedwriter.close();
        }

    }

    private void dumpThreads(Path path) throws IOException {
        ThreadMXBean threadmxbean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] athreadinfo = threadmxbean.dumpAllThreads(true, true);

        Arrays.sort(athreadinfo, Comparator.comparing(ThreadInfo::getThreadName));
        BufferedWriter bufferedwriter = Files.newBufferedWriter(path);

        try {
            ThreadInfo[] athreadinfo1 = athreadinfo;
            int i = athreadinfo.length;

            for (int j = 0; j < i; ++j) {
                ThreadInfo threadinfo = athreadinfo1[j];

                bufferedwriter.write(threadinfo.toString());
                bufferedwriter.write(10);
            }
        } catch (Throwable throwable) {
            if (bufferedwriter != null) {
                try {
                    bufferedwriter.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            }

            throw throwable;
        }

        if (bufferedwriter != null) {
            bufferedwriter.close();
        }

    }

    private void dumpNativeModules(Path path) throws IOException {
        BufferedWriter bufferedwriter = Files.newBufferedWriter(path);

        label50:
        {
            try {
                label51:
                {
                    ArrayList arraylist;

                    try {
                        arraylist = Lists.newArrayList(NativeModuleLister.listModules());
                    } catch (Throwable throwable) {
                        MinecraftServer.LOGGER.warn("Failed to list native modules", throwable);
                        break label51;
                    }

                    arraylist.sort(Comparator.comparing((nativemodulelister_a) -> {
                        return nativemodulelister_a.name;
                    }));
                    Iterator iterator = arraylist.iterator();

                    while (true) {
                        if (!iterator.hasNext()) {
                            break label50;
                        }

                        NativeModuleLister.a nativemodulelister_a = (NativeModuleLister.a) iterator.next();

                        bufferedwriter.write(nativemodulelister_a.toString());
                        bufferedwriter.write(10);
                    }
                }
            } catch (Throwable throwable1) {
                if (bufferedwriter != null) {
                    try {
                        bufferedwriter.close();
                    } catch (Throwable throwable2) {
                        throwable1.addSuppressed(throwable2);
                    }
                }

                throw throwable1;
            }

            if (bufferedwriter != null) {
                bufferedwriter.close();
            }

            return;
        }

        if (bufferedwriter != null) {
            bufferedwriter.close();
        }

    }

    private void startMetricsRecordingTick() {
        if (this.willStartRecordingMetrics) {
            this.metricsRecorder = ActiveMetricsRecorder.createStarted(new ServerMetricsSamplersProvider(SystemUtils.timeSource, this.isDedicatedServer()), SystemUtils.timeSource, SystemUtils.ioPool(), new MetricsPersister("server"), this.onMetricsRecordingStopped, (path) -> {
                this.executeBlocking(() -> {
                    this.saveDebugReport(path.resolve("server"));
                });
                this.onMetricsRecordingFinished.accept(path);
            });
            this.willStartRecordingMetrics = false;
        }

        this.profiler = GameProfilerTick.decorateFiller(this.metricsRecorder.getProfiler(), GameProfilerTick.createTickProfiler("Server"));
        this.metricsRecorder.startTick();
        this.profiler.startTick();
    }

    public void endMetricsRecordingTick() {
        this.profiler.endTick();
        this.metricsRecorder.endTick();
    }

    public boolean isRecordingMetrics() {
        return this.metricsRecorder.isRecording();
    }

    public void startRecordingMetrics(Consumer<MethodProfilerResults> consumer, Consumer<Path> consumer1) {
        this.onMetricsRecordingStopped = (methodprofilerresults) -> {
            this.stopRecordingMetrics();
            consumer.accept(methodprofilerresults);
        };
        this.onMetricsRecordingFinished = consumer1;
        this.willStartRecordingMetrics = true;
    }

    public void stopRecordingMetrics() {
        this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    }

    public void finishRecordingMetrics() {
        this.metricsRecorder.end();
    }

    public void cancelRecordingMetrics() {
        this.metricsRecorder.cancel();
        this.profiler = this.metricsRecorder.getProfiler();
    }

    public Path getWorldPath(SavedFile savedfile) {
        return this.storageSource.getLevelPath(savedfile);
    }

    public boolean forceSynchronousWrites() {
        return true;
    }

    public StructureTemplateManager getStructureManager() {
        return this.structureTemplateManager;
    }

    public SaveData getWorldData() {
        return this.worldData;
    }

    public IRegistryCustom.Dimension registryAccess() {
        return this.registries.compositeAccess();
    }

    public LayeredRegistryAccess<RegistryLayer> registries() {
        return this.registries;
    }

    public ReloadableServerRegistries.b reloadableRegistries() {
        return this.resources.managers.fullRegistries();
    }

    public ITextFilter createTextFilterForPlayer(EntityPlayer entityplayer) {
        return ITextFilter.DUMMY;
    }

    public PlayerInteractManager createGameModeForPlayer(EntityPlayer entityplayer) {
        return (PlayerInteractManager) (this.isDemo() ? new DemoPlayerInteractManager(entityplayer) : new PlayerInteractManager(entityplayer));
    }

    @Nullable
    public EnumGamemode getForcedGameType() {
        return null;
    }

    public IResourceManager getResourceManager() {
        return this.resources.resourceManager;
    }

    public boolean isCurrentlySaving() {
        return this.isSaving;
    }

    public boolean isTimeProfilerRunning() {
        return this.debugCommandProfilerDelayStart || this.debugCommandProfiler != null;
    }

    public void startTimeProfiler() {
        this.debugCommandProfilerDelayStart = true;
    }

    public MethodProfilerResults stopTimeProfiler() {
        if (this.debugCommandProfiler == null) {
            return MethodProfilerResultsEmpty.EMPTY;
        } else {
            MethodProfilerResults methodprofilerresults = this.debugCommandProfiler.stop(SystemUtils.getNanos(), this.tickCount);

            this.debugCommandProfiler = null;
            return methodprofilerresults;
        }
    }

    public int getMaxChainedNeighborUpdates() {
        return 1000000;
    }

    public void logChatMessage(IChatBaseComponent ichatbasecomponent, ChatMessageType.a chatmessagetype_a, @Nullable String s) {
        String s1 = chatmessagetype_a.decorate(ichatbasecomponent).getString();

        if (s != null) {
            MinecraftServer.LOGGER.info("[{}] {}", s, s1);
        } else {
            MinecraftServer.LOGGER.info("{}", s1);
        }

    }

    public ChatDecorator getChatDecorator() {
        return ChatDecorator.PLAIN;
    }

    public boolean logIPs() {
        return true;
    }

    public void subscribeToDebugSample(EntityPlayer entityplayer, RemoteDebugSampleType remotedebugsampletype) {}

    public boolean acceptsTransfers() {
        return false;
    }

    public void reportChunkLoadFailure(ChunkCoordIntPair chunkcoordintpair) {}

    public void reportChunkSaveFailure(ChunkCoordIntPair chunkcoordintpair) {}

    public PotionBrewer potionBrewing() {
        return this.potionBrewing;
    }

    public static record ReloadableResources(IReloadableResourceManager resourceManager, DataPackResources managers) implements AutoCloseable {

        public void close() {
            this.resourceManager.close();
        }
    }

    private static class TimeProfiler {

        final long startNanos;
        final int startTick;

        TimeProfiler(long i, int j) {
            this.startNanos = i;
            this.startTick = j;
        }

        MethodProfilerResults stop(final long i, final int j) {
            return new MethodProfilerResults() {
                @Override
                public List<MethodProfilerResultsField> getTimes(String s) {
                    return Collections.emptyList();
                }

                @Override
                public boolean saveResults(Path path) {
                    return false;
                }

                @Override
                public long getStartTimeNano() {
                    return TimeProfiler.this.startNanos;
                }

                @Override
                public int getStartTimeTicks() {
                    return TimeProfiler.this.startTick;
                }

                @Override
                public long getEndTimeNano() {
                    return i;
                }

                @Override
                public int getEndTimeTicks() {
                    return j;
                }

                @Override
                public String getProfilerResults() {
                    return "";
                }
            };
        }
    }

    public static record ServerResourcePackInfo(UUID id, String url, String hash, boolean isRequired, @Nullable IChatBaseComponent prompt) {

    }
}
