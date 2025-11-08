package com.ashe.chatplugin;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TabUpdater {
    public static LuckPerms luckPerms = LuckPermsProvider.get();

    public static void updateTeam(Player player, User user, ChatPlugin plugin) {

        String primaryGroup = user.getPrimaryGroup();
        var group = luckPerms.getGroupManager().getGroup(primaryGroup);
        if (group == null) return;
        String prefix = colorize(group.getCachedData().getMetaData().getPrefix());
        String suffix = colorize(group.getCachedData().getMetaData().getSuffix());

        if (prefix == null) prefix = "";
        if (suffix == null) suffix = "";

        String fullPrefix = prefix;
        String fullSuffix = suffix;
        String fullName = fullPrefix + player.getName() + fullSuffix;

        Bukkit.getScheduler().runTask(plugin, () -> {
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

            Team team = board.getTeam(player.getName());
            if (team == null) {
                team = board.registerNewTeam(player.getName());
            }

            team.setPrefix(fullPrefix);
            team.setSuffix(fullSuffix);

            if (!team.hasEntry(player.getName())) team.addEntry(player.getName());

            for (Player p : Bukkit.getOnlinePlayers()) p.setScoreboard(board);

            player.setDisplayName(fullName);
            player.setPlayerListName(fullName);
            
        });
    }

    public static void updateTeamOther(Player changed, Player player, User user, ChatPlugin plugin) {
        String primaryGroup = user.getPrimaryGroup();
        var group = luckPerms.getGroupManager().getGroup(primaryGroup);
        if (group == null) return;

        String prefix = colorize(group.getCachedData().getMetaData().getPrefix());
        String suffix = colorize(group.getCachedData().getMetaData().getSuffix());

        if (prefix == null) prefix = "";
        if (suffix == null) suffix = "";

        String fullPrefix = prefix;
        String fullSuffix = suffix;

        Bukkit.getScheduler().runTask(plugin, () -> {
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

            Team team = board.getTeam(changed.getName());
            if (team == null) team = board.registerNewTeam(changed.getName());

            team.setPrefix(fullPrefix);
            team.setSuffix(fullSuffix);

            if (!team.hasEntry(changed.getName())) team.addEntry(changed.getName());

            for (Player p : Bukkit.getOnlinePlayers()) p.setScoreboard(board);
        });
    }

    // & → §
    public static String colorize(String input) {
        if (input == null) return "";
        return input.replace("&", "§");
    }
}
