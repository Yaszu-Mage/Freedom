package xyz.yaszu.freedom.Commands.Arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import xyz.yaszu.freedom.Information.BaseInformation;
import xyz.yaszu.freedom.Information.Information_Handler;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Items.ItemListener;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class CustomItemArgument implements CustomArgumentType.Converted<String, String> {
    private final CustomItemType type;

    public CustomItemArgument(CustomItemType type) {
        this.type = type;
    }

    private static final DynamicCommandExceptionType ERROR_INVALID_ITEM = new DynamicCommandExceptionType(flavor -> {
        return MessageComponentSerializer.message().serialize(Component.text(flavor + " is not a valid item!"));
    });

    @Override
    public String convert(String nativeType) throws CommandSyntaxException {
        if (type == CustomItemType.BOOK) {
            BaseInformation book = Information_Handler.ITEMS.get(nativeType);
            if (book != null && book.getType() == type) {
                return nativeType;
            }
        } else {
            BaseItem item = ItemListener.ITEMS.get(nativeType);
            if (item != null && item.getType() == type) {
                return nativeType;
            }
        }
        throw ERROR_INVALID_ITEM.create(nativeType);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        if (type == CustomItemType.BOOK) {
            for (String id : Information_Handler.ITEMS.keySet()) {
                BaseInformation book = Information_Handler.ITEMS.get(id);
                if (book != null && book.getType() == type) {
                    if (id.startsWith(remaining)) {
                        builder.suggest(id);
                    }
                }
            }
        } else {
            for (String id : ItemListener.ITEMS.keySet()) {
                BaseItem item = ItemListener.ITEMS.get(id);
                if (item != null && item.getType() == type) {
                    if (id.startsWith(remaining)) {
                        builder.suggest(id);
                    }
                }
            }
        }
        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
