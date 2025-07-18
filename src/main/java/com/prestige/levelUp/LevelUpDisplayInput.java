/*
 * Copyright (c) 2020, Jordan <nightfirecat@protonmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.prestige.levelUp;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.List;

import com.prestige.PrestigePlugin;
import com.prestige.model.SkillModel;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Experience;
import net.runelite.api.FontID;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.widgets.*;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.game.chatbox.ChatboxInput;
import net.runelite.client.input.KeyListener;

public class LevelUpDisplayInput extends ChatboxInput implements KeyListener
{
    private static final int X_OFFSET = 13;
    private static final int Y_OFFSET = 16;

    private final PrestigePlugin plugin;
    private final Skill skill;
    private final int level;

    @Getter
    private boolean closeMessage;

    public LevelUpDisplayInput(PrestigePlugin plugin, Skill skill, int level) {
        this.plugin = plugin;
        this.skill = skill;
        this.level = level;
    }

    @Override
    public void open() {
        final Widget chatboxContainer = plugin.getChatboxPanelManager().getContainerWidget();

        final String skillName = skill.getName();
        final int skillExperience = plugin.getClient().getSkillExperience(skill);
        final List<SkillModel> skillModels = SkillModel.getSKILL_MODELS(skill);
        final String prefix = (skill == Skill.AGILITY || skill == Skill.ATTACK) ? "an " : "a ";

        final Widget levelUpHeader = chatboxContainer.createChild(-1, WidgetType.TEXT);
        final Widget levelUpText = chatboxContainer.createChild(-1, WidgetType.TEXT);
        final Widget levelUpContinue = chatboxContainer.createChild(-1, WidgetType.TEXT);

        final String levelUpMessage;
        if (skillExperience >= Experience.MAX_SKILL_XP) {
            plugin.getClientThread().invoke(() -> this.setFireworksGraphic(LevelUpFireworks.NINETY_NINE));
            levelUpMessage = "Congratulations, you just maxed your " + skillName + " skill.";
        } else {
            plugin.getClientThread().invoke(() -> this.setFireworksGraphic(LevelUpFireworks.NORMAL));
            levelUpMessage = "Congratulations, you just advanced " + prefix + skillName + " prestige level.";
        }
        levelUpHeader.setText(levelUpMessage);
        levelUpHeader.setTextColor(Color.BLUE.getRGB());
        levelUpHeader.setFontId(FontID.QUILL_8);
        levelUpHeader.setXPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpHeader.setOriginalX(73 + X_OFFSET);
        levelUpHeader.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpHeader.setOriginalY(15 + Y_OFFSET);
        levelUpHeader.setOriginalWidth(390);
        levelUpHeader.setOriginalHeight(30);
        levelUpHeader.setXTextAlignment(WidgetTextAlignment.CENTER);
        levelUpHeader.setYTextAlignment(WidgetTextAlignment.LEFT);
        levelUpHeader.setWidthMode(WidgetSizeMode.ABSOLUTE);
        levelUpHeader.revalidate();

        final String levelUpTextMessage;
        if (skillExperience == Experience.MAX_SKILL_XP) {
            levelUpTextMessage = "You have reached maximum experience in " + skillName;
        } else {
            levelUpTextMessage = (skill == Skill.HITPOINTS
                    ? "Your Hitpoints prestige is now " + level
                    : "Your " + skillName + " prestige level is now " + level)
                    + '.';
        }
        levelUpText.setText(levelUpTextMessage);
        levelUpText.setFontId(FontID.QUILL_8);
        levelUpText.setXPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpText.setOriginalX(73 + X_OFFSET);
        levelUpText.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpText.setOriginalY(44 + Y_OFFSET);
        levelUpText.setOriginalWidth(390);
        levelUpText.setOriginalHeight(30);
        levelUpText.setXTextAlignment(WidgetTextAlignment.CENTER);
        levelUpText.setYTextAlignment(WidgetTextAlignment.LEFT);
        levelUpText.setWidthMode(WidgetSizeMode.ABSOLUTE);
        levelUpText.revalidate();

        levelUpContinue.setText("Click here to continue");
        levelUpContinue.setTextColor(Color.BLUE.getRGB());
        levelUpContinue.setFontId(FontID.QUILL_8);
        levelUpContinue.setXPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpContinue.setOriginalX(73 + X_OFFSET);
        levelUpContinue.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpContinue.setOriginalY(74 + Y_OFFSET);
        levelUpContinue.setOriginalWidth(390);
        levelUpContinue.setOriginalHeight(17);
        levelUpContinue.setXTextAlignment(WidgetTextAlignment.CENTER);
        levelUpContinue.setYTextAlignment(WidgetTextAlignment.LEFT);
        levelUpContinue.setWidthMode(WidgetSizeMode.ABSOLUTE);
        levelUpContinue.setAction(0, "Continue");
        levelUpContinue.setOnOpListener((JavaScriptCallback) ev -> triggerCloseViaMessage());
        levelUpContinue.setOnMouseOverListener((JavaScriptCallback) ev -> levelUpContinue.setTextColor(Color.WHITE.getRGB()));
        levelUpContinue.setOnMouseLeaveListener((JavaScriptCallback) ev -> levelUpContinue.setTextColor(Color.BLUE.getRGB()));
        levelUpContinue.setHasListener(true);
        levelUpContinue.revalidate();

        for (SkillModel skillModel : skillModels) {
            buildWidgetModel(chatboxContainer, skillModel);
        }

        plugin.getChatMessageManager().queue(QueuedMessage.builder()
                .type(ChatMessageType.GAMEMESSAGE)
                .runeLiteFormattedMessage(skillExperience == Experience.MAX_SKILL_XP
                        ? "Congratulations, you've just reached max experience in " + skillName + '!'
                        : "Congratulations, you've just advanced your " + skillName + " prestige level. You are now prestige level " + level + '.')
                .build());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() != ' ') {
            return;
        }

        triggerCloseViaMessage();

        e.consume();
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public void closeIfTriggered() {
        if (closeMessage && plugin.getChatboxPanelManager().getCurrentInput() == this) {
            plugin.getChatboxPanelManager().close();
        }
    }

    void triggerClose() {
        closeMessage = true;
    }

    private void triggerCloseViaMessage() {
        final Widget levelUpContinue = plugin.getClient().getWidget(ComponentID.CHATBOX_CONTAINER).getChild(2);
        levelUpContinue.setText("Please wait...");

        closeMessage = true;
    }

    private static void buildWidgetModel(Widget chatboxContainer, SkillModel model) {
        final Widget levelUpModel = chatboxContainer.createChild(-1, WidgetType.MODEL);

        levelUpModel.setModelId(model.getModelId());
        levelUpModel.setXPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpModel.setOriginalX(model.getOriginalX() + X_OFFSET);
        levelUpModel.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        levelUpModel.setOriginalY(model.getOriginalY() + Y_OFFSET);
        levelUpModel.setOriginalWidth(model.getIconWidth());
        levelUpModel.setOriginalHeight(model.getIconHeight());
        levelUpModel.setRotationX(model.getRotationX());
        levelUpModel.setRotationY(model.getRotationY());
        levelUpModel.setRotationZ(model.getRotationZ());
        levelUpModel.setModelZoom(model.getModelZoom());
        levelUpModel.revalidate();
    }

    private void setFireworksGraphic(LevelUpFireworks firework) {
        final Player localPlayer = plugin.getClient().getLocalPlayer();
        if (localPlayer == null) {
            return;
        }

        final int fireworksGraphic = firework.getGraphicId();

        if (fireworksGraphic == -1) {
            return;
        }

        localPlayer.setGraphic(fireworksGraphic);
        localPlayer.setSpotAnimFrame(0);
    }
}
