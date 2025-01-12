package mc.plugins.expbook;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class saveexp implements Listener {

    // 每次默认保存的经验点数
    private static final int STORAGE_AMOUNT = 100;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 检测是否是右键点击
        if (!event.getAction().isRightClick()) {
            return; // 如果不是右键，则退出
        }

        // 获取玩家和手中的物品
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.ENCHANTED_BOOK || !item.hasItemMeta()) {
            return; // 如果手中没有物品，或者物品不是附魔书，直接退出
        }

        // 获取物品的元数据（ItemMeta）
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.getDisplayName().equals("§6传说中的经验之书")) {
            return; // 如果书名不是我们自定义的书，则退出
        }

        // 检查玩家是否蹲下
        if (!player.isSneaking()) {
            return; // 如果玩家未蹲下，则退出
        }

        // 检查玩家是否有足够的经验
        int playerExperience = player.getTotalExperience();
        if (playerExperience < STORAGE_AMOUNT) {
            player.sendMessage("§c你的经验值不足以存储 §e" + STORAGE_AMOUNT + " §c点！");
            return;
        }

        // 从书的 Lore 中获取当前存储的经验点数
        List<String> lore = meta.getLore();
        int storedExperience = 0; // 默认经验为 0
        if (lore != null && !lore.isEmpty()) {
            for (String line : lore) {
                if (line.startsWith("§7当前存储点数: ")) {
                    try {
                        storedExperience = Integer.parseInt(line.replace("§7当前存储点数: ", "").trim());
                    } catch (NumberFormatException e) {
                        storedExperience = 0; // 如果解析失败，经验值设置为 0
                    }
                    break;
                }
            }
        }

        // **通过静态访问方式获取最大存储点数**
        int maxStorage = expbookitem.MAX_EXPERIENCE;

        // 检查书中的经验是否已经达到上限
        if (storedExperience >= maxStorage) {
            player.sendMessage("§c这本书的经验存储已达上限（" + maxStorage + "点）！");
            return;
        }

        // 计算可存储的实际经验值
        int availableSpace = maxStorage - storedExperience; // 剩下可以存储的最大点数
        int experienceToStore = Math.min(STORAGE_AMOUNT, availableSpace); // 实际要存储的点数

        // 检查玩家是否有足够的经验来存储
        if (playerExperience < experienceToStore) {
            player.sendMessage("§c你的经验值不足以存储 §e" + experienceToStore + " §c点！");
            return;
        }

        // 更新书中存储的经验点数
        int newExperience = storedExperience + experienceToStore;

        // 从玩家总经验值中扣除实际存储的经验值
        player.giveExp(-experienceToStore);

        // 更新书的 Lore（动态修改存储点数）
        meta.setLore(List.of(
                "§7当前存储点数: " + newExperience, // 当前存储点数
                "§e蹲下+左键 -> 从书取出经验", // 操作提示：取出
                "§e蹲下+右键 -> 将经验存储到书中" // 操作提示：存入
        ));
        item.setItemMeta(meta);

        // 通知玩家成功存储经验
        player.sendMessage("§a你已向书中存储了 §e" + experienceToStore + " §a点经验，当前存储点数为 §b" + newExperience + "点！");

        // 取消事件默认行为（避免触发其他交互事件）
        event.setCancelled(true);
    }
}