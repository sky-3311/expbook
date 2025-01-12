package mc.plugins.expbook;

import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

// 定义经验书管理类
public class expbookitem {

    private final JavaPlugin plugin; // 引用插件主类，用于注册配方等功能
    public static final int MAX_EXPERIENCE = 8670; // 经验书存储的最大经验值

    // 构造函数，获取插件主类对象
    public expbookitem(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // 创建“传说中的经验之书”物品的方法
    public ItemStack getExpBook() {
        // 创建一个附魔书物品
        ItemStack customBook = new ItemStack(Material.ENCHANTED_BOOK);

        // 获取物品的元数据（ItemMeta）
        ItemMeta meta = customBook.getItemMeta();
        if (meta != null) {
            // 设置书的名字为“传说中的经验之书”，并添加颜色代码
            meta.setDisplayName("§6传说中的经验之书");

            // 设置书的描述（物品Lore），提供经验的存取提示
            meta.setLore(Arrays.asList(
                    "§7当前存储点数: 0", // 显示当前书中的存储经验点数
                    "§e蹲下+左键 -> 从书取出经验", // 提示从书中取出经验的方法
                    "§e蹲下+右键 -> 将经验存储到书中"  // 提示向书中存储经验的方法
            ));

            // 如果物品支持损坏值，设置为最大损坏值（模拟未存储经验时的状态）
            if (meta instanceof Damageable) {
                Damageable damageable = (Damageable) meta;
                int maxDurability = customBook.getType().getMaxDurability(); // 获取最大耐久值
                damageable.setDamage(maxDurability); // 设置耐久值为最大（满损坏状态）
            }

            // 将元数据重新设置回物品
            customBook.setItemMeta(meta);
        }

        // 返回自定义的经验书对象
        return customBook;
    }

    // 更新经验书的耐久值和描述（模拟存储经验状态）
    public void updateExpBar(ItemStack book, int storedExperience) {
        // 确保存储经验值在 0 到 MAX_EXPERIENCE范围内
        int actualExperience = Math.max(0, Math.min(storedExperience, MAX_EXPERIENCE));

        // 获取书的元数据
        ItemMeta meta = book.getItemMeta();

        // 如果书支持损坏值，更新耐久值以反映存储状态
        if (meta instanceof Damageable damageable) {
            // 根据存储经验值计算耐久比例，经验越多，损坏值越低
            double durability = 1.0F - (double) actualExperience / (double) MAX_EXPERIENCE;
            int maxDamage = book.getType().getMaxDurability(); // 获取最大耐久值
            damageable.setDamage((int) (durability * (double) maxDamage)); // 设置书的损坏值
        }

        // 更新书的 Lore，动态显示当前存储的经验点数
        meta.setLore(Arrays.asList(
                "§7当前存储点数: " + actualExperience, // 更新存储点数
                "§e蹲下+左键 -> 从书取出经验", // 操作提示1
                "§e蹲下+右键 -> 将经验存储到书中" // 操作提示2
        ));

        // 更新物品元数据
        book.setItemMeta(meta);
    }

    // 注册经验书的合成配方
    public void registerRecipe() {
        // 获取自定义的经验书物品
        ItemStack customBook = this.getExpBook();

        // 使用插件主类创建命名空间键，用于标识经验书的合成配方
        NamespacedKey key = new NamespacedKey(this.plugin, "legendary_exp_book");

        // 定义合成配方，指定目标物品和配方形状
        ShapedRecipe recipe = new ShapedRecipe(key, customBook);
        recipe.shape(new String[]{
                "AGA", // 第一行材料
                "NBN", // 第二行材料
                "DLD"  // 第三行材料
        });

        // 设置配方中的每种材料
        recipe.setIngredient('A', Material.ENCHANTED_GOLDEN_APPLE); // A = 附魔金苹果
        recipe.setIngredient('G', Material.ENDER_EYE);              // G = 末影之眼
        recipe.setIngredient('N', Material.NETHER_STAR);            // N = 下界之星
        recipe.setIngredient('B', Material.BOOK);                   // B = 普通书
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);          // D = 钻石块
        recipe.setIngredient('L', Material.ANVIL);                  // L = 铁砧

        // 将配方注册到 Minecraft 服务器
        Bukkit.addRecipe(recipe);
    }
}