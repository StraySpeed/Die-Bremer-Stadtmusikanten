package die.bremer.stadtmusikanten;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class PlayerScoreboard {
    /** 스코어보드를 업데이트 하는 static method */
    public static void updateScoreboard(Player player, Wallet wallet) {
        UUID id = player.getUniqueId();
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        // 스코어보드 한 번 초기화하기
        player.setScoreboard(manager.getNewScoreboard());

        Objective objective = board.registerNewObjective(" [§5브레멘음악대♫§f] ", Criteria.DUMMY, " [§5브레멘음악대♫§f] ", RenderType.INTEGER);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score score8 = objective.getScore("=================");
        score8.setScore(8);
        Score score7 = objective.getScore("> 이름");
        score7.setScore(7);
        Score score6 = objective.getScore("§9" + player.getName());
        score6.setScore(6);
        Score score5 = objective.getScore(" ");
        score5.setScore(5);
        Score score4 = objective.getScore("> §e금화 §f" + wallet.getGold(id) + "원");
        score4.setScore(4);
        Score score3 = objective.getScore("> §7은화 §f" + wallet.getSilver(id) + "원");
        score3.setScore(3);
        Score score2 = objective.getScore("> §6동화 §f" + wallet.getCopper(id) + "원");
        score2.setScore(2);
        Score score1 = objective.getScore("  ");
        score1.setScore(1);
        player.setScoreboard(board);
    }
}
