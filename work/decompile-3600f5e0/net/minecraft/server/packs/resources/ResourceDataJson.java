package net.minecraft.server.packs.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.util.profiling.GameProfilerFiller;
import org.slf4j.Logger;

public abstract class ResourceDataJson extends ResourceDataAbstract<Map<MinecraftKey, JsonElement>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Gson gson;
    private final String directory;

    public ResourceDataJson(Gson gson, String s) {
        this.gson = gson;
        this.directory = s;
    }

    @Override
    protected Map<MinecraftKey, JsonElement> prepare(IResourceManager iresourcemanager, GameProfilerFiller gameprofilerfiller) {
        Map<MinecraftKey, JsonElement> map = new HashMap();

        scanDirectory(iresourcemanager, this.directory, this.gson, map);
        return map;
    }

    public static void scanDirectory(IResourceManager iresourcemanager, String s, Gson gson, Map<MinecraftKey, JsonElement> map) {
        FileToIdConverter filetoidconverter = FileToIdConverter.json(s);
        Iterator iterator = filetoidconverter.listMatchingResources(iresourcemanager).entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<MinecraftKey, IResource> entry = (Entry) iterator.next();
            MinecraftKey minecraftkey = (MinecraftKey) entry.getKey();
            MinecraftKey minecraftkey1 = filetoidconverter.fileToId(minecraftkey);

            try {
                BufferedReader bufferedreader = ((IResource) entry.getValue()).openAsReader();

                try {
                    JsonElement jsonelement = (JsonElement) ChatDeserializer.fromJson(gson, (Reader) bufferedreader, JsonElement.class);
                    JsonElement jsonelement1 = (JsonElement) map.put(minecraftkey1, jsonelement);

                    if (jsonelement1 != null) {
                        throw new IllegalStateException("Duplicate data file ignored with ID " + String.valueOf(minecraftkey1));
                    }
                } catch (Throwable throwable) {
                    if (bufferedreader != null) {
                        try {
                            bufferedreader.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    }

                    throw throwable;
                }

                if (bufferedreader != null) {
                    bufferedreader.close();
                }
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                ResourceDataJson.LOGGER.error("Couldn't parse data file {} from {}", new Object[]{minecraftkey1, minecraftkey, jsonparseexception});
            }
        }

    }
}
