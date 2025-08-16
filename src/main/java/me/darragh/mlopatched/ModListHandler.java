package me.darragh.mlopatched;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModListHandler {
    private static final @NotNull List<String> MODS = new ArrayList<>();
    private static final @NotNull Gson GSON = new Gson();
    private static final @NotNull String PATCH_FILE_NAME = "modlistobserver-patch.json";

    public static List<String> getClientMods() {
        return MODS;
    }

    public static void loadClientMods() {
        try {
            // Does patch file exist?
            Minecraft minecraft = Minecraft.getInstance();
            Path gameDir = minecraft.gameDirectory.toPath(),
                    patchFile = gameDir.resolve(PATCH_FILE_NAME);
            if (Files.exists(patchFile)) {
                // Load the patch file
                ClientModFile clientModFile = GSON.fromJson(
                        Files.newBufferedReader(patchFile),
                        ClientModFile.class
                );
                MODS.addAll(clientModFile.modIds);
            } else {
                // Create the patch file
                ClientModFile clientModFile = new ClientModFile(
                        ModList.get() // from HandshakeMessages#C2SModListReply
                                .getMods()
                                .stream()
                                .map(IModInfo::getModId)
                                .collect(Collectors.toList())
                );
                MODS.addAll(clientModFile.modIds);
                // Save the patch file
                try (var writer = Files.newBufferedWriter(patchFile)) {
                    GSON.toJson(clientModFile, writer);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record ClientModFile(
            @NotNull @SerializedName("modIds") List<String> modIds
    ) {
    }
}
