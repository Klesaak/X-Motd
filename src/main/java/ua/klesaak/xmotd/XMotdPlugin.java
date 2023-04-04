package ua.klesaak.xmotd;

import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import lombok.val;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class XMotdPlugin extends Plugin implements Listener {
    private List<TextComponent> motds = new ArrayList<>();
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    @Override
    public void onEnable() {
        this.loadMotd();
        this.getProxy().getPluginManager().registerListener(this, this);
        this.getProxy().getPluginManager().registerCommand(this, new Command("xmotdr", "xmotd.reload") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                XMotdPlugin.this.loadMotd();
                if (sender instanceof ProxiedPlayer) sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Загружено " + XMotdPlugin.this.motds.size() + " motd'ов."));
            }
        });
    }

    @SneakyThrows
    private void loadMotd() {
        File file = new File(this.getDataFolder(), "motd.json");
        val path = file.toPath();
        if (!Files.exists(path)) {
            Files.createDirectory(path);
            Files.write(path, GSON.toJson(Collections.singletonList(Arrays.asList("строка1", "строка2"))).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        }
        List<List<String>> raw = GSON.fromJson(new String(Files.readAllBytes(path)), new TypeToken<List<List<String>>>() {}.getType());
        Joiner joiner = Joiner.on('\n');
        this.motds = raw.stream().map(joiner::join)
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .map(TextComponent::fromLegacyText)
                .map(TextComponent::new)
                .collect(Collectors.toList());
        this.getLogger().info(ChatColor.GREEN + "Загружено " + this.motds.size() + " motd'ов.");
    }

    @EventHandler
    public void onProxyPing(ProxyPingEvent event) {
        if (this.motds.isEmpty()) return;
        val response = event.getResponse();
        TextComponent random = this.motds.get(RANDOM.nextInt(0, this.motds.size()));
        response.setDescriptionComponent(random);
    }
}
