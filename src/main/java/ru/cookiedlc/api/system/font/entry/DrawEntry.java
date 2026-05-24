package ru.cookiedlc.api.system.font.entry;

import ru.cookiedlc.api.system.font.glyph.Glyph;

public record DrawEntry(float atX, float atY, int color, Glyph toDraw) {
}
