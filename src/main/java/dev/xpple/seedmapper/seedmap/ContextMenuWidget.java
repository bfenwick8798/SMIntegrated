package dev.xpple.seedmapper.seedmap;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ContextMenuWidget {

    private static final int MENU_WIDTH = 120;
    private static final int MENU_ITEM_HEIGHT = 20;
    private static final int PADDING = 4;

    private static final int BACKGROUND_COLOR = ARGB.color(200, 0, 0, 0);
    private static final int HOVER_COLOR = ARGB.color(150, 100, 100, 100);
    private static final int BORDER_COLOR = ARGB.color(255, 80, 80, 80);

    private final int x;
    private final int y;
    private final List<MenuItem> menuItems = new ArrayList<>();

    public ContextMenuWidget(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addMenuItem(Component label, Consumer<MouseButtonEvent> action) {
        this.menuItems.add(new MenuItem(label, action));
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, Font font) {
        int menuHeight = this.menuItems.size() * MENU_ITEM_HEIGHT;
        
        // Draw background
        guiGraphics.fill(this.x, this.y, this.x + MENU_WIDTH, this.y + menuHeight, BACKGROUND_COLOR);
        
        // Draw border
        guiGraphics.fill(this.x, this.y, this.x + MENU_WIDTH, this.y + 1, BORDER_COLOR);
        guiGraphics.fill(this.x, this.y + menuHeight - 1, this.x + MENU_WIDTH, this.y + menuHeight, BORDER_COLOR);
        guiGraphics.fill(this.x, this.y, this.x + 1, this.y + menuHeight, BORDER_COLOR);
        guiGraphics.fill(this.x + MENU_WIDTH - 1, this.y, this.x + MENU_WIDTH, this.y + menuHeight, BORDER_COLOR);

        // Draw menu items
        for (int i = 0; i < this.menuItems.size(); i++) {
            MenuItem menuItem = this.menuItems.get(i);
            int itemY = this.y + i * MENU_ITEM_HEIGHT;
            
            // Highlight on hover
            if (mouseX >= this.x && mouseX <= this.x + MENU_WIDTH && 
                mouseY >= itemY && mouseY <= itemY + MENU_ITEM_HEIGHT) {
                guiGraphics.fill(this.x + 1, itemY, this.x + MENU_WIDTH - 1, itemY + MENU_ITEM_HEIGHT, HOVER_COLOR);
            }
            
            // Draw text
            guiGraphics.drawString(font, menuItem.label, this.x + PADDING, itemY + (MENU_ITEM_HEIGHT - font.lineHeight) / 2, -1);
        }
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        int button = mouseButtonEvent.button();
        if (button != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }
        
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        
        int menuHeight = this.menuItems.size() * MENU_ITEM_HEIGHT;
        
        // Check if click is within menu bounds
        if (mouseX >= this.x && mouseX <= this.x + MENU_WIDTH && 
            mouseY >= this.y && mouseY <= this.y + menuHeight) {
            
            // Find which menu item was clicked
            int clickedIndex = (int) ((mouseY - this.y) / MENU_ITEM_HEIGHT);
            if (clickedIndex >= 0 && clickedIndex < this.menuItems.size()) {
                MenuItem menuItem = this.menuItems.get(clickedIndex);
                menuItem.action.accept(mouseButtonEvent);
                return true;
            }
        }
        
        return false;
    }

    private record MenuItem(Component label, Consumer<MouseButtonEvent> action) {
    }
}
