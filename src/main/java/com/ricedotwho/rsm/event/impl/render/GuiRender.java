package com.ricedotwho.rsm.event.impl.render;

import com.ricedotwho.rsm.event.Event;
import lombok.Getter;

public class GuiRender extends Event {
    
    @Getter
    public static class Start extends GuiRender { }

    @Getter
    public static class End extends GuiRender { }
}
