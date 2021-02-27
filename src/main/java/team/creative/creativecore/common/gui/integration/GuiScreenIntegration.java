package team.creative.creativecore.common.gui.integration;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.IScaleableGuiScreen;
import team.creative.creativecore.common.gui.sync.LayerOpenPacket;

public class GuiScreenIntegration extends Screen implements IGuiIntegratedParent, IScaleableGuiScreen {
    
    public final Minecraft mc = Minecraft.getInstance();
    private List<GuiLayer> layers = new ArrayList<>();
    protected ScreenEventListener listener;
    
    public GuiScreenIntegration(GuiLayer layer) {
        super(new StringTextComponent("gui-api"));
        layer.setParent(this);
        this.layers.add(layer);
        layer.init();
    }
    
    @Override
    protected void init() {
        if (listener == null)
            listener = new ScreenEventListener(this, this);
        this.addListener(listener);
    }
    
    @Override
    public int getWidth() {
        int width = 0;
        for (GuiLayer layer : layers)
            width = Math.max(width, layer.getWidth());
        return width;
    }
    
    @Override
    public int getHeight() {
        int height = 0;
        for (GuiLayer layer : layers)
            height = Math.max(height, layer.getHeight());
        return height;
    }
    
    @Override
    public void tick() {
        for (GuiLayer layer : layers)
            layer.tick();
    }
    
    @Override
    public void onClose() {
        for (GuiLayer layer : layers)
            layer.closed();
    }
    
    @Override
    public boolean isContainer() {
        return false;
    }
    
    @Override
    public boolean isClient() {
        return true;
    }
    
    @Override
    public PlayerEntity getPlayer() {
        return mc.player;
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        render(matrixStack, this, listener, mouseX, mouseY);
    }
    
    @Override
    public List<GuiLayer> getLayers() {
        return layers;
    }
    
    @Override
    public GuiLayer getTopLayer() {
        return layers.get(layers.size() - 1);
    }
    
    @Override
    public void openLayer(GuiLayer layer) {
        layer.setParent(this);
        layers.add(layer);
        layer.init();
    }
    
    @Override
    public void closeLayer(int layer) {
        layers.remove(layer);
        if (layers.isEmpty())
            closeScreen();
    }
    
    @Override
    public void closeTopLayer() {
        closeLayer(layers.size() - 1);
    }
    
    @Override
    public GuiLayer openLayer(LayerOpenPacket packet) {
        packet.execute(getPlayer());
        return layers.get(layers.size() - 1);
    }
    
    @Override
    public void mouseMoved(double x, double y) {
        listener.mouseMoved(x, y);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listener.keyPressed(keyCode, scanCode, modifiers))
            return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (listener.keyReleased(keyCode, scanCode, modifiers))
            return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (listener.charTyped(codePoint, modifiers))
            return true;
        return super.charTyped(codePoint, modifiers);
    }
    
}
