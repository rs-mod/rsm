package com.ricedotwho.rsm.event.impl.render;

import com.ricedotwho.rsm.event.Event;
import lombok.Getter;

public sealed abstract class GuiRender extends Event {
    
    @Getter
    public final static class CursorReset extends GuiRender { }

    @Getter
    public final static class CursorSet extends GuiRender { }
}
