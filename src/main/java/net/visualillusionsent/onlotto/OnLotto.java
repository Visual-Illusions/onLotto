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

import net.canarymod.ToolBox;
import net.canarymod.api.GameMode;
import net.canarymod.api.entity.living.humanoid.Player;
import net.visualillusionsent.minecraft.plugin.ChatFormat;
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
        super.enable();
        try {
            loadProps();
            items = new ItemLoader().load(this);
            lottoTimer = new Timer();
            lottoTimer.scheduleAtFixedRate(new LottoTask(this), getStartTime(), getDelayTime());
            new LottoCommandHandler(this);
        }
        catch (Exception ex) {
            getLogman().error("Failed to enable onLotto...", ex);
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

    final boolean reload() {
        try {
            lottoTimer.cancel();
            lottoProps.reload();
            items = new ItemLoader().load(this);
            restartTimer();
        }
        catch (Exception ex) {
            getLogman().error("Failed to reload onLotto...", ex);
            return false;
        }
        return true;
    }

    final boolean everyoneWins() {
        return lottoProps.getBoolean("everyone.wins");
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
        if (player.getMode().equals(GameMode.CREATIVE) && lottoProps.getBoolean("disable.creative")) {
            return false;
        }
        else if (player.getMode().equals(GameMode.ADVENTURE) && lottoProps.getBoolean("disable.adventure")) {
            return false;
        }
        else if (player.getMode().equals(GameMode.SPECTATOR) && lottoProps.getBoolean("disable.spectator")) {
            return false;
        }
        return true;
    }

    final int maxWinners() {
        return lottoProps.getInt("max.winners");
    }

    final String timeUntil() {
        return String.format("%sNext Drawing in%s %s", ChatFormat.GREEN, ChatFormat.YELLOW, ToolBox.getTimeUntil(started, (getDelayTime() / 1000)));
    }

    final void restartTimer() {
        lottoTimer.cancel();
        lottoTimer.purge();
        lottoTimer = new Timer();
        lottoTimer.scheduleAtFixedRate(new LottoTask(this), getStartTime(), getDelayTime());
    }

    private void loadProps() {
        lottoProps = new PropertiesFile("config/onLotto/onLotto.cfg");
        lottoProps.getBoolean("everyone.wins", true);
        lottoProps.setComments("everyone.wins", "Set to true to give all online users a random item (minus those in disabled groups)");
        lottoProps.getInt("max.winners", 1);
        lottoProps.setComments("max.winners", "The number of winners to choose unless everyone.wins is set");
        lottoProps.getStringArray("disabled.groups", new String[]{ "admins", "mods", "visitors" });
        lottoProps.setComments("disabled.groups", "The names of the groups to not allow winning. No parent checking is done so every group needs to be directly specified. Seperate names with commas");
        lottoProps.getBoolean("disable.creative", true);
        lottoProps.setComments("disable.creative", "Disables winning items while in Creative Mode");
        lottoProps.getBoolean("disable.adventure", true);
        lottoProps.setComments("disable.adventure", "Disables winning items while in Adventure Mode");
        lottoProps.getBoolean("disable.spectator", true);
        lottoProps.setComments("disable.spectator", "Disables winning items while in Spectator Mode");
        lottoProps.getLong("draw.delay", 30);
        lottoProps.setComments("draw.delay", "The time in minutes between drawings (default: 30)");
        lottoProps.getLong("timer.started", 0);
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
