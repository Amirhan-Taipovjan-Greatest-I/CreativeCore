package team.creative.creativecore.common.gui.controls;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.type.HashMapList;

public class GuiStackSelector extends GuiButtonFixed {
    
    protected GuiStackSelectorExtension extension;
    public StackCollector collector;
    protected HashMapList<String, ItemStack> stacks;
    
    public PlayerEntity player;
    public boolean searchBar;
    
    public GuiStackSelector(String name, int x, int y, int width, PlayerEntity player, StackCollector collector, boolean searchBar) {
        super(name, x, y, width, 18, null);
        pressed = (button) -> {
            if (extension == null)
                openBox();
            else
                closeBox();
        };
        this.searchBar = searchBar;
        this.player = player;
        this.collector = collector;
        updateCollectedStacks();
        selectFirst();
    }
    
    public GuiStackSelector(String name, int x, int y, int width, PlayerEntity player, StackCollector collector) {
        this(name, x, y, width, player, collector, true);
    }
    
    public boolean selectFirst() {
        if (stacks != null) {
            ItemStack first = stacks.getFirst();
            if (first != null) {
                setSelected(first);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public PlayerEntity getPlayer() {
        return player;
    }
    
    public void updateCollectedStacks() {
        stacks = collector.collect(player);
    }
    
    protected ItemStack selected = ItemStack.EMPTY;
    
    public boolean setSelectedForce(ItemStack stack) {
        setTitle(new TextBuilder().stack(stack).add(stack.getDisplayName()).build());
        this.selected = stack;
        raiseEvent(new GuiControlChangedEvent(this));
        return true;
    }
    
    public boolean setSelected(ItemStack stack) {
        if (stacks.contains(stack)) {
            setTitle(new TextBuilder().stack(stack).add(stack.getDisplayName()).build());
            this.selected = stack;
            raiseEvent(new GuiControlChangedEvent(this));
            return true;
        }
        return false;
    }
    
    public HashMapList<String, ItemStack> getStacks() {
        return stacks;
    }
    
    public ItemStack getSelected() {
        return selected;
    }
    
    public void openBox() {
        this.extension = createBox();
        GuiLayer layer = getLayer();
        layer.add(extension);
        extension.moveTop();
        extension.init();
        extension.setX(getControlOffsetX());
        extension.setY(getControlOffsetY() + getHeight());
        
        if (extension.getY() + extension.getHeight() > layer.getHeight() && this.getY() >= extension.getHeight())
            extension.setY(extension.getY() - this.getHeight() + extension.getHeight());
    }
    
    public void closeBox() {
        if (extension != null) {
            getLayer().remove(extension);
            extension = null;
        }
    }
    
    protected GuiStackSelectorExtension createBox() {
        return new GuiStackSelectorExtension(name + "extension", getPlayer(), getX(), getY() + getHeight(), getWidth(), 80, this);
    }
    
    public boolean select(String line) {
        return false;
    }
    
    public static abstract class StackCollector {
        
        public StackSelector selector;
        
        public StackCollector(StackSelector selector) {
            this.selector = selector;
        }
        
        public abstract HashMapList<String, ItemStack> collect(PlayerEntity player);
        
    }
    
    public static class InventoryCollector extends StackCollector {
        
        public InventoryCollector(StackSelector selector) {
            super(selector);
        }
        
        @Override
        public HashMapList<String, ItemStack> collect(PlayerEntity player) {
            HashMapList<String, ItemStack> stacks = new HashMapList<>();
            
            if (player != null) {
                // Inventory
                List<ItemStack> tempStacks = new ArrayList<>();
                for (ItemStack stack : player.inventory.mainInventory)
                    if (!stack.isEmpty() && selector.allow(stack))
                        tempStacks.add(stack.copy());
                    else {
                        LazyOptional<IItemHandler> result = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                        if (result.isPresent())
                            collect((IItemHandler) result.cast(), tempStacks);
                    }
                
                stacks.add("selector.inventory", tempStacks);
            }
            
            return stacks;
        }
        
        protected void collect(IItemHandler inventory, List<ItemStack> stacks) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty() && selector.allow(stack))
                    stacks.add(stack.copy());
                else {
                    LazyOptional<IItemHandler> result = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    if (result.isPresent())
                        collect((IItemHandler) result.cast(), stacks);
                }
                
            }
            
        }
    }
    
    public static class CreativeCollector extends InventoryCollector {
        
        public CreativeCollector(StackSelector selector) {
            super(selector);
        }
        
        @Override
        public HashMapList<String, ItemStack> collect(PlayerEntity player) {
            HashMapList<String, ItemStack> stacks = super.collect(player);
            
            NonNullList<ItemStack> tempStacks = NonNullList.create();
            
            for (Item item : ForgeRegistries.ITEMS)
                if (!item.getCreativeTabs().isEmpty())
                    item.fillItemGroup(ItemGroup.SEARCH, tempStacks);
                
            List<ItemStack> newStacks = new ArrayList<>();
            for (ItemStack stack : tempStacks) {
                if (!stack.isEmpty() && selector.allow(stack))
                    newStacks.add(stack);
            }
            stacks.add("selector.all", newStacks);
            
            return stacks;
        }
    }
    
    public static abstract class StackSelector {
        
        public abstract boolean allow(ItemStack stack);
        
    }
    
    public static class SearchSelector extends StackSelector {
        
        public String search = "";
        
        @Override
        public boolean allow(ItemStack stack) {
            return contains(search, stack);
        }
        
    }
    
    public static class GuiBlockSelector extends SearchSelector {
        
        @Override
        public boolean allow(ItemStack stack) {
            if (super.allow(stack))
                return Block.getBlockFromItem(stack.getItem()) != null && !(Block.getBlockFromItem(stack.getItem()) instanceof AirBlock);
            return false;
        }
        
    }
    
    public static boolean contains(String search, ItemStack stack) {
        if (search.equals(""))
            return true;
        if (getItemName(stack).toLowerCase().contains(search))
            return true;
        for (ITextComponent line : stack.getTooltip(null, TooltipFlags.NORMAL))
            if (line.getUnformattedComponentText().toLowerCase().contains(search))
                return true;
            
        return false;
    }
    
    public static String getItemName(ItemStack stack) {
        String itemName = "";
        try {
            itemName = stack.getDisplayName().getString();
        } catch (Exception e) {
            itemName = stack.getItem().getRegistryName().toString();
        }
        return itemName;
    }
    
}
