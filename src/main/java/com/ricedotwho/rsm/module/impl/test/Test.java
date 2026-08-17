package com.ricedotwho.rsm.module.impl.test;

import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.EnumSetSetting;

import java.util.List;

@ModuleInfo(id = "Test", aliases = "Test")
public class Test extends Module {
    private final EnumSetSetting<TestEnum> testEnumEnumSetSetting = new EnumSetSetting<>(
            "Test",
            TestEnum.class,
            List.of(),
            null,
            null,
            "Description"
    );

    private enum TestEnum {
        OPTION_1,
        OPTION_2,
        OPTION_3,
        OPTION_4
    }
}
