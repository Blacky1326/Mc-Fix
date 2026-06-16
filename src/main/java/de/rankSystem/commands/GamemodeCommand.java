package de.rankSystem.commands;

import de.rankSystem.RankSystem;
import de.rankSystem.utils.Rank;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GamemodeCommand implements CommandExecutor, TabCompleter {

    private final RankSystem plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public GamemodeCommand(RankSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize("<red>✗ Nur Spieler können diesen Befehl nutzen!</red>"));
            return true;
        }

        Rank rank = plugin.getRankManager().getPlayerRank(player);
        if (rank.getWeight() > Rank.ADMIN.getWeight()) {
            player.sendMessage(mm.deserialize("<red>✗ Du hast keine Berechtigung! (Mindest-Rang: Admin)</red>"));
            return true;
        }

        // Ziel-Spieler bestimmen
        Player target;
        if (args.length >= 1) {
            // Admin darf anderen Spieler setzen
            target = player.getServer().getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(mm.deserialize("<red>✗ Spieler <white>" + args[0] + "</white> nicht gefunden!</red>"));
                return true;
            }
            // Ziel muss ebenfalls Admin+ sein
            Rank targetRank = plugin.getRankManager().getPlayerRank(target);
            if (targetRank.getWeight() > Rank.ADMIN.getWeight()) {
                player.sendMessage(mm.deserialize("<red>✗ Gamemode kann nur Admin+ erteilt werden!</red>"));
                return true;
            }
        } else {
            target = player;
        }

        // Toggle: Creative ↔ Survival
        if (target.getGameMode() == GameMode.CREATIVE) {
            target.setGameMode(GameMode.SURVIVAL);
            target.sendMessage(mm.deserialize(
                    "<gray>» Gamemode: </gray><green><bold>Survival</bold></green>"));
            if (!target.equals(player)) {
                player.sendMessage(mm.deserialize(
                        "<gray>» </gray><white>" + target.getName() + "</white><gray> ist jetzt im </gray><green><bold>Survival</bold></green><gray>-Modus.</gray>"));
            }
        } else {
            target.setGameMode(GameMode.CREATIVE);
            target.sendMessage(mm.deserialize(
                    "<gray>» Gamemode: </gray><light_purple><bold>Creative</bold></light_purple>"));
            if (!target.equals(player)) {
                player.sendMessage(mm.deserialize(
                        "<gray>» </gray><white>" + target.getName() + "</white><gray> ist jetzt im </gray><light_purple><bold>Creative</bold></light_purple><gray>-Modus.</gray>"));
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player player)) return new ArrayList<>();
        Rank rank = plugin.getRankManager().getPlayerRank(player);
        if (rank.getWeight() > Rank.ADMIN.getWeight()) return new ArrayList<>();

        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            player.getServer().getOnlinePlayers().forEach(p -> {
                Rank r = plugin.getRankManager().getPlayerRank(p);
                if (r.getWeight() <= Rank.ADMIN.getWeight()) names.add(p.getName());
            });
            return names;
        }
        return new ArrayList<>();
    }
}
