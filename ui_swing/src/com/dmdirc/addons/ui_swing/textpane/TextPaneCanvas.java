/*
 * Copyright (c) 2006-2014 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.textpane;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.ui.messages.IRCDocument;
import com.dmdirc.ui.messages.IRCTextAttribute;
import com.dmdirc.ui.messages.LinePosition;
import com.dmdirc.util.StringUtils;
import com.dmdirc.util.collections.ListenerList;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.MouseInputListener;

import static com.google.common.base.Preconditions.checkNotNull;

/** Canvas object to draw text. */
class TextPaneCanvas extends JPanel implements MouseInputListener,
        ComponentListener, AdjustmentListener, ConfigChangeListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 8;
    /** Hand cursor. */
    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    /** Single Side padding for textpane. */
    private static final int SINGLE_SIDE_PADDING = 3;
    /** Both Side padding for textpane. */
    private static final int DOUBLE_SIDE_PADDING = SINGLE_SIDE_PADDING * 2;
    /** Padding to add to line height. */
    private static final double LINE_PADDING = 0.2;
    /** IRCDocument. */
    private final IRCDocument document;
    /** parent textpane. */
    private final TextPane textPane;
    /** Position -> TextLayout. */
    private final Map<Rectangle, TextLayout> positions;
    /** TextLayout -> Line numbers. */
    private final Map<TextLayout, LineInfo> textLayouts;
    /** Start line. */
    private int startLine;
    /** Selection. */
    private LinePosition selection;
    /** First visible line (from the top). */
    private int firstVisibleLine;
    /** Last visible line (from the top). */
    private int lastVisibleLine;
    /** Config Manager. */
    private final AggregateConfigProvider manager;
    /** Quick copy? */
    private boolean quickCopy;
    /** Mouse click listeners. */
    private final ListenerList listeners = new ListenerList();

    /**
     * Creates a new text pane canvas.
     *
     * @param parent   parent text pane for the canvas
     * @param document IRCDocument to be displayed
     */
    public TextPaneCanvas(final TextPane parent, final IRCDocument document) {
        this.document = document;
        textPane = parent;
        this.manager = parent.getWindow().getContainer().getConfigManager();
        startLine = 0;
        setDoubleBuffered(true);
        setOpaque(true);
        textLayouts = new HashMap<>();
        positions = new HashMap<>();
        selection = new LinePosition(-1, -1, -1, -1);
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
        manager.addChangeListener("ui", "quickCopy", this);

        updateCachedSettings();
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    /**
     * Paints the text onto the canvas.
     *
     * @param graphics graphics object to draw onto
     */
    @Override
    public void paintComponent(final Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics;
        final Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().
                getDesktopProperty("awt.font.desktophints");
        if (desktopHints != null) {
            g.addRenderingHints(desktopHints);
        }
        paintOntoGraphics(g);
    }

    /**
     * Re calculates positions of lines and repaints if required.
     */
    protected void recalc() {
        if (isVisible()) {
            repaint();
        }
    }

    /**
     * Updates cached config settings.
     */
    private void updateCachedSettings() {
        quickCopy = manager.getOptionBool("ui", "quickCopy");
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                recalc();
            }
        });
    }

    private void paintOntoGraphics(final Graphics2D g) {
        final float formatWidth = getWidth() - DOUBLE_SIDE_PADDING;
        final float formatHeight = getHeight();
        float drawPosY = formatHeight;

        textLayouts.clear();
        positions.clear();

        //check theres something to draw and theres some space to draw in
        if (document.getNumLines() == 0 || formatWidth < 1) {
            setCursor(Cursor.getDefaultCursor());
            return;
        }

        // Check the start line is in range
        startLine = Math.max(0, Math.min(document.getNumLines() - 1, startLine));

        //sets the last visible line
        lastVisibleLine = startLine;
        firstVisibleLine = startLine;

        // Iterate through the lines
        for (int line = startLine; line >= 0; line--) {
            drawPosY = paintLineOntoGraphics(g, formatWidth, formatHeight, drawPosY, line);
            if (drawPosY <= 0) {
                break;
            }
        }

        checkForLink();
    }

    private float paintLineOntoGraphics(final Graphics2D g, final float formatWidth,
            final float formatHeight, final float drawPosY, final int line) {
        final AttributedCharacterIterator iterator = document.getStyledLine(line);
        final int lineHeight = (int) (document.getLineHeight(line) * (LINE_PADDING + 1));
        final int paragraphStart = iterator.getBeginIndex();
        final int paragraphEnd = iterator.getEndIndex();
        final LineBreakMeasurer lineMeasurer =
                new LineBreakMeasurer(iterator, g.getFontRenderContext());
        lineMeasurer.setPosition(paragraphStart);

        final int wrappedLine = getNumWrappedLines(lineMeasurer, paragraphStart,
                paragraphEnd, formatWidth);
        float newDrawPosY = drawPosY;

        if (wrappedLine > 1) {
            newDrawPosY -= lineHeight * wrappedLine;
        }

        if (line == startLine) {
            newDrawPosY += DOUBLE_SIDE_PADDING;
        }

        int numberOfWraps = 0;
        int chars = 0;
        // Loop through each wrapped line
        while (lineMeasurer.getPosition() < paragraphEnd) {
            final TextLayout layout = checkNotNull(lineMeasurer.nextLayout(formatWidth));

            // Calculate the Y offset
            if (wrappedLine == 1) {
                newDrawPosY -= lineHeight;
            } else if (numberOfWraps != 0) {
                newDrawPosY += lineHeight;
            }

            // Calculate the initial X position
            final float drawPosX;
            if (layout.isLeftToRight()) {
                drawPosX = SINGLE_SIDE_PADDING;
            } else {
                drawPosX = formatWidth - layout.getAdvance();
            }

            // Check if the target is in range
            if (newDrawPosY >= 0 || newDrawPosY <= formatHeight) {
                g.setColor(textPane.getForeground());

                layout.draw(g, drawPosX, newDrawPosY + layout.getDescent());
                doHighlight(line, chars, layout, g, newDrawPosY, drawPosX);
                firstVisibleLine = line;
                textLayouts.put(layout, new LineInfo(line, numberOfWraps));
                positions.put(new Rectangle(0,
                        (int) (newDrawPosY + 1.5 - layout.getAscent() + layout.getDescent()),
                        (int) formatWidth + DOUBLE_SIDE_PADDING,
                        (int) (layout.getAscent() + layout.getDescent())),
                        layout);
            }

            numberOfWraps++;
            chars += layout.getCharacterCount();
        }
        if (numberOfWraps > 1) {
            newDrawPosY -= lineHeight * (wrappedLine - 1);
        }
        return newDrawPosY;
    }

    /**
     * Returns the number of times a line will wrap.
     *
     * @param lineMeasurer   LineBreakMeasurer to work out wrapping for
     * @param paragraphStart Start index of the paragraph
     * @param paragraphEnd   End index of the paragraph
     * @param formatWidth    Width to wrap at
     *
     * @return Number of times the line wraps
     */
    private int getNumWrappedLines(final LineBreakMeasurer lineMeasurer,
            final int paragraphStart,
            final int paragraphEnd,
            final float formatWidth) {
        int wrappedLine = 0;

        while (lineMeasurer.getPosition() < paragraphEnd) {
            lineMeasurer.nextLayout(formatWidth);
            wrappedLine++;
        }

        lineMeasurer.setPosition(paragraphStart);

        return wrappedLine;
    }

    /**
     * Redraws the text that has been highlighted.
     *
     * @param line     Line number
     * @param chars    Number of characters so far in the line
     * @param layout   Current line textlayout
     * @param g        Graphics surface to draw highlight on
     * @param drawPosY current y location of the line
     * @param drawPosX current x location of the line
     */
    private void doHighlight(final int line, final int chars,
            final TextLayout layout, final Graphics2D g,
            final float drawPosY, final float drawPosX) {
        final LinePosition selectedRange = getSelectedRange();
        final int selectionStartLine = selectedRange.getStartLine();
        final int selectionStartChar = selectedRange.getStartPos();
        final int selectionEndLine = selectedRange.getEndLine();
        final int selectionEndChar = selectedRange.getEndPos();

        //Does this line need highlighting?
        if (selectionStartLine <= line && selectionEndLine >= line) {
            final int firstChar;

            // Determine the first char we care about
            if (selectionStartLine < line || selectionStartChar < chars) {
                firstChar = chars;
            } else {
                firstChar = selectionStartChar;
            }

            // ... And the last
            final int lastChar;
            if (selectionEndLine > line || selectionEndChar > chars + layout.getCharacterCount()) {
                lastChar = chars + layout.getCharacterCount();
            } else {
                lastChar = selectionEndChar;
            }

            // If the selection includes the chars we're showing
            if (lastChar > chars && firstChar < chars + layout.getCharacterCount()) {
                doHighlight(line,
                        layout.getLogicalHighlightShape(firstChar - chars, lastChar - chars), g,
                        drawPosY, drawPosX, firstChar, lastChar);
            }
        }
    }

    private void doHighlight(final int line, final Shape logicalHighlightShape, final Graphics2D g,
            final float drawPosY, final float drawPosX, final int firstChar, final int lastChar) {
        String text = document.getLine(line).getText();
        if (firstChar >= 0 && text.length() > lastChar) {
            text = text.substring(firstChar, lastChar);
        }

        if (text.isEmpty()) {
            return;
        }

        final AttributedCharacterIterator iterator = document.getStyledLine(line);
        if (iterator.getEndIndex() == iterator.getBeginIndex()) {
            return;
        }
        final AttributedString as = new AttributedString(iterator, firstChar, lastChar);

        as.addAttribute(TextAttribute.FOREGROUND, textPane.getBackground());
        as.addAttribute(TextAttribute.BACKGROUND, textPane.getForeground());
        final TextLayout newLayout = new TextLayout(as.getIterator(),
                g.getFontRenderContext());
        final int trans = (int) (newLayout.getDescent() + drawPosY);

        if (firstChar != 0) {
            g.translate(logicalHighlightShape.getBounds().getX(), 0);
        }

        newLayout.draw(g, drawPosX, trans);

        if (firstChar != 0) {
            g.translate(-1 * logicalHighlightShape.getBounds().getX(), 0);
        }
    }

    @Override
    public void adjustmentValueChanged(final AdjustmentEvent e) {
        if (startLine != e.getValue()) {
            startLine = e.getValue();
            recalc();
        }
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        final LineInfo lineInfo = getClickPosition(getMousePosition(), true);
        fireMouseEvents(getClickType(lineInfo), MouseEventType.CLICK, e);

        if (lineInfo.getLine() != -1) {
            final String clickedText = document.getLine(lineInfo.getLine()).getText();

            final int start;
            final int end;
            if (lineInfo.getIndex() == -1) {
                start = -1;
                end = -1;
            } else {
                final int[] extent = StringUtils.indiciesOfWord(clickedText, lineInfo.getIndex());
                start = extent[0];
                end = extent[1];
            }

            if (e.getClickCount() == 2) {
                setSelection(lineInfo.getLine(), start, end, e.isShiftDown());
            } else if (e.getClickCount() == 3) {
                setSelection(lineInfo.getLine(), 0, clickedText.length(), e.isShiftDown());
            }
        }
    }

    /**
     * Sets the selection to a range of characters on the specified line. If quick copy is enabled,
     * the selection will be copied.
     *
     * @param line The line of the selection
     * @param start The start of the selection
     * @param end The end of the selection
     * @param copyControlCharacters Whether or not to copy control characters.
     */
    private void setSelection(final int line, final int start, final int end,
            final boolean copyControlCharacters) {
        selection.setStartLine(line);
        selection.setEndLine(line);
        selection.setStartPos(start);
        selection.setEndPos(end);
        if (quickCopy) {
            textPane.copy(copyControlCharacters);
            clearSelection();
        }
    }

    /**
     * Returns the type of text this click represents.
     *
     * @param lineInfo Line info of click.
     *
     * @return Click type for specified position
     */
    public ClickTypeValue getClickType(final LineInfo lineInfo) {
        if (lineInfo.getLine() != -1) {
            final AttributedCharacterIterator iterator = document.getStyledLine(
                    lineInfo.getLine());
            final int index = lineInfo.getIndex();
            if (index >= iterator.getBeginIndex() && index <= iterator.
                    getEndIndex()) {
                iterator.setIndex(lineInfo.getIndex());
                final Object linkAttribute = iterator.getAttributes()
                        .get(IRCTextAttribute.HYPERLINK);
                if (linkAttribute instanceof String) {
                    return new ClickTypeValue(ClickType.HYPERLINK, (String) linkAttribute);
                }
                final Object channelAttribute = iterator.getAttributes()
                        .get(IRCTextAttribute.CHANNEL);
                if (channelAttribute instanceof String) {
                    return new ClickTypeValue(ClickType.CHANNEL, (String) channelAttribute);
                }
                final Object nickAttribute = iterator.getAttributes()
                        .get(IRCTextAttribute.NICKNAME);
                if (nickAttribute instanceof String) {
                    return new ClickTypeValue(ClickType.NICKNAME, (String) nickAttribute);
                }
            } else {
                return new ClickTypeValue(ClickType.NORMAL, "");
            }
        }
        return new ClickTypeValue(ClickType.NORMAL, "");
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        fireMouseEvents(getClickType(getClickPosition(e.getPoint(), false)),
                MouseEventType.PRESSED, e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            highlightEvent(MouseEventType.CLICK, e);
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        fireMouseEvents(getClickType(getClickPosition(e.getPoint(), false)),
                MouseEventType.RELEASED, e);
        if (quickCopy) {
            textPane.copy((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    clearSelection();
                }
            });
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            highlightEvent(MouseEventType.RELEASED, e);
        }
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        if (e.getModifiersEx() == InputEvent.BUTTON1_DOWN_MASK) {
            highlightEvent(MouseEventType.DRAG, e);
        }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        checkForLink();
    }

    /** Checks for a link under the cursor and sets appropriately. */
    private void checkForLink() {
        final AttributedCharacterIterator iterator = getIterator(getMousePosition());

        if (iterator != null
                && (iterator.getAttribute(IRCTextAttribute.HYPERLINK) != null
                || iterator.getAttribute(IRCTextAttribute.CHANNEL) != null
                || iterator.getAttribute(IRCTextAttribute.NICKNAME) != null)) {
            setCursor(HAND_CURSOR);
            return;
        }

        if (getCursor() == HAND_CURSOR) {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Retrieves a character iterator for the text at the specified mouse position.
     *
     * @since 0.6.4
     * @param mousePosition The mouse position to retrieve text for
     *
     * @return A corresponding character iterator, or null if the specified mouse position doesn't
     *         correspond to any text
     */
    private AttributedCharacterIterator getIterator(final Point mousePosition) {
        final LineInfo lineInfo = getClickPosition(mousePosition, false);

        if (lineInfo.getLine() != -1
                && document.getLine(lineInfo.getLine()) != null) {
            final AttributedCharacterIterator iterator = document.getStyledLine(lineInfo.getLine());

            if (lineInfo.getIndex() < iterator.getBeginIndex()
                    || lineInfo.getIndex() > iterator.getEndIndex()) {
                return null;
            }

            iterator.setIndex(lineInfo.getIndex());
            return iterator;
        }

        return null;
    }

    /**
     * Sets the selection for the given event.
     *
     * @param type mouse event type
     * @param e    responsible mouse event
     */
    protected void highlightEvent(final MouseEventType type, final MouseEvent e) {
        if (isVisible()) {
            final Point point = e.getLocationOnScreen();
            SwingUtilities.convertPointFromScreen(point, this);
            if (!contains(point)) {
                final Rectangle bounds = getBounds();
                final Point mousePos = e.getPoint();
                if (mousePos.getX() < bounds.getX()) {
                    point.setLocation(bounds.getX() + SINGLE_SIDE_PADDING, point.getY());
                } else if (mousePos.getX() > bounds.getX() + bounds.getWidth()) {
                    point.setLocation(bounds.getX() + bounds.getWidth()
                            - SINGLE_SIDE_PADDING, point.getY());
                }

                if (mousePos.getY() < bounds.getY()) {
                    point.setLocation(point.getX(), bounds.getY() + DOUBLE_SIDE_PADDING);
                } else if (mousePos.getY() > bounds.getY() + bounds.getHeight()) {
                    point.setLocation(bounds.getX() + bounds.getWidth() - SINGLE_SIDE_PADDING,
                            bounds.getY() + bounds.getHeight() - DOUBLE_SIDE_PADDING - 1);
                }
            }
            LineInfo info = getClickPosition(point, true);
            final Rectangle first = getFirstLineRectangle();
            final Rectangle last = getLastLineRectangle();
            if (info.getLine() == -1 && info.getPart() == -1 && contains(point)
                    && document.getNumLines() != 0 && first != null && last != null) {
                if (first.getY() >= point.getY()) {
                    info = getFirstLineInfo();
                } else if (last.getY() <= point.getY()) {
                    info = getLastLineInfo();
                }
            }

            if (info.getLine() != -1 && info.getPart() != -1) {
                if (type == MouseEventType.CLICK) {
                    selection.setStartLine(info.getLine());
                    selection.setStartPos(info.getIndex());
                }
                selection.setEndLine(info.getLine());
                selection.setEndPos(info.getIndex());

                recalc();
            }
        }
    }

    /**
     * Returns the visible rectangle of the first line.
     *
     * @return First line's rectangle
     */
    private Rectangle getFirstLineRectangle() {
        TextLayout firstLineLayout = null;
        for (Map.Entry<TextLayout, LineInfo> entry : textLayouts.entrySet()) {
            if (entry.getValue().getLine() == firstVisibleLine) {
                firstLineLayout = entry.getKey();
            }
        }
        if (firstLineLayout == null) {
            return null;
        }
        for (Map.Entry<Rectangle, TextLayout> entry : positions.entrySet()) {
            if (entry.getValue() == firstLineLayout) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Returns the last line's visible rectangle.
     *
     * @return Last line's rectangle
     */
    private Rectangle getLastLineRectangle() {
        TextLayout lastLineLayout = null;
        for (Map.Entry<TextLayout, LineInfo> entry : textLayouts.entrySet()) {
            if (entry.getValue().getLine() == lastVisibleLine) {
                lastLineLayout = entry.getKey();
            }
        }
        if (lastLineLayout == null) {
            return null;
        }
        for (Map.Entry<Rectangle, TextLayout> entry : positions.entrySet()) {
            if (entry.getValue() == lastLineLayout) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Returns the LineInfo for the first visible line.
     *
     * @return First line's line info
     */
    private LineInfo getFirstLineInfo() {
        int firstLineParts = Integer.MAX_VALUE;
        for (Map.Entry<TextLayout, LineInfo> entry : textLayouts.entrySet()) {
            if (entry.getValue().getLine() == firstVisibleLine
                    && entry.getValue().getPart() < firstLineParts) {
                firstLineParts = entry.getValue().getPart();
            }
        }
        return new LineInfo(firstVisibleLine, firstLineParts);
    }

    /**
     * Returns the LineInfo for the last visible line.
     *
     * @return Last line's line info
     */
    private LineInfo getLastLineInfo() {
        int lastLineParts = -1;
        for (Map.Entry<TextLayout, LineInfo> entry : textLayouts.entrySet()) {
            if (entry.getValue().getLine() == lastVisibleLine
                    && entry.getValue().getPart() > lastLineParts) {
                lastLineParts = entry.getValue().getPart();
            }
        }
        return new LineInfo(lastVisibleLine + 1, lastLineParts);
    }

    /**
     *
     * Returns the line information from a mouse click inside the textpane.
     *
     * @param point     mouse position
     * @param selection Are we selecting text?
     *
     * @return line number, line part, position in whole line
     */
    public LineInfo getClickPosition(final Point point, final boolean selection) {
        int lineNumber = -1;
        int linePart = -1;
        int pos = 0;

        if (point != null) {
            for (Map.Entry<Rectangle, TextLayout> entry : positions.entrySet()) {
                if (entry.getKey().contains(point)) {
                    lineNumber = textLayouts.get(entry.getValue()).getLine();
                    linePart = textLayouts.get(entry.getValue()).getPart();
                }
            }

            pos = getHitPosition(lineNumber, linePart, (int) point.getX(),
                    (int) point.getY(), selection);
        }

        return new LineInfo(lineNumber, linePart, pos);
    }

    /**
     * Returns the character index for a specified line and part for a specific hit position.
     *
     * @param lineNumber Line number
     * @param linePart   Line part
     * @param x          X position
     * @param y          Y position
     *
     * @return Hit position
     */
    private int getHitPosition(final int lineNumber, final int linePart,
            final int x, final int y, final boolean selection) {
        int pos = 0;

        for (Map.Entry<Rectangle, TextLayout> entry : positions.entrySet()) {
            if (textLayouts.get(entry.getValue()).getLine() == lineNumber) {
                if (textLayouts.get(entry.getValue()).getPart() < linePart) {
                    pos += entry.getValue().getCharacterCount();
                } else if (textLayouts.get(entry.getValue()).getPart() == linePart) {
                    final TextHitInfo hit =
                            entry.getValue().hitTestChar(x - DOUBLE_SIDE_PADDING, y);
                    if (selection || x > entry.getValue().getBounds().getX()) {
                        pos += hit.getInsertionIndex();
                    } else {
                        pos += hit.getCharIndex();
                    }
                }
            }
        }
        return pos;
    }

    /**
     * Returns the selected range info.
     *
     * @return Selected range info
     */
    protected LinePosition getSelectedRange() {
        return selection.getNormalised();
    }

    /** Clears the selection. */
    protected void clearSelection() {
        selection.setEndLine(selection.getStartLine());
        selection.setEndPos(selection.getStartPos());
        recalc();
    }

    /**
     * Selects the specified region of text.
     *
     * @param position Line position
     */
    public void setSelectedRange(final LinePosition position) {
        selection = new LinePosition(position);
        recalc();
    }

    /**
     * Returns the first visible line.
     *
     * @return the line number of the first visible line
     */
    public int getFirstVisibleLine() {
        return firstVisibleLine;
    }

    /**
     * Returns the last visible line.
     *
     * @return the line number of the last visible line
     */
    public int getLastVisibleLine() {
        return lastVisibleLine;
    }

    /**
     * Returns the number of visible lines.
     *
     * @return Number of visible lines
     */
    public int getNumVisibleLines() {
        return lastVisibleLine - firstVisibleLine;
    }

    @Override
    public void componentResized(final ComponentEvent e) {
        recalc();
    }

    @Override
    public void componentMoved(final ComponentEvent e) {
        //Ignore
    }

    @Override
    public void componentShown(final ComponentEvent e) {
        //Ignore
    }

    @Override
    public void componentHidden(final ComponentEvent e) {
        //Ignore
    }

    @Override
    public void configChanged(final String domain, final String key) {
        updateCachedSettings();
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
        final AttributedCharacterIterator iterator = getIterator(
                event.getPoint());

        if (iterator != null
                && iterator.getAttribute(IRCTextAttribute.TOOLTIP) != null) {
            return iterator.getAttribute(IRCTextAttribute.TOOLTIP).toString();
        }

        return super.getToolTipText(event);
    }

    /**
     * Fires mouse clicked events with the associated values.
     *
     * @param clickType Click type
     * @param eventType Mouse event type
     * @param event     Triggering mouse event
     */
    private void fireMouseEvents(final ClickTypeValue clickType,
            final MouseEventType eventType, final MouseEvent event) {
        for (TextPaneListener listener : listeners.get(TextPaneListener.class)) {
            listener.mouseClicked(clickType, eventType, event);
        }
    }

    /**
     * Adds a textpane listener.
     *
     * @param listener Listener to add
     */
    public void addTextPaneListener(final TextPaneListener listener) {
        listeners.add(TextPaneListener.class, listener);
    }

    /**
     * Removes a textpane listener.
     *
     * @param listener Listener to remove
     */
    public void removeTextPaneListener(final TextPaneListener listener) {
        listeners.remove(TextPaneListener.class, listener);
    }

}
