package com.example.waymod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * Команда /way:
 *   /way give <измерение> <x> <y> <z> — выдать блок перехода с заданной целью (оператор)
 *   /way set  <измерение> <pos>       — задать цель блоку перехода на позиции (оператор)
 *   /way spawner <моб>                — выдать крафтовый спавнер (оператор ИЛИ креатив)
 *   /way spawnerpro <моб>             — выдать Спавнер 2000 Pro (оператор ИЛИ креатив)
 *   /way link  <pos>                  — привязать передатчик (оператор)
 * Выживающему без прав спавнеры командой не выдаются — только оператор или креатив.
 */
public final class WayCommand {
	private WayCommand() {
	}

	public static void initialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				Commands.literal("way")
						// Корень без фильтра: ветки фильтруются сами (WayPerms).
						.then(Commands.literal("give")
								.requires(WayPerms::moderator)
								.then(Commands.argument("dimension", StringArgumentType.word())
										.suggests((context, builder) -> WayDims.suggest(builder))
										.then(Commands.argument("x", IntegerArgumentType.integer())
												.then(Commands.argument("y", IntegerArgumentType.integer())
														.then(Commands.argument("z", IntegerArgumentType.integer())
																.executes(WayCommand::giveWayBlock))))
								)
						)
						.then(Commands.literal("set")
								.requires(WayPerms::moderator)
								.then(Commands.argument("dimension", StringArgumentType.word())
										.suggests((context, builder) -> WayDims.suggest(builder))
										.then(Commands.argument("pos", BlockPosArgument.blockPos())
												.executes(WayCommand::setWayBlock)))
						)
						.then(Commands.literal("spawner")
								.requires(WayPerms::spawners)
								.then(Commands.argument("mob", StringArgumentType.word())
										.executes(WayCommand::giveSpawner))
						)
						.then(Commands.literal("spawnerpro")
								.requires(WayPerms::spawners)
								.then(Commands.argument("mob", StringArgumentType.word())
										.executes(WayCommand::giveProSpawner))
						)
						.then(Commands.literal("link")
								.requires(WayPerms::moderator)
								.then(Commands.argument("pos", BlockPosArgument.blockPos())
										.executes(WayCommand::linkTransmitter))
						)
		));
	}

	/** /way give <измерение> <x> <y> <z> */
	private static int giveWayBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayerOrException();

		WayTarget target = readTarget(context);
		if (target == null) {
			return 0;
		}
		ItemStack stack = new ItemStack(ModItems.WAY_BLOCK_ITEM);
		stack.set(ModComponents.WAY_TARGET, target);
		giveItem(player, stack);
		source.sendSuccess(() -> Component.translatable(
				"command.waymod.gave", target.dimension(), target.x(), target.y(), target.z()), false);
		return 1;
	}

	/** /way set <измерение> <pos> */
	private static int setWayBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();

		WayTarget target = readTarget(context);
		if (target == null) {
			return 0;
		}
		BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
		if (source.getLevel().getBlockEntity(pos) instanceof WayBlockEntity way) {
			way.setTarget(target);
			source.sendSuccess(() -> Component.translatable(
					"command.waymod.set", target.dimension(), target.x(), target.y(), target.z()), false);
			return 1;
		}
		source.sendFailure(Component.translatable("command.waymod.not_way_block"));
		return 0;
	}

	/** /way spawner <моб> — выдаёт крафтовый спавнер */
	private static int giveSpawner(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		return giveMobBlock(context, ModItems.CRAFTABLE_SPAWNER_ITEM, "command.waymod.spawner_give");
	}

	/** /way spawnerpro <моб> — выдаёт «Спавнер 2000 Pro» с мобом */
	private static int giveProSpawner(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		return giveMobBlock(context, ModItems.PRO_SPAWNER_ITEM, "command.waymod.spawner_pro_give");
	}

	/** Общая выдача блока-спавнера с мобом: /way spawner* <моб> */
	private static int giveMobBlock(CommandContext<CommandSourceStack> context, Item item, String messageKey) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayerOrException();

		String mobInput = context.getArgument("mob", String.class).toLowerCase(Locale.ROOT);
		if (mobInput.contains(":")) {
			// "minecraft:pig" или id из другого мода — проверяем, что строка вообще парсится
			try {
				Identifier.parse(mobInput);
			} catch (RuntimeException invalid) {
				source.sendFailure(Component.translatable("command.waymod.bad_mob", mobInput));
				return 0;
			}
		} else {
			mobInput = "minecraft:" + mobInput;
		}

		// Проверяем, что такой моб есть
		if (EntityType.byString(mobInput).isEmpty()) {
			source.sendFailure(Component.translatable("command.waymod.bad_mob", mobInput));
			return 0;
		}

		// Лямбда требует effectively final
		final String mobId = mobInput;
		ItemStack stack = new ItemStack(item);
		stack.set(ModComponents.MOB, mobId);
		giveItem(player, stack);
		source.sendSuccess(() -> Component.translatable(messageKey, mobId), false);
		return 1;
	}

	/** /way link <pos> — привязывает «прицельный» передатчик к контейнеру на позиции */
	private static int linkTransmitter(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayerOrException();

		WayTarget transmitterAt = player.getAttached(ModAttachments.LINKED_TRANSMITTER);
		if (transmitterAt == null) {
			source.sendFailure(Component.translatable("command.waymod.link_no_transmitter"));
			return 0;
		}
		if (!source.getLevel().dimension().identifier().toString().equals(transmitterAt.dimension())) {
			source.sendFailure(Component.translatable("command.waymod.link_wrong_dim"));
			return 0;
		}
		BlockPos transmitterPos = new BlockPos(transmitterAt.x(), transmitterAt.y(), transmitterAt.z());
		if (source.getLevel().getBlockEntity(transmitterPos) instanceof TransmitterBlockEntity transmitter) {
			BlockPos target = BlockPosArgument.getBlockPos(context, "pos");
			transmitter.setTarget(source.getLevel(), target, source.getLevel().dimension().identifier().toString());
			source.sendSuccess(() -> Component.translatable(
					"command.waymod.linked_ok", target.getX(), target.getY(), target.getZ()), false);
			player.removeAttached(ModAttachments.LINKED_TRANSMITTER);
			return 1;
		}
		source.sendFailure(Component.translatable("command.waymod.link_no_transmitter"));
		return 0;
	}

	/** Разбирает измерение + x/y/z; при ошибке отправляет сообщение и возвращает null. */
	private static WayTarget readTarget(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		String dimensionInput = context.getArgument("dimension", String.class);
		String dimensionId = WayDims.tryResolve(dimensionInput);
		if (dimensionId == null || !WayDims.exists(source.getServer(), dimensionId)) {
			source.sendFailure(Component.translatable("command.waymod.bad_dimension", dimensionInput));
			return null;
		}
		return new WayTarget(
				dimensionId,
				IntegerArgumentType.getInteger(context, "x"),
				IntegerArgumentType.getInteger(context, "y"),
				IntegerArgumentType.getInteger(context, "z")
		);
	}

	/** Выдача предмета в инвентарь; если инвентарь полный — выкинуть в мир у ног игрока. */
	private static void giveItem(ServerPlayer player, ItemStack stack) {
		// == ЗАМЕНИТЕ-ЕСЛИ-ОШИБКА ==
		if (!player.addItem(stack)) {
			player.drop(stack, false);
		}
	}
}
