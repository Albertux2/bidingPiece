package com.albertux2.bidingpiece.client.component.screen;

import com.albertux2.bidingpiece.auction.Auction;
import com.albertux2.bidingpiece.client.component.button.FancyButton;
import com.albertux2.bidingpiece.container.AuctionContainer;
import com.albertux2.bidingpiece.network.NetworkHandler;
import com.albertux2.bidingpiece.network.RequestAuctionStatePacket;
import com.albertux2.bidingpiece.network.UpdateAuctionStatePacket;
import com.albertux2.bidingpiece.network.UpdateExhibitedItemsPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuctionScreen extends ContainerScreen<AuctionContainer> {
    private final List<BetItem> exhibitedItems = new ArrayList<>();
    private Auction currentAuction;
    private SelectedListComponent selectedListComponent;
    private Button toggleAuctionButton;
    private Button showBidsButton;

    public AuctionScreen(AuctionContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
        NetworkHandler.INSTANCE.sendToServer(new RequestAuctionStatePacket(container.getPodiumPos()));
        NetworkHandler.INSTANCE.sendToServer(new UpdateExhibitedItemsPacket());
    }

    @Override
    public void tick() {
        super.tick();
        if (minecraft.player.tickCount % 20 == 0) {
            NetworkHandler.INSTANCE.sendToServer(new UpdateExhibitedItemsPacket());
        }
    }

    public void updateExhibitedItems(List<ItemStack> items) {
        List<ItemStack> currentItems = this.exhibitedItems.stream()
            .map(BetItem::getStack)
            .collect(Collectors.toList());

        if (!items.equals(currentItems)) {
            this.exhibitedItems.clear();
            Map<String, BetItem> flattened = new HashMap<>();

            items.stream()
                .filter(i -> !i.isEmpty())
                .forEach(x -> flattened.computeIfAbsent(
                    x.getItem().getDescriptionId(),
                    s -> new BetItem(x, 0)
                ).sumQuantity(x.getCount()));

            this.exhibitedItems.addAll(flattened.values());
            this.init();
        }
    }

    @Override
    protected void init() {
        super.init();
        this.imageWidth = this.width;
        this.imageHeight = this.height;
        this.leftPos = 0;
        this.topPos = 0;

        selectedListComponent = new SelectedListComponent(exhibitedItems, this.font, false);

        toggleAuctionButton = this.addButton(new FancyButton(
            this.width / 2 - 100,
            this.height - 50,
            200, 20,
            new StringTextComponent(currentAuction != null && currentAuction.isActive() ? "Cancel Auction" : "Start Auction"),
            button -> NetworkHandler.INSTANCE.sendToServer(new UpdateAuctionStatePacket(
                currentAuction != null && currentAuction.isActive()
                    ? null
                    : new Auction(minecraft.player.getUUID(), menu.getExhibitedItems(), true)
            ))
        ));

        showBidsButton = this.addButton(new FancyButton(
            this.width / 2 - 100,
            this.height - 25,
            200, 20,
            new StringTextComponent("Show Bids"),
            button -> {
                if (currentAuction != null && currentAuction.isActive()) {
                    Minecraft.getInstance().setScreen(new BidsScreen(this, currentAuction));
                }
            }
        ));

        showBidsButton.active = currentAuction != null && currentAuction.isActive();
    }

    public void updateAuctionState(Auction auction) {
        this.currentAuction = auction;
        if (toggleAuctionButton != null) {
            toggleAuctionButton.setMessage(new StringTextComponent(
                currentAuction != null && currentAuction.isActive() ? "Cancel Auction" : "Start Auction"
            ));
        }
        if (showBidsButton != null) {
            showBidsButton.active = currentAuction != null && currentAuction.isActive();
        }
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        if (selectedListComponent != null) {
            int startY = 40;
            int itemH = Math.max(24, (int) Math.round(24 * 1.0));
            selectedListComponent.setLayout(
                this.width,
                startY,
                itemH,
                16,
                16,
                1.0,
                Math.min(6, Math.max(3, (this.height - startY) / itemH))
            );
            selectedListComponent.render(ms);
        }

        drawCenteredString(ms, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(ms, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderBg(MatrixStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
    }

    @Override
    protected void renderLabels(MatrixStack ms, int x, int y) {
        // No renderizar nada aquí para ocultar el título del inventario
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
