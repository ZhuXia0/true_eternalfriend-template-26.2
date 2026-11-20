# True Eternal Friend

Fabric mod for Minecraft 26.2. Use the Eternal Friend Tag on a tamed wolf to add 10 attack damage. The wolf revives after 12,000 running server ticks (10 minutes) at its original position and dimension.

## Versions

Minecraft 26.2; Java 25; Fabric Loader 0.20.1; Fabric API 0.161.0+26.2; Loom 1.19-SNAPSHOT and Gradle 9.8.2 from the supplied template. The requested Loom 1.17 and Gradle 9.5.1 differ from this template; the template versions were tested.

## Install and build

Install Fabric Loader and Fabric API, then place build/libs/true_eternal_friend-1.0.0.jar in mods. Run ./gradlew build (Windows: .\\gradlew.bat build). Run ./gradlew runServer for a development server. On first run, review and accept the Minecraft EULA yourself.

## Test

Craft the tag shapelessly from one diamond and one totem of undying. Use it on a tamed wolf, then check the attack attribute, duplicate-use message, and item consumption in survival versus creative. Use /kill on the wolf, keep the server running for 10 minutes, and verify revival. Restart the save while the countdown is pending to check persistence. The countdown pauses while the server is stopped. Unloaded original chunks are retried every 20 ticks; offline owners are supported. The default policy always uses the original location.

The mod has its own Creative Mode tab, with the Eternal Friend Tag as its icon and item. Two advancements are awarded once per player: "True Eternal Friend" when a player first binds a tamed wolf, and "Goodbye? Hello." when one of their eternal wolves first revives. If the owner is offline at revival, the award remains in the saved queue and is granted after they log in. The tag user and wolf owner can be different players; each advancement follows its stated event and recipient.

## Texture generation

Run python tools/gen_tag_texture.py with Pillow installed. This deterministic fallback produces the 16x16 RGBA item texture and a 64x64 mod icon. An image generation tool can instead use the prompt in the task request and replace the texture.

## 26.2 differences and limitations

Minecraft 26.2 uses Mojang package names, Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE, Identifier.fromNamespaceAndPath, Item.Properties.setId, ValueInput/ValueOutput, SavedDataType, ServerTickEvents.END_LEVEL_TICK, and EntityTypes.WOLF. These were checked against locally cached 26.2 Minecraft and Fabric API jars. A minimal Wolf mixin stores the permanent marker. The Fabric ALLOW_DEATH event removes the wolf before vanilla drops and experience. A missing dimension or unloaded original chunk delays revival; the wolf is never moved to its owner.

The development server loaded the mod but stopped at the initial EULA prompt, so in-game behavior has not been manually tested.
