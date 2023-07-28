package com.stit76.stscenes.client.gui.npcCustomizer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.stit76.stscenes.STScenes;
import com.stit76.stscenes.client.gui.components.AlwaysShowNameCheckBox;
import com.stit76.stscenes.client.gui.components.CustomButton;
import com.stit76.stscenes.client.gui.components.FollowCheckBox;
import com.stit76.stscenes.client.gui.components.ShowNameCheckBox;
import com.stit76.stscenes.common.entity.AbstractSTNPC;
import com.stit76.stscenes.networking.SimpleNetworkWrapper;
import com.stit76.stscenes.networking.packet.server.visualData.ChangeNameC2SPacket;
import com.stit76.stscenes.networking.packet.server.visualData.ChangeTextureC2SPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class NpcCustomizerScreen extends Screen {
    public static ResourceLocation background = new ResourceLocation(STScenes.MODID, "textures/gui/npc_spawner_gui.png");
    int xMouse;
    int yMouse;
    int winSizeX = 314;
    int winSizeY = 263;
    int leftPos = 0;
    int topPos = 0;
    boolean showModel;
    boolean showBehavior;
    private Player player;
    private String name;
    private AbstractSTNPC npc;
    private EditBox nameEditBox;
    private EditBox textureEditBox;


    public NpcCustomizerScreen(Component p_96550_, AbstractSTNPC npc,Player player) {
        super(p_96550_);
        this.showModel = true;
        this.name = p_96550_.getString();
        this.npc = npc;
        this.player = player;
    }

    @Override
    protected void init() {
        leftPos = this.width / 2 - (winSizeX / 2);
        topPos = this.height / 2 - (winSizeY / 2);
        addButtons();
        renderMenu();
        super.init();
    }

    @Override
    public void render(PoseStack poseStack, int p_96563_, int p_96564_, float p_96565_) {
        this.xMouse = p_96563_;
        this.yMouse = p_96564_;
        renderBg(poseStack);
        super.render(poseStack, p_96563_, p_96564_, p_96565_);
    }

    @Override
    public void onClose() {
        saveModelMenu();
        super.onClose();
    }

    @Override
    public void removed() {
        saveModelMenu();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderBg(PoseStack poseStack) {
        renderBackground(poseStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, background);
        blit(poseStack, leftPos, topPos, 0, 0, winSizeX,winSizeY,405,405);
        renderLine(poseStack,this.name,7,7,false);
        InventoryScreen.renderEntityInInventoryFollowsMouse(poseStack, leftPos + 261, topPos + 80, 30, (float) (leftPos + 261) - this.xMouse, (float) (topPos + 80) - this.yMouse, this.npc);
    }
    private void renderLine(PoseStack poseStack, String text, int x, int y,boolean isCentred) {
        if(isCentred){
            drawCenteredString(poseStack, this.font, text, leftPos + x, topPos + y, 16777215);
        }else {
            drawString(poseStack, this.font, text, leftPos + x, topPos + y, 16777215);
        }
    }
    private void addCustomButton(String text, int x, int y, Button.OnPress buttonOnPress) {
        addRenderableWidget(customButton(text, x, y, buttonOnPress));
    }

    private CustomButton customButton(String text, int x, int y, Button.OnPress buttonOnPress) {
        CustomButton customButton = new CustomButton(text,leftPos + x,
                topPos + y, 101,22,
                0,266,21 + 4,
                background,405,405, buttonOnPress);
        return customButton;
    }

    private void addButtons() {
        int x = 208;
        int y = 107;
        addCustomButton("Model", x, y, (p_93751_) -> {
            resetMenu();
            this.showModel = true;
            minecraft.setScreen(this);
        });
        addCustomButton("Behavior", x, y + (27 * 1), (p_93751_) -> {
            saveModelMenu();
            resetMenu();
            this.showBehavior = true;
            minecraft.setScreen(this);
        });
    }
    public void resetMenu(){
        this.showModel = false;
        this.showBehavior = false;
    }
    private void renderMenu(){
        if(showModel){
            nameEditBox = new EditBox(this.font,leftPos + 9,topPos + 27,194,16,Component.nullToEmpty("Name"));
            textureEditBox = new EditBox(this.font,leftPos + 9,topPos + 27 + 16 + 5, 194 , 16,Component.nullToEmpty("Texture"));
            AlwaysShowNameCheckBox alwaysShowTheName = new AlwaysShowNameCheckBox(this.npc,leftPos + 9 + 20 + 70,topPos + 27 + ((16 + 5) * 2),20,20,Component.nullToEmpty("Always"),npc.visualData.getAlwaysShowName(),true);
            ShowNameCheckBox showName = new ShowNameCheckBox(this.npc,alwaysShowTheName,leftPos + 9,topPos + 27 + ((16 + 5) * 2),20,20,Component.nullToEmpty("Show name"),npc.visualData.getShowName(),true);

            nameEditBox.setValue(npc.visualData.getName());
            textureEditBox.setMaxLength(400);
            textureEditBox.setValue(this.npc.visualData.getTexture());

            addRenderableWidget(nameEditBox);
            addRenderableWidget(textureEditBox);
            addRenderableWidget(showName);
            addRenderableWidget(alwaysShowTheName);
        } else if (showBehavior) {
            FollowCheckBox followCheckBox = new FollowCheckBox(this.npc,leftPos + 9,topPos + 27,20,20,Component.nullToEmpty("Follow"),npc.behaviourData.getFollow(),true);
            addRenderableWidget(followCheckBox);
        }
    }
    public void saveModelMenu(){
        if(showModel){
            SimpleNetworkWrapper.sendToServer(new ChangeNameC2SPacket(npc.getUUID(),this.nameEditBox.getValue()));
            SimpleNetworkWrapper.sendToServer(new ChangeTextureC2SPacket(npc.getUUID(),this.textureEditBox.getValue()));
        }
    }

    public AbstractSTNPC getNpc(){return this.npc;}
    public void setNpc(AbstractSTNPC npc){this.npc = npc;}


}
