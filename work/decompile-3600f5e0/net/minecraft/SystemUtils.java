package net.minecraft;

import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceImmutableList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.DispenserRegistry;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SingleKeyCache;
import net.minecraft.util.TimeSource;
import net.minecraft.util.datafix.DataConverterRegistry;
import net.minecraft.world.level.block.state.properties.IBlockState;
import org.slf4j.Logger;

public class SystemUtils {

    static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_MAX_THREADS = 255;
    private static final int DEFAULT_SAFE_FILE_OPERATION_RETRIES = 10;
    private static final String MAX_THREADS_SYSTEM_PROPERTY = "max.bg.threads";
    private static final ExecutorService BACKGROUND_EXECUTOR = makeExecutor("Main");
    private static final ExecutorService IO_POOL = makeIoExecutor("IO-Worker-", false);
    private static final ExecutorService DOWNLOAD_POOL = makeIoExecutor("Download-", true);
    private static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
    public static final int LINEAR_LOOKUP_THRESHOLD = 8;
    public static final long NANOS_PER_MILLI = 1000000L;
    public static TimeSource.a timeSource = System::nanoTime;
    public static final Ticker TICKER = new Ticker() {
        public long read() {
            return SystemUtils.timeSource.getAsLong();
        }
    };
    public static final UUID NIL_UUID = new UUID(0L, 0L);
    public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = (FileSystemProvider) FileSystemProvider.installedProviders().stream().filter((filesystemprovider) -> {
        return filesystemprovider.getScheme().equalsIgnoreCase("jar");
    }).findFirst().orElseThrow(() -> {
        return new IllegalStateException("No jar file system provider found");
    });
    private static Consumer<String> thePauser = (s) -> {
    };

    public SystemUtils() {}

    public static <K, V> Collector<Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Entry::getKey, Entry::getValue);
    }

    public static <T> Collector<T, ?, List<T>> toMutableList() {
        return Collectors.toCollection(Lists::newArrayList);
    }

    public static <T extends Comparable<T>> String getPropertyName(IBlockState<T> iblockstate, Object object) {
        return iblockstate.getName((Comparable) object);
    }

    public static String makeDescriptionId(String s, @Nullable MinecraftKey minecraftkey) {
        return minecraftkey == null ? s + ".unregistered_sadface" : s + "." + minecraftkey.getNamespace() + "." + minecraftkey.getPath().replace('/', '.');
    }

    public static long getMillis() {
        return getNanos() / 1000000L;
    }

    public static long getNanos() {
        return SystemUtils.timeSource.getAsLong();
    }

    public static long getEpochMillis() {
        return Instant.now().toEpochMilli();
    }

    public static String getFilenameFormattedDateTime() {
        return SystemUtils.FILENAME_DATE_TIME_FORMATTER.format(ZonedDateTime.now());
    }

    private static ExecutorService makeExecutor(String s) {
        int i = MathHelper.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, getMaxThreads());
        Object object;

        if (i <= 0) {
            object = MoreExecutors.newDirectExecutorService();
        } else {
            AtomicInteger atomicinteger = new AtomicInteger(1);

            object = new ForkJoinPool(i, (forkjoinpool) -> {
                ForkJoinWorkerThread forkjoinworkerthread = new ForkJoinWorkerThread(forkjoinpool) {
                    protected void onTermination(Throwable throwable) {
                        if (throwable != null) {
                            SystemUtils.LOGGER.warn("{} died", this.getName(), throwable);
                        } else {
                            SystemUtils.LOGGER.debug("{} shutdown", this.getName());
                        }

                        super.onTermination(throwable);
                    }
                };

                forkjoinworkerthread.setName("Worker-" + s + "-" + atomicinteger.getAndIncrement());
                return forkjoinworkerthread;
            }, SystemUtils::onThreadException, true);
        }

        return (ExecutorService) object;
    }

    private static int getMaxThreads() {
        String s = System.getProperty("max.bg.threads");

        if (s != null) {
            try {
                int i = Integer.parseInt(s);

                if (i >= 1 && i <= 255) {
                    return i;
                }

                SystemUtils.LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{"max.bg.threads", s, 255});
            } catch (NumberFormatException numberformatexception) {
                SystemUtils.LOGGER.error("Could not parse {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{"max.bg.threads", s, 255});
            }
        }

        return 255;
    }

    public static ExecutorService backgroundExecutor() {
        return SystemUtils.BACKGROUND_EXECUTOR;
    }

    public static ExecutorService ioPool() {
        return SystemUtils.IO_POOL;
    }

    public static ExecutorService nonCriticalIoPool() {
        return SystemUtils.DOWNLOAD_POOL;
    }

    public static void shutdownExecutors() {
        shutdownExecutor(SystemUtils.BACKGROUND_EXECUTOR);
        shutdownExecutor(SystemUtils.IO_POOL);
    }

    private static void shutdownExecutor(ExecutorService executorservice) {
        executorservice.shutdown();

        boolean flag;

        try {
            flag = executorservice.awaitTermination(3L, TimeUnit.SECONDS);
        } catch (InterruptedException interruptedexception) {
            flag = false;
        }

        if (!flag) {
            executorservice.shutdownNow();
        }

    }

    private static ExecutorService makeIoExecutor(String s, boolean flag) {
        AtomicInteger atomicinteger = new AtomicInteger(1);

        return Executors.newCachedThreadPool((runnable) -> {
            Thread thread = new Thread(runnable);

            thread.setName(s + atomicinteger.getAndIncrement());
            thread.setDaemon(flag);
            thread.setUncaughtExceptionHandler(SystemUtils::onThreadException);
            return thread;
        });
    }

    public static void throwAsRuntime(Throwable throwable) {
        throw throwable instanceof RuntimeException ? (RuntimeException) throwable : new RuntimeException(throwable);
    }

    private static void onThreadException(Thread thread, Throwable throwable) {
        pauseInIde(throwable);
        if (throwable instanceof CompletionException) {
            throwable = throwable.getCause();
        }

        if (throwable instanceof ReportedException reportedexception) {
            DispenserRegistry.realStdoutPrintln(reportedexception.getReport().getFriendlyReport());
            System.exit(-1);
        }

        SystemUtils.LOGGER.error(String.format(Locale.ROOT, "Caught exception in thread %s", thread), throwable);
    }

    @Nullable
    public static Type<?> fetchChoiceType(TypeReference typereference, String s) {
        return !SharedConstants.CHECK_DATA_FIXER_SCHEMA ? null : doFetchChoiceType(typereference, s);
    }

    @Nullable
    private static Type<?> doFetchChoiceType(TypeReference typereference, String s) {
        Type<?> type = null;

        try {
            type = DataConverterRegistry.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getDataVersion().getVersion())).getChoiceType(typereference, s);
        } catch (IllegalArgumentException illegalargumentexception) {
            SystemUtils.LOGGER.error("No data fixer registered for {}", s);
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                throw illegalargumentexception;
            }
        }

        return type;
    }

    public static Runnable wrapThreadWithTaskName(String s, Runnable runnable) {
        return SharedConstants.IS_RUNNING_IN_IDE ? () -> {
            Thread thread = Thread.currentThread();
            String s1 = thread.getName();

            thread.setName(s);

            try {
                runnable.run();
            } finally {
                thread.setName(s1);
            }

        } : runnable;
    }

    public static <V> Supplier<V> wrapThreadWithTaskName(String s, Supplier<V> supplier) {
        return SharedConstants.IS_RUNNING_IN_IDE ? () -> {
            Thread thread = Thread.currentThread();
            String s1 = thread.getName();

            thread.setName(s);

            Object object;

            try {
                object = supplier.get();
            } finally {
                thread.setName(s1);
            }

            return object;
        } : supplier;
    }

    public static <T> String getRegisteredName(IRegistry<T> iregistry, T t0) {
        MinecraftKey minecraftkey = iregistry.getKey(t0);

        return minecraftkey == null ? "[unregistered]" : minecraftkey.toString();
    }

    public static <T> Predicate<T> allOf(List<? extends Predicate<T>> list) {
        List<Predicate<T>> list1 = List.copyOf(list);
        Predicate predicate;

        switch (list1.size()) {
            case 0:
                predicate = (object) -> {
                    return true;
                };
                break;
            case 1:
                predicate = (Predicate) list1.get(0);
                break;
            case 2:
                predicate = ((Predicate) list1.get(0)).and((Predicate) list1.get(1));
                break;
            default:
                predicate = (object) -> {
                    Iterator iterator = list1.iterator();

                    Predicate predicate1;

                    do {
                        if (!iterator.hasNext()) {
                            return true;
                        }

                        predicate1 = (Predicate) iterator.next();
                    } while (predicate1.test(object));

                    return false;
                };
        }

        return predicate;
    }

    public static <T> Predicate<T> anyOf(List<? extends Predicate<T>> list) {
        List<Predicate<T>> list1 = List.copyOf(list);
        Predicate predicate;

        switch (list1.size()) {
            case 0:
                predicate = (object) -> {
                    return false;
                };
                break;
            case 1:
                predicate = (Predicate) list1.get(0);
                break;
            case 2:
                predicate = ((Predicate) list1.get(0)).or((Predicate) list1.get(1));
                break;
            default:
                predicate = (object) -> {
                    Iterator iterator = list1.iterator();

                    Predicate predicate1;

                    do {
                        if (!iterator.hasNext()) {
                            return false;
                        }

                        predicate1 = (Predicate) iterator.next();
                    } while (!predicate1.test(object));

                    return true;
                };
        }

        return predicate;
    }

    public static SystemUtils.OS getPlatform() {
        String s = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        return s.contains("win") ? SystemUtils.OS.WINDOWS : (s.contains("mac") ? SystemUtils.OS.OSX : (s.contains("solaris") ? SystemUtils.OS.SOLARIS : (s.contains("sunos") ? SystemUtils.OS.SOLARIS : (s.contains("linux") ? SystemUtils.OS.LINUX : (s.contains("unix") ? SystemUtils.OS.LINUX : SystemUtils.OS.UNKNOWN)))));
    }

    public static Stream<String> getVmArguments() {
        RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();

        return runtimemxbean.getInputArguments().stream().filter((s) -> {
            return s.startsWith("-X");
        });
    }

    public static <T> T lastOf(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> T findNextInIterable(Iterable<T> iterable, @Nullable T t0) {
        Iterator<T> iterator = iterable.iterator();
        T t1 = iterator.next();

        if (t0 != null) {
            T t2 = t1;

            while (t2 != t0) {
                if (iterator.hasNext()) {
                    t2 = iterator.next();
                }
            }

            if (iterator.hasNext()) {
                return iterator.next();
            }
        }

        return t1;
    }

    public static <T> T findPreviousInIterable(Iterable<T> iterable, @Nullable T t0) {
        Iterator<T> iterator = iterable.iterator();

        Object object;
        Object object1;

        for (object1 = null; iterator.hasNext(); object1 = object) {
            object = iterator.next();
            if (object == t0) {
                if (object1 == null) {
                    object1 = iterator.hasNext() ? Iterators.getLast(iterator) : t0;
                }
                break;
            }
        }

        return object1;
    }

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T t0, Consumer<? super T> consumer) {
        consumer.accept(t0);
        return t0;
    }

    public static <V> CompletableFuture<List<V>> sequence(List<? extends CompletableFuture<V>> list) {
        if (list.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        } else if (list.size() == 1) {
            return ((CompletableFuture) list.get(0)).thenApply(List::of);
        } else {
            CompletableFuture<Void> completablefuture = CompletableFuture.allOf((CompletableFuture[]) list.toArray(new CompletableFuture[0]));

            return completablefuture.thenApply((ovoid) -> {
                return list.stream().map(CompletableFuture::join).toList();
            });
        }
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFast(List<? extends CompletableFuture<? extends V>> list) {
        CompletableFuture<List<V>> completablefuture = new CompletableFuture();

        Objects.requireNonNull(completablefuture);
        return fallibleSequence(list, completablefuture::completeExceptionally).applyToEither(completablefuture, Function.identity());
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFastAndCancel(List<? extends CompletableFuture<? extends V>> list) {
        CompletableFuture<List<V>> completablefuture = new CompletableFuture();

        return fallibleSequence(list, (throwable) -> {
            if (completablefuture.completeExceptionally(throwable)) {
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    CompletableFuture<? extends V> completablefuture1 = (CompletableFuture) iterator.next();

                    completablefuture1.cancel(true);
                }
            }

        }).applyToEither(completablefuture, Function.identity());
    }

    private static <V> CompletableFuture<List<V>> fallibleSequence(List<? extends CompletableFuture<? extends V>> list, Consumer<Throwable> consumer) {
        List<V> list1 = Lists.newArrayListWithCapacity(list.size());
        CompletableFuture<?>[] acompletablefuture = new CompletableFuture[list.size()];

        list.forEach((completablefuture) -> {
            int i = list1.size();

            list1.add((Object) null);
            acompletablefuture[i] = completablefuture.whenComplete((object, throwable) -> {
                if (throwable != null) {
                    consumer.accept(throwable);
                } else {
                    list1.set(i, object);
                }

            });
        });
        return CompletableFuture.allOf(acompletablefuture).thenApply((ovoid) -> {
            return list1;
        });
    }

    public static <T> Optional<T> ifElse(Optional<T> optional, Consumer<T> consumer, Runnable runnable) {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        } else {
            runnable.run();
        }

        return optional;
    }

    public static <T> Supplier<T> name(Supplier<T> supplier, Supplier<String> supplier1) {
        return supplier;
    }

    public static Runnable name(Runnable runnable, Supplier<String> supplier) {
        return runnable;
    }

    public static void logAndPauseIfInIde(String s) {
        SystemUtils.LOGGER.error(s);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            doPause(s);
        }

    }

    public static void logAndPauseIfInIde(String s, Throwable throwable) {
        SystemUtils.LOGGER.error(s, throwable);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            doPause(s);
        }

    }

    public static <T extends Throwable> T pauseInIde(T t0) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            SystemUtils.LOGGER.error("Trying to throw a fatal exception, pausing in IDE", t0);
            doPause(t0.getMessage());
        }

        return t0;
    }

    public static void setPause(Consumer<String> consumer) {
        SystemUtils.thePauser = consumer;
    }

    private static void doPause(String s) {
        Instant instant = Instant.now();

        SystemUtils.LOGGER.warn("Did you remember to set a breakpoint here?");
        boolean flag = Duration.between(instant, Instant.now()).toMillis() > 500L;

        if (!flag) {
            SystemUtils.thePauser.accept(s);
        }

    }

    public static String describeError(Throwable throwable) {
        return throwable.getCause() != null ? describeError(throwable.getCause()) : (throwable.getMessage() != null ? throwable.getMessage() : throwable.toString());
    }

    public static <T> T getRandom(T[] at, RandomSource randomsource) {
        return at[randomsource.nextInt(at.length)];
    }

    public static int getRandom(int[] aint, RandomSource randomsource) {
        return aint[randomsource.nextInt(aint.length)];
    }

    public static <T> T getRandom(List<T> list, RandomSource randomsource) {
        return list.get(randomsource.nextInt(list.size()));
    }

    public static <T> Optional<T> getRandomSafe(List<T> list, RandomSource randomsource) {
        return list.isEmpty() ? Optional.empty() : Optional.of(getRandom(list, randomsource));
    }

    private static BooleanSupplier createRenamer(final Path path, final Path path1) {
        return new BooleanSupplier() {
            public boolean getAsBoolean() {
                try {
                    Files.move(path, path1);
                    return true;
                } catch (IOException ioexception) {
                    SystemUtils.LOGGER.error("Failed to rename", ioexception);
                    return false;
                }
            }

            public String toString() {
                String s = String.valueOf(path);

                return "rename " + s + " to " + String.valueOf(path1);
            }
        };
    }

    private static BooleanSupplier createDeleter(final Path path) {
        return new BooleanSupplier() {
            public boolean getAsBoolean() {
                try {
                    Files.deleteIfExists(path);
                    return true;
                } catch (IOException ioexception) {
                    SystemUtils.LOGGER.warn("Failed to delete", ioexception);
                    return false;
                }
            }

            public String toString() {
                return "delete old " + String.valueOf(path);
            }
        };
    }

    private static BooleanSupplier createFileDeletedCheck(final Path path) {
        return new BooleanSupplier() {
            public boolean getAsBoolean() {
                return !Files.exists(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + String.valueOf(path) + " is deleted";
            }
        };
    }

    private static BooleanSupplier createFileCreatedCheck(final Path path) {
        return new BooleanSupplier() {
            public boolean getAsBoolean() {
                return Files.isRegularFile(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + String.valueOf(path) + " is present";
            }
        };
    }

    private static boolean executeInSequence(BooleanSupplier... abooleansupplier) {
        BooleanSupplier[] abooleansupplier1 = abooleansupplier;
        int i = abooleansupplier.length;

        for (int j = 0; j < i; ++j) {
            BooleanSupplier booleansupplier = abooleansupplier1[j];

            if (!booleansupplier.getAsBoolean()) {
                SystemUtils.LOGGER.warn("Failed to execute {}", booleansupplier);
                return false;
            }
        }

        return true;
    }

    private static boolean runWithRetries(int i, String s, BooleanSupplier... abooleansupplier) {
        for (int j = 0; j < i; ++j) {
            if (executeInSequence(abooleansupplier)) {
                return true;
            }

            SystemUtils.LOGGER.error("Failed to {}, retrying {}/{}", new Object[]{s, j, i});
        }

        SystemUtils.LOGGER.error("Failed to {}, aborting, progress might be lost", s);
        return false;
    }

    public static void safeReplaceFile(Path path, Path path1, Path path2) {
        safeReplaceOrMoveFile(path, path1, path2, false);
    }

    public static boolean safeReplaceOrMoveFile(Path path, Path path1, Path path2, boolean flag) {
        if (Files.exists(path, new LinkOption[0]) && !runWithRetries(10, "create backup " + String.valueOf(path2), createDeleter(path2), createRenamer(path, path2), createFileCreatedCheck(path2))) {
            return false;
        } else if (!runWithRetries(10, "remove old " + String.valueOf(path), createDeleter(path), createFileDeletedCheck(path))) {
            return false;
        } else if (!runWithRetries(10, "replace " + String.valueOf(path) + " with " + String.valueOf(path1), createRenamer(path1, path), createFileCreatedCheck(path)) && !flag) {
            runWithRetries(10, "restore " + String.valueOf(path) + " from " + String.valueOf(path2), createRenamer(path2, path), createFileCreatedCheck(path));
            return false;
        } else {
            return true;
        }
    }

    public static int offsetByCodepoints(String s, int i, int j) {
        int k = s.length();
        int l;

        if (j >= 0) {
            for (l = 0; i < k && l < j; ++l) {
                if (Character.isHighSurrogate(s.charAt(i++)) && i < k && Character.isLowSurrogate(s.charAt(i))) {
                    ++i;
                }
            }
        } else {
            for (l = j; i > 0 && l < 0; ++l) {
                --i;
                if (Character.isLowSurrogate(s.charAt(i)) && i > 0 && Character.isHighSurrogate(s.charAt(i - 1))) {
                    --i;
                }
            }
        }

        return i;
    }

    public static Consumer<String> prefix(String s, Consumer<String> consumer) {
        return (s1) -> {
            consumer.accept(s + s1);
        };
    }

    public static DataResult<int[]> fixedSize(IntStream intstream, int i) {
        int[] aint = intstream.limit((long) (i + 1)).toArray();

        if (aint.length != i) {
            Supplier<String> supplier = () -> {
                return "Input is not a list of " + i + " ints";
            };

            return aint.length >= i ? DataResult.error(supplier, Arrays.copyOf(aint, i)) : DataResult.error(supplier);
        } else {
            return DataResult.success(aint);
        }
    }

    public static DataResult<long[]> fixedSize(LongStream longstream, int i) {
        long[] along = longstream.limit((long) (i + 1)).toArray();

        if (along.length != i) {
            Supplier<String> supplier = () -> {
                return "Input is not a list of " + i + " longs";
            };

            return along.length >= i ? DataResult.error(supplier, Arrays.copyOf(along, i)) : DataResult.error(supplier);
        } else {
            return DataResult.success(along);
        }
    }

    public static <T> DataResult<List<T>> fixedSize(List<T> list, int i) {
        if (list.size() != i) {
            Supplier<String> supplier = () -> {
                return "Input is not a list of " + i + " elements";
            };

            return list.size() >= i ? DataResult.error(supplier, list.subList(0, i)) : DataResult.error(supplier);
        } else {
            return DataResult.success(list);
        }
    }

    public static void startTimerHackThread() {
        Thread thread = new Thread("Timer hack thread") {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(2147483647L);
                    } catch (InterruptedException interruptedexception) {
                        SystemUtils.LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                        return;
                    }
                }
            }
        };

        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(SystemUtils.LOGGER));
        thread.start();
    }

    public static void copyBetweenDirs(Path path, Path path1, Path path2) throws IOException {
        Path path3 = path.relativize(path2);
        Path path4 = path1.resolve(path3);

        Files.copy(path2, path4);
    }

    public static String sanitizeName(String s, CharPredicate charpredicate) {
        return (String) s.toLowerCase(Locale.ROOT).chars().mapToObj((i) -> {
            return charpredicate.test((char) i) ? Character.toString((char) i) : "_";
        }).collect(Collectors.joining());
    }

    public static <K, V> SingleKeyCache<K, V> singleKeyCache(Function<K, V> function) {
        return new SingleKeyCache<>(function);
    }

    public static <T, R> Function<T, R> memoize(final Function<T, R> function) {
        return new Function<T, R>() {
            private final Map<T, R> cache = new ConcurrentHashMap();

            public R apply(T t0) {
                return this.cache.computeIfAbsent(t0, function);
            }

            public String toString() {
                String s = String.valueOf(function);

                return "memoize/1[function=" + s + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> bifunction) {
        return new BiFunction<T, U, R>() {
            private final Map<Pair<T, U>, R> cache = new ConcurrentHashMap();

            public R apply(T t0, U u0) {
                return this.cache.computeIfAbsent(Pair.of(t0, u0), (pair) -> {
                    return bifunction.apply(pair.getFirst(), pair.getSecond());
                });
            }

            public String toString() {
                String s = String.valueOf(bifunction);

                return "memoize/2[function=" + s + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T> List<T> toShuffledList(Stream<T> stream, RandomSource randomsource) {
        ObjectArrayList<T> objectarraylist = (ObjectArrayList) stream.collect(ObjectArrayList.toList());

        shuffle(objectarraylist, randomsource);
        return objectarraylist;
    }

    public static IntArrayList toShuffledList(IntStream intstream, RandomSource randomsource) {
        IntArrayList intarraylist = IntArrayList.wrap(intstream.toArray());
        int i = intarraylist.size();

        for (int j = i; j > 1; --j) {
            int k = randomsource.nextInt(j);

            intarraylist.set(j - 1, intarraylist.set(k, intarraylist.getInt(j - 1)));
        }

        return intarraylist;
    }

    public static <T> List<T> shuffledCopy(T[] at, RandomSource randomsource) {
        ObjectArrayList<T> objectarraylist = new ObjectArrayList(at);

        shuffle(objectarraylist, randomsource);
        return objectarraylist;
    }

    public static <T> List<T> shuffledCopy(ObjectArrayList<T> objectarraylist, RandomSource randomsource) {
        ObjectArrayList<T> objectarraylist1 = new ObjectArrayList(objectarraylist);

        shuffle(objectarraylist1, randomsource);
        return objectarraylist1;
    }

    public static <T> void shuffle(List<T> list, RandomSource randomsource) {
        int i = list.size();

        for (int j = i; j > 1; --j) {
            int k = randomsource.nextInt(j);

            list.set(j - 1, list.set(k, list.get(j - 1)));
        }

    }

    public static <T> CompletableFuture<T> blockUntilDone(Function<Executor, CompletableFuture<T>> function) {
        return (CompletableFuture) blockUntilDone(function, CompletableFuture::isDone);
    }

    public static <T> T blockUntilDone(Function<Executor, T> function, Predicate<T> predicate) {
        BlockingQueue<Runnable> blockingqueue = new LinkedBlockingQueue();

        Objects.requireNonNull(blockingqueue);
        T t0 = function.apply(blockingqueue::add);

        while (!predicate.test(t0)) {
            try {
                Runnable runnable = (Runnable) blockingqueue.poll(100L, TimeUnit.MILLISECONDS);

                if (runnable != null) {
                    runnable.run();
                }
            } catch (InterruptedException interruptedexception) {
                SystemUtils.LOGGER.warn("Interrupted wait");
                break;
            }
        }

        int i = blockingqueue.size();

        if (i > 0) {
            SystemUtils.LOGGER.warn("Tasks left in queue: {}", i);
        }

        return t0;
    }

    public static <T> ToIntFunction<T> createIndexLookup(List<T> list) {
        int i = list.size();

        if (i < 8) {
            Objects.requireNonNull(list);
            return list::indexOf;
        } else {
            Object2IntMap<T> object2intmap = new Object2IntOpenHashMap(i);

            object2intmap.defaultReturnValue(-1);

            for (int j = 0; j < i; ++j) {
                object2intmap.put(list.get(j), j);
            }

            return object2intmap;
        }
    }

    public static <T> ToIntFunction<T> createIndexIdentityLookup(List<T> list) {
        int i = list.size();

        if (i < 8) {
            ReferenceList<T> referencelist = new ReferenceImmutableList(list);

            Objects.requireNonNull(referencelist);
            return referencelist::indexOf;
        } else {
            Reference2IntMap<T> reference2intmap = new Reference2IntOpenHashMap(i);

            reference2intmap.defaultReturnValue(-1);

            for (int j = 0; j < i; ++j) {
                reference2intmap.put(list.get(j), j);
            }

            return reference2intmap;
        }
    }

    public static <A, B> Typed<B> writeAndReadTypedOrThrow(Typed<A> typed, Type<B> type, UnaryOperator<Dynamic<?>> unaryoperator) {
        Dynamic<?> dynamic = (Dynamic) typed.write().getOrThrow();

        return readTypedOrThrow(type, (Dynamic) unaryoperator.apply(dynamic), true);
    }

    public static <T> Typed<T> readTypedOrThrow(Type<T> type, Dynamic<?> dynamic) {
        return readTypedOrThrow(type, dynamic, false);
    }

    public static <T> Typed<T> readTypedOrThrow(Type<T> type, Dynamic<?> dynamic, boolean flag) {
        DataResult<Typed<T>> dataresult = type.readTyped(dynamic).map(Pair::getFirst);

        try {
            return flag ? (Typed) dataresult.getPartialOrThrow(IllegalStateException::new) : (Typed) dataresult.getOrThrow(IllegalStateException::new);
        } catch (IllegalStateException illegalstateexception) {
            CrashReport crashreport = CrashReport.forThrowable(illegalstateexception, "Reading type");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Info");

            crashreportsystemdetails.setDetail("Data", (Object) dynamic);
            crashreportsystemdetails.setDetail("Type", (Object) type);
            throw new ReportedException(crashreport);
        }
    }

    public static <T> List<T> copyAndAdd(List<T> list, T t0) {
        return ImmutableList.builderWithExpectedSize(list.size() + 1).addAll(list).add(t0).build();
    }

    public static <T> List<T> copyAndAdd(T t0, List<T> list) {
        return ImmutableList.builderWithExpectedSize(list.size() + 1).add(t0).addAll(list).build();
    }

    public static <K, V> Map<K, V> copyAndPut(Map<K, V> map, K k0, V v0) {
        return ImmutableMap.builderWithExpectedSize(map.size() + 1).putAll(map).put(k0, v0).buildKeepingLast();
    }

    public static enum OS {

        LINUX("linux"), SOLARIS("solaris"), WINDOWS("windows") {
            @Override
            protected String[] getOpenUrlArguments(URL url) {
                return new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()};
            }
        },
        OSX("mac") {
            @Override
            protected String[] getOpenUrlArguments(URL url) {
                return new String[]{"open", url.toString()};
            }
        },
        UNKNOWN("unknown");

        private final String telemetryName;

        OS(final String s) {
            this.telemetryName = s;
        }

        public void openUrl(URL url) {
            try {
                Process process = (Process) AccessController.doPrivileged(() -> {
                    return Runtime.getRuntime().exec(this.getOpenUrlArguments(url));
                });

                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
            } catch (IOException | PrivilegedActionException privilegedactionexception) {
                SystemUtils.LOGGER.error("Couldn't open url '{}'", url, privilegedactionexception);
            }

        }

        public void openUri(URI uri) {
            try {
                this.openUrl(uri.toURL());
            } catch (MalformedURLException malformedurlexception) {
                SystemUtils.LOGGER.error("Couldn't open uri '{}'", uri, malformedurlexception);
            }

        }

        public void openFile(File file) {
            try {
                this.openUrl(file.toURI().toURL());
            } catch (MalformedURLException malformedurlexception) {
                SystemUtils.LOGGER.error("Couldn't open file '{}'", file, malformedurlexception);
            }

        }

        protected String[] getOpenUrlArguments(URL url) {
            String s = url.toString();

            if ("file".equals(url.getProtocol())) {
                s = s.replace("file:", "file://");
            }

            return new String[]{"xdg-open", s};
        }

        public void openUri(String s) {
            try {
                this.openUrl((new URI(s)).toURL());
            } catch (MalformedURLException | IllegalArgumentException | URISyntaxException urisyntaxexception) {
                SystemUtils.LOGGER.error("Couldn't open uri '{}'", s, urisyntaxexception);
            }

        }

        public String telemetryName() {
            return this.telemetryName;
        }
    }
}
