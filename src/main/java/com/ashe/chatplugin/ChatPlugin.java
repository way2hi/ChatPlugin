package com.ashe.chatplugin;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ChatPlugin extends JavaPlugin implements Listener {
    private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        luckPerms = LuckPermsProvider.get();
        getServer().getPluginManager().registerEvents(this, this);

        EventBus bus = luckPerms.getEventBus();
        bus.subscribe(this, UserDataRecalculateEvent.class, event -> {
            Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
            if (player != null && player.isOnline()) {
                TabUpdater.updateTeam(player, event.getUser(), this);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.equals(player)) TabUpdater.updateTeamOther(player, p, event.getUser(), this);
                }
            }
        });

        getLogger().info("ChatPlugin enabled.");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        var player = event.getPlayer();
        var user = luckPerms.getUserManager().getUser(player.getUniqueId());

        if (user == null) return;

        String prefix = TabUpdater.colorize(user.getCachedData().getMetaData().getPrefix());
        String suffix = TabUpdater.colorize(user.getCachedData().getMetaData().getSuffix());
        if (prefix == null) prefix = "";
        if (suffix == null) suffix = "";

        String name = player.getName();
        String message = event.getMessage();

        event.setFormat(prefix + name + suffix + "§7 » §f" + message);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            TabUpdater.updateTeam(player, user, this);
        } else {
            luckPerms.getUserManager().loadUser(player.getUniqueId()).thenAccept(userLoaded ->
                TabUpdater.updateTeam(player, userLoaded, this));
        }
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (command.getName().equalsIgnoreCase("chatplugin")) {
            Component message =
            Component.text("[", NamedTextColor.GRAY)
                .append(Component.text("ChatPlugin", NamedTextColor.DARK_AQUA)
                    .decorate(TextDecoration.BOLD))
                .append(Component.text("] ChatPlugin ", NamedTextColor.GRAY))
                .append(Component.text("v" + getDescription().getVersion() + " ", NamedTextColor.AQUA))
                .append(Component.text("made by ", NamedTextColor.GRAY))
                .append(Component.text("Ashe", NamedTextColor.AQUA)
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.openUrl("https://github.com/way2hi")));

            sender.sendMessage(message);
            return true;
        }
        return false;
    }
}
