package net.lerariemann.infinity.mixin;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.access.NetherPortalBlockAccess;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.loading.DimensionGrabber;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

import java.nio.file.Paths;
import java.util.Queue;
import java.util.Set;

import static net.minecraft.block.NetherPortalBlock.AXIS;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin implements NetherPortalBlockAccess {

	@Inject(at = @At("HEAD"), method = "onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
	private void injected(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo info) {
		if (!world.isClient() && entity instanceof ItemEntity) {
			ItemStack itemStack = ((ItemEntity)entity).getStack();
			if (itemStack.getItem() == Items.WRITTEN_BOOK || itemStack.getItem() == Items.WRITABLE_BOOK) {
				NbtCompound compound = itemStack.getNbt();
				if(compound != null){
					LogManager.getLogger().info(compound.asString());
					HashCode f = Hashing.sha256().hashString(compound.asString(), StandardCharsets.UTF_8);
					int i = f.asInt() & Integer.MAX_VALUE;
					MinecraftServer server = world.getServer();
					RandomProvider prov = ((MinecraftServerAccess)(server)).getDimensionProvider();
					if (prov.rule("seedDependentDimensions")) i = (int)(world.getServer().getWorld(World.OVERWORLD).getSeed()) ^ i;
					modifyPortal(world, pos, state, i);
					entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
					RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, new Identifier(InfinityMod.MOD_ID, "generated_" + i));
					if ((server.getWorld(key) == null) && (!((MinecraftServerAccess)(server)).hasToAdd(key))) {
						RandomDimension d = new RandomDimension(i, server);
						if (prov.rule("runtimeGenerationEnabled")) {
							DimensionGrabber grabber = new DimensionGrabber(server.getRegistryManager());
							DimensionOptions options = grabber.grab_all(Paths.get(d.storagePath), i);
							((MinecraftServerAccess) (server)).addWorld(key, options);
						}
					}
					world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1f, 1f);
				}
			}
		}
	}

	@Redirect(method="getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
	private boolean injected(BlockState neighborState, Block block) {
		return (neighborState.getBlock() instanceof NetherPortalBlock);
	}

	private void changeDim(World world, BlockPos pos, Direction.Axis axis, int i) {
		world.setBlockState(pos, ModBlocks.NEITHER_PORTAL.getDefaultState().with(AXIS, axis));
		BlockEntity blockEntity = world.getBlockEntity(pos);
		((NeitherPortalBlockEntity)blockEntity).setDimension(i);
	}

	@Override
	public void modifyPortal(World world, BlockPos pos, BlockState state, int i) {
		Set<BlockPos> set = Sets.newHashSet();
		Queue<BlockPos> queue = Queues.newArrayDeque();
		queue.add(pos);
		BlockPos blockPos;
		Direction.Axis axis = state.get(AXIS);
		while ((blockPos = queue.poll()) != null) {
			set.add(blockPos);
			BlockState blockState = world.getBlockState(blockPos);
			if (blockState.getBlock() instanceof NetherPortalBlock || blockState.getBlock() instanceof NeitherPortalBlock) {
				this.changeDim(world, blockPos, axis, i);
				BlockPos blockPos2 = blockPos.offset(Direction.UP);
				if (!set.contains(blockPos2))
					queue.add(blockPos2);
				blockPos2 = blockPos.offset(Direction.DOWN);
				if (!set.contains(blockPos2))
					queue.add(blockPos2);
				blockPos2 = blockPos.offset(Direction.NORTH);
				if (!set.contains(blockPos2))
					queue.add(blockPos2);
				blockPos2 = blockPos.offset(Direction.SOUTH);
				if (!set.contains(blockPos2))
					queue.add(blockPos2);
				blockPos2 = blockPos.offset(Direction.WEST);
				if (!set.contains(blockPos2))
					queue.add(blockPos2);
				blockPos2 = blockPos.offset(Direction.EAST);
				if (!set.contains(blockPos2))
					queue.add(blockPos2);
			}
		}
	}
}
