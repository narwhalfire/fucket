package narwhalfire.fucket.item;

import narwhalfire.fucket.Fucket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * The Ocean Fucket: A fucket that drains/fills up the entire body of water/area the clicked block is connected to.
 */
public class ItemOceanFucket extends FucketBase {

    private int capacity = 1024000;
    private int count = 0;


    public ItemOceanFucket() {
        this.setRegistryName(Fucket.MOD_ID, "ocean_fucket");
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand handIn) {

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

            if (worldIn.getBlockState(target.getBlockPos()).getBlock().equals(Blocks.WATER)) {

                return ActionResult.newResult(EnumActionResult.PASS, itemStack);

            } else {

                NBTTagCompound compound = itemStackTagCompund.getCompoundTag("FucketContained");
                count = compound.getInteger("Count");

                boolean dirty = placeWater(worldIn, target.getBlockPos(), EnumOceanFucketModes.BELOW.facings);

                if (dirty) {

                    if (count == 0) {
                        itemStackTagCompund.removeTag("FucketContained");
                    } else {
                        compound.setInteger("Count", count);
                        itemStackTagCompund.setTag("FucketContained", compound);
                    }

                    itemStack.setTagCompound(itemStackTagCompund);
                    return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);

                } else {

                    return ActionResult.newResult(EnumActionResult.FAIL, itemStack);

                }

            }

        } else {

            if (worldIn.getBlockState(target.getBlockPos()).getBlock().equals(Blocks.WATER)) {

                count = 0;
                boolean dirty = collectWater(worldIn, target.getBlockPos(), EnumOceanFucketModes.BELOW.facings);

                if (dirty) {

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

    private boolean collectWater(@Nonnull World world, @Nonnull BlockPos pos, EnumFacing... facings) {

        if (!world.getBlockState(pos).getBlock().equals(Blocks.WATER)) {

            return false;

        } else {

            boolean bool = false;
            boolean tmp;

            for (int i = 0; i < facings.length; i++) {

                EnumFacing[] facings_ = new EnumFacing[facings.length-1];

                int o = 0;
                for (int j = 0; j < facings_.length; j++) {

                    o += (facings[j+o] == facings[i].getOpposite()) ? 1 : 0;

                    facings_[j] = facings[j+o];

                }

                tmp = collectWater(world, pos.offset(facings[i]), facings_);
                bool = bool || tmp;

            }

            world.setBlockToAir(pos);
            count++;

            return bool;

        }

    }

    private boolean placeWater(@Nonnull World world, @Nonnull BlockPos pos, EnumFacing... facings) {

        if (!world.mayPlace(world.getBlockState(pos).getBlock(), pos, true, EnumFacing.UP, null)) {

            return false;

        } else {

            boolean bool = false;
            boolean tmp;

            for (int i = 0; i < facings.length; i++) {

                EnumFacing[] facings_ = new EnumFacing[facings.length-1];

                int o = 0;
                for (int j = 0; j < facings_.length; j++) {

                    o += (facings[j+o] == facings[i].getOpposite()) ? 1 : 0;

                    facings_[j] = facings[j+o];

                }

                tmp = placeWater(world, pos.offset(facings[i]), facings_);
                bool = bool || tmp;

            }

            world.setBlockState(pos, Blocks.WATER.getDefaultState());
            count--;

            return bool;

        }

    }

    public static enum EnumOceanFucketModes {

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
