package net.minecraft.server;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.commands.CommandDispatcher;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.WorldLoadListenerLogger;
import net.minecraft.server.packs.repository.ResourcePackRepository;
import net.minecraft.server.packs.repository.ResourcePackSourceVanilla;
import net.minecraft.util.MathHelper;
import net.minecraft.util.datafix.DataConverterRegistry;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.WorldSettings;
import net.minecraft.world.level.chunk.storage.RegionFileCompression;
import net.minecraft.world.level.dimension.WorldDimension;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.Convertable;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.SaveData;
import net.minecraft.world.level.storage.WorldDataServer;
import net.minecraft.world.level.storage.WorldInfo;
import org.slf4j.Logger;

public class Main {

    private static final Logger LOGGER = LogUtils.getLogger();

    public Main() {}

    @DontObfuscate
    public static void main(String[] astring) {
        SharedConstants.tryDetectVersion();
        OptionParser optionparser = new OptionParser();
        OptionSpec<Void> optionspec = optionparser.accepts("nogui");
        OptionSpec<Void> optionspec1 = optionparser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpec<Void> optionspec2 = optionparser.accepts("demo");
        OptionSpec<Void> optionspec3 = optionparser.accepts("bonusChest");
        OptionSpec<Void> optionspec4 = optionparser.accepts("forceUpgrade");
        OptionSpec<Void> optionspec5 = optionparser.accepts("eraseCache");
        OptionSpec<Void> optionspec6 = optionparser.accepts("recreateRegionFiles");
        OptionSpec<Void> optionspec7 = optionparser.accepts("safeMode", "Loads level with vanilla datapack only");
        OptionSpec<Void> optionspec8 = optionparser.accepts("help").forHelp();
        OptionSpec<String> optionspec9 = optionparser.accepts("universe").withRequiredArg().defaultsTo(".", new String[0]);
        OptionSpec<String> optionspec10 = optionparser.accepts("world").withRequiredArg();
        OptionSpec<Integer> optionspec11 = optionparser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1, new Integer[0]);
        OptionSpec<String> optionspec12 = optionparser.accepts("serverId").withRequiredArg();
        OptionSpec<Void> optionspec13 = optionparser.accepts("jfrProfile");
        OptionSpec<Path> optionspec14 = optionparser.accepts("pidFile").withRequiredArg().withValuesConvertedBy(new PathConverter(new PathProperties[0]));
        OptionSpec<String> optionspec15 = optionparser.nonOptions();

        try {
            OptionSet optionset = optionparser.parse(astring);

            if (optionset.has(optionspec8)) {
                optionparser.printHelpOn(System.err);
                return;
            }

            Path path = (Path) optionset.valueOf(optionspec14);

            if (path != null) {
                writePidFile(path);
            }

            CrashReport.preload();
            if (optionset.has(optionspec13)) {
                JvmProfiler.INSTANCE.start(Environment.SERVER);
            }

            DispenserRegistry.bootStrap();
            DispenserRegistry.validate();
            SystemUtils.startTimerHackThread();
            Path path1 = Paths.get("server.properties");
            DedicatedServerSettings dedicatedserversettings = new DedicatedServerSettings(path1);

            dedicatedserversettings.forceSave();
            RegionFileCompression.configure(dedicatedserversettings.getProperties().regionFileComression);
            Path path2 = Paths.get("eula.txt");
            EULA eula = new EULA(path2);

            if (optionset.has(optionspec1)) {
                Main.LOGGER.info("Initialized '{}' and '{}'", path1.toAbsolutePath(), path2.toAbsolutePath());
                return;
            }

            if (!eula.hasAgreedToEULA()) {
                Main.LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }

            File file = new File((String) optionset.valueOf(optionspec9));
            Services services = Services.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), file);
            String s = (String) Optional.ofNullable((String) optionset.valueOf(optionspec10)).orElse(dedicatedserversettings.getProperties().levelName);
            Convertable convertable = Convertable.createDefault(file.toPath());
            Convertable.ConversionSession convertable_conversionsession = convertable.validateAndCreateAccess(s);
            Dynamic dynamic;

            if (convertable_conversionsession.hasWorldData()) {
                WorldInfo worldinfo;

                try {
                    dynamic = convertable_conversionsession.getDataTag();
                    worldinfo = convertable_conversionsession.getSummary(dynamic);
                } catch (NbtException | ReportedNbtException | IOException ioexception) {
                    Convertable.b convertable_b = convertable_conversionsession.getLevelDirectory();

                    Main.LOGGER.warn("Failed to load world data from {}", convertable_b.dataFile(), ioexception);
                    Main.LOGGER.info("Attempting to use fallback");

                    try {
                        dynamic = convertable_conversionsession.getDataTagFallback();
                        worldinfo = convertable_conversionsession.getSummary(dynamic);
                    } catch (NbtException | ReportedNbtException | IOException ioexception1) {
                        Main.LOGGER.error("Failed to load world data from {}", convertable_b.oldDataFile(), ioexception1);
                        Main.LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", convertable_b.dataFile(), convertable_b.oldDataFile());
                        return;
                    }

                    convertable_conversionsession.restoreLevelDataFromOld();
                }

                if (worldinfo.requiresManualConversion()) {
                    Main.LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }

                if (!worldinfo.isCompatible()) {
                    Main.LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            } else {
                dynamic = null;
            }

            Dynamic<?> dynamic1 = dynamic;
            boolean flag = optionset.has(optionspec7);

            if (flag) {
                Main.LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }

            ResourcePackRepository resourcepackrepository = ResourcePackSourceVanilla.createPackRepository(convertable_conversionsession);

            WorldStem worldstem;

            try {
                WorldLoader.c worldloader_c = loadOrCreateConfig(dedicatedserversettings.getProperties(), dynamic1, flag, resourcepackrepository);

                worldstem = (WorldStem) SystemUtils.blockUntilDone((executor) -> {
                    return WorldLoader.load(worldloader_c, (worldloader_a) -> {
                        IRegistry<WorldDimension> iregistry = worldloader_a.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);

                        if (dynamic1 != null) {
                            LevelDataAndDimensions leveldataanddimensions = Convertable.getLevelDataAndDimensions(dynamic1, worldloader_a.dataConfiguration(), iregistry, worldloader_a.datapackWorldgen());

                            return new WorldLoader.b<>(leveldataanddimensions.worldData(), leveldataanddimensions.dimensions().dimensionsRegistryAccess());
                        } else {
                            Main.LOGGER.info("No existing world data, creating new world");
                            WorldSettings worldsettings;
                            WorldOptions worldoptions;
                            WorldDimensions worlddimensions;

                            if (optionset.has(optionspec2)) {
                                worldsettings = MinecraftServer.DEMO_SETTINGS;
                                worldoptions = WorldOptions.DEMO_OPTIONS;
                                worlddimensions = WorldPresets.createNormalWorldDimensions(worldloader_a.datapackWorldgen());
                            } else {
                                DedicatedServerProperties dedicatedserverproperties = dedicatedserversettings.getProperties();

                                worldsettings = new WorldSettings(dedicatedserverproperties.levelName, dedicatedserverproperties.gamemode, dedicatedserverproperties.hardcore, dedicatedserverproperties.difficulty, false, new GameRules(), worldloader_a.dataConfiguration());
                                worldoptions = optionset.has(optionspec3) ? dedicatedserverproperties.worldOptions.withBonusChest(true) : dedicatedserverproperties.worldOptions;
                                worlddimensions = dedicatedserverproperties.createDimensions(worldloader_a.datapackWorldgen());
                            }

                            WorldDimensions.b worlddimensions_b = worlddimensions.bake(iregistry);
                            Lifecycle lifecycle = worlddimensions_b.lifecycle().add(worldloader_a.datapackWorldgen().allRegistriesLifecycle());

                            return new WorldLoader.b<>(new WorldDataServer(worldsettings, worldoptions, worlddimensions_b.specialWorldProperty(), lifecycle), worlddimensions_b.dimensionsRegistryAccess());
                        }
                    }, WorldStem::new, SystemUtils.backgroundExecutor(), executor);
                }).get();
            } catch (Exception exception) {
                Main.LOGGER.warn("Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", exception);
                return;
            }

            IRegistryCustom.Dimension iregistrycustom_dimension = worldstem.registries().compositeAccess();
            boolean flag1 = optionset.has(optionspec6);

            if (optionset.has(optionspec4) || flag1) {
                forceUpgrade(convertable_conversionsession, DataConverterRegistry.getDataFixer(), optionset.has(optionspec5), () -> {
                    return true;
                }, iregistrycustom_dimension, flag1);
            }

            SaveData savedata = worldstem.worldData();

            convertable_conversionsession.saveDataTag(iregistrycustom_dimension, savedata);
            final DedicatedServer dedicatedserver = (DedicatedServer) MinecraftServer.spin((thread) -> {
                DedicatedServer dedicatedserver1 = new DedicatedServer(thread, convertable_conversionsession, resourcepackrepository, worldstem, dedicatedserversettings, DataConverterRegistry.getDataFixer(), services, WorldLoadListenerLogger::createFromGameruleRadius);

                dedicatedserver1.setPort((Integer) optionset.valueOf(optionspec11));
                dedicatedserver1.setDemo(optionset.has(optionspec2));
                dedicatedserver1.setId((String) optionset.valueOf(optionspec12));
                boolean flag2 = !optionset.has(optionspec) && !optionset.valuesOf(optionspec15).contains("nogui");

                if (flag2 && !GraphicsEnvironment.isHeadless()) {
                    dedicatedserver1.showGui();
                }

                return dedicatedserver1;
            });
            Thread thread = new Thread("Server Shutdown Thread") {
                public void run() {
                    dedicatedserver.halt(true);
                }
            };

            thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(Main.LOGGER));
            Runtime.getRuntime().addShutdownHook(thread);
        } catch (Exception exception1) {
            Main.LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", exception1);
        }

    }

    private static void writePidFile(Path path) {
        try {
            long i = ProcessHandle.current().pid();

            Files.writeString(path, Long.toString(i));
        } catch (IOException ioexception) {
            throw new UncheckedIOException(ioexception);
        }
    }

    private static WorldLoader.c loadOrCreateConfig(DedicatedServerProperties dedicatedserverproperties, @Nullable Dynamic<?> dynamic, boolean flag, ResourcePackRepository resourcepackrepository) {
        boolean flag1;
        WorldDataConfiguration worlddataconfiguration;

        if (dynamic != null) {
            WorldDataConfiguration worlddataconfiguration1 = Convertable.readDataConfig(dynamic);

            flag1 = false;
            worlddataconfiguration = worlddataconfiguration1;
        } else {
            flag1 = true;
            worlddataconfiguration = new WorldDataConfiguration(dedicatedserverproperties.initialDataPackConfiguration, FeatureFlags.DEFAULT_FLAGS);
        }

        WorldLoader.d worldloader_d = new WorldLoader.d(resourcepackrepository, worlddataconfiguration, flag, flag1);

        return new WorldLoader.c(worldloader_d, CommandDispatcher.ServerType.DEDICATED, dedicatedserverproperties.functionPermissionLevel);
    }

    public static void forceUpgrade(Convertable.ConversionSession convertable_conversionsession, DataFixer datafixer, boolean flag, BooleanSupplier booleansupplier, IRegistryCustom iregistrycustom, boolean flag1) {
        Main.LOGGER.info("Forcing world upgrade!");
        WorldUpgrader worldupgrader = new WorldUpgrader(convertable_conversionsession, datafixer, iregistrycustom, flag, flag1);
        IChatBaseComponent ichatbasecomponent = null;

        while (!worldupgrader.isFinished()) {
            IChatBaseComponent ichatbasecomponent1 = worldupgrader.getStatus();

            if (ichatbasecomponent != ichatbasecomponent1) {
                ichatbasecomponent = ichatbasecomponent1;
                Main.LOGGER.info(worldupgrader.getStatus().getString());
            }

            int i = worldupgrader.getTotalChunks();

            if (i > 0) {
                int j = worldupgrader.getConverted() + worldupgrader.getSkipped();

                Main.LOGGER.info("{}% completed ({} / {} chunks)...", new Object[]{MathHelper.floor((float) j / (float) i * 100.0F), j, i});
            }

            if (!booleansupplier.getAsBoolean()) {
                worldupgrader.cancel();
            } else {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException interruptedexception) {
                    ;
                }
            }
        }

    }
}
