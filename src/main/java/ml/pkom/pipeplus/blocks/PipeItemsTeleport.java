package ml.pkom.pipeplus.blocks;

import alexiil.mc.mod.pipes.blocks.BlockPipeItem;
import alexiil.mc.mod.pipes.blocks.TilePipe;
import ml.pkom.pipeplus.PipePlus;
import ml.pkom.pipeplus.TeleportManager;
import ml.pkom.pipeplus.blockentities.IPipeTeleportTileEntity;
import ml.pkom.pipeplus.blockentities.PipeItemsTeleportEntity;
import ml.pkom.pipeplus.superClass.blocks.BlockPipeTeleport;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.util.UUID;

public class PipeItemsTeleport extends BlockPipeTeleport implements BlockPipeItem {
    public static FabricBlockSettings blockSettings = FabricBlockSettings.of(Material.SUPPORTED);
    public UUID latestOwner;

    static {
        blockSettings.strength(0.5F, 1.0F);
        blockSettings.sounds(BlockSoundGroup.GLASS);
        blockSettings.breakByTool(FabricToolTags.PICKAXES);
        blockSettings.breakByHand(true);
    }

    public PipeItemsTeleport(Settings settings) {
        super(settings);
    }


    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.onPlaced(world, pos, state, entity, stack);
        if (entity instanceof PlayerEntity) {
            UUID owner = entity.getUuid();
            if (PipeItemsTeleportEntity.getTilePipe(world, pos) == null) return;
            PipeItemsTeleportEntity pipe = PipeItemsTeleportEntity.getTilePipe(world, pos);
            TeleportManager.instance.remove(pipe, pipe.getFrequency());
            pipe.setOwnerNameAndUUID(owner);
            // なぜかサーバーまでもクライアント判定になるのでここで修正を入れる。
            PipePlus.log(Level.INFO, (world.isClient() ? "isClient" : "isServer"));
            pipe.setLocation(world, pos);
            TeleportManager.instance.add(pipe, pipe.getFrequency());
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (!PipeItemsTeleportEntity.tileMap.containsKey(PipePlus.pos2str(pos))) return;
        if (PipeItemsTeleportEntity.tileMap.get(PipePlus.pos2str(pos)).canPlayerModifyPipe(player)) {
            TeleportManager.instance.remove(PipeItemsTeleportEntity.tileMap.get(PipePlus.pos2str(pos)), PipeItemsTeleportEntity.tileMap.get(PipePlus.pos2str(pos)).frequency);
            PipeItemsTeleportEntity.tileMap.remove(PipePlus.pos2str(pos));
            return;
        }
        world.setBlockState(pos, state);
    }

    @Override
    public TilePipe createBlockEntity(BlockView view) {
        //PipeItemsTeleportEntity tileEntity = new PipeItemsTeleportEntity();
        //TeleportManager.instance.add(tileEntity, 0);
        return new PipeItemsTeleportEntity();
    }

    public static Settings getSettings() {
        return blockSettings;
    }

    public static PipeItemsTeleport newBlock() {
        return new PipeItemsTeleport(getSettings());
    }
}
