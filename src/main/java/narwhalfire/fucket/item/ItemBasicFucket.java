package narwhalfire.fucket.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;

/**
 * The Basic Fucket: Just a normal fucket capable of storing entities and whatnot.
 */
public class ItemBasicFucket extends FucketBase {


    /**
     * Called on equipped item right click
     */
    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand handIn) {

        ItemStack itemStack = playerIn.getHeldItem(handIn);

        RayTraceResult target = this.rayTrace(worldIn, playerIn, true);
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
                       target.entityHit.getClass().isInstance(storedEntity.getEntityClass())) {

                Entity targetEntity = target.entityHit;
                targetEntity.setDropItemsWhenDead(false);
                targetEntity.setDead();
                count++;

            } else if (target.typeOfHit == RayTraceResult.Type.BLOCK) {

                BlockPos spawnloc = target.getBlockPos().offset(target.sideHit);

                Entity newEntity = storedEntity.newInstance(worldIn);
                newEntity.posX = spawnloc.getX();
                newEntity.posY = spawnloc.getY();
                newEntity.posZ = spawnloc.getZ();
                worldIn.spawnEntity(newEntity);
                count--;

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
}
