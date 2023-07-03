package die.bremer.stadtmusikanten;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;


public class Wallet implements Serializable {
    /** 지갑 파일 이름 */
    static final private String fileName = "Wallet.ser";

    /** 금화/은화/동화를 저장하는 HashMap */
    private HashMap<UUID, Integer> gold = new HashMap<UUID, Integer>();
    private HashMap<UUID, Integer> silver = new HashMap<UUID, Integer>();
    private HashMap<UUID, Integer> copper = new HashMap<UUID, Integer>();

    /** Player ID를 기준으로 지갑을 초기화하는 Constructor */
    Wallet() {
    }

    /** Getter */
    public int getGold(UUID id) {
        return this.gold.get(id);
    }

    public int getSilver(UUID id) {
        return this.silver.get(id);
    }

    public int getCopper(UUID id) {
        return this.copper.get(id);
    }

    /** Setter */
    public void putGold(UUID id, int value) {
        this.gold.put(id, value);
    }

    public void putSilver(UUID id, int value) {
        this.silver.put(id, value);
    }

    public void putCopper(UUID id, int value) {
        this.copper.put(id, value);
    }

    /** 해당 아이디로 금액을 초기화하기 */
    public void addPlayer(UUID id) {
        if (!this.gold.containsKey(id)) {
            this.gold.put(id, 10);
            this.silver.put(id, 10);
            this.copper.put(id, 10);
        }
    }

    /** 지갑을 불러오는 method */
    public static Wallet loadWallet() {
        Wallet w = null;
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream inputStream = new ObjectInputStream(fis);
            w = (Wallet)inputStream.readObject();
            inputStream.close();
            return w;
        } catch (IOException | ClassNotFoundException e) {
            w = new Wallet();
        }
        return w;
    }

    /** 지갑을 저장하는 method */
    public void saveWallet() {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oStream = new ObjectOutputStream(fos);
            oStream.writeObject(this);
            oStream.close();
        } catch (IOException e) {
        }
    }
}

class ChargeCommand implements CommandExecutor{
    private Wallet wallet;
    ChargeCommand(Wallet wallet) {
        this.wallet = wallet;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {  //명령어 사용자가 플레이어인 경우
            Player player = (Player)sender; //명령어 사용자 객체를 플레이어 객체로 변환할 수 있음

            UUID id = player.getUniqueId(); // ID 가져오기
            wallet.putGold(id, wallet.getGold(id) + 10);
            wallet.putSilver(id, wallet.getSilver(id) + 10);
            wallet.putCopper(id, wallet.getCopper(id) + 10);
            
            player.sendMessage("금액이 충전되었습니다.");    //사용자에게 메시지 발신
            player.sendMessage(Integer.toString(wallet.getGold(id)));
            return true;    //true값을 반환하면 명령어가 성공한 것으로 간주
        }
        else if(sender instanceof ConsoleCommandSender) {   //명령어 사용자가 콘솔인 경우
            sender.sendMessage("콘솔에서는 이 명령어를 실행할 수 없습니다.");
            return false;   //false값을 반환하면 명령어가 실패한 것으로 간주
        }
        return false;   //false값을 반환하면 명령어가 실패한 것으로 간주
    }
}

class WalletSetCommand implements CommandExecutor {
    private Wallet wallet;
    private final String errorMsg = "§c사용법: /walletset <player> <gold|silver|copper|all> <value>";
    private final String[] walletValue = new String[]{"gold", "silver", "copper", "all"};
    WalletSetCommand(Wallet wallet) {
        this.wallet = wallet;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 조건 확인
        if (args.length != 3){
            sender.sendMessage(errorMsg);
            return false;
        }

        Player player = Bukkit.getPlayer(args[0]);
        String key = args[1];
        int value = Integer.parseInt(args[2]);

        // 조건이 안맞으면 errorMsg출력
        if (player == null | !(Arrays.asList(walletValue).contains(key))) {
            sender.sendMessage(errorMsg);
            return false;
        }
        
        // wallet 데이터 조정
        if (key == "gold") {
            wallet.putGold(player.getUniqueId(), value);
        }
        else if (key == "silver") {
            wallet.putSilver(player.getUniqueId(), value);
        }
        else if (key == "copper") {
            wallet.putCopper(player.getUniqueId(), value);
        }
        else {
            wallet.putGold(player.getUniqueId(), value);
            wallet.putSilver(player.getUniqueId(), value);
            wallet.putCopper(player.getUniqueId(), value);
        }
        // wallet 데이터 저장하기
        wallet.saveWallet();

        sender.sendMessage("§2" + player.getName() + "§f 님의 \"§e" + key + "\"§f 값을 §5" + Integer.toString(value) + "§f 로 조정하였습니다.");
        return true;
    }
}