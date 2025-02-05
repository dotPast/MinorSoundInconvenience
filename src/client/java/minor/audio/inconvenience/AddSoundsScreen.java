package minor.audio.inconvenience;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.SliderComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
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
        root.surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout soundList = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        FlowLayout header = Containers.horizontalFlow(Sizing.fill(), Sizing.content());
        FlowLayout soundLayout = Containers.verticalFlow(Sizing.content(), Sizing.content());

        TextBoxComponent searchBar = Components.textBox(Sizing.expand()).text("");

        generateEntries(soundLayout, searchBar.getText());

        searchBar.onChanged().subscribe((query) -> {
            soundLayout.clearChildren();
            generateEntries(soundLayout, query);
        });

        header.child(Components.label(Text.translatable("text.menu.minor-audio-inconvenience.add.title")).verticalTextAlignment(VerticalAlignment.CENTER).sizing(Sizing.content(), Sizing.fixed(20)));
        header.child(Components.label(Text.translatable("text.menu.minor-audio-inconvenience.add.search").append(":")).verticalTextAlignment(VerticalAlignment.CENTER).sizing(Sizing.content(), Sizing.fixed(20)).margins(Insets.left(16)));
        header.child(searchBar.margins(Insets.left(4)));

        soundList.child(header);
        soundList.child(soundLayout);

        root.child(
                Containers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(
                                Containers.verticalScroll(Sizing.fill(80), Sizing.fill(75), soundList)
                        )
                        .padding(Insets.of(10))
                        .surface(Surface.DARK_PANEL)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .horizontalAlignment(HorizontalAlignment.CENTER)
        );
    }

    public void generateEntries(FlowLayout layout, String searchQuery) {
        MinecraftClient gameInstance = MinecraftClient.getInstance();
        SoundManager soundManager = gameInstance.getSoundManager();

        Map<String, CollapsibleContainer> containers = new HashMap<>();

        for (Identifier soundID : soundManager.getKeys()) {
            if (searchQuery.length() == 0) {
                String soundNamespace = soundID.getNamespace();

                CollapsibleContainer container;

                if (containers.isEmpty() || !containers.containsKey(soundNamespace)) {
                    container = Containers.collapsible(Sizing.content(), Sizing.content(), Text.literal(soundNamespace), false);
                } else {
                    container = containers.get(soundNamespace);
                }

                container.child(generateSoundComponent(soundID));

                containers.put(soundNamespace, container);
            } else {
                if (soundID.getPath().contains(searchQuery)) {
                    String soundNamespace = soundID.getNamespace();

                    CollapsibleContainer container;

                    if (containers.isEmpty() || !containers.containsKey(soundNamespace)) {
                        container = Containers.collapsible(Sizing.content(), Sizing.content(), Text.literal(soundNamespace), false);
                    } else {
                        container = containers.get(soundNamespace);
                    }

                    container.child(generateSoundComponent(soundID));

                    containers.put(soundNamespace, container);
                }
            }
        }

        for (CollapsibleContainer container : containers.values()) {
            layout.child(container);
        }
    }

    public GridLayout generateSoundComponent(Identifier soundID) {
        MinecraftClient gameInstance = MinecraftClient.getInstance();
        SoundManager soundManager = gameInstance.getSoundManager();

        GridLayout soundComponent = Containers.grid(Sizing.expand(), Sizing.content(), 1, 2);

        FlowLayout soundInfo = Containers.horizontalFlow(Sizing.expand(50), Sizing.content());
        FlowLayout soundInteraction = Containers.horizontalFlow(Sizing.expand(50), Sizing.content());

        soundInfo.child(
                Components.button(Text.literal("▶"), button -> {
                            SoundInstance soundInstance;
                            if (gameInstance.getServer() != null || gameInstance.getCurrentServerEntry() != null) {
                                if (gameInstance.player != null) {
                                    soundInstance = new PositionedSoundInstance(
                                            SoundEvent.of(soundID),
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
                                            SoundEvent.of(soundID),
                                            SoundCategory.MASTER,
                                            Random.create()
                                    ) {
                                    };
                                }
                            } else {
                                soundInstance = new AbstractSoundInstance(
                                        SoundEvent.of(soundID),
                                        SoundCategory.MASTER,
                                        Random.create()
                                ) {
                                };
                            }

                            soundManager.play(soundInstance);
                        })
                        .margins(Insets.right(4))
                        .sizing(Sizing.fixed(20))
        );

        soundInfo.child(
                Components.label(Text.literal(soundID.getPath())).verticalTextAlignment(VerticalAlignment.CENTER).sizing(Sizing.content(), Sizing.fixed(20))
        );

        SliderComponent volumeSlider = Components.slider(Sizing.fill(10)).message(
                (String progress) -> Text.translatable("text.menu.minor-audio-inconvenience.add.volumeslider").append(Text.literal(String.format(
                        ": %s",
                        (int) Math.ceil(Double.parseDouble(progress) * 100)
                )).append("%"))
        ).value(1).scrollStep(0.01);

        if (gameInstance.options.getGuiScale().getValue() == 4) {
            volumeSlider.setWidth(50);
            volumeSlider.message((String progress) -> Text.literal(String.valueOf((int) Math.ceil(Double.parseDouble(progress) * 100))).append("%"));
        } else {
            volumeSlider.setWidth(100);
        }

        soundInteraction.child(
                volumeSlider.margins(Insets.right(4))
        );

        for (String configId : MinorAudioInconvenience.CONFIG.soundList()) {
            String[] configSplit = configId.split("=");

            if (configSplit[0].equals(soundID.toString())) {
                volumeSlider.value(Double.parseDouble(configSplit[1]));
            }
        }

        volumeSlider.onChanged().subscribe(
                (value) -> {
                    Double volume = Math.ceil(value * 100) / 100;

                    MinorAudioInconvenience.CONFIG.soundList().removeIf(entry -> entry.split("=")[0].equals(soundID.toString()));

                    if (value != 1) {
                        MinorAudioInconvenience.CONFIG.soundList().add(String.format("%s=%s", soundID, volume));
                    }
                    MinorAudioInconvenience.CONFIG.save();
                }
        );

        soundComponent.child(soundInfo.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER), 0, 0);
        soundComponent.child(soundInteraction.alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER), 0, 1);

        return soundComponent;
    }
}
