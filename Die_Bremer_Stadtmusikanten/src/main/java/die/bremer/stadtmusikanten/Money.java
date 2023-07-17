package die.bremer.stadtmusikanten;

import java.util.Arrays;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** 돈에 대해 지정하는 CLASS */
public abstract class Money {
    /** 금화 ItemStack */
    public static final ItemStack GOLD = createGuiItem(Material.GOLD_INGOT, "§e[금화]", "빛나는 §e금화", "화폐로 사용됩니다.");
    /** 은화 ItemStack */
    public static final ItemStack SILVER = createGuiItem(Material.IRON_INGOT, "§7[은화]", "빛나는 §7은화", "화폐로 사용됩니다.");
    /** 동화 ItemStack */
    public static final ItemStack COPPER = createGuiItem(Material.COPPER_INGOT, "§6[동화]", "빛나는 §6동화", "화폐로 사용됩니다.");
    /** 나가기 ItemStack */
    public static final ItemStack EXIT = createGuiItem(Material.ACACIA_DOOR, "나가기", "현재 메뉴를 나갑니다.");

    // Nice little method to create a gui item with a custom name, and description
    protected static ItemStack createGuiItem(final Material material, @Nullable final String name, @Nullable final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        if (!(name == null))
            meta.setDisplayName(name);

        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
