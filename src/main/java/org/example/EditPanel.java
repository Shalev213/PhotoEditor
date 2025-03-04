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
    private final int systemButtonsFontSize = 25;
    private int currentBrightnessChange = 0;
    private boolean[] changedValues;
    private boolean isGSClicked = false;
    private boolean isDarkClicked = false;
    private String imagePath;
    private File fileImage;
    private SystemButton saveButton;
    private SystemButton returnButton;
    private SystemButton grayScaleButton;
    private SystemButton smoothButton;
    private SystemButton brightButton;
    private SystemButton mirrorSideButton;
    private SystemButton mirrorUpButton;
    private SystemButton previousButton;
    private SystemButton nextButton;
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
    private int returnButtonWidth = 105;
    private int returnButtonHeight = 50;
    private int returnButtonX = 15;
    private int currentHeight;
    private int currentWidth;

    //  *********** add a shadow *********
    public EditPanel() {
        this.lobbyBackground = new ImageIcon("src/main/resources/EditorBackground.png");

        this.changedValues = new boolean[7];
        this.nextBufferedStack = new Stack<>();
        this.previousBufferedStack = new Stack<>();
        this.previousSlideValueStack = new Stack<>();

        this.previousButton = new SystemButton("<--", 20, false);

        this.previousButton.addActionListener(e -> {
            if (!previousSlideValueStack.empty()) {
                int previousValue = previousSlideValueStack.pop();
                this.darkBrightSlider.setValue(previousValue);  // עדכון הערך ב-Slider
            } else {
                this.darkBrightSlider.setValue(3);
            }
            if (!this.previousBufferedStack.empty()) {
                // שמירה על התמונה הנוכחית למחסנית "next"
                this.nextBufferedStack.push(this.bufferedImage);

                // עדכון התמונה לפי התמונה הקודמת במחסנית
                this.bufferedImage = this.previousBufferedStack.pop();
                labelPhoto.setIcon(new ImageIcon(bufferedImage));


                // עדכון ה-Slider לערך הקודם מתוך מחסנית הערכים
//                if (!previousSlideValueStack.empty()) {
//                    int previousValue = previousSlideValueStack.pop();
//                    this.darkBrightSlider.setValue(previousValue);  // עדכון הערך ב-Slider
//                }

                isGSClicked = false;
                this.repaint();
            }

        });
        this.add(previousButton);


        this.nextButton = new SystemButton("-->", 20, false);

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

        this.returnButton = new SystemButton("return", 30, false);
        this.returnButton.setFont(new Font("Arial", Font.BOLD, this.systemButtonsFontSize));

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
        this.darkBrightSlider.setBackground(null);

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

        this.grayScaleButton = new SystemButton("<html>gray<br>scale", 20, true);
        this.add(this.grayScaleButton);

        this.grayScaleButton.addActionListener(e -> {
            if (!isGSClicked) {
                this.nextBufferedStack.clear();
                this.previousBufferedStack.push(this.bufferedImage);
                this.bufferedImage = grayScale(this.bufferedImage);
                isGSClicked = true;
            } else if (this.previousBufferedStack.empty()) {
                isGSClicked = false;
            }
        });

        this.saveButton = new SystemButton("save", this.systemButtonsFontSize, false);
        this.saveButton.addActionListener(_ -> saveImage());
        this.add(this.saveButton);

        this.smoothButton = new SystemButton("smooth", 20, true);
        this.add(this.smoothButton);

        this.smoothButton.addActionListener(_ -> {
            this.nextBufferedStack.clear();
            this.previousBufferedStack.push(this.bufferedImage);
            this.bufferedImage = smoothing(this.bufferedImage);
        });

        this.brightButton = new SystemButton("bright", 20, true);
        this.add(this.brightButton);

        this.brightButton.addActionListener(e -> {
            this.nextBufferedStack.clear();
            this.previousBufferedStack.push(this.bufferedImage);
            this.bufferedImage = bright(this.bufferedImage);
        });

        this.mirrorSideButton = new SystemButton("<html>mirror<br>side", 20, true);
        this.add(this.mirrorSideButton);

        this.mirrorSideButton.addActionListener(e -> {// להוסיף buffered מקומי שישתנה
            this.nextBufferedStack.clear();
            this.previousBufferedStack.push(this.bufferedImage);
            this.bufferedImage = deepCopy(mirrorSide(this.bufferedImage));
            updateImageWithCurrentBrightness();
        });


        this.mirrorUpButton = new SystemButton("<html>mirror<br>up", 20, true);
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
        this.currentWidth = this.minWindowWidth;
        this.currentHeight = this.minWindowHeight;
        this.setSize(this.currentWidth, this.currentHeight);
//        this.returnButton.setBounds(this.returnButtonX, bufferedImage.getNumYTiles() + bufferedImage.getHeight() + 270, this.returnButtonWidth, this.returnButtonHeight);
        this.returnButton.setBounds(this.returnButtonX, this.currentHeight - this.returnButtonHeight - 50, this.returnButtonWidth, this.returnButtonHeight);

        this.labelPhoto = new JLabel(new ImageIcon(bufferedImage));
        this.labelPhoto.setBounds((minWindowWidth - bufferedImage.getWidth()) / 2, 20, bufferedImage.getWidth(), bufferedImage.getHeight());
    }

    private void setPanelByImage() {
        this.currentWidth = this.bufferedImage.getWidth() + 200;
        this.currentHeight = this.bufferedImage.getHeight() + 200;
        this.setSize(this.currentWidth, this.currentHeight);
        this.returnButton.setBounds(this.returnButtonX, bufferedImage.getNumYTiles() + bufferedImage.getHeight() + 90, this.returnButtonWidth, this.returnButtonHeight);
        this.labelPhoto = new JLabel(new ImageIcon(bufferedImage));
        this.labelPhoto.setBounds(93, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
    }

    private void setMinimalPanelHeight() {
        this.currentWidth = this.bufferedImage.getWidth() + 200;
        this.currentHeight = this.minWindowHeight;
        this.setSize(this.currentWidth, this.currentHeight);
        this.returnButton.setBounds(this.returnButtonX, bufferedImage.getNumYTiles() + bufferedImage.getHeight() + 170, this.returnButtonWidth, this.returnButtonHeight);
        this.labelPhoto = new JLabel(new ImageIcon(bufferedImage));
        this.labelPhoto.setBounds(100, 20, bufferedImage.getWidth(), bufferedImage.getHeight());
    }

    private void setMinimalPanelWidth() {
        this.currentWidth = this.minWindowWidth;
        this.currentHeight = bufferedImage.getHeight() + 200;
        this.setSize(this.currentWidth, this.currentHeight);
        this.returnButton.setBounds(this.returnButtonX, bufferedImage.getNumYTiles() + bufferedImage.getHeight() + 90, this.returnButtonWidth, this.returnButtonHeight);

//        this.returnButton.setBounds(20, bufferedImage.getNumYTiles() + bufferedImage.getHeight() + 90, this.returnButtonWidth, this.returnButtonHeight);
        this.labelPhoto = new JLabel(new ImageIcon(bufferedImage));
        this.labelPhoto.setBounds((minWindowWidth - bufferedImage.getWidth()) / 2, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
    }

    public void setDefaultPanel() {
        this.currentWidth = 800;
        this.currentHeight = 650;
        this.setSize(this.currentWidth, this.currentHeight);
        this.returnButton.setBounds(this.returnButtonX, 540, this.returnButtonWidth, this.returnButtonHeight);

        if (fileImage.exists()) { // ***** add a correct label *****
            System.out.println("Too big photo");
            this.typeOfError.setText("<html><body style='text-align: justify; width: 300px;'>Your photo is too big.<br>Pay attention,the photo must be with a maximum size of 700 height by 800 width.</body></html>");
        } else {
            System.out.println("invalid URL, please try again");
            this.typeOfError.setText("<html><body style='text-align: justify; width: 300px;'>Your URL is invalid.<br>Pay attention, the URL address must include two backslashes between each folder. Also, the address must have no quotation marks on the sides and no spaces.</body></html>");
        }
    }

    private BufferedImage grayScale(BufferedImage original) {
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

    private BufferedImage smoothing(BufferedImage original) {
        BufferedImage output = deepCopy(original);

        int filterSize = 3; // size of checking is: filterSize X filterSize
        int filterRadius = filterSize / 2;

        for (int x = filterRadius; x < original.getWidth() - filterRadius; x++) {
            for (int y = filterRadius; y < original.getHeight() - filterRadius; y++) {
                int redSum = 0, greenSum = 0, blueSum = 0, alphaSum = 0;
                int pixelCount = 0;

                // מעבר על הפיקסלים הסובבים
                for (int i = -filterRadius; i <= filterRadius; i++) {
                    for (int j = -filterRadius; j <= filterRadius; j++) {
                        int rgb = original.getRGB(x + i, y + j);
                        Color color = new Color(rgb, true);

                        redSum += color.getRed();
                        greenSum += color.getGreen();
                        blueSum += color.getBlue();
                        alphaSum += color.getAlpha();
                        pixelCount++;
                    }
                }

                // חישוב ממוצע הצבעים
                int avgRed = redSum / pixelCount;
                int avgGreen = greenSum / pixelCount;
                int avgBlue = blueSum / pixelCount;
                int avgAlpha = alphaSum / pixelCount;

                Color newColor = new Color(avgRed, avgGreen, avgBlue, avgAlpha);
                output.setRGB(x, y, newColor.getRGB());
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
        this.isGSClicked = isWBClicked;
    }

    public void resetAll() {
        this.isGSClicked = false;
        this.returnButton.setVisible(false);
        this.grayScaleButton.setVisible(false);
        this.previousButton.setVisible(false);
        this.nextButton.setVisible(false);
        this.mirrorSideButton.setVisible(false);
        this.mirrorUpButton.setVisible(false);
        this.smoothButton.setVisible(false);
        this.brightButton.setVisible(false);
        this.saveButton.setVisible(false);
        this.darkBrightSlider.setVisible(false);

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
        this.grayScaleButton.setVisible(true);
//        this.grayScaleButton.setBounds(returnButton.getX() + returnButton.getWidth() + 5, returnButton.getY(), (int) (returnButton.getWidth() * 0.7), returnButton.getHeight());
        this.grayScaleButton.setBounds(returnButton.getX(), returnButton.getY() - 80, (int) (returnButton.getWidth() * 0.8), returnButton.getHeight());

        this.mirrorSideButton.setVisible(true);
        this.mirrorSideButton.setBounds(grayScaleButton.getX() + grayScaleButton.getWidth() + 5, grayScaleButton.getY(), grayScaleButton.getWidth(), grayScaleButton.getHeight());
        this.mirrorUpButton.setVisible(true);
        this.mirrorUpButton.setBounds(mirrorSideButton.getX() + mirrorSideButton.getWidth() + 5, mirrorSideButton.getY(), mirrorSideButton.getWidth(), mirrorSideButton.getHeight());
        this.smoothButton.setVisible(true);
        this.smoothButton.setBounds(mirrorUpButton.getX() + mirrorUpButton.getWidth() + 5, mirrorUpButton.getY(), returnButton.getWidth(), mirrorUpButton.getHeight());
//        this.brightButton.setVisible(true);
//        this.brightButton.setBounds(smoothButton.getX() + smoothButton.getWidth() + 5, smoothButton.getY(), mirrorUpButton.getWidth(), smoothButton.getHeight());
        this.saveButton.setVisible(true);
        this.saveButton.setBounds(smoothButton.getX() + smoothButton.getWidth() + 5, returnButton.getY(), (int) (1.2 * mirrorUpButton.getWidth()), smoothButton.getHeight());


        this.darkBrightSlider.setVisible(true);
        this.darkBrightSlider.setSize(returnButton.getWidth() + 270, returnButton.getHeight());
        this.darkBrightSlider.setLocation((this.getWidth() - darkBrightSlider.getWidth()) / 2, grayScaleButton.getY() + grayScaleButton.getHeight() + 10);
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
        fileChooser.setDialogTitle("Save");
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