package org.example;

import javax.swing.*;

public class Window extends JFrame {
    private OpeningPanel openingPanel;
    private EditPanel editPanel;

    public Window () {
        openingPanel = new OpeningPanel();
        this.add(openingPanel);
        editPanel = new EditPanel();
        this.add(editPanel);

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setSize(openingPanel.getWidth(), openingPanel.getHeight());
        this.setLocationRelativeTo(null);

        this.openingPanel.getStartButton().addActionListener(e -> {
            this.editPanel.resetAll();

            this.editPanel.setPhoto(this.openingPanel.getTextField().getText());

            this.openingPanel.setVisible(false);
            this.editPanel.setVisible(true);
            this.editPanel.getReturnButton().setVisible(true);
            this.setSize(editPanel.getWidth(), editPanel.getHeight());
            this.setLocationRelativeTo(null);
//            this.editPanel.setIsWBClicked(false);
        });
        this.editPanel.getReturnButton().addActionListener(e -> {
            this.editPanel.removeLabelImage();

            this.editPanel.setVisible(false);
            this.editPanel.getReturnButton().setVisible(false);
            this.openingPanel.setVisible(true);
            this.setSize(openingPanel.getWidth(), openingPanel.getHeight());
            this.setLocationRelativeTo(null);

        });
    }
    public void showWindow () {
        this.setVisible(true);
    }
}
