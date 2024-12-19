package org.example;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class OpeningPanel extends JPanel {
    private JButton startButton;
    private JTextField textField;
    private JTextArea textArea;
    private final ImageIcon lobbyBackground;

    public OpeningPanel () {
        this.lobbyBackground = new ImageIcon("src/main/resources/OpeningPanelBackground.png");
        this.setBackground(Color.cyan);
        this.setSize(800,650);
        this.setLayout(null);
        this.setVisible(true);

        this.startButton = new JButton("START");
        this.startButton.setFont(new Font("Arial", Font.BOLD, 35));
        this.startButton.setBounds(300,500,152,80);
        this.startButton.setFocusPainted(false);

        this.add(startButton);

        this.textField = new JTextField();
        this.textField.setFont(new Font("Arial", Font.ITALIC, 25));
        this.textField.setBounds(100, 300, 600,70);
        this.textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(null, "Enter a path with two backslash between the libraries:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.RIGHT),
                BorderFactory.createEmptyBorder(5, 5, 5,5)

        ));

        this.add(textField);

        this.textField.addActionListener(e -> {
            startButton.doClick();
        });

    }
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (lobbyBackground != null){
            this.lobbyBackground.paintIcon(null, graphics, 0, 0);
        }
    }
    public JTextField getTextField() {
        return textField;
    }

    public JButton getStartButton() {
        return startButton;
    }

}
