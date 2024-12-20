package org.example;

import javax.swing.*;
import java.awt.*;

public class SystemButton extends JButton {

    public SystemButton(String text, int size, boolean isFilterButton) {
        this.setText(text);
        this.setFont(new Font("Arial", Font.BOLD, size));
        this.setFocusPainted(false);
        this.setVisible(false);
        if (isFilterButton){
            setFilterButtonColor();
        }else {
            setSystemButtonColor();
        }

    }

    private void setFilterButtonColor(){
        this.setForeground(new Color(0x02EBC0));
        this.setBackground(new Color(231, 80, 6));
    }

    private void setSystemButtonColor(){
        this.setForeground(new Color(0xFFFFFF));
        this.setBackground(new Color(0x0560E8));
    }
}
