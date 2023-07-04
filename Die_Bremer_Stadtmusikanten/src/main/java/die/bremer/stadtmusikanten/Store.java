package die.bremer.stadtmusikanten;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Store {
    
}

class StoreGUI implements Listener, CommandExecutor {
    private final Inventory inv;
    private Wallet wallet;
    private final ItemStack GOLD = createGuiItem(Material.GOLD_INGOT, "§e[금화]", "빛나는 §e금화", "화폐로 사용됩니다.");
    private final ItemStack SILVER = createGuiItem(Material.IRON_INGOT, "§7[은화]", "빛나는 §7은화", "화폐로 사용됩니다.");
    private final ItemStack COPPER = createGuiItem(Material.COPPER_INGOT, "§6[동화]", "빛나는 §6동화", "화폐로 사용됩니다.");
    private final ItemStack EXIT = createGuiItem(Material.ACACIA_DOOR, "나가기", "현재 메뉴를 나갑니다.");

    public StoreGUI() {
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
        inv = Bukkit.createInventory(null, 9, "은행");

        // Put the items into the inventory
        initializeItems();
    }
    public StoreGUI(Wallet wallet, Dbs plugin) {
        this();
        this.wallet = wallet;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // You can call this whenever you want to put the items in
    public void initializeItems() {
        inv.setItem(1, GOLD);
        inv.setItem(2, SILVER);
        inv.setItem(3, COPPER);
        inv.setItem(8, EXIT);
    }   

    // Nice little method to create a gui item with a custom name, and description
    protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(name);

        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    /** 환전 상점 들어가는 method */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {  //명령어 사용자가 플레이어인 경우
            Player player = (Player)sender; //명령어 사용자 객체를 플레이어 객체로 변환할 수 있음
            openInventory(player);  // 인벤토리 열기
            return true;
        }
        else if(sender instanceof ConsoleCommandSender) {   //명령어 사용자가 콘솔인 경우
            sender.sendMessage("콘솔에서 사용 불가한 명령어입니다.");
            return false;   //false값을 반환하면 명령어가 실패한 것으로 간주
        }
        return false;   //false값을 반환하면 명령어가 실패한 것으로 간주
    }

    // You can open the inventory with this
    public void openInventory(final HumanEntity ent) {
        ent.openInventory(inv);
    }

    /** 클릭 시 행동을 수행 */
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;

        e.setCancelled(true);
        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player p = (Player) e.getWhoClicked();

        // Using slots click is a best option for your inventory click's
        deposit(p, e.getCurrentItem());
    }

    /** 인출하는 내부 메소드 */
    private void deposit(Player player, ItemStack item) {
        UUID id = player.getUniqueId();
        if (item.equals(GOLD)) {
            if (wallet.getGold(id) > 0) {
                player.getInventory().addItem(GOLD); 
                wallet.putGold(id, wallet.getGold(id) - 1);
                player.sendMessage(GOLD.getItemMeta().getDisplayName() + " §f이(가) 인출되었습니다.");
            }
            else {
                player.sendMessage(GOLD.getItemMeta().getDisplayName() + " §f잔액이 부족합니다.");
            }
        }
        else if (item.equals(SILVER)) {
            if (wallet.getSilver(id) > 0) {
                player.getInventory().addItem(SILVER); 
                wallet.putSilver(id, wallet.getSilver(id) - 1);
                player.sendMessage(SILVER.getItemMeta().getDisplayName() + " §f이(가) 인출되었습니다.");
            }
            else {
                player.sendMessage(SILVER.getItemMeta().getDisplayName() + " §f잔액이 부족합니다.");
            }
        }
        else if (item.equals(COPPER)) {
            if (wallet.getCopper(id) > 0) {
                player.getInventory().addItem(COPPER); 
                wallet.putCopper(id, wallet.getCopper(id) - 1);
                player.sendMessage(COPPER.getItemMeta().getDisplayName() + " §f이(가) 인출되었습니다.");
            }
            else {
                player.sendMessage(SILVER.getItemMeta().getDisplayName() + " §f잔액이 부족합니다.");
            }
        }
        else if (item.equals(EXIT)) {
            player.closeInventory();
        }
        /** 지갑 업데이트 */
        PlayerScoreboard.updateScoreboard(player, wallet);
    }

    /** 드래그 막기 */
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory().equals(inv)) {
          e.setCancelled(true);
        }
    }
}