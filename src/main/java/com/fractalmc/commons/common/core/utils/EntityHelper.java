package com.fractalmc.commons.common.core.utils;

import com.fractalmc.commons.FractalCommons;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityHelper
{
    public static final UUID UUID_DUMMY = UUID.fromString("DEADBEEF-DEAD-BEEF-DEAD-DEADBEEFD00D");

    public static HashMap<String, GameProfile> gameProfileCache = new HashMap<>();
    public static PlayerProfileCache profileCache;
    public static MinecraftSessionService sessionService;

    private static GameProfile dummyGameProfile = new GameProfile(UUID_DUMMY, "ForgeDev");

    @SideOnly(Side.CLIENT)
    public static void injectMinecraftPlayerGameProfile()
    {
        gameProfileCache.put(Minecraft.getMinecraft().getSession().getUsername(), Minecraft.getMinecraft().getSession().getProfile());
    }

    public static final Map<UUID, BossInfoClient> BOSS_INFO_STORE = Maps.newLinkedHashMap(); //These aren't even really necessary anymore, Clientside boss data doesn't store boss info and stuff like that, server does tracking.

    public static int countInInventory(IInventory inventory, Item item, int damage)
    {
        int totalCount = 0;

        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack itemStack = inventory.getStackInSlot(i);

            if (!itemStack.isEmpty() && itemStack.getItem() == item && (damage <= -1 || itemStack.getMetadata() == damage))
            {
                totalCount += itemStack.getCount();
            }
        }

        return totalCount;
    }

    public static void playSoundAtEntity(Entity entity, SoundEvent soundEvent, SoundCategory soundCat, float volume, float pitch)
    {
        entity.getEntityWorld().playSound(entity.getEntityWorld().isRemote ? FractalCommons.proxy.getMcPlayer() : null, entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ, soundEvent, soundCat, volume, pitch);
    }

    public static Vec3d getEntityPositionEyes(Entity entity, float partialTicks)
    {
        if (partialTicks == 1.0F)
        {
            return new Vec3d(entity.posX, entity.posY + (double)entity.getEyeHeight(), entity.posZ);
        }
        else
        {
            double xCoord = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
            double yCoord = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)entity.getEyeHeight();
            double zCoord = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;

            return new Vec3d(xCoord, yCoord, zCoord);
        }
    }

    public static float interpolateRotation(float prevRotation, float nextRotation, float partialTick)
    {
        float f;

        for (f = nextRotation - prevRotation; f < -180.0F; f += 360.0F)
        {

        }

        while (f >= 180.0F)
        {
            f -= 360.0F;
        }

        return prevRotation + partialTick * f;
    }

    public static float interpolateValues(float prevVal, float nextVal, float partialTick)
    {
        return prevVal + partialTick * (nextVal - prevVal);
    }

    public static void setVelocity(Entity entity, double motX, double motY, double motZ)
    {
        entity.motionX = motX;
        entity.motionY = motY;
        entity.motionZ = motZ;
    }

    public static RayTraceResult getEntityLook(Entity entity, double d)
    {
        return getEntityLook(entity, d, false);
    }

    public static RayTraceResult getEntityLook(Entity entity, double d, boolean ignoreEntities) //goes through liquid
    {
        return getEntityLook(entity, d, ignoreEntities, false, true, 1.0F);
    }

    public static RayTraceResult getEntityLook(Entity entity, double dist, boolean ignoreEntities, boolean ignoreTransparentBlocks, boolean ignoreLiquid, float renderTick)
    {
        Vec3d entityPositionEyes = getEntityPositionEyes(entity, renderTick);
        Vec3d entityLook = entity.getLook(renderTick);
        Vec3d vec3d = entityPositionEyes.add(new Vec3d(entityLook.x * dist, entityLook.y * dist, entityLook.z * dist));

        RayTraceResult rayTrace = rayTraceBlocks(entity.getEntityWorld(), dist, entityPositionEyes, vec3d, !ignoreLiquid, ignoreTransparentBlocks, false, true);

        if (!ignoreEntities)
        {
            double distance = dist;

            if (rayTrace != null)
            {
                distance = rayTrace.hitVec.distanceTo(entityPositionEyes);
            }

            Entity entityTrace = null;
            Vec3d vec3 = null;

            float f = 1.0F;

            List<Entity> list = entity.getEntityWorld().getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().expand(entityLook.x * dist, entityLook.y * dist, entityLook.z * dist).grow((double)f), Predicates.and(EntitySelectors.NOT_SPECTATING, entity1 -> entity.canBeCollidedWith()));

            double distance2 = distance;

            for (int j = 0; j < list.size(); ++j)
            {
                Entity entity1 = list.get(j);

                float collisionBorderSize = entity1.getCollisionBorderSize();

                AxisAlignedBB boundingBox = entity1.getEntityBoundingBox().grow((double) collisionBorderSize);
                RayTraceResult movingObjectPos = boundingBox.calculateIntercept(entityPositionEyes, vec3d);

                if (boundingBox.contains(entityPositionEyes))
                {
                    if (distance2 >= 0.0D)
                    {
                        entityTrace = entity1;
                        vec3 = movingObjectPos == null ? entityPositionEyes : movingObjectPos.hitVec;
                        distance2 = 0.0D;
                    }
                }
                else if (movingObjectPos != null)
                {
                    double distance3 = entityPositionEyes.distanceTo(movingObjectPos.hitVec);

                    if (distance3 < distance2 || distance2 == 0.0D)
                    {
                        if (entity1 == entity.getRidingEntity() && !entity.canRiderInteract())
                        {
                            if (distance2 == 0.0D)
                            {
                                entityTrace = entity1;
                                vec3 = movingObjectPos.hitVec;
                            }
                        }
                        else
                        {
                            entityTrace = entity1;
                            vec3 = movingObjectPos.hitVec;
                            distance2 = distance3;
                        }
                    }
                }
            }

            if (entityTrace != null && (distance2 < distance || rayTrace == null))
            {
                rayTrace = new RayTraceResult(entityTrace, vec3);
            }
        }

        return rayTrace;
    }

    public static RayTraceResult rayTraceBlocks(World world, double dist, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreTransparentBlocks, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock)
    {
        if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z))
        {
            if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z))
            {
                int i = MathHelper.floor(vec32.x);
                int j = MathHelper.floor(vec32.y);
                int k = MathHelper.floor(vec32.z);
                int l = MathHelper.floor(vec31.x);
                int i1 = MathHelper.floor(vec31.y);
                int j1 = MathHelper.floor(vec31.z);

                BlockPos blockPos = new BlockPos(l, i1, j1);
                IBlockState blockState = world.getBlockState(blockPos);
                Block block = blockState.getBlock();

                if ((!ignoreBlockWithoutBoundingBox || blockState.getCollisionBoundingBox(world, blockPos) != Block.NULL_AABB) && block.canCollideCheck(blockState, stopOnLiquid) && !(ignoreTransparentBlocks && isTransparent(block, blockState, world, blockPos)))
                {
                    RayTraceResult mop = blockState.collisionRayTrace(world, blockPos, vec31, vec32);

                    if (mop != null)
                    {
                        return mop;
                    }
                }

                RayTraceResult movingObjectPosition = null;
                int k1 = (int)Math.ceil(dist + 1) * 2;

                while (k1-- >= 0)
                {
                    if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z))
                    {
                        return null;
                    }

                    if (l == i && i1 == j && j1 == k)
                    {
                        return returnLastUncollidableBlock ? movingObjectPosition : null;
                    }

                    boolean flag = true;
                    boolean flag1 = true;
                    boolean flag2 = true;

                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (j > i1)
                    {
                        d1 = (double)i1 + 1.0D;
                    }
                    else if (j < i1)
                    {
                        d1 = (double)i1 + 0.0D;
                    }
                    else
                    {
                        flag = false;
                    }

                    if (k > j1)
                    {
                        d2 = (double)j1 + 1.0D;
                    }
                    else if (k < j1)
                    {
                        d2 = (double)j1 + 0.0D;
                    }
                    else
                    {
                        flag1 = false;
                    }

                    if (i > l)
                    {
                        d0 = (double)l + 1.0D;
                    }
                    else if (i < l)
                    {
                        d0 = (double)l + 0.0D;
                    }
                    else
                    {
                        flag2 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;

                    double d6 = vec32.x - vec31.x;
                    double d7 = vec32.y - vec31.y;
                    double d8 = vec32.z - vec31.z;

                    if (flag2)
                    {
                        d3 = (d0 - vec31.x) / d6;
                    }

                    if (flag)
                    {
                        d4 = (d1 - vec31.y) / d7;
                    }

                    if (flag1)
                    {
                        d5 = (d2 - vec31.z) / d8;
                    }

                    if (d3 == -0.0D)
                    {
                        d3 = -1.0E-4D;
                    }

                    if (d4 == -0.0D)
                    {
                        d4 = -1.0E-4D;
                    }

                    if (d5 == -0.0D)
                    {
                        d5 = -1.0E-4D;
                    }

                    EnumFacing facing;

                    if (d3 < d4 && d3 < d5)
                    {
                        facing = i > l ? EnumFacing.WEST : EnumFacing.EAST;

                        vec31 = new Vec3d(d0, vec31.y + d7 * d3, vec31.z + d8 * d3);
                    }
                    else if (d4 < d5)
                    {
                        facing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;

                        vec31 = new Vec3d(vec31.x + d6 * d4, d1, vec31.z + d8 * d4);
                    }
                    else
                    {
                        facing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;

                        vec31 = new Vec3d(vec31.x + d6 * d5, vec31.y + d7 * d5, d2);
                    }

                    l = MathHelper.floor(vec31.x) - (facing == EnumFacing.EAST ? 1 : 0);
                    i1 = MathHelper.floor(vec31.y) - (facing == EnumFacing.UP ? 1 : 0);
                    j1 = MathHelper.floor(vec31.z) - (facing == EnumFacing.SOUTH ? 1 : 0);

                    blockPos = new BlockPos(l, i1, j1);
                    IBlockState blockState1 = world.getBlockState(blockPos);
                    Block block1 = blockState1.getBlock();

                    if ((!ignoreBlockWithoutBoundingBox || blockState1.getMaterial() == Material.PORTAL || blockState1.getCollisionBoundingBox(world, blockPos) != Block.NULL_AABB) && !(ignoreTransparentBlocks && isTransparent(block1, blockState1, world, blockPos)))
                    {
                        if (block1.canCollideCheck(blockState1, stopOnLiquid))
                        {
                            RayTraceResult movingObjectPosition1 = blockState1.collisionRayTrace(world, blockPos, vec31, vec32);

                            if (movingObjectPosition1 != null)
                            {
                                return movingObjectPosition1;
                            }
                        }
                        else
                        {
                            movingObjectPosition = new RayTraceResult(RayTraceResult.Type.MISS, vec31, facing, blockPos);
                        }
                    }
                }

                return returnLastUncollidableBlock ? movingObjectPosition : null;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    public static void putEntityWithinAABB(Entity entity, AxisAlignedBB aabb)
    {
        if (entity.getEntityBoundingBox().maxX > aabb.maxX)
        {
            entity.posX += aabb.maxX - entity.getEntityBoundingBox().maxX;
        }
        if (entity.getEntityBoundingBox().minX < aabb.minX)
        {
            entity.posX += aabb.minX - entity.getEntityBoundingBox().minX;
        }
        if (entity.posY + entity.getEyeHeight() > aabb.maxY)
        {
            entity.posY += aabb.maxY - entity.posY - entity.getEyeHeight();
        }
        if (entity.posY < aabb.minY)
        {
            entity.posY += aabb.minY - entity.posY + 0.001D;
        }
        if (entity.getEntityBoundingBox().maxZ > aabb.maxZ)
        {
            entity.posZ += aabb.maxZ - entity.getEntityBoundingBox().maxZ;
        }
        if (entity.getEntityBoundingBox().minZ < aabb.minZ)
        {
            entity.posZ += aabb.minZ - entity.getEntityBoundingBox().minZ;
        }
    }

    public static double[] simulateMoveEntity(Entity entity, double x, double y, double z)
    {
        if (entity.noClip)
        {
            return new double[] { x, y, z };
        }
        else
        {
            double xCoord = x;
            double yCoord = y;
            double zCoord = z;

            boolean isSneakingPlayer = entity.onGround && entity.isSneaking() && entity instanceof EntityPlayer;

            if (isSneakingPlayer)
            {
                for (double d6 = 0.05D; x != 0.0D && entity.getEntityWorld().getCollisionBoxes(entity, entity.getEntityBoundingBox().offset(x, -1.0D, 0.0D)).isEmpty(); xCoord = x)
                {
                    if (x < 0.05D && x >= -0.05D)
                    {
                        x = 0.0D;
                    }
                    else if (x > 0.0D)
                    {
                        x -= 0.05D;
                    }
                    else
                    {
                        x += 0.05D;
                    }
                }

                for (; z != 0.0D && entity.getEntityWorld().getCollisionBoxes(entity, entity.getEntityBoundingBox().offset(0.0D, -1.0D, z)).isEmpty(); zCoord = z)
                {
                    if (z < 0.05D && z >= -0.05D)
                    {
                        z = 0.0D;
                    }
                    else if (z > 0.0D)
                    {
                        z -= 0.05D;
                    }
                    else
                    {
                        z += 0.05D;
                    }
                }

                for (; x != 0.0D && z != 0.0D && entity.getEntityWorld().getCollisionBoxes(entity, entity.getEntityBoundingBox().offset(x, -1.0D, z)).isEmpty(); zCoord = z)
                {
                    if(x < 0.05D && x >= -0.05D)
                    {
                        x = 0.0D;
                    }
                    else if(x > 0.0D)
                    {
                        x -= 0.05D;
                    }
                    else
                    {
                        x += 0.05D;
                    }

                    xCoord = x;

                    if(z < 0.05D && z >= -0.05D)
                    {
                        z = 0.0D;
                    }
                    else if(z > 0.0D)
                    {
                        z -= 0.05D;
                    }
                    else
                    {
                        z += 0.05D;
                    }
                }
            }

            List<AxisAlignedBB> collisionBoxes = entity.getEntityWorld().getCollisionBoxes(entity, entity.getEntityBoundingBox().expand(x, y, z));
            AxisAlignedBB boundingBox = entity.getEntityBoundingBox();

            int i = 0;

            for (int j = collisionBoxes.size(); i < j; ++i)
            {
                y = collisionBoxes.get(i).calculateYOffset(entity.getEntityBoundingBox(), y);
            }

            entity.setEntityBoundingBox(entity.getEntityBoundingBox().offset(0.0D, y, 0.0D));

            int j1 = 0;

            for (int k = collisionBoxes.size(); j1 < k; ++j1)
            {
                x = collisionBoxes.get(j1).calculateXOffset(entity.getEntityBoundingBox(), x);
            }

            entity.setEntityBoundingBox(entity.getEntityBoundingBox().offset(x, 0.0D, 0.0D));

            j1 = 0;

            for (int k4 = collisionBoxes.size(); j1 < k4; ++j1)
            {
                z = collisionBoxes.get(j1).calculateZOffset(entity.getEntityBoundingBox(), z);
            }

            entity.setEntityBoundingBox(boundingBox);

            return new double[] { x, y, z };
        }
    }

    public static AxisAlignedBB rotateAABB(EnumFacing.Axis axis, AxisAlignedBB aabb, double degree, double originX, double originY, double originZ)
    {
        double rads = Math.toRadians(degree);

        if (axis == EnumFacing.Axis.X)
        {
            double oriZ = aabb.minZ - originZ;
            double oriY = aabb.minY - originY;

            double z1 = oriZ * Math.cos(rads) + oriY * Math.sin(rads);
            double y1 = -oriZ * Math.sin(rads) + oriY * Math.cos(rads);

            oriZ = aabb.maxZ - originZ;
            oriY = aabb.maxY - originY;

            double z2 = oriZ * Math.cos(rads) + oriY * Math.sin(rads);
            double y2 = -oriZ * Math.sin(rads) + oriY * Math.cos(rads);

            return new AxisAlignedBB(aabb.minX, Math.min(y1, y2) + originY, Math.min(z1, z2) + originZ, aabb.maxX, Math.max(y1, y2) + originY, Math.max(z1, z2) + originZ);
        }
        else if (axis == EnumFacing.Axis.Y)
        {
            double oriX = aabb.minX - originX;
            double oriZ = aabb.minZ - originZ;

            double x1 = oriX * Math.cos(rads) + oriZ * Math.sin(rads);
            double z1 = -oriX * Math.sin(rads) + oriZ * Math.cos(rads);

            oriX = aabb.maxX - originX;
            oriZ = aabb.maxZ - originZ;

            double x2 = oriX * Math.cos(rads) + oriZ * Math.sin(rads);
            double z2 = -oriX * Math.sin(rads) + oriZ * Math.cos(rads);

            return new AxisAlignedBB(Math.min(x1, x2) + originX, aabb.minY, Math.min(z1, z2) + originZ, Math.max(x1, x2) + originX, aabb.maxY, Math.max(z1, z2) + originZ);
        }
        else if (axis == EnumFacing.Axis.Z)
        {
            double oriX = aabb.minX - originX;
            double oriY = aabb.minY - originY;

            double x1 = oriX * Math.cos(rads) + oriY * Math.sin(rads);
            double y1 = -oriX * Math.sin(rads) + oriY * Math.cos(rads);

            oriX = aabb.maxX - originX;
            oriY = aabb.maxY - originY;

            double x2 = oriX * Math.cos(rads) + oriY * Math.sin(rads);
            double y2 = -oriX * Math.sin(rads) + oriY * Math.cos(rads);

            return new AxisAlignedBB(Math.min(x1, x2) + originX, Math.min(y1, y2) + originY, aabb.minZ, Math.max(x1, x2) + originX, Math.max(y1, y2) + originY, aabb.maxZ);
        }

        return aabb;
    }

    public static boolean isTransparent(Block block, IBlockState state, World world, BlockPos pos)
    {
        return block.getLightOpacity(state, world, pos) != 0xff;
    }
}