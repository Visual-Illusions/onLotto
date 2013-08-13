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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.canarymod.Canary;
import net.canarymod.chat.Colors;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.chat.TextFormat;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.CommandListener;
import net.visualillusionsent.utils.StringUtils;
import net.visualillusionsent.utils.VersionChecker;

public class LottoCommandHandler implements CommandListener {

    private final OnLotto onlotto;
    private final List<String> about;

    public LottoCommandHandler(OnLotto onlotto) throws CommandDependencyException {
        Canary.commands().registerCommands(this, onlotto, false);
        this.onlotto = onlotto;

        List<String> pre = new ArrayList<String>();
        pre.add(center(Colors.CYAN + "--- " + Colors.LIGHT_GREEN + onlotto.getName() + Colors.ORANGE + " v" + onlotto.getRawVersion() + Colors.CYAN + " ---"));
        pre.add("$VERSION_CHECK$");
        pre.add(Colors.ORANGE + "Build: " + Colors.LIGHT_GREEN + onlotto.getBuildNumber());
        pre.add(Colors.ORANGE + "Built: " + Colors.LIGHT_GREEN + onlotto.getBuildTime());
        pre.add(Colors.ORANGE + "Developers: " + Colors.LIGHT_GREEN + "DarkDiplomat");
        pre.add(Colors.ORANGE + "Website: " + Colors.LIGHT_GREEN + "http://wiki.visualillusionsent.net/onLotto");
        pre.add(Colors.ORANGE + "Issues: " + Colors.LIGHT_GREEN + "http://git.io/");

        // Next line should always remain at the end of the About
        pre.add(center("§aCopyright © 2011-2013 §2Visual §6I§9l§bl§4u§as§2i§5o§en§7s §2Entertainment"));
        about = Collections.unmodifiableList(pre);
    }

    @Command(aliases = { "onlotto" },
        description = "Information Command",
        permissions = { "" },
        toolTip = "/onlotto")
    public final void lottobase(MessageReceiver msgrec, String[] args) {
        for (String msg : about) {
            if (msg.equals("$VERSION_CHECK$")) {
                VersionChecker vc = onlotto.getVersionChecker();
                Boolean islatest = vc.isLatest();
                if (islatest == null) {
                    msgrec.message(center(Colors.GRAY + "VersionCheckerError: " + vc.getErrorMessage()));
                }
                else if (!vc.isLatest()) {
                    msgrec.message(center(Colors.GRAY + vc.getUpdateAvailibleMessage()));
                }
                else {
                    msgrec.message(center(Colors.LIGHT_GREEN + "Latest Version Installed"));
                }
            }
            else {
                msgrec.message(msg);
            }
        }
        // Win Eligable?

        //-- Help --
        msgrec.message("§2/onLotto time §6- displays time till drawing");
        if (msgrec.hasPermission("onlotto.broadcast")) {
            msgrec.message("§2/onlotto broadcast §6- broadcast time till drawing");
            msgrec.message("§2/onlotto draw §6- draws lotto immediately");
        }
    }

    public final void lottoTime(MessageReceiver msgrec, String[] args) {
        msgrec.message(onlotto.timeUntil());
    }

    @Command(aliases = { "broadcast" },
        description = "Broadcasts time till draw",
        permissions = { "onlotto.broadcast" },
        toolTip = "/onlotto broadcast",
        parent = "onlotto")
    public final void lottoBroadcast(MessageReceiver msgrec, String[] args) {
        Canary.getServer().broadcastMessage(onlotto.timeUntil());
    }

    @Command(aliases = { "draw" },
        description = "Force draws the lotto",
        permissions = { "onlotto.draw" },
        toolTip = "/onlotto broadcast",
        parent = "onlotto")
    public final void lottodraw(MessageReceiver msgrec, String[] args) {
        onlotto.restartTimer();
        new LottoTask(onlotto).run();
    }

    private final String center(String toCenter) {
        String strColorless = TextFormat.removeFormatting(toCenter);
        return StringUtils.padCharLeft(toCenter, (int) (Math.floor(63 - strColorless.length()) / 2), ' ');
    }
}
