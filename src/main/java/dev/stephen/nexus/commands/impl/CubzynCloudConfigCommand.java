package dev.stephen.nexus.commands.impl;

import dev.stephen.nexus.Client;
import dev.stephen.nexus.commands.Command;
import dev.stephen.nexus.utils.mc.ChatUtils;
import dev.stephen.nexus.utils.mc.PlayerUtil;

public class CubzynCloudConfigCommand extends Command {
    public CubzynCloudConfigCommand() {
        super("cubzyncloudconfig", new String[]{"<load/list>", "<configName>"});
    }

    @Override
    public void execute(String[] args) {
        if (args[0] == null) {
            sendMessage("Please specify an action");
            return;
        }

        if (args[0].equalsIgnoreCase("load")) {
            if (args[1] == null) {
                sendMessage("Please enter a config name");
                return;
            }

            Client.INSTANCE.getConfigManager().loadCloudConfig(args[1],args[2],true);
        } else if (args[0].equalsIgnoreCase("list")) {
            sendMessage(Client.INSTANCE.getConfigManager().getCloudConfigList(true));
        } else {
            sendMessage("Invalid Action");
        }
    }
}
