package br.com.phs.dbbrowser.utils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SuggestionPanel {
    private final JList mList;
    private final JPopupMenu popupMenu;
    private final String subWord;
    private final int insertionPosition;
    private final JTextPane mTextPane;

    public SuggestionPanel(JTextPane textPane, JList list, int position, String subWord, Point location) {
        this.mTextPane = textPane;
        this.mList = list;
        this.insertionPosition = position;
        this.subWord = subWord;
        popupMenu = new JPopupMenu();
        popupMenu.removeAll();
        popupMenu.setOpaque(false);
        popupMenu.setBorder(null);
        popupMenu.add(mList, BorderLayout.CENTER);
        popupMenu.show(mTextPane, location.x, mTextPane.getBaseline(0, 0) + location.y);

        mList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (insertSelection(false))
                    hide();
            }
        });

    }

    /**
     *
     * @param removeJump - must be true because button ENTER to insert suggestion put "\n" end line
     * @return - success operation
     */
    public boolean insertSelection(boolean removeJump) {
        try {
            final String selectedSuggestion = ((String) mList.getSelectedValue()).substring(subWord.length());
            mTextPane.getDocument().insertString(insertionPosition, selectedSuggestion+" ", null);
            if (removeJump) {
                int position = mTextPane.getCaretPosition();
                mTextPane.getDocument().remove(position-1, 1);
            }
            return true;
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }

        this.hide();
        return false;
    }

    public void moveUp() {
        selectIndex(mList.getSelectedIndex() - 1);
    }

    public void moveDown() {
        selectIndex(mList.getSelectedIndex() + 1);
    }

    private void selectIndex(int index) {
        final int position = mTextPane.getCaretPosition();
        mList.setSelectedIndex(index);
        SwingUtilities.invokeLater(() -> mTextPane.setCaretPosition(position));
    }

    public void hide() {
        popupMenu.setVisible(false);
    }

}
