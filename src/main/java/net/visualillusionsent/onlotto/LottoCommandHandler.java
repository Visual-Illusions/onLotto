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

import net.canarymod.Canary;
import net.canarymod.chat.Colors;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPluginInformationCommand;
import net.visualillusionsent.utils.VersionChecker;

public class LottoCommandHandler extends VisualIllusionsCanaryPluginInformationCommand {
    private final OnLotto onlotto;

    public LottoCommandHandler(OnLotto onlotto) throws CommandDependencyException {
        super(onlotto);
        Canary.commands().registerCommands(this, onlotto, false);
        this.onlotto = onlotto;
    }

    @Command(aliases = {"onlotto"},
            description = "Information Command",
            permissions = {""},
            toolTip = "/onlotto")
    public final void lottobase(MessageReceiver msgrec, String[] args) {
        for (String msg : about) {
            if (msg.equals("$VERSION_CHECK$")) {
                VersionChecker vc = plugin.getVersionChecker();
                Boolean isLatest = vc.isLatest();
                if (isLatest == null) {
                    msgrec.message(center(Colors.GRAY + "VersionCheckerError: " + vc.getErrorMessage()));
                } else if (!vc.isLatest()) {
                    msgrec.message(center(Colors.GRAY + vc.getUpdateAvailibleMessage()));
                } else {
                    msgrec.message(center(Colors.LIGHT_GREEN + "Latest Version Installed"));
                }
            } else {
                msgrec.message(msg);
            }
        }

        //-- Help --
        msgrec.message("§2/onLotto time §6- displays time till drawing");
        if (msgrec.hasPermission("onlotto.broadcast")) {
            msgrec.message("§2/onlotto broadcast §6- broadcast time till drawing");
            msgrec.message("§2/onlotto draw §6- draws lotto immediately");
        }
    }

    @Command(aliases = {"time"},
            description = "Returns time till draw",
            permissions = {"onlotto.time"},
            toolTip = "/onlotto time",
            parent = "onlotto")
    public final void lottoTime(MessageReceiver msgrec, String[] args) {
        msgrec.message(onlotto.timeUntil());
    }

    @Command(aliases = {"broadcast"},
            description = "Broadcasts time till draw",
            permissions = {"onlotto.broadcast"},
            toolTip = "/onlotto broadcast",
            parent = "onlotto")
    public final void lottoBroadcast(MessageReceiver msgrec, String[] args) {
        Canary.getServer().broadcastMessage(onlotto.timeUntil());
    }

    @Command(aliases = {"draw"},
            description = "Force draws the lotto",
            permissions = {"onlotto.draw"},
            toolTip = "/onlotto broadcast",
            parent = "onlotto")
    public final void lottodraw(MessageReceiver msgrec, String[] args) {
        onlotto.restartTimer();
        new LottoTask(onlotto).run();
    }
}
