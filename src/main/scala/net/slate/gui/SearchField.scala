package net.slate.gui

import java.awt.Color
import java.awt.event.FocusListener 

import swing.event._
import swing.TextField
import scala.util.Properties.isMac

import net.slate.editor.search.TextSearch

object SearchField {

  def apply(prompt: String) = {
    val field = new TextField(prompt) {
      columns = 30
      foreground = Color.decode("0xBBBBBB")

      reactions += {
        case ValueChanged(_) => if(text != prompt && text.trim.length > 0) {
          //net.slate.Launch.currentScript.text.peer.setCaretPosition(text.trim.length -1)
          TextSearch.findNext(text, false, true)
        }
      }
      
      peer.addFocusListener(new FocusListener {
        override def focusGained(e: java.awt.event.FocusEvent) {
          if (text == prompt) text = ""
        }
        
        override def focusLost(e: java.awt.event.FocusEvent) {
          if (text == "") text = prompt
        }
      })
    }

    if (isMac) field.peer.putClientProperty("JTextField.variant", "search");

    field
  }
}