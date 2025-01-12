package mc.plugins.expbook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Expbook extends JavaPlugin {

    @Override
    public void onEnable() {
        // 插件启动逻辑
        getLogger().info("插件已成功启动！");

        // 创建经验书管理器实例
        expbookitem expBookItem = new expbookitem(this);

        // 注册 saveexp 监听器
        Bukkit.getPluginManager().registerEvents(new saveexp(), this);

        // 注册监听器（经验提取监听器），需要传入 expBookItem 实例
        Bukkit.getPluginManager().registerEvents(new extractexp(expBookItem), this);

        // 注册经验书的合成配方
        expBookItem.registerRecipe();

        getLogger().info("所有功能已成功加载！");
    }

    @Override
    public void onDisable() {
        // 插件关闭逻辑
        getLogger().info("插件已成功关闭！");
    }
}