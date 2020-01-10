package xyz.derkades.serverselectorx;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.changeme.nbtapi.NBTItem;
import xyz.derkades.derkutils.Cooldown;

public class ItemClickListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onInteract(final PlayerInteractEvent event){
		if (event.getAction() == Action.PHYSICAL) {
			return;
		}

		final FileConfiguration inventory = Main.getConfigurationManager().inventory;

		if (event.isCancelled() && inventory.getBoolean("ignore-cancelled", false)) {
			return;
		}

		final Player player = event.getPlayer();
		final ItemStack item = player.getInventory().getItemInHand();

		if (item == null || item.getType() == Material.AIR) {
			return;
		}

		final NBTItem nbt = new NBTItem(item);

		if (!nbt.hasKey("SSXItem")) {
			return;
		}

		final String itemName = nbt.getString("SSXItem");

		if (!Main.getConfigurationManager().items.containsKey(itemName)) {
			player.sendMessage("An configuration file does not exist for an item with the name '" + itemName + "'.");
			return;
		}

		final FileConfiguration config = Main.getConfigurationManager().items.get(itemName);

		final List<String> actions;

		if (config.isList("left-click-actions")) {
			if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
				actions = config.getStringList("left-click-actions");
			} else {
				actions = config.getStringList("actions");
			}
		} else {
			actions = config.getStringList("actions");
		}

		/*
		 * On 1.9-1.12, this event is sometimes called twice (once for each hand). Bukkit
		 * has a proper method of checking which hand is used, but since this version is
		 * compiled against 1.8 I can't use that here. Unless I use reflection, which I am
		 * not going to, because I'm lazy. A cooldown of 200ms does the same thing.
		 */
		if (Cooldown.getCooldown("ssxclick" + player.getName()) > 0) {
			return;
		}
		Cooldown.addCooldown("ssxclick" + player.getName(), 200);

		xyz.derkades.serverselectorx.actions.Action.runActions(player, actions);

		final boolean cancel = inventory.getBoolean("cancel-click-event", false);
		if (cancel) {
			event.setCancelled(true);
		}
	}
}