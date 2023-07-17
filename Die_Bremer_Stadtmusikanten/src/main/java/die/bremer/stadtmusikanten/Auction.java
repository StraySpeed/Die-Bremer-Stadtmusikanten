package die.bremer.stadtmusikanten;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** 경매장 정보를 저장하는 클래스 */
class AuctionFile implements Serializable {
    /** 경매장 파일 이름 */
    static final private String fileName = "Auction.ser";
    private ArrayList<AuctionItem> AuctionList = new ArrayList<>();
    static final int MAXSIZE = 9 * 6;
    private int itemCount = 0;

    /** Constructor */
    AuctionFile() {
    }

    /** 경매장을 불러오는 method */
    public static AuctionFile loadAuction() {
        AuctionFile w = null;
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream inputStream = new ObjectInputStream(fis);
            w = (AuctionFile)inputStream.readObject();
            inputStream.close();
            return w;
        } catch (IOException | ClassNotFoundException e) {
            w = new AuctionFile();
        }
        return w;
    }

    /** 경매장을 저장하는 method */
    public void saveAuction() {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oStream = new ObjectOutputStream(fos);
            oStream.writeObject(this);
            oStream.close();
        } catch (IOException e) {
        }
    }

    public ArrayList<AuctionItem> getAuctionItems() {
        return AuctionList;
    }

    /** 경매장에 아이템 추가
     * 다 찼으면 false 반환
     */
    public boolean addItem(AuctionItem item) {
        if (itemCount == MAXSIZE) {
            return false;
        }
        AuctionList.add(item);
        itemCount += 1;
        saveAuction();
        return true;
    }

    /** 경매장에서 아이템 제거 */
    public void removeItem(AuctionItem item) {
        if (item.getamount() > 1) {
            item.decreaseamount();
        }
        else {
            AuctionList.remove(item);
            itemCount -= 1;
        }
        saveAuction();
    }
}

/** 경매장 아이템을 가리키는 클래스 */
class AuctionItem {
    private Player player;
    private ItemStack item;
    private int price;
    public AuctionItem(Player player, ItemStack item, int price) {
        this.player = player;
        this.item = item;
        this.price = price;
    }

    /** 개수 임의 조정 */
    public void putamount(int amount) {
        this.item.setAmount(amount);
    }

    /** 개수 1개 줄이기 */
    public void decreaseamount() {
        this.item.setAmount(this.item.getAmount() - 1);
    }

    /** 현재 개수 가져오기 */
    public int getamount() {
        return this.item.getAmount();
    }

    /** ItemStack으로 반환 */
    public ItemStack returnItem() {
        return createItem();
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getPrice() {
        return price;
    }

    /** 가격 붙은 아이템으로 변환 */
    private ItemStack createItem() {
        final ItemMeta meta = item.getItemMeta();
        String[] lore = {"판매자 : " + player.getName(), "가격 : " + Integer.toString(price)};
        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}

/** 경매장에 파는 커맨드 */
class AddAuctionCommand implements CommandExecutor {
    private AuctionFile auction;
    private final String errorMsg = "§c사용법: /sellat <value>";
    public AddAuctionCommand(AuctionFile auction) {
        this.auction = auction;
    }

    /** 경매장에 등록하는 method */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {  //명령어 사용자가 플레이어가 아닌 경우 에러
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return false;
        }
        int price = -1;
        // 조건 확인
        if (args.length != 1){
            sender.sendMessage(errorMsg);
            return false;
        }
        // 금액이 정수인지 확인
        try {
            price = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e) {
            sender.sendMessage(errorMsg);
            return false;
        }

        ItemStack item = ((Player)sender).getInventory().getItemInMainHand();
        if (item == null) {
            sender.sendMessage("판매할 아이템이 없습니다.");
            return false;
        }
        AuctionItem sellItem = new AuctionItem((Player)sender, item, price);
        auction.addItem(sellItem);
        auction.saveAuction();
        return true;
    }
}


public class Auction implements Listener, CommandExecutor {
    private final Inventory inv;
    private Wallet wallet;
    private AuctionFile auction;

    public Auction() {
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
        inv = Bukkit.createInventory(null, 9 * 6, "경매장");

        // Put the items into the inventory
        initializeItems();
    }
    public Auction(Wallet wallet, AuctionFile auction, Dbs plugin) {
        this();
        this.wallet = wallet;
        this.auction = auction;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // You can call this whenever you want to put the items in
    public void initializeItems() {
        inv.clear();
        int cnt = 0;
        for (AuctionItem items: auction.getAuctionItems()) {
            inv.setItem(cnt, items.returnItem());
            cnt += 1;
        }
    }   

    /** 경매장 들어가는 method */
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
        initializeItems();
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
        if (e.isShiftClick()) {
            buyMaximum(p, e.getSlot());
        }
        else {
            buyItem(p, e.getSlot());
        }
    }

    private void buyItem(Player player, int location) {
        AuctionItem item = auction.getAuctionItems().get(location);
        if (player == item.getPlayer()) {
            player.getInventory().addItem(item.getItem());
            auction.removeItem(item);
            player.sendMessage("판매를 취소하였습니다.");
            return;
        }
        if (payMoney(player, item)) {
            player.getInventory().addItem(item.getItem());
            auction.removeItem(item);
            player.sendMessage("물건을 구매하였습니다.");
        }
        else {
            player.sendMessage("금액이 부족합니다.");
        }
    }

    private void buyMaximum(Player player, int location) {
        AuctionItem item = auction.getAuctionItems().get(location);
        int amount = item.getamount();
        if (player == item.getPlayer()) {
            while (amount > 0) {
                player.getInventory().addItem(item.getItem());
                auction.removeItem(item);
                amount -= 1;
            }
            player.sendMessage("판매를 취소하였습니다.");
            return;
        }
        while (amount > 0 && payMoney(player, item)) {
            player.getInventory().addItem(item.getItem());
            auction.removeItem(item);
            amount -= 1;
        }
        player.sendMessage("최대 개수를 구매했습니다.");
        return;
    }

    private boolean payMoney(Player player, AuctionItem item) {
        UUID id = player.getUniqueId();
        int price = item.getPrice();
        if (wallet.getGold(id) < price / 100) {
            return false;
        }
        price %= 100;
        if (wallet.getSilver(id) < price / 10) {
            return false;
        }
        price %= 10;
        if (wallet.getCopper(id) < price) {
            return false;
        }
        
        // 지불가능하면 지불하기
        wallet.putGold(id, wallet.getGold(id) - (price / 100));
        wallet.putSilver(id, wallet.getSilver(id) - (price % 100 / 10));
        wallet.putCopper(id, wallet.getCopper(id) - (price % 100 % 10));
        PlayerScoreboard.updateScoreboard(player, wallet);
        return true;
    }


    /** 드래그 막기 */
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory().equals(inv)) {
          e.setCancelled(true);
        }
    }
}