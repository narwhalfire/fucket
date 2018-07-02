package narwhalfire.fucket.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * The Basic Fucket: Just a normal fucket capable of storing entities and whatnot.
 */
public class ItemBasicFucket extends FucketBase {


    @Override
    public boolean hasContainerItem(ItemStack stack) {
        //todo: check if there is stuff in there.
        return false;
    }

    @Override
    @Nonnull
    public ItemStack getContainerItem(ItemStack itemStack) {
        //todo: figure out what to do here
        return super.getContainerItem(itemStack);
    }

    /**
     * Called on equipped item right click
     */
    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
