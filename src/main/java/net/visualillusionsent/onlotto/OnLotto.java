/*
 * This file is part of onLotto.
 *
 * Copyright © 2011 Visual Illusions Entertainment
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

import java.io.IOException;
import java.util.Timer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import net.canarymod.ToolBox;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.chat.TextFormat;
import net.canarymod.plugin.Plugin;
import net.visualillusionsent.utils.ProgramStatus;
import net.visualillusionsent.utils.PropertiesFile;
import net.visualillusionsent.utils.VersionChecker;

public final class OnLotto extends Plugin {

    private Timer lottoTimer;
    private PropertiesFile lottoProps;
    private long started;
    private WeightedItem[] items;

    /* VERSION CONTROL */
    private final VersionChecker vc;
    private float version;
    private short build;
    private String buildTime;
    private ProgramStatus status;

    public OnLotto() {
        readManifest();
        vc = new VersionChecker(getName(), String.valueOf(version), String.valueOf(build), "http://visualillusionsent.net/minecraft/plugins/", status, false);
    }

    @Override
    public final boolean enable() {
        try {
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
        lottoProps.getStringArray("disabled.groups", new String[] { "admins", "mods", "visitors" });
        lottoProps.setComments("disabled.groups", "The names of the groups to not allow winning. No parent checking is done so every group needs to be directly specified. Seperate names with commas");
        lottoProps.getLong("draw.delay", 1L);
        lottoProps.setComments("draw.delay", "The time in minutes between drawings");
        lottoProps.getLong("timer.started", 0L);
        lottoProps.setComments("timer.started", "* DO NOT EDIT THIS PROPERTY * (Resets the timer between restars)");
        lottoProps.save();
    }

    public long getStartTime() {
        long time = ToolBox.getUnixTimestamp() - lottoProps.getLong("timer.started");
        return time < 60L ? 60000L : time > getDelayTime() ? getDelayTime() * 1000 : time * 1000;
    }

    public long getDelayTime() {
        long time = lottoProps.getLong("draw.delay") * 60000;
        return time < 60000L ? 60000L : time;
    }

    /* VERSIONING GETTERS */
    final Manifest getManifest() throws Exception {
        Manifest toRet = null;
        Exception ex = null;
        JarFile jar = null;
        try {
            jar = new JarFile(getJarPath());
            toRet = jar.getManifest();
        }
        catch (Exception e) {
            ex = e;
        }
        finally {
            if (jar != null) {
                try {
                    jar.close();
                }
                catch (IOException e) {}
            }
            if (ex != null) {
                throw ex;
            }
        }
        return toRet;
    }

    final void readManifest() {
        try {
            Manifest manifest = getManifest();
            Attributes mainAttribs = manifest.getMainAttributes();
            version = Float.parseFloat(mainAttribs.getValue("Version").replace("-SNAPSHOT", ""));
            build = Short.parseShort(mainAttribs.getValue("Build"));
            buildTime = mainAttribs.getValue("Build-Time");
            try {
                status = ProgramStatus.valueOf(mainAttribs.getValue("ProgramStatus"));
            }
            catch (IllegalArgumentException iaex) {
                status = ProgramStatus.UNKNOWN;
            }
        }
        catch (Exception ex) {
            version = -1.0F;
            build = -1;
            buildTime = "19700101-0000";
        }
    }

    final void checkStatus() {
        if (status == ProgramStatus.UNKNOWN) {
            getLogman().severe(String.format("%s has declared itself as an 'UNKNOWN STATUS' build. Use is not advised and could cause damage to your system!", getName()));
        }
        else if (status == ProgramStatus.ALPHA) {
            getLogman().warning(String.format("%s has declared itself as a 'ALPHA' build. Production use is not advised!", getName()));
        }
        else if (status == ProgramStatus.BETA) {
            getLogman().warning(String.format("%s has declared itself as a 'BETA' build. Production use is not advised!", getName()));
        }
        else if (status == ProgramStatus.RELEASE_CANDIDATE) {
            getLogman().info(String.format("%s has declared itself as a 'Release Candidate' build. Expect some bugs.", getName()));
        }
    }

    final void checkVersion() {
        Boolean islatest = vc.isLatest();
        if (islatest == null) {
            getLogman().warning("VersionCheckerError: " + vc.getErrorMessage());
        }
        else if (!vc.isLatest()) {
            getLogman().warning(vc.getUpdateAvailibleMessage());
            getLogman().warning(String.format("You can view update info @ http://wiki.visualillusionsent.net/%s#ChangeLog", getName()));
        }
    }

    final float getRawVersion() {
        return version;
    }

    final short getBuildNumber() {
        return build;
    }

    final String getBuildTime() {
        return buildTime;
    }

    final VersionChecker getVersionChecker() {
        return vc;
    }
}
