package com.ricedotwho.rsm.module.api.settings.impl;//package com.ricedotwho.rsm.ui.clickgui.settings.impl;
//
//import com.google.gson.JsonObject;
//import com.ricedotwho.rsm.module.api.settings.Setting;
//import lombok.Getter;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.BooleanSupplier;
//
//@Getter
//public class MultiSetting extends Setting<ArrayList<String>> {
//
//    private ArrayList<String> values;
//
//
//    public MultiSetting(String name, List<String> defaultValues, List<String> modes, BooleanSupplier supplier) {
//        super(name, supplier);
//        this.value = new ArrayList<>(defaultValues);
//        this.values = new ArrayList<>(modes);
//    }
//
//    public MultiSetting(String name, List<String> defaultValues, List<String> modes) {
//        super(name, null);
//        this.value = new ArrayList<>(defaultValues);
//        this.values = new ArrayList<>(modes);
//    }
//
//    public boolean isChecked(String value){
//        return this.value.contains(value);
//    }
//
//    public void setChecked(String value, boolean state){
//        if (state){
//            if (!this.value.contains(value)){
//                this.value.add(value);
//            }
//        } else {
//            this.value.remove(value);
//        }
//    }
//
//    @Override
//    public void loadFromJson(JsonObject obj) {
//
//    }
//
//    @Override
//    public void saveToJson(JsonObject obj) {
//
//    }
//
//    @Override
//    public String getType() {
//        return "multi";
//    }
//}
