/*
 * Die-Bremer-Stadtmusikanten Entry Point
 */
package die.bremer.stadtmusikanten;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class Dbs extends JavaPlugin {
    private Wallet wallet = Wallet.loadWallet();
    private AuctionFile auction = AuctionFile.loadAuction();
    /**
     * Plugin ON
     */
    @Override
    public void onEnable(){
        getLogger().info("Plugin Enabled.");
        _loadCommands();
        _loadEvents();
    }

    /**
     * 플러그인을 로드 하였을 때(활성화 이전) 발생하는 콜백 함수
     * 버킷을 실행하거나 /reload 명령어로 재시작하였을 때 발생합니다.
     * 여기서 getCommands를 호출하면 NullPointerException이 발생함
     */
    @Override
    public void onLoad() {
        getLogger().info("Plugin Reloaded.");
        //_loadCommands();
        //_loadEvents();
    }

    /**
     * Plugin OFF
     */    
    @Override
    public void onDisable(){
        getLogger().info("Plugin Disabled.");
        wallet.saveWallet();
    }

    /** Commands를 load */
    private void _loadCommands() {
        getCommand("charge").setExecutor(new ChargeCommand(wallet));
        getCommand("walletset").setExecutor(new WalletSetCommand(wallet));
        getCommand("exchange").setExecutor(new Store(wallet, this));
        getCommand("auction").setExecutor(new Auction(wallet, auction, this));
        getCommand("sellat").setExecutor(new AddAuctionCommand(auction));
    }

    /** Events를 load */
    private void _loadEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEventHandler(wallet), this);
    }
}

class PlayerEventHandler implements Listener {
    private Wallet wallet;
    PlayerEventHandler(Wallet wallet) {
        this.wallet = wallet;
    }

    /**
     * Triggered When a Player joined the server
     */
    @EventHandler
    public void PlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        boolean hasPlayed = player.hasPlayedBefore();
        Location loc = player.getLocation();
        if (hasPlayed) {
            player.sendMessage("§9" + player.getName() + "§f 님, 돌아오신 것을 환영합니다 !");
        }
        else {
            player.sendTitle("§5브레멘음악대♫", "§5브레멘음악대♫§f 서버에 어서 오세요!", 10, 70, 20);
            player.sendMessage("§2" + player.getName() + "§f 님이 처음 서버에 접속하셨습니다.");
        }
        player.setPlayerListHeaderFooter(" §5브레멘음악대♫ \n", "\n §9" + player.getName() + "§f님, 서버에 어서 오세요! \n");
        wallet.addPlayer(player.getUniqueId());
        PlayerScoreboard.updateScoreboard(player, wallet);
        loc.getWorld().spawnEntity(loc, EntityType.SPLASH_POTION);
    }
}