package br.com.phs.dbbrowser;

import br.com.phs.dbbrowser.db.ui.MainScreen;

import java.awt.*;

public class Main {

    public static void main(String[] args) {
        EventQueue.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        Frame frame = new MainScreen();
        frame.setVisible(true);
    }

}
