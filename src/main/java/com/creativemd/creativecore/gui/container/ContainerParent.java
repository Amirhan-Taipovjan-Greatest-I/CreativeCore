package com.creativemd.creativecore.gui.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.creativemd.creativecore.gui.ContainerControl;
import com.creativemd.creativecore.gui.CoreControl;
import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.controls.container.SlotControl;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;

public abstract class ContainerParent extends ContainerControl implements IControlParent {

	public ContainerParent(String name) {
		super(name);
	}
	
	public ArrayList<ContainerControl> controls = new ArrayList<>();
	
	@Override
	public List getControls() {
		return controls;
	}
	
	@Override
	public CoreControl get(String name)
    {
    	for (int i = 0; i < controls.size(); i++) {
			if(controls.get(i).name.equalsIgnoreCase(name))
				return controls.get(i);
		}
    	return null;
    }
    
	@Override
    public boolean has(String name)
    {
    	for (int i = 0; i < controls.size(); i++) {
			if(controls.get(i).name.equalsIgnoreCase(name))
				return true;
		}
    	return false;
    }
	
	@Override
	public void onOpened()
    {
    	for (int i = 0; i < controls.size(); i++) {
    		controls.get(i).parent = this;
    		controls.get(i).onOpened();
    	}
		refreshControls();
    }
	
	@Override
	public void onClosed()
	{
		for (int i = 0; i < controls.size(); i++) {
			controls.get(i).onClosed();
		}
		//eventBus.removeAllEventListeners();
	}
	
	public void updateEqualContainers()
	{
		if(parent != null)
			getParent().updateEqualContainers();
	}
	
	@Override
	public void refreshControls() {
		for (int i = 0; i < controls.size(); i++)
		{
			controls.get(i).parent = this;
			controls.get(i).setID(i);
		}
	}

	public void sendNBTUpdate(ContainerControl control, NBTTagCompound nbt)
	{
		if(parent != null)
			getParent().sendNBTUpdate(control, nbt);
	}
	
	@Override
	public void onTick()
	{
		for (int i = 0; i < controls.size(); i++) {
			controls.get(i).onTick();
		}
	}
	
	/*=============================Helper Methods=============================*/
	
	public ArrayList<Slot> getSlots()
	{
		ArrayList<Slot> slots = new ArrayList<Slot>();
		for (int i = 0; i < controls.size(); i++) {
			if(controls.get(i) instanceof SlotControl)
				slots.add(((SlotControl)controls.get(i)).slot);
		}
		return slots;
	}
	
	public void addSlotToContainer(Slot slot)
	{
		//slot.xDisplayPosition += 8;
		//slot.yDisplayPosition += 8;
		//if(!inventories.contains(slot.inventory))
			//inventories.add(slot.inventory);
		controls.add(new SlotControl(slot));
	}
	
	public void addPlayerSlotsToContainer(EntityPlayer player)
	{
		addPlayerSlotsToContainer(player, 8, 84);
	}
	
	public void addPlayerSlotsToContainer(EntityPlayer player, int x, int y)
	{
		int l;
        for (l = 0; l < 3; ++l)
        {
            for (int i1 = 0; i1 < 9; ++i1)
            {
            	addSlotToContainer(new Slot(player.inventory, i1 + l * 9 + 9, i1 * 18 + x, l * 18+y));
            }
        }

        for (l = 0; l < 9; ++l)
        {
        	addSlotToContainer(new Slot(player.inventory, l, l * 18+x, 58+y));
        }
	}
	
}
