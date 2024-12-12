package minor.audio.inconvenience;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.SliderComponent;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.core.Insets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AddSoundsScreen extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        MinecraftClient gameInstance = MinecraftClient.getInstance();
        SoundManager soundManager = gameInstance.getSoundManager();

        root.surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout soundList = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        FlowLayout soundLayout = Containers.verticalFlow(Sizing.content(), Sizing.content());

        Map<String, CollapsibleContainer> containers = new HashMap<>();

        for (Identifier soundID : soundManager.sounds.keySet()) {
            String soundNamespace = soundID.getNamespace();
            String[] splitSoundPath = soundID.getPath().split("\\.");

            CollapsibleContainer container;

            if (containers.isEmpty() || !containers.containsKey(soundNamespace)) {
                container = Containers.collapsible(Sizing.content(), Sizing.content(), Text.literal(soundNamespace), false);
            } else {
                container = containers.get(soundNamespace);
            }

            FlowLayout soundOption = Containers.horizontalFlow(Sizing.content(), Sizing.content());

            soundOption.child(
                    Components.button(Text.literal("▶"), button -> {
                        SoundInstance soundInstance;
                        if (gameInstance.getServer() != null || gameInstance.getCurrentServerEntry() != null) {
                            if (gameInstance.player != null) {
                                soundInstance = new PositionedSoundInstance(
                                        new SoundEvent(soundID, null), 
                                        SoundCategory.MASTER,
                                        1f,
                                        1f,
                                        Random.create(),
                                        gameInstance.player.getX(),
                                        gameInstance.player.getY(),
                                        gameInstance.player.getZ()
                                );
                            } else {
                                soundInstance = new AbstractSoundInstance(
                                        new SoundEvent(soundID, null),
                                        SoundCategory.MASTER,
                                        Random.create()
                                ) {};
                            }
                        } else {
                            soundInstance = new AbstractSoundInstance(
                                    new SoundEvent(soundID, null),
                                    SoundCategory.MASTER,
                                    Random.create()
                            ) {};
                        }

                        gameInstance.getSoundManager().play(soundInstance);
                    })
                            .margins(Insets.of(0, 0, 0, 16))
            );

            soundOption.child(
                    Components.label(Text.literal(soundID.getPath()))
            );

            SliderComponent volumeSlider = Components.slider(Sizing.fill(10)).message(
                    (String progress) -> Text.translatable("text.menu.minor-audio-inconvenience.add.volumeslider.value").append(Text.literal(String.format(
                            ": %s",
                            (int) Math.ceil(Double.parseDouble(progress) * 100)
                    )).append("%"))
            ).value(1).scrollStep(0.01);

            soundOption.child(
                    volumeSlider.margins(Insets.of(0, 0, 16, 16))
            );

            ButtonComponent addToConfigButton = Components.button(Text.translatable("text.menu.minor-audio-inconvenience.add.button.addtoconfig"), button -> {
                Double volume = Math.ceil(volumeSlider.value() * 100) / 100;

                for (String configId : MinorAudioInconvenience.CONFIG.soundList()) {
                    if (configId.split("=")[0].equals(soundID.toString())) {
                        MinorAudioInconvenience.CONFIG.soundList().remove(configId);
                        break;
                    }
                }

                MinorAudioInconvenience.CONFIG.soundList().add(String.format("%s=%s", soundID, volume));
                MinorAudioInconvenience.CONFIG.save();
                button.setMessage(Text.translatable("text.menu.minor-audio-inconvenience.add.button.addtoconfig.success"));
            });

            for (String configId : MinorAudioInconvenience.CONFIG.soundList()) {
                String[] configSplit = configId.split("=");

                if (configSplit[0].equals(soundID.toString())) {
                    volumeSlider.value(Double.parseDouble(configSplit[1]));
                }
            }

            soundOption.child(addToConfigButton);

            container.child(soundOption);

            containers.put(soundNamespace, container);
        }

        for (CollapsibleContainer container : containers.values()) {
            soundLayout.child(container);
        }

        soundList.child(Components.label(Text.translatable("text.menu.minor-audio-inconvenience.add.title")).horizontalTextAlignment(HorizontalAlignment.CENTER));
        soundList.child(soundLayout);

        root.child(
                Containers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(
                                Containers.verticalScroll(Sizing.fill(50), Sizing.fill(50), soundList)
                        )
                        .padding(Insets.of(10))
                        .surface(Surface.DARK_PANEL)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .horizontalAlignment(HorizontalAlignment.CENTER)
        );
    }
}
