package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Stack;

public class EditPanel extends JPanel {
    private final ImageIcon lobbyBackground;
    private int currentBrightnessChange = 0;
    private boolean[] changedValues;
    private boolean isWBClicked = false;
    private boolean isDarkClicked = false;
    private String imagePath;
    private File fileImage;
    private JButton saveButton;
    private JButton returnButton;
    private JButton whiteBlackButton;
    private JButton darkButton;
    private JButton brightButton;
    private JButton mirrorSideButton;
    private JButton mirrorUpButton;
    private JButton previousButton;
    private JButton nextButton;
    private BufferedImage bufferedImage;
    private JLabel labelPhoto;
    private JLabel typeOfError;
    private boolean isSizeImageValid;
    private int maxWindowWidth = 1000;
    private int maxWindowHeight = 900;
    private int minWindowWidth = 750;
    private int minWindowHeight = 600;
    private boolean isSizeImageMinimal;
    private boolean isOnlyHeightMinimal;
    private boolean isOnlyWidthMinimal;
    private Stack<BufferedImage> previousBufferedStack;
    private Stack<BufferedImage> nextBufferedStack;
    private Stack<Integer> previousSlideValueStack;
    private JSlider darkBrightSlider;
    private BufferedImage originalImage;
    private BufferedImage bufferedImage1;


    //  *********** add a shadow *********
    public EditPanel() {
        this.lobbyBackground = new ImageIcon("src/main/resources/EditorBackground.png");

        this.changedValues = new boolean[7];
        this.previousBufferedStack = new Stack<>();
        this.nextBufferedStack = new Stack<>();
        this.previousSlideValueStack = new Stack<>();

        this.previousButton = new JButton("<--");
        this.previousButton.setVisible(false);
        this.previousButton.setFont(new Font("Arial", Font.BOLD, 20));
        this.previousButton.setFocusPainted(false);
        this.previousButton.setForeground(new Color(0xFFFFFF));
        this.previousButton.setBackground(new Color(0x0560E8));
        this.previousButton.setFocusable(true);


        this.previousButton.addActionListener(e -> {
            if (!this.previousBufferedStack.empty()) {
                // שמירה על התמונה הנוכחית למחסנית "next"
                this.nextBufferedStack.push(this.bufferedImage);

                // עדכון התמונה לפי התמונה הקודמת במחסנית
                this.bufferedImage = this.previousBufferedStack.pop();
                labelPhoto.setIcon(new ImageIcon(bufferedImage));


                // עדכון ה-Slider לערך הקודם מתוך מחסנית הערכים
                if (!previousSlideValueStack.empty()) {
                    int previousValue = previousSlideValueStack.pop();
                    this.darkBrightSlider.setValue(previousValue);  // עדכון הערך ב-Slider
                }

                isWBClicked = false;
                this.repaint();
            }
        });
        this.add(previousButton);


        this.nextButton = new JButton("-->");
        this.nextButton.setVisible(false);
        this.nextButton.setFont(new Font("Arial", Font.BOLD, 20));
        this.nextButton.setFocusPainted(false);
        this.nextButton.setForeground(new Color(0xFFFFFF));
        this.nextButton.setBackground(new Color(0x0560E8));
        this.nextButton.setFocusable(true);

        this.nextButton.addActionListener(e -> {

            if (!this.nextBufferedStack.empty()) {
//                this.nextButton.setEnabled(true);

                this.previousBufferedStack.push(this.bufferedImage);
                this.bufferedImage = this.nextBufferedStack.pop();
                labelPhoto.setIcon(new ImageIcon(bufferedImage));
                this.repaint();
            }
//            else {
//                this.nextButton.setEnabled(false);
//            }
        });
        this.add(nextButton);

        this.typeOfError = new JLabel();
        this.typeOfError.setBounds(145, 80, 500, 320);
        this.typeOfError.setFont(new Font("Arial", Font.ITALIC, 30));
        this.typeOfError.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(typeOfError);
        this.setLayout(null);
        this.setSize(800, 700);//setDefaultPanel()

        this.returnButton = new JButton("return");
        this.returnButton.setFont(new Font("Arial", Font.BOLD, 30));
        this.returnButton.setFocusPainted(false);
        this.returnButton.setForeground(new Color(0xFFFFFF));
        this.returnButton.setBackground(new Color(0x0560E8));
        this.returnButton.setFocusable(true);

        InputMap inputMap = returnButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = returnButton.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "backspacePressed");
        actionMap.put("backspacePressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnButton.doClick();
            }
        });

        this.add(returnButton);

        this.darkBrightSlider = new JSlider(JSlider.HORIZONTAL, 0, 6, 3);
        Font customFont = new Font("Arial", Font.BOLD, 15);
        this.darkBrightSlider.setMajorTickSpacing(3);
        this.darkBrightSlider.setMinorTickSpacing(1);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, createCustomLabel("dark", customFont));
        labelTable.put(3, createCustomLabel("medium", customFont));
        labelTable.put(6, createCustomLabel("bright  ", customFont));
//        this.darkBrightSlider.setFont(new Font("Arial", Font.BOLD, 70));
//        this.darkBrightSlider.setForeground(new Color(0xE011E60A, true));
        this.darkBrightSlider.setBackground(Color.cyan);

        // Set the custom labels to the slider
        this.darkBrightSlider.setLabelTable(labelTable);

        this.darkBrightSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            int value = source.getValue();

            BufferedImage updatedImage = deepCopy(this.bufferedImage);

            // שמירת תמונה וערך ה-Slider במחסניות
//            if (this.previousBufferedStack.isEmpty()) {
//                this.previousBufferedStack.push(deepCopy(this.bufferedImage));
//                this.previousSlideValueStack.push(value);
//            }

            currentBrightnessChange = (value - 3) * 15;
            System.out.println("Slider value: " + value + ", Brightness change: " + currentBrightnessChange);

            updatedImage = changeBrightness(updatedImage, currentBrightnessChange);

            // הצגת התמונה החדשה
            labelPhoto.setIcon(new ImageIcon(updatedImage));
            repaint();

            this.nextBufferedStack.clear();
            this.previousBufferedStack.push(updatedImage);
            this.previousSlideValueStack.push(value);
        });


        this.darkBrightSlider.setPaintTicks(true);
        this.darkBrightSlider.setPaintLabels(true);
        this.add(darkBrightSlider);

        this.whiteBlackButton = new JButton("<html>black<br>and white");
        this.whiteBlackButton.setFont(new Font("Arial", Font.BOLD, 20));
        this.whiteBlackButton.setFocusPainted(false);
        this.whiteBlackButton.setForeground(new Color(0x02EBC0));
        this.whiteBlackButton.setBackground(new Color(231, 80, 6));
        this.whiteBlackButton.setVisible(false);

        this.add(this.whiteBlackButton);

        this.whiteBlackButton.addActionListener(e -> {
            if (!isWBClicked) {
                this.nextBufferedStack.clear();
                this.previousBufferedStack.push(this.bufferedImage);
                this.bufferedImage = whiteBlack(this.bufferedImage);
                isWBClicked = true;
            } else if (this.previousBufferedStack.empty()) {
                isWBClicked = false;
            }
        });

        this.saveButton = new JButton("Save Image");
        this.saveButton.setFont(new Font("Arial", Font.BOLD, 20));
        this.saveButton.setFocusPainted(false);
        this.saveButton.setForeground(new Color(0x02EBC0));
        this.saveButton.setBackground(new Color(231, 80, 6));
        this.saveButton.setVisible(false);
        this.saveButton.addActionListener(e -> saveImage());
        this.add(this.saveButton);

        this.darkButton = new JButton("dark");
        this.darkButton.setFont(new Font("Arial", Font.BOLD, 20));
        this.darkButton.setFocusPainted(false);
        this.darkButton.setForeground(new Color(0x02EBC0));
        this.darkButton.setBackground(new Color(231, 80, 6));
        this.darkButton.setVisible(false);

        this.add(this.darkButton);

        this.darkButton.addActionListener(e -> {
            this.nextBufferedStack.clear();
            this.previousBufferedStack.push(this.bufferedImage);
            if (!isDarkClicked) {
                isDarkClicked = true;
                this.bufferedImage = dark(this.bufferedImage);
            } else {
                isDarkClicked = false;
                this.bufferedImage = bright(this.bufferedImage);
            }
        });

        this.brightButton = new JButton("bright");
        this.brightButton.setFont(new Font("Arial", Font.BOLD, 20));
        this.brightButton.setFocusPainted(false);
        this.brightButton.setForeground(new Color(0x02EBC0));
        this.brightButton.setBackground(new Color(231, 80, 6));
        this.brightButton.setVisible(false);

        this.add(this.brightButton);

        this.brightButton.addActionListener(e -> {
            this.nextBufferedStack.clear();
            this.previousBufferedStack.push(this.bufferedImage);
            this.bufferedImage = bright(this.bufferedImage);
        });

        this.mirrorSideButton = new JButton("<html>mirror<br>side");
        this.mirrorSideButton.setFont(new Font("Arial", Font.BOLD, 20));
        this.mirrorSideButton.setFocusPainted(false);
        this.mirrorSideButton.setForeground(new Color(0x02EBC0));
        this.mirrorSideButton.setBackground(new Color(231, 80, 6));
        this.mirrorSideButton.setVisible(false);

        this.add(this.mirrorSideButton);

        this.mirrorSideButton.addActionListener(e -> {// להוסיף buffered מקומי שישתנה
            this.nextBufferedStack.clear();
            this.previousBufferedStack.push(this.bufferedImage);
            this.bufferedImage = deepCopy(mirrorSide(this.bufferedImage));
            updateImageWithCurrentBrightness();
        });


        this.mirrorUpButton = new JButton("<html>mirror<br>up");
        this.mirrorUpButton.setFont(new Font("Arial", Font.BOLD, 20));
        this.mirrorUpButton.setFocusPainted(false);
        this.mirrorUpButton.setForeground(new Color(0x02EBC0));
        this.mirrorUpButton.setBackground(new Color(231, 80, 6));
        this.mirrorUpButton.setVisible(false);

        this.add(this.mirrorUpButton);

        this.mirrorUpButton.addActionListener(e -> {
            this.nextBufferedStack.clear();
            this.previousBufferedStack.push(this.bufferedImage);
            this.bufferedImage = mirrorUp(this.bufferedImage);
            updateImageWithCurrentBrightness();
        });
    }

    private void resetArray(boolean[] changedValues, boolean value) {
        for (int i = 0; i < changedValues.length; i++) {
            changedValues[i] = value;
        }
    }


    public void setPhoto(String myUrl) {

        try {
            imagePath = myUrl;
            System.out.println(imagePath);
            fileImage = new File(imagePath);

            if (fileImage.exists()) {
                bufferedImage = ImageIO.read(fileImage);
                this.originalImage = deepCopy(bufferedImage);

                isSizeImageValid = bufferedImage.getWidth() + 200 <= maxWindowWidth && bufferedImage.getHeight() + 200 <= maxWindowHeight;
                isSizeImageMinimal = bufferedImage.getWidth() + 200 < minWindowWidth && bufferedImage.getHeight() + 200 < minWindowHeight;
                isOnlyHeightMinimal = bufferedImage.getHeight() + 200 < minWindowHeight;
                isOnlyWidthMinimal = bufferedImage.getWidth() + 200 < minWindowWidth;

                if (isSizeImageValid) {

                    //550 - 800 width <> 400 - 700 height
                    //4 מקרים גובה רוחב  ***** לבדוק עם תמונות *****
                    if (isSizeImageMinimal) {
                        setMinimalPanelSize();
                    } else if (isOnlyWidthMinimal) {
                        setMinimalPanelWidth();
                    } else if (isOnlyHeightMinimal) {
                        setMinimalPanelHeight();
                    } else {
                        setPanelByImage();
                    }
                    this.labelPhoto.setBorder(BorderFactory.createLineBorder(Color.BLACK, 10));
                    this.add(labelPhoto);
                    this.typeOfError.setText("");
                    setButtons();
                } else {
                    setDefaultPanel();
                }
            } else {
                System.out.println("cannot find!");

                setDefaultPanel();
            }
            this.setBackground(Color.yellow);
            this.setLayout(null);
            this.setVisible(false);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JButton getReturnButton() {
        return returnButton;
    }

    public void removeLabelImage() {
        if (this.labelPhoto != null) {
//            this.labelPhoto.setIcon(null);
            this.remove(labelPhoto);
        }
    }

    private void setMinimalPanelSize() {
        this.setSize(minWindowWidth, minWindowHeight);
        this.returnButton.setBounds(20, bufferedImage.getNumYTiles() + bufferedImage.getHeight() + 200, 130, 70);
        this.labelPhoto = new JLabel(new ImageIcon(bufferedImage));
        this.labelPhoto.setBounds((minWindowWidth - bufferedImage.getWidth()) / 2, 20, bufferedImage.getWidth(), bufferedImage.getHeight());
    }

    private void setPanelByImage() {
        this.setSize(bufferedImage.getWidth() + 200, bufferedImage.getHeight() + 200);
        this.returnButton.setBounds(20, bufferedImage.getNumYTiles() + bufferedImage.getHeight() + 20, 130, 70);
        this.labelPhoto = new JLabel(new ImageIcon(bufferedImage));
        this.labelPhoto.setBounds(93, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
    }

    private void setMinimalPanelHeight() {
        this.setSize(bufferedImage.getWidth() + 200, minWindowHeight);
        this.returnButton.setBounds(20, bufferedImage.getNumYTiles() + bufferedImage.getHeight() + 140, 130, 70);
        this.labelPhoto = new JLabel(new ImageIcon(bufferedImage));
        this.labelPhoto.setBounds(100, 20, bufferedImage.getWidth(), bufferedImage.getHeight());
    }

    private void setMinimalPanelWidth() {
        this.setSize(minWindowWidth, bufferedImage.getHeight() + 200);
        this.returnButton.setBounds(20, bufferedImage.getNumYTiles() + bufferedImage.getHeight() + 20, 130, 70);
        this.labelPhoto = new JLabel(new ImageIcon(bufferedImage));
        this.labelPhoto.setBounds((minWindowWidth - bufferedImage.getWidth()) / 2, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
    }

    public void setDefaultPanel() {
        this.setSize(800, 650);
        this.returnButton.setBounds(10, 470, 130, 70);

        if (fileImage.exists()) { // ***** add a correct label *****
            System.out.println("Too big photo");
            this.typeOfError.setText("<html><body style='text-align: justify; width: 300px;'>Your photo is too big.<br>Pay attention,the photo must be with a maximum size of 700 height by 800 width.</body></html>");
        } else {
            System.out.println("invalid URL, please try again");
            this.typeOfError.setText("<html><body style='text-align: justify; width: 300px;'>Your URL is invalid.<br>Pay attention, the URL address must include two backslashes between each folder. Also, the address must have no quotation marks on the sides and no spaces.</body></html>");
        }
    }

    private BufferedImage whiteBlack(BufferedImage original) {
        BufferedImage output = deepCopy(original);

        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                int argb = original.getRGB(x, y);
                Color currentColor = new Color(argb, true);

                if (currentColor.getAlpha() == 0) {
                    output.setRGB(x, y, 0);
                } else {
                    int alpha = currentColor.getAlpha();
                    int red = currentColor.getRed();
                    int green = currentColor.getGreen();
                    int blue = currentColor.getBlue();
                    int average = (red + green + blue) / 3;
                    Color updateColor = new Color(average, average, average, alpha);
                    output.setRGB(x, y, updateColor.getRGB());
                }
            }
        }

        labelPhoto.setIcon(new ImageIcon(output));
        this.repaint();
        return output;
    }

    public BufferedImage mirrorSide(BufferedImage original) {
        BufferedImage output = deepCopy(original);
        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                output.setRGB(x, y, original.getRGB(original.getWidth() - x - 1, y));
            }
        }
        labelPhoto.setIcon(new ImageIcon(output));
        this.repaint();
        return output;
    }

    private BufferedImage dark(BufferedImage original) {
        BufferedImage output = deepCopy(original);

        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                int argb = original.getRGB(x, y);
                Color currentColor = new Color(argb, true);

                if (currentColor.getAlpha() == 0) {
                    output.setRGB(x, y, 0);
                } else {
                    int alpha = currentColor.getAlpha();
                    int red = currentColor.getRed() + 20;
                    int green = currentColor.getGreen() + 20;
                    int blue = currentColor.getBlue() + 20;
                    Color brightColor = new Color(red, green, blue, alpha);
                    output.setRGB(x, y, brightColor.getRGB());
//                    Color newColor = currentColor.darker();
//                    output.setRGB(x, y, newColor.getRGB());
                }
            }
        }

        labelPhoto.setIcon(new ImageIcon(output));
        this.repaint();
        return output;
    }

    private BufferedImage changeBrightness(BufferedImage image, int amount) {
        BufferedImage output = deepCopy(image);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb, true);
                int alpha = color.getAlpha();
                int red = clamp(color.getRed() + amount);
                int green = clamp(color.getGreen() + amount);
                int blue = clamp(color.getBlue() + amount);
                output.setRGB(x, y, new Color(red, green, blue, alpha).getRGB());
            }
        }
//        labelPhoto.setIcon(new ImageIcon(output));
//        this.repaint();
        return output;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private BufferedImage bright(BufferedImage original) {
        BufferedImage output = deepCopy(original);

        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                int argb = original.getRGB(x, y);
                Color currentColor = new Color(argb, true);

                if (currentColor.getAlpha() == 0) {
                    output.setRGB(x, y, 0);
                } else {
                    Color newColor = currentColor.brighter();
                    output.setRGB(x, y, newColor.getRGB());
                }
            }
        }

        labelPhoto.setIcon(new ImageIcon(output));
        this.repaint();
        return output;
    }

    public BufferedImage mirrorUp(BufferedImage original) {
        BufferedImage output = deepCopy(original);
        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                output.setRGB(x, y, original.getRGB(x, original.getHeight() - y - 1));
            }
        }
        this.originalImage = deepCopy(bufferedImage);

        labelPhoto.setIcon(new ImageIcon(output));
        this.repaint();
        return output;
    }

    public void setIsWBClicked(boolean isWBClicked) {
        this.isWBClicked = isWBClicked;
    }

    public void resetAll() {
        this.isWBClicked = false;
        this.returnButton.setVisible(false);
        this.whiteBlackButton.setVisible(false);
        this.previousButton.setVisible(false);
        this.nextButton.setVisible(false);
        this.mirrorSideButton.setVisible(false);
        this.mirrorUpButton.setVisible(false);
        this.darkButton.setVisible(false);
        this.brightButton.setVisible(false);

        this.nextBufferedStack.clear();
        this.previousBufferedStack.clear();

    }

    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel colorModel = bi.getColorModel();
        boolean isAlphaPreMultiplied = colorModel.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(colorModel, raster, isAlphaPreMultiplied, null);
    }

    private JLabel createCustomLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        return label;
    }

    private void setButtons() {
        this.nextButton.setVisible(true);
        this.nextButton.setBounds(this.getWidth() - 95, 10, 70, 70);
        this.previousButton.setVisible(true);
        this.previousButton.setBounds(10, 10, 70, 70);
        this.whiteBlackButton.setVisible(true);
        this.whiteBlackButton.setBounds(returnButton.getX() + returnButton.getWidth() + 5, returnButton.getY(), returnButton.getWidth(), returnButton.getHeight());
        this.mirrorSideButton.setVisible(true);
        this.mirrorSideButton.setBounds(whiteBlackButton.getX() + whiteBlackButton.getWidth() + 5, whiteBlackButton.getY(), whiteBlackButton.getWidth() - 35, whiteBlackButton.getHeight());
        this.mirrorUpButton.setVisible(true);
        this.mirrorUpButton.setBounds(mirrorSideButton.getX() + mirrorSideButton.getWidth() + 5, mirrorSideButton.getY(), mirrorSideButton.getWidth(), mirrorSideButton.getHeight());
        this.darkButton.setVisible(true);
        this.darkButton.setBounds(mirrorUpButton.getX() + mirrorUpButton.getWidth() + 5, mirrorUpButton.getY(), mirrorUpButton.getWidth() - 15, mirrorUpButton.getHeight());
//        this.brightButton.setVisible(true);
//        this.brightButton.setBounds(darkButton.getX() + darkButton.getWidth() + 5, darkButton.getY(), mirrorUpButton.getWidth(), darkButton.getHeight());
        this.saveButton.setVisible(true);
        this.saveButton.setBounds(darkButton.getX() + darkButton.getWidth() + 5, darkButton.getY(), mirrorUpButton.getWidth(), darkButton.getHeight());


        this.darkBrightSlider.setVisible(true);
        this.darkBrightSlider.setSize(returnButton.getWidth() + 270, returnButton.getHeight() - 20);
        this.darkBrightSlider.setLocation((this.getWidth() - darkBrightSlider.getWidth()) / 2, returnButton.getY() + returnButton.getHeight() + 10);
//        this.darkBrightSlider.setBounds((this.getWidth() - darkBrightSlider.getWidth()) / 4 , returnButton.getY() + returnButton.getHeight() + 10, returnButton.getWidth() + 270, returnButton.getHeight() - 20);
    }

    private void updateImageWithCurrentBrightness() {
        BufferedImage updatedImage = deepCopy(this.bufferedImage);
        updatedImage = changeBrightness(updatedImage, currentBrightnessChange);
        labelPhoto.setIcon(new ImageIcon(updatedImage));
        repaint();
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (lobbyBackground != null) {
            graphics.drawImage(lobbyBackground.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void saveImage() {
        // בוחר את המיקום שבו יישמר הקובץ
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG Images", "png"));

        int userSelection = fileChooser.showSaveDialog(this);  // מציג את תיבת הדו-שיח לשמירה
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // אם המשתמש לא הוסיף סיומת לפייל, נוסיף סיומת .png
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.endsWith(".png")) {
                filePath += ".png";
            }

            // שמירת התמונה בקובץ
            try {
                ImageIO.write(bufferedImage, "PNG", new File(filePath));
                JOptionPane.showMessageDialog(this, "Image saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving image.");
            }
        }
    }

}

//exeption: mirror -> BW -> mirror -> BW