package com.ricedotwho.rsm.packet.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record ClientboundZeroHello(String version) implements CustomPacketPayload {
    public static final StreamCodec<@NotNull FriendlyByteBuf, @NotNull ClientboundZeroHello> CODEC = CustomPacketPayload.codec(ClientboundZeroHello::write, ClientboundZeroHello::new);
    public static final Type<@NotNull ClientboundZeroHello> TYPE = new Type<>(Identifier.fromNamespaceAndPath("zero", "hello"));

    public ClientboundZeroHello(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.version);
    }

    @Override
    public @NotNull Type<@NotNull ClientboundZeroHello> type() {
        return TYPE;
    }
}
