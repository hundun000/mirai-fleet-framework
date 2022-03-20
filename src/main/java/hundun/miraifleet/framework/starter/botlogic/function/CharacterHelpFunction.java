package hundun.miraifleet.framework.starter.botlogic.function;
/**
 * @author hundun
 * Created on 2022/02/11
 */

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.BaseFunction;
import lombok.Getter;
import net.mamoe.mirai.console.command.AbstractCommand;
import net.mamoe.mirai.console.command.Command;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.internal.command.CommandManagerImpl;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

public class CharacterHelpFunction extends BaseFunction<Void> {
    @Getter
    private final CompositeCommandFunctionComponent commandComponent;
    
    public CharacterHelpFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName
            ) {
        super(
            baseBotLogic,
            plugin,
            characterName,
            "CharacterHelpFunction",
            true,
            null
            );
        this.commandComponent = new CompositeCommandFunctionComponent(plugin, characterName, functionName);
    }
    
    @Override
    public AbstractCommand provideCommand() {
        return commandComponent;
    }

    public class CompositeCommandFunctionComponent extends AbstractCompositeCommandFunctionComponent {
        public CompositeCommandFunctionComponent(JvmPlugin plugin, String characterName, String functionName) {
            super(plugin, characterName, functionName);
        }
        
        @SubCommand("help")
        public void help(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }
            
            List<Command> list = CommandManager.INSTANCE.getAllRegisteredCommands();
            String test = list.stream()
                    .filter(command -> {
                                boolean hasPerm = PermissionService.testPermission(command.getPermission(), sender.getPermitteeId());
                                boolean fromThisPlugin = command.getOwner() == plugin;
                                return hasPerm && fromThisPlugin;
                    })
                    .map(command -> {
                                StringBuilder builder = new StringBuilder();
                                List<String> lines = command.getUsage().lines().collect(Collectors.toList());
                                if (!lines.isEmpty()) {
                                    builder.append("◆ " + lines.remove(0) + "\n");
                                    lines.forEach(line -> builder.append(line + "\n"));
                                } else {
                                    builder.append("/" + command.getPrimaryName() + " " + command.getDescription());
                                }
                                return builder.toString();
                    })
                    .collect(Collectors.joining("\n"))
                    
                    ;
            sender.sendMessage(test);
        }
    }
}
