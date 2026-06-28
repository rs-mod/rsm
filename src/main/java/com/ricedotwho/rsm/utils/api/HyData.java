//package com.ricedotwho.rsm.utils.api;
//
//import com.google.gson.annotations.SerializedName;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.val;
//
//import java.util.*;
//import java.util.regex.Pattern;
//
///*
// * Original code Copyright (c) 2026, odtheking (https://github.com/odtheking/Odin/blob/main/src/main/kotlin/com/odtheking/odin/utils/network/hypixelapi/HypixelData.kt)
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *
// * 1. Redistributions of source code must retain the above copyright notice,
// *    this list of conditions and the following disclaimer.
// *
// * 2. Redistributions in binary form must reproduce the above copyright notice,
// *    this list of conditions and the following disclaimer in the documentation
// *    and/or other materials provided with the distribution.
// *
// * 3. Neither the name of the copyright holder nor the names of its contributors
// *    may be used to endorse or promote products derived from this software
// *    without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// * POSSIBILITY OF SUCH DAMAGE.
// */
//
//public class HyData {
//    public static final Pattern MP_REGEX = Pattern.compile("§7§4☠ §cRequires §5.+§c\\.");
//
//    @Data
//    @NoArgsConstructor
//    public static class PlayerInfo {
//        private ProfilesData profileData;
//        private String uuid;
//        private String name;
//
//        public MemberData getMemberData() {
//            if (profileData == null) return null;
//            return profileData.getProfiles().stream()
//                    .filter(Profiles::isSelected)
//                    .findFirst()
//                    .map(p -> p.getMembers().get(uuid))
//                    .orElse(null);
//        }
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class ProfilesData {
//        private String error;
//        private String cause;
//
//        @SerializedName("profiles")
//        private List<Profiles> profileList = new ArrayList<>();
//
//        public List<Profiles> getProfiles() {
//            return profileList == null ? Collections.emptyList() : profileList;
//        }
//
//        public String getFailed() {
//            return error != null ? error : cause;
//        }
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class Profiles {
//        @SerializedName("profile_id")
//        private String profileId;
//        private Map<String, MemberData> members = new HashMap<>();
//        @SerializedName("game_mode")
//        private String gameMode = "normal";
//        private BankingData banking = new BankingData();
//        @SerializedName("cute_name")
//        private String cuteName;
//        private boolean selected;
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class MemberData {
//        private RiftData rift = new RiftData();
//        @SerializedName("accessory_bag_storage")
//        private AccessoryBagStorage accessoryBagStorage = new AccessoryBagStorage();
//        @SerializedName("item_data")
//        private MiscItemData miscItemData = new MiscItemData();
//        private CurrencyData currencies = new CurrencyData();
//        private DungeonsData dungeons = new DungeonsData();
//        @SerializedName("pets_data")
//        private PetsData pets = new PetsData();
//        @SerializedName("player_id")
//        private String playerId;
//        @SerializedName("nether_island_player_data")
//        private CrimsonIsle crimsonIsle = new CrimsonIsle();
//        @SerializedName("player_stats")
//        private PlayerStats playerStats = new PlayerStats();
//        private Inventory inventory = new Inventory();
//        private Map<String, Long> collection = new HashMap<>();
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class PlayerStats {
//        private Map<String, Float> kills = new HashMap<>();
//        private Map<String, Float> deaths = new HashMap<>();
//
//        public int getBloodMobKills() {
//            return (int)(kills.getOrDefault("watcher_summon_undead", 0f) + kills.getOrDefault("master_watcher_summon_undead", 0f));
//        }
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class CrimsonIsle {
//        private Abiphone abiphone = new Abiphone();
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class Abiphone {
//        @SerializedName("active_contacts")
//        private List<String> activeContacts = new ArrayList<>();
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class RiftData {
//        private RiftAccess access = new RiftAccess();
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class RiftAccess {
//        @SerializedName("consumed_prism")
//        private boolean consumedPrism;
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class PetsData {
//        private List<Pet> pets = new ArrayList<>();
//        public Pet getActivePet() {
//            return pets.stream().filter(Pet::isActive).findFirst().orElse(null);
//        }
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class Pet {
//        private String uuid;
//        private String uniqueId;
//        private String type = "";
//        private double exp;
//        private boolean active;
//        private String tier = "";
//        private String heldItem;
//        private int candyUsed;
//        private String skin;
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class DungeonsData {
//        @SerializedName("dungeon_types")
//        private DungeonTypes dungeonTypes = new DungeonTypes();
//        @SerializedName("player_classes")
//        private Map<String, ClassData> classes = new HashMap<>();
//        @SerializedName("selected_dungeon_class")
//        private String selectedClass;
//        @SerializedName("daily_runs")
//        private DailyRunData dailyRuns = new DailyRunData();
//        @SerializedName("last_dungeon_run")
//        private String lastDungeonRun;
//        private long secrets;
//
//        public int getTotalRuns() {
//            int total = 0;
//            for (int i = 1; i < 7; i++) {
//                String tier = String.valueOf(i);
//                total += (int) (this.dungeonTypes.catacombs.tierComps.getOrDefault(tier, 0F) + this.dungeonTypes.masterMode.tierComps.getOrDefault(tier, 0F));
//            }
//            return total;
//        }
//
//        public float getAvgSecrets() {
//            float totalRuns = getTotalRuns();
//            if (totalRuns > 0) return totalRuns / secrets;
//            return 0f;
//        }
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class DungeonTypes {
//        private DungeonTypeData catacombs = new DungeonTypeData();
//        @SerializedName("master_catacombs")
//        private DungeonTypeData masterMode = new DungeonTypeData();
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class DailyRunData {
//        @SerializedName("current_day_stamp")
//        private Long currentDayStamp;
//        @SerializedName("completed_runs_count")
//        private long completedRunsCount;
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class ClassData {
//        private double experience;
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class DungeonTypeData {
//        @SerializedName("times_played") private Map<String, Double> timesPlayed;
//        private double experience;
//        @SerializedName("tier_completions") private Map<String, Float> tierComps = new HashMap<>();
//        @SerializedName("milestone_completions")
//        private final Map<String, Float> milestoneComps = new HashMap<>();
//        @SerializedName("fastest_time")
//        private final Map<String, Float> fastestTimes = new HashMap<>();
//        @SerializedName("best_score")
//        private final Map<String, Float> bestScore = new HashMap<>();
//        @SerializedName("mobs_killed")
//        private final Map<String, Float> mobsKilled = new HashMap<>();
//        @SerializedName("most_mobs_killed")
//        private final Map<String, Float> mostMobsKilled = new HashMap<>();
//        @SerializedName("most_damage_berserk")
//        private final Map<String, Double> mostDamageBers = new HashMap<>();
//        @SerializedName("most_healing")
//        private final Map<String, Double> mostHealing = new HashMap<>();
//        @SerializedName("watcher_kills")
//        private final Map<String, Float> watcherKills = new HashMap<>();
//        @SerializedName("highest_tier_completed")
//        private int highestTierComp = 0;
//        @SerializedName("most_damage_tank")
//        private final Map<String, Double> mostDamageTank = new HashMap<>();
//        @SerializedName("most_damage_healer")
//        private final Map<String, Double> mostDamageHealer = new HashMap<>();
//        @SerializedName("fastest_time_s")
//        private final Map<String, Double> fastestTimeS = new HashMap<>();
//        @SerializedName("most_damage_mage")
//        private final Map<String, Double> mostDamageMage = new HashMap<>();
//        @SerializedName("fastest_time_s_plus")
//        private final Map<String, Double> fastestTimeSPlus = new HashMap<>();
//        @SerializedName("most_damage_Archer")
//        private final Map<String, Double> mostDamageArcher = new HashMap<>();
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class CurrencyData {
//        @SerializedName("coin_purse")
//        private double coins;
//        @SerializedName("motes_purse")
//        private double motes;
//        private Map<String, EssenceData> essence = new HashMap<>();
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class EssenceData {
//        private long current;
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class MiscItemData {
//        private long soulflow;
//        @SerializedName("favorite_arrow")
//        private String favoriteArrow;
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class AccessoryBagStorage {
//        private TuningData tuning = new TuningData();
//        @SerializedName("selected_power")
//        String selectedPower = null;
//        @SerializedName("unlocked_powers")
//        private List<String> unlockedPowers = new ArrayList<>();
//        @SerializedName("bag_upgrades_purchased")
//        private int bagUpgrades = 0;
//        @SerializedName("highest_magical_power")
//        private long highestMP = 0;
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class TuningData {
//        @SerializedName("slot_0")
//        private Map<String, Integer> currentTunings = new HashMap<>();
//        private int highestUnlockedSlot;
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class Inventory {
//        @SerializedName("inv_contents")
//        private InventoryContents invContents = new InventoryContents();
//        @SerializedName("ender_chest_contents")
//        private InventoryContents eChestContents = new InventoryContents();
//        @SerializedName("backpack_icons")
//        private Map<String, InventoryContents> backpackIcons = new HashMap<>();
//        @SerializedName("bag_contents")
//        private Map<String, InventoryContents> bagContents = new HashMap<>();
//        @SerializedName("inv_armor")
//        private InventoryContents invArmor = new InventoryContents();
//        @SerializedName("equipment_contents")
//        private InventoryContents equipment = new InventoryContents();
//        @SerializedName("wardrobe_equipped_slot")
//        private Integer wardrobeEquipped = null;
//        @SerializedName("backpack_contents")
//        private Map<String, InventoryContents> backpackContents = new HashMap<>();
//        @SerializedName("sacks_counts")
//        private Map<String, Long> sacks = new HashMap<>();
//        @SerializedName("personal_vault_contents")
//        private InventoryContents personalVault = new InventoryContents();
//        @SerializedName("wardrobe_contents")
//        private InventoryContents wardrobeContents = new InventoryContents();
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class BankingData {
//        private double balance;
//    }
//
//    @Data
//    public static class InventoryContents {
//        private Integer type;
//        private String data = "";
//
//        public InventoryContents() {
//
//        }
//
//        public List<ItemData> getItemStacks() {
//            // TODO: idk
//            return Collections.emptyList();
//        }
//    }
//
//    @Data
//    @NoArgsConstructor
//    public static class ItemData {
//        private String name;
//        private String id;
//        private List<String> lore = new ArrayList<>();
//    }
//}
//
