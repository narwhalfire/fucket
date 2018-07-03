package narwhalfire.fucket.item;

import narwhalfire.fucket.Fucket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * The Basic Fucket: Just a normal fucket capable of storing entities and whatnot.
 */
public class ItemBasicFucket extends FucketBase {


    public ItemBasicFucket() {
        this.setRegistryName(Fucket.MOD_ID, "basic_fucket");
    }

    /**
     * Called on equipped item right click
     */
    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand handIn) {

        ItemStack itemStack = playerIn.getHeldItem(handIn);

        RayTraceResult target = this.rayTraceFucket(worldIn, playerIn);
        if (target == null || target.typeOfHit == RayTraceResult.Type.MISS) {
            return ActionResult.newResult(EnumActionResult.PASS, itemStack);
        }

        NBTTagCompound itemStackTagCompound = itemStack.getTagCompound();
        if (itemStackTagCompound == null) {
            itemStackTagCompound = new NBTTagCompound();
        }

        if (itemStackTagCompound.hasKey("FucketContained", 10)) {

            NBTTagCompound compound = itemStackTagCompound.getCompoundTag("FucketContained");
            int count = compound.getInteger("Count");
            String entityID = compound.getString("EntityID");
            EntityEntry storedEntity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityID));

            if (storedEntity == null) {

                // uh oh spaghetti-o
                return ActionResult.newResult(EnumActionResult.FAIL, itemStack);

            } else if (target.typeOfHit == RayTraceResult.Type.ENTITY &&
                       target.entityHit.getClass().equals(storedEntity.getEntityClass())) {

                count++;
                Entity targetEntity = target.entityHit;
                targetEntity.setDropItemsWhenDead(false);
                if (!worldIn.isRemote) {
                    targetEntity.setDead();
                }

            } else if (target.typeOfHit == RayTraceResult.Type.BLOCK) {

                BlockPos spawnloc = target.getBlockPos().offset(target.sideHit);

                count--;
                Entity newEntity = storedEntity.newInstance(worldIn);
                newEntity.setLocationAndAngles(spawnloc.getX(), spawnloc.getY(), spawnloc.getZ(), 0,0);
                if (!worldIn.isRemote) {
                    worldIn.spawnEntity(newEntity);

                }

            } else {
                return ActionResult.newResult(EnumActionResult.FAIL, itemStack);
            }

            if (count == 0) {
                itemStackTagCompound.removeTag("FucketContained");
            } else {

                compound.setInteger("Count", count);
                compound.setString("EntityID", entityID);
                itemStackTagCompound.setTag("FucketContained", compound);

            }

            itemStack.setTagCompound(itemStackTagCompound);

            return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);

        } else {

            if (target.typeOfHit == RayTraceResult.Type.ENTITY) {

                Entity targetEntity = target.entityHit;
                EntityEntry targetEntityEntry = EntityRegistry.getEntry(targetEntity.getClass());
                ResourceLocation resourceLocation = ForgeRegistries.ENTITIES.getKey(targetEntityEntry);

                if (resourceLocation != null) {

                    targetEntity.setDropItemsWhenDead(false);
                    targetEntity.setDead();

                    String entityID = resourceLocation.toString();
                    NBTTagCompound compound = new NBTTagCompound();

                    compound.setInteger("Count", 1);
                    compound.setString("EntityID", entityID);
                    itemStackTagCompound.setTag("FucketContained", compound);

                    itemStack.setTagCompound(itemStackTagCompound);

                    return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);

                } else {
                    return ActionResult.newResult(EnumActionResult.FAIL, itemStack);
                }

            } else {
                return ActionResult.newResult(EnumActionResult.PASS, itemStack);
            }

        }

    }

    @Nullable
    protected RayTraceResult rayTraceFucket(World world, EntityPlayer player) {

        Entity entity = null;
        RayTraceResult result = null;
        double reachD = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();

        Vec3d looking = player.getLook(1.0F);
        Vec3d start = player.getPositionEyes(1.0F);
        Vec3d end = start.addVector(looking.x * reachD, looking.y * reachD, looking.z * reachD);
        Vec3d hit = null;

        List<Entity> entityList = world.getEntitiesWithinAABBExcludingEntity(player, player
                .getEntityBoundingBox().expand(looking.x * reachD, looking.y * reachD, looking.z * reachD));

        for (Entity entityC : entityList) {

            AxisAlignedBB axisAlignedBB = entityC.getEntityBoundingBox().grow((double) entityC.getCollisionBorderSize());
            RayTraceResult rayTraceResult = axisAlignedBB.calculateIntercept(start, end);

            if (axisAlignedBB.contains(start)) {

                entity = entityC;
                hit = rayTraceResult == null ? start : rayTraceResult.hitVec;

            } else if (rayTraceResult != null) {

                entity = entityC;
                hit = rayTraceResult.hitVec;

            }

        }

        if (entity != null) {

            result = new RayTraceResult(entity, hit);

        } else {

            result = rayTrace(world, player, false);

        }

        return result;
    }
}
