package com.albertux2.bidingpiece.client.component.screen;

import com.albertux2.bidingpiece.auction.Auction;
import com.albertux2.bidingpiece.client.component.DrawUtils;
import com.albertux2.bidingpiece.client.component.UserListComponent;
import com.albertux2.bidingpiece.client.component.button.FancyButton;
import com.albertux2.bidingpiece.network.NetworkHandler;
import com.albertux2.bidingpiece.network.SelectWinnerMessage;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BidsScreen extends Screen {
    private final AuctionScreen parentScreen;
    private Auction auction;
    private UserListComponent userList;
    private SelectedListComponent bidsList;
    private FancyButton selectWinnerButton;
    private String errorMessage = null;

    public BidsScreen(AuctionScreen parentScreen, Auction auction) {
        super(new StringTextComponent("Current Bids"));
        this.parentScreen = parentScreen;
        this.auction = auction;
    }

    @Override
    protected void init() {
        if (minecraft != null) {
            // Botón Back
            this.addButton(new FancyButton(
                this.width / 2 - 100,
                this.height - 30,
                200, 20,
                new StringTextComponent("Back"),
                button -> {
                    if (minecraft != null) {
                        minecraft.setScreen(parentScreen);
                    }
                }
            ));

            // Botón Select Winner
            selectWinnerButton = this.addButton(new FancyButton(
                this.width / 2 - 100,
                this.height - 55,
                200, 20,
                new StringTextComponent("Select Winner"),
                button -> selectWinner()
            ));
        }

        initializeComponents();
        updateButtonStates();
    }

    private void selectWinner() {
        UUID selectedUser = userList.getSelectedUser();
        if (selectedUser != null && auction != null && auction.isActive() &&
            Minecraft.getInstance().player != null &&
            auction.getOwner().equals(Minecraft.getInstance().player.getUUID())) {
            NetworkHandler.INSTANCE.sendToServer(new SelectWinnerMessage(parentScreen.getMenu().getPodiumPos(), selectedUser));
        }
    }

    private void updateButtonStates() {
        if (selectWinnerButton != null && minecraft != null && minecraft.player != null) {
            boolean isOwner = auction != null && auction.getOwner().equals(minecraft.player.getUUID());
            boolean hasSelectedUser = userList != null && userList.getSelectedUser() != null;
            selectWinnerButton.active = isOwner && hasSelectedUser && auction.isActive();
            selectWinnerButton.visible = isOwner;
        }
    }

    private void initializeComponents() {
        if (auction != null && auction.getBids() != null) {
            userList = new UserListComponent(new ArrayList<>(auction.getBids().keySet()), this.font);
            updateBidsList(null);
        }
    }

    private void updateBidsList(UUID selectedUser) {
        List<BetItem> bidItems = new ArrayList<>();

        if (selectedUser != null && auction != null &&
            auction.getBids() != null &&
            auction.getBids().containsKey(selectedUser)) {
            bidItems.addAll(auction.getBids().get(selectedUser));
        }

        bidsList = new SelectedListComponent(bidItems, this.font, true);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        String title = auction != null && !auction.getBids().isEmpty() ?
            "Current Bids" : "No bids yet";
        int titleWidth = font.width(title);
        drawCenteredString(matrixStack, font,
            new StringTextComponent(title).withStyle(TextFormatting.GOLD),
            this.width / 2, 20, 0xFFFFFF);

        // Render error message if exists
        renderErrorMsg(matrixStack, titleWidth);

        // Render user list in the upper half
        if (userList != null) {
            int startY = 40;
            int itemHeight = 36;
            userList.setLayout(width, startY, itemHeight);
            userList.setVisibleRows(Math.min(4, Math.max(2, (this.height / 2 - startY - 40) / itemHeight)));
            userList.render(matrixStack);
        }

        // Render selected user's bids in the lower half
        if (bidsList != null) {
            int startY = this.height / 2 + 20;
            int itemH = 24;
            bidsList.setLayout(
                this.width,
                startY,
                itemH,
                16,
                16,
                1.0,
                Math.min(4, Math.max(2, (this.height - startY - 40) / itemH))
            );
            bidsList.render(matrixStack);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void renderErrorMsg(MatrixStack matrixStack, int titleWidth) {
        if (errorMessage != null && !errorMessage.isEmpty()) {
            int startX = this.width / 2 + titleWidth / 2 + 20; // 20 píxeles de margen después del título
            int maxLineWidth = this.width - startX - 10; // 10 píxeles de margen derecho
            List<String> lines = new ArrayList<>();

            // Partir el mensaje en palabras
            String[] words = errorMessage.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                if (font.width(currentLine + " " + word) <= maxLineWidth) {
                    if (currentLine.length() > 0) currentLine.append(" ");
                    currentLine.append(word);
                } else {
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    } else {
                        lines.add(word);
                    }
                }
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }

            // Renderizar cada línea
            for (int i = 0; i < lines.size(); i++) {
                DrawUtils.drawTextWithBorder(matrixStack, this.font, lines.get(i),
                    startX, 20 + i * 12, 0xffab2a3e, 0xFFFFFF);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (userList != null && userList.clicked(mouseX, mouseY)) {
            UUID selectedUser = userList.getSelectedUser();
            updateBidsList(selectedUser);
            updateButtonStates();
            return true;
        }
        if (bidsList != null && bidsList.clicked(mouseX, mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseY < this.height / 2 && userList != null) {
            return userList.mouseScrolled(mouseX, mouseY, delta);
        } else if (bidsList != null && bidsList.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void updateAuctionState(Auction auction) {
        this.auction = auction;
        updateBidsList(userList.getSelectedUser());
        updateButtonStates();
    }

    public void setErrorMessage(String message) {
        this.errorMessage = message;
    }
}
