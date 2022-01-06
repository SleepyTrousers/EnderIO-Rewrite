package com.enderio.machines.client.screen;

import com.enderio.base.client.screen.EIOScreen;
import com.enderio.base.client.screen.EnumIconWidget;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.base.common.util.Vector2i;
import com.enderio.machines.EIOMachines;
import com.enderio.machines.client.widget.FluidStackWidget;
import com.enderio.machines.common.menu.FluidTankMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FluidTankScreen extends EIOScreen<FluidTankMenu> {
    private static final ResourceLocation BG_TEXTURE = EIOMachines.loc("textures/gui/tank.png");
    public FluidTankScreen(FluidTankMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new FluidStackWidget(this, getMenu().getBlockEntity()::getFluidTank, 80 + leftPos, 21 + topPos, 16, 47));
        addRenderableWidget(new EnumIconWidget<>(this, leftPos + imageWidth - 8 - 12, topPos + 6, () -> menu.getBlockEntity().getRedstoneControl(),
            control -> menu.getBlockEntity().setRedstoneControl(control), EIOLang.REDSTONE_MODE));
    }

    @Override
    protected ResourceLocation getBackgroundImage() {
        return BG_TEXTURE;
    }

    @Override
    protected Vector2i getBackgroundImageSize() {
        return new Vector2i(176, 166);
    }
}