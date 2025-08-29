package com.albertux2.bidingpiece.client.component.screen;

import com.albertux2.bidingpiece.container.AuctionContainer;
import com.albertux2.bidingpiece.network.NetworkHandler;
import com.albertux2.bidingpiece.network.UpdateExhibitedItemsPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionScreen extends ContainerScreen<AuctionContainer> {
    private final List<BetItem> betItems = new ArrayList<>();
    private List<ItemStack> exhibitedItems = new ArrayList<>();
    private SelectedListComponent selectedListComponent;

    public AuctionScreen(AuctionContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
        requestExhibitedItems();
    }

    private void requestExhibitedItems() {
        NetworkHandler.INSTANCE.sendToServer(new UpdateExhibitedItemsPacket());
    }

    public void updateExhibitedItems(List<ItemStack> items) {
        this.exhibitedItems = items;
        this.init();
    }

    @Override
    protected void init() {
        super.init();
        betItems.clear();
        Map<String, BetItem> flattened = new HashMap<>();
        exhibitedItems.stream()
            .filter(i -> !i.isEmpty())
            .forEach(x -> flattened.computeIfAbsent(
                x.getItem().getDescriptionId(),
                s -> new BetItem(x, 0)
            ).sumQuantity(x.getCount()));
        betItems.addAll(flattened.values());

        selectedListComponent = new SelectedListComponent(betItems, this.font, false);
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        int startY = 40;
        int itemH = Math.max(24, (int) Math.round(24 * 1.0));
        selectedListComponent.setLayout(this.width, startY, itemH, 16, 16, 1.0, Math.min(6, Math.max(3, (this.height - startY) / itemH)));
        selectedListComponent.render(ms);
        super.render(ms, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        // Este método es necesario para ContainerScreen
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        // Este método es necesario para ContainerScreen
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedListComponent != null && selectedListComponent.clicked(mouseX, mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (selectedListComponent != null && selectedListComponent.mouseScrolled(mouseX, mouseY, delta)) return true;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
