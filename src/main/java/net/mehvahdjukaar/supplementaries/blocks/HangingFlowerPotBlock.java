package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.blocks.tiles.HangingFlowerPotBlockTile;
import net.mehvahdjukaar.supplementaries.blocks.tiles.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.common.FlowerPotHelper;
import net.mehvahdjukaar.supplementaries.common.Resources;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HangingFlowerPotBlock extends Block{

    protected static final VoxelShape SHAPE = Block.makeCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);
    public static final BooleanProperty TILE = Resources.TILE; // is it tile only. used for rendering to store model
    public HangingFlowerPotBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(TILE, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return context.getFace() == Direction.DOWN?super.getStateForPlacement(context):null;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(TILE);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof HangingFlowerPotBlockTile) {
            HangingFlowerPotBlockTile te = ((HangingFlowerPotBlockTile)tileEntity);
            Block pot = te.pot.getBlock();
            if(pot instanceof FlowerPotBlock) {
                ItemStack itemstack = player.getHeldItem(handIn);
                Item item = itemstack.getItem();

                if (FlowerPotHelper.isEmptyPot(pot)) {
                    Block newPot = item instanceof BlockItem ? FlowerPotHelper.fullPots.get(pot).getOrDefault(item.getRegistryName(), Blocks.AIR.delegate).get() : Blocks.AIR;
                    if (newPot != Blocks.AIR) {
                        te.setHeldBlock(newPot.getDefaultState());
                        player.addStat(Stats.POT_FLOWER);
                        if (!player.abilities.isCreativeMode) {
                            itemstack.shrink(1);
                        }
                    }
                } else {
                    //drop item
                    ItemStack flowerItem = pot.getItem(worldIn, pos, state);
                    if (!flowerItem.equals(new ItemStack(this))) {
                        if (itemstack.isEmpty()) {
                            player.setHeldItem(handIn, flowerItem);
                        } else if (!player.addItemStackToInventory(flowerItem)) {
                            player.dropItem(flowerItem, false);
                        }
                    }
                    te.setHeldBlock(((FlowerPotBlock) pot).getEmptyPot().getDefaultState());
                }
                return ActionResultType.func_233537_a_(worldIn.isRemote);
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return state.get(TILE)?BlockRenderType.MODEL : BlockRenderType.INVISIBLE;
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new HangingFlowerPotBlockTile();
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof HangingFlowerPotBlockTile) {
            return new ItemStack(((HangingFlowerPotBlockTile) te).pot.getBlock());
        }
        return new ItemStack(Blocks.FLOWER_POT, 1);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof HangingFlowerPotBlockTile){
            Block b = ((HangingFlowerPotBlockTile) tileentity).pot.getBlock();
            if(b instanceof FlowerPotBlock)
                return Arrays.asList(new ItemStack(((FlowerPotBlock) b).getFlower()), new ItemStack(((FlowerPotBlock) b).getEmptyPot()));
        }

        return super.getDrops(state,builder);
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.UP && !this.isValidPosition(stateIn, worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return hasEnoughSolidSide(worldIn, pos.up(), Direction.DOWN);
    }
}