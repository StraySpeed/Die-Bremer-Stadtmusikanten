package die.bremer.stadtmusikanten;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class Store implements Listener, CommandExecutor {
    private final Inventory inv;
    private Wallet wallet;
    private final ItemStack GOLD = Money.GOLD;
    private final ItemStack SILVER = Money.SILVER;
    private final ItemStack COPPER = Money.COPPER;
    private final ItemStack EXIT = Money.EXIT;

    public Store() {
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
        inv = Bukkit.createInventory(null, 9, "은행");

        // Put the items into the inventory
        initializeItems();
    }
    public Store(Wallet wallet, Dbs plugin) {
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
                player.sendMessage(COPPER.getItemMeta().getDisplayName() + " §f잔액이 부족합니다.");
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

    /** 화폐를 들고 사용 시 충전하는 method */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;
        ItemStack holdingItem = e.getPlayer().getInventory().getItemInMainHand();
        UUID id = e.getPlayer().getUniqueId();
        if (holdingItem == null) return;
        if (holdingItem.isSimilar(Money.GOLD)) {
            this.wallet.putGold(id, this.wallet.getGold(id) + 1);
            e.getPlayer().sendMessage(holdingItem.getItemMeta().getDisplayName() + "§f 가 충전되었습니다.");
            holdingItem.setAmount(holdingItem.getAmount() - 1);
            PlayerScoreboard.updateScoreboard(e.getPlayer(), this.wallet);
        }
        else if (holdingItem.isSimilar(Money.SILVER)) {
            this.wallet.putSilver(id, this.wallet.getSilver(id) + 1);
            e.getPlayer().sendMessage(holdingItem.getItemMeta().getDisplayName() + "§f 가 충전되었습니다.");
            holdingItem.setAmount(holdingItem.getAmount() - 1);
            PlayerScoreboard.updateScoreboard(e.getPlayer(), this.wallet);
        }
        else if (holdingItem.isSimilar(Money.COPPER)) {
            this.wallet.putCopper(id, this.wallet.getCopper(id) + 1);
            e.getPlayer().sendMessage(holdingItem.getItemMeta().getDisplayName() + "§f 가 충전되었습니다.");
            holdingItem.setAmount(holdingItem.getAmount() - 1);
            PlayerScoreboard.updateScoreboard(e.getPlayer(), this.wallet);
        }
    }
}