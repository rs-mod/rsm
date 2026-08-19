package com.ricedotwho.rsm.event.impl.render;

import com.ricedotwho.rsm.event.Event;
import lombok.Getter;

public abstract class GuiRender extends Event {
    
    @Getter
    public static class CursorReset extends GuiRender { }

    @Getter
    public static class CursorSet extends GuiRender { }
}
