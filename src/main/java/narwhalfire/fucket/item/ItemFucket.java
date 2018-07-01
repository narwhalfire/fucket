package narwhalfire.fucket.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemFucket extends Item {


    private final int fluidCapacity;
    @Nonnull
    private final ItemStack empty;


    public ItemFucket(int fluidCapacity, @Nonnull ItemStack empty) {
        this.fluidCapacity = fluidCapacity;
        this.empty = empty;
    }


}
