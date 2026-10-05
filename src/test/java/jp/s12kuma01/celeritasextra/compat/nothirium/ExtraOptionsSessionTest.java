package jp.s12kuma01.celeritasextra.compat.nothirium;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExtraOptionsSessionTest {
    @Test void editsAcrossChildPagesStayPendingAndOnlyCommitChangedEntries() {
        var toggleProperty = new Property("sky", "true", Property.Type.BOOLEAN);
        toggleProperty.setDefaultValue("true");
        var distanceProperty = new Property("distance", "8", Property.Type.INTEGER);
        distanceProperty.setDefaultValue("0").setMinValue(0).setMaxValue(32);
        var toggle = new ConfigElement(toggleProperty);
        var distance = new ConfigElement(distanceProperty);
        var child = new DummyCategoryElement("fog", "", List.of(distance));
        var root = new DummyCategoryElement("video", "", List.of(toggle, child));
        var session = new ExtraOptionsSession(List.of(root));
        assertEquals(8, session.value(distance), "Forge's string-valued integers must support numeric sliders");
        assertEquals(Boolean.TRUE, session.value(toggle));
        assertFalse(session.changed());
        session.set(toggle, false);
        session.set(distance, 20);
        assertTrue(toggleProperty.getBoolean());
        assertEquals(8, distanceProperty.getInt());
        assertTrue(session.changed());
        assertSame(distance, session.children(child).getFirst());
        session.reset(session.children(child));
        assertEquals(0, session.value(distance), "String-valued defaults must also support numeric sliders");
        session.commit();
        assertFalse(toggleProperty.getBoolean());
        assertEquals(0, distanceProperty.getInt());
    }

    @Test void cancelDoesNotPublishChangesAndEditingAnArrayCannotMutateTheSnapshot() {
        var property = new Property("exemptions", new String[]{"example.Beacon"}, Property.Type.STRING);
        var element = new ConfigElement(property);
        var session = new ExtraOptionsSession(List.of(element));
        Object[] draft = (Object[]) session.value(element);
        draft[0] = "example.*";
        assertFalse(session.changed());
        session.set(element, draft);
        draft[0] = "accidental mutation";
        assertArrayEquals(new String[]{"example.*"}, (Object[]) session.value(element));
        assertArrayEquals(new String[]{"example.Beacon"}, property.getStringList());
        // Discarding the session is the root screen's Cancel/Esc behavior.
        var nextSession = new ExtraOptionsSession(List.of(element));
        assertArrayEquals(new String[]{"example.Beacon"}, (Object[]) nextSession.value(element));
        session.commit();
        assertArrayEquals(new String[]{"example.*"}, property.getStringList());
    }
}
