package br.com.phs.dbbrowser.ui.utils

import java.awt.Cursor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JFrame

fun handMouseClickListener(
    released: (e: MouseEvent?)-> Unit,
    pressed: ((e: MouseEvent?) -> Unit)? = null,
    parent: JFrame? = null
): MouseListener {
    return object : MouseAdapter() {
        override fun mouseEntered(e: MouseEvent?) {
            parent?.cursor = Cursor(Cursor.HAND_CURSOR)
        }
        override fun mouseExited(e: MouseEvent?) {
            parent?.cursor = Cursor(Cursor.DEFAULT_CURSOR)
        }
        override fun mouseReleased(e: MouseEvent?) {
            released(e)
        }
        override fun mousePressed(e: MouseEvent) {
            if (pressed != null)
                pressed(e)
        }
    }
}