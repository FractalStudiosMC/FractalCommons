package com.fractalmc.commons.common.entity;

import com.fractalmc.commons.FractalCommons;
import com.fractalmc.commons.api.events.BlockEntityEvent;
import com.fractalmc.commons.common.grab.GrabHandler;
import com.fractalmc.commons.common.packet.BlockEntityDataPackets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Set;

public class EntityBlock extends Entity
{
    private static final DataParameter<Float> ROT_YAW = EntityDataManager.createKey(EntityBlock.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> ROT_PITCH = EntityDataManager.createKey(EntityBlock.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> CAN_ROTATE = EntityDataManager.createKey(EntityBlock.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> BEHAVIOUR = EntityDataManager.createKey(EntityBlock.class, DataSerializers.VARINT);
    private static final DataParameter<BlockPos> ORIGIN = EntityDataManager.createKey(EntityBlock.class, DataSerializers.BLOCK_POS);

    public EntityLivingBase creator;

    public float rotYaw;
    public float rotPitch;
    public float prevRotYaw;
    public float prevRotPitch;

    public double prevMotionX;
    public double prevMotionY;
    public double prevMotionZ;

    public int timeOnGround;
    public int timeExisting;
    public int timeout;

    public boolean canDropItems;

    public boolean requestedSetupPacket;
    public boolean setup;

    public IBlockState[][][] blocks;
    public NBTTagCompound[][][] tileEntityNBTs;
    public TileEntityMobSpawner[][][] mobSpawners;

    @SideOnly(Side.CLIENT)
    public TileEntity[][][] renderingTileEntities;

    public static float maxRotFac = 25F;

    public EntityBlock(World world)
    {
        super(world);

        setSize(0.95F, 0.95F);

        preventEntitySpawning = true;
        isImmuneToFire = true;
        canDropItems = true;
        blocks = new IBlockState[][][] { new IBlockState[][] { new IBlockState[] { Blocks.AIR.getDefaultState() } } };
        tileEntityNBTs = new NBTTagCompound[1][1][1];
    }

    @Nullable
    public static EntityBlock createEntityBlock(World world, EntityLivingBase living, Set<BlockPos> poses)
    {
        return MinecraftForge.EVENT_BUS.post(new BlockEntityEvent.Pickup(world, living, poses)) || poses.isEmpty() ? null : new EntityBlock(world, living, poses);
    }

    private EntityBlock(World world, EntityLivingBase living, Set<BlockPos> poses)
    {
        this(world);

        creator = living;

        int lowX = Integer.MAX_VALUE;
        int lowY = Integer.MAX_VALUE;
        int lowZ = Integer.MAX_VALUE;

        int highX = Integer.MIN_VALUE;
        int highY = Integer.MIN_VALUE;
        int highZ = Integer.MIN_VALUE;

        for (BlockPos pos : poses)
        {
            if (pos.getX() < lowX)
            {
                lowX = pos.getX();
            }
            if (pos.getY() < lowY)
            {
                lowY = pos.getY();
            }
            if (pos.getZ() < lowZ)
            {
                lowZ = pos.getZ();
            }
            if (pos.getX() > highX)
            {
                highX = pos.getX();
            }
            if (pos.getY() > highY)
            {
                highY = pos.getY();
            }
            if (pos.getZ() > highZ)
            {
                highZ = pos.getZ();
            }
        }

        int countX = highX - lowX + 1;
        int countY = highY - lowY + 1;
        int countZ = highZ - lowZ + 1;

        blocks = new IBlockState[countX][countY][countZ];
        tileEntityNBTs = new NBTTagCompound[countX][countY][countZ];
        mobSpawners = new TileEntityMobSpawner[countX][countY][countZ];

        world.restoringBlockSnapshots = true;

        for (BlockPos pos : poses)
        {
            IBlockState state = world.getBlockState(pos);

            if (state.getBlock() != Blocks.AIR)
            {
                blocks[highX - pos.getX()][highY - pos.getY()][highZ - pos.getZ()] = state;

                TileEntity te = world.getTileEntity(pos);

                if (te != null && state.getBlock().hasTileEntity(state))
                {
                    world.setTileEntity(pos, state.getBlock().createTileEntity(world, state));

                    tileEntityNBTs[highX - pos.getX()][highY - pos.getY()][highZ - pos.getZ()] = new NBTTagCompound();

                    te.writeToNBT(tileEntityNBTs[highX - pos.getX()][highY - pos.getY()][highZ - pos.getZ()]);
                    te.invalidate();

                    TileEntity te1 = state.getBlock().createTileEntity(world, state);

                    if (te instanceof TileEntityMobSpawner && te1 instanceof TileEntityMobSpawner)
                    {
                        te1.readFromNBT(tileEntityNBTs[highX - pos.getX()][highY - pos.getY()][highZ - pos.getZ()]);

                        mobSpawners[highX - pos.getX()][highY - pos.getY()][highZ - pos.getZ()] = (TileEntityMobSpawner)te1;
                    }
                }
            }

            world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 2);
        }

        world.restoringBlockSnapshots = false;

        for (BlockPos pos : poses)
        {
            world.setBlockToAir(pos);
        }

        setSize();
        setLocationAndAngles(lowX + (countX / 2F), lowY + 0.025D, lowZ + (countZ / 2F), 0F, 0F);

        getDataManager().set(ORIGIN, new BlockPos(lowX, lowY, lowZ));
    }

    public void setSize()
    {
        setSize(Math.max(blocks.length, blocks[0][0].length) - 0.05F, blocks[0].length - 0.05F);
    }

    @Override
    protected void setSize(float width, float height)
    {
        float f = this.width;

        this.width = width;
        this.height = height;

        if (blocks == null)
        {
            setEntityBoundingBox(new AxisAlignedBB(this.getEntityBoundingBox().minX, this.getEntityBoundingBox().minY, this.getEntityBoundingBox().minZ, this.getEntityBoundingBox().minX + (double)this.width, this.getEntityBoundingBox().minY + (double)this.height, this.getEntityBoundingBox().minZ + (double)this.width));
        }
        else
        {
            setEntityBoundingBox(new AxisAlignedBB(this.getEntityBoundingBox().minX, this.getEntityBoundingBox().minY, this.getEntityBoundingBox().minZ, this.getEntityBoundingBox().minX + (double)(blocks.length - 0.05F), this.getEntityBoundingBox().minY + (double)(blocks[0].length - 0.05F), this.getEntityBoundingBox().minZ + (double)(blocks[0][0].length - 0.05F)));
        }

        if (this.width > f && !this.firstUpdate && !this.world.isRemote)
        {
            move(MoverType.SELF, (double)(f - this.width), 0.0D, (double)(f - this.width));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isInRangeToRenderDist(double distance)
    {
        double d = getEntityBoundingBox().getAverageEdgeLength() * 20.0D; // * 20D is the new renderDistanceWeight

        if (Double.isNaN(d))
        {
            d = 1.0D;
        }

        d = d * 64.0D * getRenderDistanceWeight();

        return distance < d * d;
    }

    @Override
    public void setPosition(double x, double y, double z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;

        if (blocks == null)
        {
            float halfWidth = this.width / 2.0F;
            float height = this.height;

            this.setEntityBoundingBox(new AxisAlignedBB(x - (double)halfWidth, y, z - (double)halfWidth, x + (double)halfWidth, y + (double)height, z + (double)halfWidth));
        }
        else
        {
            float fx = (blocks.length - 0.05F) / 2.0F;
            float fy = (blocks[0].length - 0.05F);
            float fz = (blocks[0][0].length - 0.05F) / 2.0F;

            setEntityBoundingBox(new AxisAlignedBB(x - (double)fx, y, z - (double)fz, x + (double)fx, y + (double)fy, z + (double)fz));
        }
    }

    @Override
    protected void entityInit()
    {
        getDataManager().register(ROT_YAW, rand.nextFloat() * (2F * maxRotFac) - maxRotFac); //rotFactor Yaw
        getDataManager().register(ROT_PITCH, rand.nextFloat() * (2F * maxRotFac) - maxRotFac); //rotFactor Pitch
        getDataManager().register(CAN_ROTATE, true); //canRotate
        getDataManager().register(BEHAVIOUR, 0); //behaviour
        getDataManager().register(ORIGIN, BlockPos.ORIGIN); //behaviour
    }

    public void setRotFacYaw(float f)
    {
        getDataManager().set(ROT_YAW, f);
    }

    public float getRotFacYaw()
    {
        return getDataManager().get(ROT_YAW);
    }

    public void setRotFacPitch(float f)
    {
        getDataManager().set(ROT_PITCH, f);
    }

    public float getRotFacPitch()
    {
        return getDataManager().get(ROT_PITCH);
    }

    public void setCanRotate(boolean flag)
    {
        getDataManager().set(CAN_ROTATE, flag);
    }

    public boolean getCanRotate()
    {
        return getDataManager().get(CAN_ROTATE);
    }

    public void setBehaviour(int i)
    {
        getDataManager().set(BEHAVIOUR, 1);
    }

    public int getBehaviour()
    {
        return getDataManager().get(BEHAVIOUR);
    }

    public BlockPos getOrigin()
    {
        return getDataManager().get(ORIGIN);
    }

    @Override
    public void onUpdate()
    {
        if (world.isRemote && !setup)
        {
            if (!requestedSetupPacket)
            {
                requestedSetupPacket = true;
                FractalCommons.channel.sendToServer(new BlockEntityDataPackets.PacketRequestBlockEntityData(this));
            }

            return;
        }

        timeExisting++;

        if (!world.isRemote && posY < -500D)
        {
            setDead();

            return;
        }

        noClip = timeExisting < timeout;

        prevRotYaw = rotYaw;
        prevRotPitch = rotPitch;
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        if(!GrabHandler.isGrabbed(this, FMLCommonHandler.instance().getEffectiveSide()))
        {
            rotYaw += getRotFacYaw();
            rotPitch += getRotFacPitch();
        }

        prevMotionX = motionX;
        prevMotionY = motionY;
        prevMotionZ = motionZ;

        motionY -= 0.06D;

        move(MoverType.SELF, motionX, motionY, motionZ);

        boolean setBlock = false;

        if (!GrabHandler.isGrabbed(this, FMLCommonHandler.instance().getEffectiveSide()))
        {
            if (onGround && ticksExisted > 2)
            {
                timeOnGround++;

                if (motionY == 0.0D)
                {
                    if (prevMotionY < -0.1D)
                    {
                        double minBounceFactor = Math.sqrt(100D / 75D);
                        float blockHardness = getAverageBlockHardness();
                        double bounceFactor = (blockHardness < minBounceFactor ? minBounceFactor : blockHardness);

                        motionY = prevMotionY * -(1D / (2 * bounceFactor));

                        setRotFacYaw(rand.nextFloat() * (2F * maxRotFac) - maxRotFac);
                        setRotFacPitch(rand.nextFloat() * (2F * maxRotFac) - maxRotFac);
                    }
                    if (timeOnGround > 3)
                    {
                        setRotFacYaw(getRotFacYaw() * 0.6F);
                        setRotFacPitch(getRotFacPitch() * 0.6F);
                    }

                    motionX *= 0.7D;
                    motionZ *= 0.7D;

                    if (Math.abs(prevMotionX) < 0.05D && Math.abs(prevMotionZ) < 0.05D && Math.abs(prevMotionY) < 0.05D && Math.abs(getRotFacYaw()) < 0.05D && Math.abs(getRotFacPitch()) < 0.05D)
                    {
                        setBlock = true;
                    }
                }
            }
            else
            {
                timeOnGround = 0;
            }

            if (motionX == 0D && prevMotionX != motionX)
            {
                double minBounceFactor = Math.sqrt(100D / 75D);
                float blockHardness = getAverageBlockHardness();
                double bounceFactor = 2D * (blockHardness < minBounceFactor ? minBounceFactor : blockHardness);

                motionX = prevMotionX * -(1D / (bounceFactor * bounceFactor));

                setRotFacYaw(rand.nextFloat() * (2F * maxRotFac) - maxRotFac);
                setRotFacPitch(rand.nextFloat() * (2F * maxRotFac) - maxRotFac);
            }

            if (motionZ == 0D && prevMotionZ != motionZ)
            {
                double minBounceFactor = Math.sqrt(100D / 75D);
                float blockHardness = getAverageBlockHardness();
                double bounceFactor = 2D * (blockHardness < minBounceFactor ? minBounceFactor : blockHardness);

                motionZ = prevMotionZ * -(1D / (bounceFactor * bounceFactor));

                setRotFacYaw(rand.nextFloat() * (2F * maxRotFac) - maxRotFac);
                setRotFacPitch(rand.nextFloat() * (2F * maxRotFac) - maxRotFac);
            }
        }
        else if (!world.isRemote)
        {
            for (int i = 0; i < mobSpawners.length; i++)
            {
                for (int j = mobSpawners[i].length - 1; j >= 0; j--)
                {
                    for (int k = 0; k < mobSpawners[i][j].length; k++)
                    {
                        if (mobSpawners[i][j][k] != null)
                        {
                            BlockPos pos = new BlockPos(posX - (((getEntityBoundingBox().maxX - getEntityBoundingBox().minX) + 0.05D) / 2F) + blocks.length - i - 0.5D, posY + blocks[i].length - j - 0.5D, posZ - (((getEntityBoundingBox().maxZ - getEntityBoundingBox().minZ) + 0.05D) / 2F) + blocks[i][j].length - k - 0.5D);
                            TileEntityMobSpawner spawner = mobSpawners[i][j][k];
                            spawner.setWorld(world);
                            spawner.setPos(pos);
                            //Update the spawner twice to double spawn rate.
                            spawner.update();
                            spawner.update();
                        }
                    }
                }
            }
        }

        if (!world.isRemote && (setBlock || timeExisting > (20 * 60 * 5)))
        {
            setDead();

            for (int i = 0; i < blocks.length; i++)
            {
                for (int j = blocks[i].length - 1; j >= 0; j--)
                {
                    for (int k = 0; k < blocks[i][j].length; k++)
                    {
                        if (blocks[i][j][k] != null)
                        {
                            BlockPos pos = new BlockPos(posX - (((getEntityBoundingBox().maxX - getEntityBoundingBox().minX) + 0.05D) / 2F) + blocks.length - i - 0.5D, posY + blocks[i].length - j - 0.5D, posZ - (((getEntityBoundingBox().maxZ - getEntityBoundingBox().minZ) + 0.05D) / 2F) + blocks[i][j].length - k - 0.5D);

                            if (MinecraftForge.EVENT_BUS.post(new BlockEntityEvent.Place(world, this, creator, blocks[i][j][k], pos)) || !world.setBlockState(pos, blocks[i][j][k], 2) && canDropItems)
                            {
                                blocks[i][j][k].getBlock().dropBlockAsItem(world, pos, blocks[i][j][k], 0);

                                if (tileEntityNBTs[i][j][k] != null && blocks[i][j][k].getBlock().hasTileEntity(blocks[i][j][k]))
                                {
                                    TileEntity te = blocks[i][j][k].getBlock().createTileEntity(world, blocks[i][j][k]);

                                    if (te instanceof IInventory)
                                    {
                                        te.readFromNBT(tileEntityNBTs[i][j][k]);
                                        handleIInventoryBreak((IInventory)te);
                                    }
                                    else if (te instanceof IItemHandler)
                                    {
                                        te.readFromNBT(tileEntityNBTs[i][j][k]);
                                        handleIItemHandlerBreak((IItemHandler)te);
                                    }
                                }
                            }
                            else if (tileEntityNBTs[i][j][k] != null && blocks[i][j][k].getBlock().hasTileEntity(blocks[i][j][k]))
                            {
                                TileEntity te = world.getTileEntity(pos);

                                if (te != null)
                                {
                                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                                    te.writeToNBT(nbttagcompound);
                                    Iterator iterator = tileEntityNBTs[i][j][k].getKeySet().iterator();

                                    while (iterator.hasNext())
                                    {
                                        String s = (String)iterator.next();
                                        NBTBase nbtbase = tileEntityNBTs[i][j][k].getTag(s);

                                        if (!s.equals("x") && !s.equals("y") && !s.equals("z"))
                                        {
                                            nbttagcompound.setTag(s, nbtbase.copy());
                                        }
                                    }

                                    te.readFromNBT(nbttagcompound);
                                    te.markDirty();
                                }
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < blocks.length; i++)
            {
                for (int j = blocks[i].length - 1; j >= 0; j--)
                {
                    for (int k = 0; k < blocks[i][j].length; k++)
                    {
                        if (blocks[i][j][k] != null)
                        {
                            BlockPos pos = new BlockPos(posX - (((getEntityBoundingBox().maxX - getEntityBoundingBox().minX) + 0.05D) / 2F) + blocks.length - i - 0.5D, posY + blocks[i].length - j - 0.5D, posZ - (((getEntityBoundingBox().maxZ - getEntityBoundingBox().minZ) + 0.05D) / 2F) + blocks[i][j].length - k - 0.5D);

                            world.notifyNeighborsRespectDebug(pos, Blocks.AIR, false);
                        }
                    }
                }
            }
        }

        motionX *= 0.95D;
        motionY *= 0.95D;
        motionZ *= 0.95D;
    }

    public float getAverageBlockHardness()
    {
        float hardness = 0.0F;
        int count = 0;

        for (int i = 0; i < blocks.length; i++)
        {
            for (int j = 0; j < blocks[i].length; j++)
            {
                for (int k = 0; k < blocks[i][j].length; k++)
                {
                    if (blocks[i][j][k] != null)
                    {
                        hardness += blocks[i][j][k].getBlock().blockHardness;

                        count++;
                    }
                }
            }
        }

        return hardness / (float)count;
    }

    public void handleIItemHandlerBreak(IItemHandler itemHandler)
    {
        if (itemHandler != null)
        {
            for (int i = 0; i < itemHandler.getSlots(); ++i)
            {
                int tries = 0;

                while (tries < 200)
                {
                    tries++;

                    ItemStack itemStack = itemHandler.extractItem(i, Short.MAX_VALUE, false);

                    if (itemStack.isEmpty() || itemStack.getCount() <= 0)
                    {
                        break;
                    }

                    float randOne = this.rand.nextFloat() * 0.8F + 0.1F;
                    float randTwo = this.rand.nextFloat() * 0.8F + 0.1F;
                    float randThree = this.rand.nextFloat() * 0.8F + 0.1F;

                    EntityItem entityItem = new EntityItem(world, (double)((float)posX + randOne), (double)((float)posY + randTwo), (double)((float)posZ + randThree), itemStack);

                    float motionConst = 0.05F;

                    entityItem.motionX = (double)((float)this.rand.nextGaussian() * motionConst);
                    entityItem.motionY = (double)((float)this.rand.nextGaussian() * motionConst + 0.2F);
                    entityItem.motionZ = (double)((float)this.rand.nextGaussian() * motionConst);

                    if (itemStack.hasTagCompound())
                    {
                        entityItem.getItem().setTagCompound(itemStack.getTagCompound().copy());
                    }

                    world.spawnEntity(entityItem);
                }
            }
        }
    }

    public void handleIInventoryBreak(IInventory inventory)
    {
        if (inventory != null)
        {
            for (int i = 0; i < inventory.getSizeInventory(); ++i)
            {
                ItemStack itemStack = inventory.getStackInSlot(i);

                if (!itemStack.isEmpty())
                {
                    float randX = this.rand.nextFloat() * 0.8F + 0.1F;
                    float randY = this.rand.nextFloat() * 0.8F + 0.1F;

                    EntityItem entityItem;

                    for (float randZ = this.rand.nextFloat() * 0.8F + 0.1F; itemStack.getCount() > 0; world.spawnEntity(entityItem))
                    {
                        int randInt = this.rand.nextInt(21) + 10;

                        if (randInt > itemStack.getCount())
                        {
                            randInt = itemStack.getCount();
                        }

                        itemStack.shrink(randInt);

                        entityItem = new EntityItem(world, (double)((float)posX + randX), (double)((float)posY + randY), (double)((float)posZ + randZ), new ItemStack(itemStack.getItem(), randInt, itemStack.getItemDamage()));

                        float motionConst = 0.05F;

                        entityItem.motionX = (double)((float)this.rand.nextGaussian() * motionConst);
                        entityItem.motionY = (double)((float)this.rand.nextGaussian() * motionConst + 0.2F);
                        entityItem.motionZ = (double)((float)this.rand.nextGaussian() * motionConst);

                        if (itemStack.hasTagCompound())
                        {
                            entityItem.getItem().setTagCompound(itemStack.getTagCompound().copy());
                        }
                    }
                }
            }
        }
    }

    public void shatter()
    {
        if (!(blocks.length == 1 && blocks[0].length == 1 && blocks[0][0].length == 1) && !isDead)
        {
            setDead();

            for (int i = 0; i < blocks.length; i++)
            {
                for (int j = 0; j < blocks[i].length; j++)
                {
                    for (int k = 0; k < blocks[i][j].length; k++)
                    {
                        if (blocks[i][j][k] != null)
                        {
                            EntityBlock block = new EntityBlock(world);

                            block.creator = this.creator;
                            block.blocks = new IBlockState[1][1][1];
                            block.tileEntityNBTs = new NBTTagCompound[1][1][1];
                            block.blocks[0][0][0] = blocks[i][j][k];
                            block.tileEntityNBTs[0][0][0] = tileEntityNBTs[i][j][k];

                            block.setSize();
                            block.setLocationAndAngles(posX - (width / 2F) + i, posY + j, posZ - (width / 2F) + k, 0F, 0F);
                            block.setCanRotate(block.getCanRotate());
                            block.setBehaviour(block.getBehaviour());

                            float randMag = 0.1F;

                            block.motionX = motionX + (rand.nextFloat() * 2F - 1F) * randMag;
                            block.motionY = motionY + (rand.nextFloat() * 2F - 1F) * randMag;
                            block.motionZ = motionZ + (rand.nextFloat() * 2F - 1F) * randMag;

                            world.spawnEntity(block);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return timeExisting > timeout && !isDead;
    }

    @Override
    public boolean canBePushed()
    {
        return timeExisting > timeout && !isDead;
    }

    @Override
    protected void dealFireDamage(int amount)
    {

    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entity)
    {
        return entity.getEntityBoundingBox();
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox()
    {
        return getEntityBoundingBox();
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag)
    {
        timeExisting = tag.getInteger("timeExisting");

        setCanRotate(tag.getBoolean("canRotate"));
        setBehaviour(tag.getInteger("behaviour"));

        if (tag.getInteger("lengthX") <= 0 || tag.getInteger("lengthY") <= 0 || tag.getInteger("lengthZ") <= 0)
        {
            blocks = new IBlockState[][][] { new IBlockState[][] { new IBlockState[] { Blocks.AIR.getDefaultState() } } };
            tileEntityNBTs = new NBTTagCompound[1][1][1];
        }
        else
        {
            blocks = new IBlockState[tag.getInteger("lengthX")][tag.getInteger("lengthY")][tag.getInteger("lengthZ")];
            tileEntityNBTs = new NBTTagCompound[tag.getInteger("lengthX")][tag.getInteger("lengthY")][tag.getInteger("lengthZ")];
        }

        for (int i = 0; i < blocks.length; i++)
        {
            for (int j = 0; j < blocks[i].length; j++)
            {
                for (int k = 0; k < blocks[i][j].length; k++)
                {
                    if (tag.hasKey("state_" + Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k)))
                    {
                        blocks[i][j][k] = Block.getBlockFromName(tag.getString("state_" + Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k))).getStateFromMeta(tag.getByte("meta_" + Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k)) & 255);

                        if (blocks[i][j][k] != null && tag.hasKey("tileEnt_" + Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k)))
                        {
                            tileEntityNBTs[i][j][k] = tag.getCompoundTag("tileEnt_" + Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k));
                        }
                    }
                }
            }
        }

        setSize();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag)
    {
        tag.setInteger("timeExisting", timeExisting);

        tag.setBoolean("canRotate", getCanRotate());
        tag.setInteger("behaviour", getBehaviour());

        tag.setInteger("lengthX", blocks.length);
        tag.setInteger("lengthY", blocks[0].length);
        tag.setInteger("lengthZ", blocks[0][0].length);

        for (int i = 0; i < blocks.length; i++)
        {
            for (int j = 0; j < blocks[i].length; j++)
            {
                for (int k = 0; k < blocks[i][j].length; k++)
                {
                    if (blocks[i][j][k] != null)
                    {
                        Block block = blocks[i][j][k].getBlock();
                        ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(block);

                        tag.setString("state_" + Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k), resourcelocation.toString());
                        tag.setByte("meta_" + Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k), (byte)block.getMetaFromState(blocks[i][j][k]));

                        if (tileEntityNBTs[i][j][k] != null)
                        {
                            tag.setTag("tileEnt_" + Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k), tileEntityNBTs[i][j][k]);
                        }
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean canRenderOnFire()
    {
        return false;
    }
}