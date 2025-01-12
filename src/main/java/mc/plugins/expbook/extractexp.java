package mc.plugins.expbook;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;

public class extractexp implements Listener {

    // 每次提取的经验点数
    private static final int EXTRACTION_AMOUNT = 100;

    // 引用自定义经验书管理类
    private final expbookitem expBookItem;

    // 构造方法
    public extractexp(expbookitem expBookItem) {
        this.expBookItem = expBookItem;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 检测是否是左键点击
        if (!event.getAction().isLeftClick()) {
            return;
        }

        // 获取玩家和手中的物品
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // 确保手中物品有效，同时是自定义的经验书
        if (item == null || item.getType() != Material.ENCHANTED_BOOK || !item.hasItemMeta()) {
            return; // 如果不是附魔书，直接退出
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.getDisplayName().equals("§6传说中的经验之书")) {
            return; // 如果书的名称不符合，则退出
        }

        // 检查玩家是否蹲下
        if (!player.isSneaking()) {
            return; // 如果玩家未蹲下，则退出
        }

        // 获取书的 Lore 并解析当前存储的经验点数
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

        // 检查存储的经验是否为0点
        if (storedExperience == 0) {
            player.sendMessage("§c这本书中没有可以取出的经验！");
            event.setCancelled(true); // 取消事件，避免触发其他操作
            return;
        }

        // 如果存储的经验不足一次提取，取出书中所有剩余经验
        int extractedExperience;
        if (storedExperience < EXTRACTION_AMOUNT) {
            extractedExperience = storedExperience; // 提取书中所有经验
            storedExperience = 0; // 剩余经验归零
            player.sendMessage("§a书中的经验不足 §e" + EXTRACTION_AMOUNT + " §a点，已提取书中全部经验！");
        } else {
            extractedExperience = EXTRACTION_AMOUNT; // 正常提取 100 点经验
            storedExperience -= EXTRACTION_AMOUNT; // 减少对应点数
            player.sendMessage("§a你从书中提取了 §e" + extractedExperience + " §a点经验，书中剩余 §b" + storedExperience + " §a点经验！");
        }

        // 更新书的 Lore 和耐久条状态
        meta.setLore(List.of(
                "§7当前存储点数: " + storedExperience,
                "§e蹲下+左键 -> 从书取出经验",
                "§e蹲下+右键 -> 将经验存储到书中"
        ));
        item.setItemMeta(meta);
        expBookItem.updateExpBar(item, storedExperience);

        // 给玩家添加经验
        player.giveExp(extractedExperience);

        // 取消事件默认行为（避免触发其他交互事件）
        event.setCancelled(true);
    }
}