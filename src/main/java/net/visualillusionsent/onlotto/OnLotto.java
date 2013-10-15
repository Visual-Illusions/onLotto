/*
 * This file is part of onLotto.
 *
 * Copyright © 2011-2013 Visual Illusions Entertainment
 *
 * onLotto is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * onLotto is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with onLotto.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.onlotto;

import net.canarymod.ToolBox;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.chat.TextFormat;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPlugin;
import net.visualillusionsent.utils.PropertiesFile;

import java.util.Timer;

public final class OnLotto extends VisualIllusionsCanaryPlugin {

    private Timer lottoTimer;
    private PropertiesFile lottoProps;
    private long started;
    private WeightedItem[] items;

    @Override
    public final boolean enable() {
        try {
            checkStatus();
            checkVersion();
            loadProps();
            items = new ItemLoader().load(this).toArray(new WeightedItem[0]);
            lottoTimer = new Timer();
            lottoTimer.scheduleAtFixedRate(new LottoTask(this), getStartTime(), getDelayTime());
            new LottoCommandHandler(this);
        }
        catch (Exception ex) {
            getLogman().logStacktrace("Failed to enable onLotto...", ex);
            if (lottoTimer != null) {
                lottoTimer.cancel();
            }
            return false;
        }
        return true;
    }

    @Override
    public final void disable() {
        lottoTimer.cancel();
    }

    final boolean everyoneWins() {
        return true;
    }

    final void setStart(long time) {
        this.started = time;
        lottoProps.setLong("timer.started", time);
        lottoProps.save();
    }

    final WeightedItem[] getItems() {
        return items;
    }

    final boolean canPlayerWin(Player player) {
        for (String group : lottoProps.getStringArray("disabled.groups")) {
            if (player.isInGroup(group, false)) {
                return false;
            }
        }
        return true;
    }

    final int maxWinners() {
        return lottoProps.getInt("max.winners");
    }

    final String timeUntil() {
        return String.format("%sNext Drawing in%s %s", TextFormat.GREEN, TextFormat.YELLOW, ToolBox.getTimeUntil(started, (getDelayTime() / 1000)));
    }

    final void restartTimer() {
        lottoTimer.cancel();
        lottoTimer.purge();
        lottoTimer = new Timer();
        lottoTimer.scheduleAtFixedRate(new LottoTask(this), getStartTime(), getDelayTime());
    }

    private final void loadProps() {
        lottoProps = new PropertiesFile("config/onLotto/onLotto.cfg");
        lottoProps.getBoolean("everyone.wins", true);
        lottoProps.setComments("everyone.wins", "Set to true to give all online users a random item (minus those in disabled groups)");
        lottoProps.getInt("max.winners", 1);
        lottoProps.setComments("max.winners", "The number of winners to choose unless everyone.wins is set");
        lottoProps.getStringArray("disabled.groups", new String[]{ "admins", "mods", "visitors" });
        lottoProps.setComments("disabled.groups", "The names of the groups to not allow winning. No parent checking is done so every group needs to be directly specified. Seperate names with commas");
        lottoProps.getLong("draw.delay", 1L);
        lottoProps.setComments("draw.delay", "The time in minutes between drawings");
        lottoProps.getLong("timer.started", 0L);
        lottoProps.setComments("timer.started", "* DO NOT EDIT THIS PROPERTY * (Resets the timer between restarts)");
        lottoProps.save();
    }

    public final long getStartTime() {
        long time = ToolBox.getUnixTimestamp() - lottoProps.getLong("timer.started");
        return time < 60L ? 60000L : time > getDelayTime() ? getDelayTime() * 1000 : time * 1000;
    }

    public final long getDelayTime() {
        long time = lottoProps.getLong("draw.delay") * 60000;
        return time < 60000L ? 60000L : time;
    }
}
