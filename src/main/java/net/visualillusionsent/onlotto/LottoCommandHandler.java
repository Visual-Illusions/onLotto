/*
 * This file is part of onLotto.
 *
 * Copyright © 2011-2014 Visual Illusions Entertainment
 *
 * onLotto is free software: you can redistribute it and/or modify
 * it under the terms of the ${gpl.type} as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the ${gpl.type} for more details.
 *
 * You should have received a copy of the ${gpl.type} along with this program.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.onlotto;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.TabComplete;
import net.canarymod.commandsys.TabCompleteHelper;
import net.visualillusionsent.minecraft.plugin.ModMessageReceiver;
import net.visualillusionsent.minecraft.plugin.canary.CanaryMessageReceiver;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPluginInformationCommand;

import java.util.Iterator;
import java.util.List;

public final class LottoCommandHandler extends VisualIllusionsCanaryPluginInformationCommand {
    public LottoCommandHandler(OnLotto onlotto) throws CommandDependencyException {
        super(onlotto);
        onlotto.registerCommands(this, false);
    }

    @Command(
            aliases = { "onlotto" },
            description = "Information Command",
            permissions = { "" },
            toolTip = "/onlotto"
    )
    public final void lottobase(MessageReceiver msgrec, String[] args) {
        super.sendInformation(msgrec);
    }

    @Override
    protected void messageInject(ModMessageReceiver mmr) {
        //-- Help --
        MessageReceiver receiver = ((CanaryMessageReceiver)mmr).unwrap();
        if (receiver.hasPermission("onlotto.time")) {
            mmr.message("§2/onLotto time §6- displays time till drawing");
        }
        if (receiver.hasPermission("onlotto.broadcast")) {
            mmr.message("§2/onlotto broadcast §6- broadcast time till drawing");
        }
        if (receiver.hasPermission("onlotto.draw")) {
            mmr.message("§2/onlotto draw §6- draws lotto immediately");
        }
        if (receiver.hasPermission("onlotto.reload")) {
            mmr.message("§2/onlotto reload §6- reloads onLotto configuration and items");
        }
        if (receiver.hasPermission("onlotto.debug")) {
            mmr.message("§2/onlotto debug <item#> §6- Give the specified Item index for debugging purposes");
        }
    }

    @Command(
            aliases = { "time" },
            description = "Returns time till draw",
            permissions = { "onlotto.time" },
            toolTip = "/onlotto time",
            parent = "onlotto"
    )
    public final void lottoTime(MessageReceiver msgrec, String[] args) {
        msgrec.message(((OnLotto) getPlugin()).timeUntil());
    }

    @Command(
            aliases = { "broadcast" },
            description = "Broadcasts time till draw",
            permissions = { "onlotto.broadcast" },
            toolTip = "/onlotto broadcast",
            parent = "onlotto"
    )
    public final void lottoBroadcast(MessageReceiver msgrec, String[] args) {
        Canary.getServer().broadcastMessage(((OnLotto) getPlugin()).timeUntil());
    }

    @Command(
            aliases = { "draw" },
            description = "Force draws the lotto",
            permissions = { "onlotto.draw" },
            toolTip = "/onlotto draw",
            parent = "onlotto"
    )
    public final void lottodraw(MessageReceiver msgrec, String[] args) {
        ((OnLotto) getPlugin()).restartTimer();
        new LottoTask(((OnLotto) getPlugin())).run();
    }

    @Command(
            aliases = { "reload" },
            description = "Reloads onLotto configuration and items",
            permissions = { "onlotto.reload" },
            toolTip = "/onlotto reload",
            parent = "onlotto"
    )
    public final void lottoReload(MessageReceiver msgrec, String[] args) {
        ((OnLotto) getPlugin()).reload();
    }

    @Command(
            aliases = { "debug" },
            description = "Give the specified Item index for debugging purposes",
            permissions = { "onlotto.debug" },
            toolTip = "/onlotto debug <item#>",
            parent = "onlotto",
            min = 2
    )
    public final void lottoDebug(MessageReceiver msgrec, String[] args) {
        try {
            int index = Integer.parseInt(args[1]);
            WeightedItem[] items = ((OnLotto) getPlugin()).getItems();
            if (index < 0 || index > items.length - 1) {
                msgrec.notice("Invalid Item Index");
                return;
            }
            WeightedItem weightedItem = items[index];
            if (msgrec instanceof Player) {
                ((Player) msgrec).giveItem(weightedItem.getItem().clone());
            }
            else {
                msgrec.notice(weightedItem.getItem().toString());
            }
        }
        catch (NumberFormatException nfex) {
            msgrec.notice("Invalid Item Index");
        }
    }

    @TabComplete(commands = "onlotto")
    public final List<String> lottoTabComplete(MessageReceiver msgrec, String[] args) {
        if (args.length != 1) {
            return null;
        }
        //time broadcast draw
        List<String> temp = TabCompleteHelper.matchTo(args, new String[]{ "time", "broadcast", "draw", "reload", "debug" });
        if (temp != null) {
            Iterator<String> matches = temp.iterator();
            while (matches.hasNext()) {
                String match = matches.next();
                if (!msgrec.hasPermission("onlotto.".concat(match))) {
                    matches.remove();
                }
            }
        }
        return temp;
    }
}
