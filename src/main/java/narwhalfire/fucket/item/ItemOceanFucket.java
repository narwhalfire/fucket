package narwhalfire.fucket.item;

import narwhalfire.fucket.Fucket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * The Ocean Fucket: A fucket that drains/fills up the entire body of water/area the clicked block is connected to.
 */
public class ItemOceanFucket extends FucketBase {

    private int capacity = 1024000;
    private final int MAX_RADIUS = 128;


    public ItemOceanFucket() {
        this.setRegistryName(Fucket.MOD_ID, "ocean_fucket");
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand handIn) {

        Fluid fluid = FluidRegistry.WATER;

        ItemStack itemStack = playerIn.getHeldItem(handIn);

        RayTraceResult target = this.rayTrace(worldIn, playerIn, true);
        if (target == null) {
            return ActionResult.newResult(EnumActionResult.PASS, itemStack);
        }

        NBTTagCompound itemStackTagCompund = itemStack.getTagCompound();
        if (itemStackTagCompund == null) {
            itemStackTagCompund = new NBTTagCompound();
        }

        if (itemStackTagCompund.hasKey("FucketContained", 10)) {
            // there is stuff in the fucket

            if (isFluidAtPosition(worldIn, target.getBlockPos(), fluid)) {
                // we already have a fluid

                return ActionResult.newResult(EnumActionResult.PASS, itemStack);

            } else {
                // now we place the fluid

                NBTTagCompound compound = itemStackTagCompund.getCompoundTag("FucketContained");
                int count = compound.getInteger("Count");

                count = placeFluid(worldIn, target.getBlockPos(), fluid, count, EnumOceanFucketModes.BELOW.facings);

                if (count == 0) {
                    itemStackTagCompund.removeTag("FucketContained");
                } else {
                    compound.setInteger("Count", count);
                    itemStackTagCompund.setTag("FucketContained", compound);
                }

                itemStack.setTagCompound(itemStackTagCompund);

                return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);

            }

        } else {
            // the fucket is empty

            if (isFluidAtPosition(worldIn, target.getBlockPos(), fluid)) {

                int count = collectFluid(worldIn, target.getBlockPos(), fluid, EnumOceanFucketModes.BELOW.facings);

                if (count != 0) {

                    NBTTagCompound compound = new NBTTagCompound();
                    compound.setInteger("Count", count);
                    itemStackTagCompund.setTag("FucketContained", compound);
                    itemStack.setTagCompound(itemStackTagCompund);
                    return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);

                } else {

                    return ActionResult.newResult(EnumActionResult.FAIL, itemStack);

                }


            } else {

                return ActionResult.newResult(EnumActionResult.PASS, itemStack);

            }

        }

    }

    private int collectFluid(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Fluid fluid, EnumFacing... facings) {

        int count = 0;

        BlockPos seed = pos;

        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(seed);

        while (!queue.isEmpty()) {

            seed = queue.remove();

            if (pos.distanceSq(seed) > this.MAX_RADIUS) {
                continue;
            }

            for (int i = 0; i < facings.length; i++) {

                BlockPos seedOffset = seed.offset(facings[i]);

                if (isFluidAtPosition(world, seedOffset, fluid)) {

                    world.setBlockToAir(seedOffset);
                    count++;
                    queue.add(seedOffset);

                }

                if (count >= this.capacity) {
                    break;
                }

            }

            if (count == this.capacity) {
                break;
            } else if (count > this.capacity) {
                // uh oh
                break;
            }

        }

        return count;

    }

    private int placeFluid(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Fluid fluid, int count, EnumFacing... facings) {

        BlockPos seed = pos;

        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(seed);

        while (!queue.isEmpty()) {

            seed = queue.remove();

            if (pos.distanceSq(seed) > this.MAX_RADIUS) {
                continue;
            }

            for (int i = 0; i < facings.length; i++) {

                BlockPos seedOffset = seed.offset(facings[i]);

                if (world.mayPlace(fluid.getBlock(), seedOffset, false, EnumFacing.UP, null)) {

                    world.setBlockState(seedOffset, fluid.getBlock().getDefaultState());
                    count--;
                    queue.add(seedOffset);

                }

                if (count == 0) {
                    break;
                }

            }

            if (count == 0) {
                break;
            } else if (count < 0) {
                // uh oh
                break;
            }

        }

        return count;

    }

    private static boolean isFluidAtPosition(@Nonnull World world, BlockPos pos, Fluid fluid) {
        if (pos == null || fluid == null) {
            return false;
        } else {
            return world.getBlockState(pos).getBlock().equals(fluid.getBlock());
        }
    }

    public enum EnumOceanFucketModes {

        BODY(EnumFacing.VALUES),
        LAYER(EnumFacing.HORIZONTALS),
        ABOVE(EnumFacing.UP, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH),
        BELOW(EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH),
        COLUMN(EnumFacing.UP, EnumFacing.DOWN),
        LINE()
        ;

        EnumFacing[] facings;

        EnumOceanFucketModes(EnumFacing... facings) {
            this.facings = facings;
        }
    }
}
