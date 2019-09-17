package com.mrcrayfish.furniture.block;

import com.mrcrayfish.furniture.common.mail.PostOffice;
import com.mrcrayfish.furniture.tileentity.MailBoxTileEntity;
import com.mrcrayfish.furniture.util.TileEntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Author: MrCrayfish
 */
public class MailBoxBlock extends FurnitureHorizontalWaterloggedBlock
{
    public MailBoxBlock(Properties properties)
    {
        super(properties);
        this.setDefaultState(this.getStateContainer().getBaseState().with(DIRECTION, Direction.NORTH).with(WATERLOGGED, false));
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack)
    {
        if(entity instanceof ServerPlayerEntity)
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof MailBoxTileEntity)
            {
                MailBoxTileEntity mailBox = (MailBoxTileEntity) tileEntity;
                mailBox.setId(UUID.randomUUID());
                mailBox.setOwner((ServerPlayerEntity) entity);
                PostOffice.registerMailBox((ServerPlayerEntity) entity, mailBox.getId(), "Mail Box", pos);
                TileEntityUtil.sendUpdatePacket(tileEntity);
            }
        }
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if(!world.isRemote)
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof MailBoxTileEntity)
            {
                MailBoxTileEntity mailBox = (MailBoxTileEntity) tileEntity;
                if(mailBox.getId() != null && mailBox.getOwnerId() != null)
                {
                    PostOffice.unregisterMailBox(mailBox.getOwnerId(), mailBox.getId());
                }
            }
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult result)
    {
        if(!world.isRemote())
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof MailBoxTileEntity)
            {
                MailBoxTileEntity mailBox = (MailBoxTileEntity) tileEntity;
                if(playerEntity.getUniqueID().equals(mailBox.getOwnerId()))
                {
                    if(!playerEntity.getName().getString().equals(mailBox.getOwnerName()))
                    {
                        mailBox.setOwnerName(playerEntity.getName().getString());
                        TileEntityUtil.sendUpdatePacket(tileEntity);
                    }
                }
                playerEntity.openContainer((INamedContainerProvider) tileEntity);
            }
        }
        return true;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new MailBoxTileEntity();
    }
}
