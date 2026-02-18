package com.ricedotwho.rsm.module.impl.other;

import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.KeyboardHandler;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

@Getter
@ModuleInfo(aliases = "SessionLogin", id = "SessionLogin", category = Category.OTHER)
public class SessionLogin extends Module {

    public SessionLogin() {
    }

    @Override
    public void onEnable() {
        String sessionString = getClipboard();

        if (sessionString == null || sessionString.isEmpty()) {
            ChatUtils.chat("Clipboard is empty!");
            this.toggle();
            return;
        }

        setSession(sessionString);
        this.toggle();
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void reset() {

    }

    private String getClipboard() {
        try {
            //gets the clipboard
            Minecraft minecraft = Minecraft.getInstance();
            KeyboardHandler keyboard = minecraft.keyboardHandler;

            String clipboard = keyboard.getClipboard();
            return clipboard.trim();

        } catch (Exception e) {
            ChatUtils.chat("either you dont have it copied or the clipboard function isnt working");
            return null;
        }//im still learning try and catch thing
    }

    private UUID formatUUID(String uuidString) {
        //preciate it claude :)
        uuidString = uuidString.replace("-", "");

        // Format as: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx (the uuid)
        String formatted = String.format(
                "%s-%s-%s-%s-%s",
                uuidString.substring(0, 8),
                uuidString.substring(8, 12),
                uuidString.substring(12, 16),
                uuidString.substring(16, 20),
                uuidString.substring(20, 32)
        );

        return UUID.fromString(formatted);
    }

    private String getUsernameFromToken(String token) {
        try {
            // JWT tokens are in format: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            // Decode (base64)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

            // Extract username from "name":"username" in the JSON
            int nameIndex = payload.indexOf("\"name\":\"");
            if (nameIndex == -1) return null;

            int startIndex = nameIndex + 8; // Skip past "name":"
            int endIndex = payload.indexOf("\"", startIndex);

            if (endIndex == -1) return null;

            return payload.substring(startIndex, endIndex);
        } catch (Exception e) {
            return null; // :shrug:
        }
    }

    public void setSession(String sessionString) {
        try {
            String[] parts = sessionString.split(":", 2);

            if (parts.length != 2) {
                ChatUtils.chat("Invalid format! Use: ssid:uuid. make sure there isnt a token: in the front of the msg");
                return;
            }

            String sessionId = parts[0].trim();
            String uuidStr = parts[1].trim();
            UUID uuid = formatUUID(uuidStr);
            String username = getUsernameFromToken(sessionId);
            if (username == null)return;

            Minecraft minecraft = Minecraft.getInstance();
            User newUser = new User(username, uuid, sessionId, Optional.empty(), Optional.empty());

            // Use reflection to set the user field (i have no idea.. this part was ai)
            Field userField = Minecraft.class.getDeclaredField("user");
            userField.setAccessible(true);
            userField.set(minecraft, newUser);

            // Verify the change
            User currentUser = minecraft.getUser();
            ChatUtils.chat("New session:");
            ChatUtils.chat("  Username: " + currentUser.getName());
            ChatUtils.chat("Session changed successfully!");

        } catch (Exception e) {
            ChatUtils.chat("Failed to set session: " + e.getMessage());
        }
    }
}