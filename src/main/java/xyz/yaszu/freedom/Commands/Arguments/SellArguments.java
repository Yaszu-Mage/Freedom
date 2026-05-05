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
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Subsystems.CurrencyManager;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class SellArguments implements CustomArgumentType.Converted<CurrencyManager.SellType, String> {

    private static final DynamicCommandExceptionType ERROR_INVALID_SOUL = new DynamicCommandExceptionType(flavor -> {
        return MessageComponentSerializer.message().serialize(Component.text(flavor + " is not a valid selltype!"));
    });

    @Override
    public CurrencyManager.SellType convert(String nativeType) throws CommandSyntaxException {
        try {
            return CurrencyManager.SellType.valueOf(nativeType);
        } catch (IllegalArgumentException ignored) {
            throw ERROR_INVALID_SOUL.create(nativeType);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (CurrencyManager.SellType flavor : CurrencyManager.SellType.values()) {
            String name = flavor.toString();

            // Only suggest if the flavor name matches the user input
            if (name.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(flavor.toString());
            }
        }

        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}