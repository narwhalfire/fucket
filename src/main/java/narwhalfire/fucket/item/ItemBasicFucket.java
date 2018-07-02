package narwhalfire.fucket.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * The Basic Fucket: Just a normal fucket capable of storing entities and whatnot.
 */
public class ItemBasicFucket extends FucketBase {


    @Override
    public boolean hasContainerItem(@Nonnull ItemStack stack) {
        //todo: check if there is stuff in there.
        return false;
    }

    @Override
    @Nonnull
    public ItemStack getContainerItem(@Nonnull ItemStack itemStack) {
        //todo: figure out what to do here
        return super.getContainerItem(itemStack);
    }

    /**
     * Called on equipped item right click
     */
    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand handIn) {

        ItemStack itemStack = playerIn.getHeldItem(handIn);

        //todo: figure out what's in the fucket

        RayTraceResult target = this.rayTrace(worldIn, playerIn, true);

        if (target == null) {
            return ActionResult.newResult(EnumActionResult.PASS, itemStack);
        }


        //todo: figure out what was clicked


        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
