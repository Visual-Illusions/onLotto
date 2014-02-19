/*
 * This file is part of onLotto.
 *
 * Copyright © 2011-2014 Visual Illusions Entertainment
 *
 * onLotto is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.onlotto;

import net.canarymod.Canary;
import net.canarymod.ToolBox;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.Item;
import net.canarymod.chat.TextFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

public final class LottoTask extends TimerTask {

    private final static Random randGen = new Random();
    private final OnLotto onlotto;

    LottoTask(OnLotto onlotto) {
        this.onlotto = onlotto;
        onlotto.setStart(ToolBox.getUnixTimestamp());
    }

    @Override
    public final void run() {
        if (Canary.getServer().getNumPlayersOnline() == 0) {
            onlotto.getLogman().info("No one online to win...");
            return;
        }
        if (onlotto.everyoneWins()) {
            ArrayList<String> winners = new ArrayList<String>();
            for (Player player : Canary.getServer().getPlayerList()) {
                if (onlotto.canPlayerWin(player)) {
                    Item winning = getRandomItem();
                    player.message(String.format("%sYou have won %s%d %sof%s %s", TextFormat.GREEN, TextFormat.TURQUIOSE, winning.getAmount(), TextFormat.GREEN, TextFormat.CYAN, winning.getDisplayName()));
                    player.dropItem(winning);
                    winners.add(player.getName() + ":" + winning.getDisplayName() + "{" + winning.getAmount() + "}");
                }
            }
            Canary.getServer().broadcastMessage(String.format("%sLottery Drawn!%s %d %sPlayers have won items!", TextFormat.GREEN, TextFormat.TURQUIOSE, winners.size(), TextFormat.GREEN));
            onlotto.getLogman().info(String.format("Lottery Drawn! %s", winners.toString()));
            onlotto.setStart(ToolBox.getUnixTimestamp());
            Canary.getServer().broadcastMessage(onlotto.timeUntil());
        }
        else {
            getRandomWinners();
        }
    }

    private void getRandomWinners() {
        ArrayList<Player> players = new ArrayList<Player>(Canary.getServer().getNumPlayersOnline());
        Collections.copy(players, Canary.getServer().getPlayerList());
        Iterator<Player> playItr = players.iterator();
        while (playItr.hasNext()) {
            Player checking = playItr.next();
            if (!onlotto.canPlayerWin(checking)) {
                playItr.remove();
            }
        }
        if (players.isEmpty()) {
            Canary.getServer().broadcastMessage("No one online eligable to win...");
        }
        else {
            ArrayList<String> winners = new ArrayList<String>();
            for (int count = 0; count < onlotto.maxWinners(); count++) {
                if (players.isEmpty() || players.size() <= count) {
                    break;
                }
                int randwin = randGen.nextInt(players.size());
                Player player = players.get(randwin);
                Item winning = getRandomItem();
                player.message(String.format("%sYou have won %s%d %sof%s %s", TextFormat.GREEN, TextFormat.TURQUIOSE, winning.getAmount(), TextFormat.GREEN, TextFormat.CYAN, winning.getDisplayName()));
                if (!player.getInventory().insertItem(winning)) {
                    player.dropItem(winning);
                }
                players.remove(player);
                winners.add(player.getName() + ":" + winning.getDisplayName() + "{" + winning.getAmount() + "}");
            }
            Canary.getServer().broadcastMessage(String.format("%sLottery Drawn!%s %d %sPlayers have won items!", TextFormat.GREEN, TextFormat.TURQUIOSE, winners.size(), TextFormat.GREEN));
            onlotto.getLogman().info(String.format("Lottery Drawn! %s", winners.toString()));
            onlotto.setStart(ToolBox.getUnixTimestamp());
            Canary.getServer().broadcastMessage(onlotto.timeUntil());
        }
    }

    private Item getRandomItem() {
        List<WeightedItem> items = Arrays.asList(onlotto.getItems());
        Collections.shuffle(items); // Shuffle up the list so the first few items will get a chance at selection too
        // Compute the total weight of all items together
        double totalWeight = 0.0d;
        for (WeightedItem i : items) {
            totalWeight += i.getWeight();
        }
        // Now choose a random item
        int randomIndex = -1;
        double random = Math.random() * totalWeight;
        for (int index = 0; index < items.size(); ++index) {
            random -= items.get(index).getWeight();
            if (random <= 0.0d) {
                randomIndex = index;
                break;
            }
        }
        if (randomIndex == -1) {
            return getRandomItem(); //retry
        }
        return items.get(randomIndex).getItem();
    }
}
