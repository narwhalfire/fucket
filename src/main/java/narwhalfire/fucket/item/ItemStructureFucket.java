package narwhalfire.fucket.item;

import narwhalfire.fucket.Fucket;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * The Structure Fucket: An advanced fucket capable of storing predefined structures.
 * Structures include but are not limited to the following:
 * -trees
 * that's all I planned so far
 */
public class ItemStructureFucket extends FucketBase {


    public ItemStructureFucket() {
        this.setRegistryName(Fucket.MOD_ID, "structure_fucket");
    }

    /**
     * Called when the equipped item is right clicked.
     */
    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand handIn) {

        ItemStack itemStack = playerIn.getHeldItem(handIn);

        RayTraceResult target = this.rayTrace(worldIn, playerIn, false);
        if (target == null || target.typeOfHit == RayTraceResult.Type.MISS) {
            return ActionResult.newResult(EnumActionResult.PASS, itemStack);
        }

        NBTTagCompound itemStackTagCompound = itemStack.getTagCompound();
        if (itemStackTagCompound == null) {
            itemStackTagCompound = new NBTTagCompound();
        }

        if (itemStackTagCompound.hasKey("FucketContained", 10)) {

            NBTTagCompound compound = itemStackTagCompound.getCompoundTag("FucketContained");
            int[] sizeXYZ = compound.getIntArray("SizeXYZ");
            byte[] bytes = compound.getByteArray("StructureBytes");
            String structure = compound.getString("StructureType");
            String[] structureBlocks = compound.getString("StructureBlocks").split(";");

            int x = sizeXYZ[0];
            int y = sizeXYZ[1];
            int z = sizeXYZ[2];

            BlockPos blockPos = target.getBlockPos().offset(target.sideHit);

            // check if the all blocks in the structure can be placed where they need to be
            boolean flag = false;
            for (int k = 0; k < z && !flag; k++) {
                for (int j = 0; j < y && !flag; j++) {
                    for (int i = 0; i < x & !flag; i++) {
                        byte b = bytes[i*j*k];
                        if (b >= 0) {
                            BlockPos pos = blockPos.add(i,j,k);
                            Block block = worldIn.getBlockState(pos).getBlock();
                            flag = !worldIn.mayPlace(block, pos, true, EnumFacing.UP, null);
                        }
                    }
                }
            }

            // if there are no problems with placement, the go ahead and place the blocks
            if (!flag) {
                for (int k = 0; k < z; k++) {
                    for (int j = 0; j < y; j++) {
                        for (int i = 0; x < z; i++) {
                            byte b = bytes[i*j*k];
                            if (b >= 0) {
                                BlockPos pos = blockPos.add(i,j,k);
                                IBlockState blockState = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(structureBlocks[b])).getDefaultState();
                                if (!worldIn.setBlockState(pos, blockState, 11)) {

                                    FMLLog.log.warn("Block " + structureBlocks[b] + " failed to place at location xyx: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " while trying to place structure '" + structure + "' from " + this.getRegistryName());

                                }
                            }
                        }
                    }
                }
            }

            if (flag) {
                return ActionResult.newResult(EnumActionResult.FAIL, itemStack);
            } else {
                return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);
            }


        } else {

            //

        }

        return ActionResult.newResult(EnumActionResult.FAIL, itemStack);
    }
}
