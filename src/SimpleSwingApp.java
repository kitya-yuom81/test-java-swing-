import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SimpleSwingApp extends JFrame {

    // --- Fields used across actions/components ---
    private JTextField nameField;
    private JTextArea displayArea;
    private JButton addButton;
    private JButton clearButton;
    private JLabel statusBar;

    public SimpleSwingApp() {
        super("Simple Swing Example");
        initLookAndFeel();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(480, 360));

        setJMenuBar(buildMenuBar());
        add(buildContent(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null); // center window
    }

    /* ----------------- Look & Feel ----------------- */

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Component.arrowType", "triangle");
        } catch (Exception ignored) { /* fallback ok */ }
    }

    /* ----------------- Menu Bar ----------------- */

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu file = new JMenu("File");
        file.setMnemonic('F');

        JMenuItem save = new JMenuItem(new SaveAction());
        save.setText("Save Log…");
        save.setMnemonic('S');
        save.setAccelerator(KeyStroke.getKeyStroke(
                'S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
        ));

        JMenuItem clear = new JMenuItem(new ClearAction());
        clear.setText("Clear");
        clear.setMnemonic('C');

        JMenuItem exit = new JMenuItem(new ExitAction());
        exit.setText("Exit");
        exit.setMnemonic('E');
        exit.setAccelerator(KeyStroke.getKeyStroke(
                'W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
        ));

        file.add(save);
        file.add(clear);
        file.addSeparator();
        file.add(exit);

        JMenu help = new JMenu("Help");
        help.setMnemonic('H');
        JMenuItem about = new JMenuItem(new AboutAction());
        about.setText("About");
        about.setMnemonic('A');
        help.add(about);

        menuBar.add(file);
        menuBar.add(help);
        return menuBar;
    }

    /* ----------------- Main Content ----------------- */

    private JComponent buildContent() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top form
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 0, 8);
        gc.gridy = 0;

        JLabel nameLabel = new JLabel("Enter your name:");
        nameField = new JTextField(18);
        nameField.setToolTipText("Type a name and press Enter or click Add");

        addButton = new JButton(new AddAction());
        addButton.setText("Add");
        addButton.setToolTipText("Add a greeting (Enter)");

        clearButton = new JButton(new ClearAction());
        clearButton.setText("Clear");
        clearButton.setToolTipText("Clear the greetings");

        // Layout: label, text field, Add, Clear
        gc.gridx = 0; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        form.add(nameLabel, gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(nameField, gc);
        gc.gridx = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        form.add(addButton, gc);
        gc.gridx = 3;
        form.add(clearButton, gc);

        // Center output area with scroll
        displayArea = new JTextArea(12, 40);
        displayArea.setEditable(false);
        displayArea.setLineWrap(true);
        displayArea.setWrapStyleWord(true);
        displayArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(
                displayArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        // Enable/disable "Add" based on input
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            private void toggle() {
                addButton.setEnabled(!nameField.getText().trim().isEmpty());
            }
            public void insertUpdate(DocumentEvent e) { toggle(); }
            public void removeUpdate(DocumentEvent e) { toggle(); }
            public void changedUpdate(DocumentEvent e) { toggle(); }
        });
        addButton.setEnabled(false);

        // Make Enter trigger Add
        getRootPane().setDefaultButton(addButton);

        root.add(form, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        return root;
    }

    private JComponent buildStatusBar() {
        statusBar = new JLabel(" Ready");
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        bar.add(statusBar, BorderLayout.WEST);
        return bar;
    }

    /* ----------------- Actions ----------------- */

    private class AddAction extends AbstractAction {
        AddAction() {
            putValue(NAME, "Add");
            putValue(SHORT_DESCRIPTION, "Append a greeting to the log");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            String raw = nameField.getText();
            String name = raw == null ? "" : raw.trim();

            if (name.isEmpty()) {
                beep("Please enter a name.");
                nameField.requestFocusInWindow();
                return;
            }
            if (name.length() > 60) {
                beep("Name is too long (max 60 characters).");
                nameField.requestFocusInWindow();
                return;
            }
            displayArea.append("Hello, " + name + "!\n");
            nameField.setText("");
            nameField.requestFocusInWindow();
            setStatus("Added greeting for \"" + name + "\"");
        }
    }

    private class ClearAction extends AbstractAction {
        ClearAction() {
            putValue(NAME, "Clear");
            putValue(SHORT_DESCRIPTION, "Clear all greetings");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (displayArea.getText().isEmpty()) {
                setStatus("Nothing to clear.");
                return;
            }
            int res = JOptionPane.showConfirmDialog(
                    SimpleSwingApp.this,
                    "Clear all greetings?",
                    "Confirm Clear",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (res == JOptionPane.YES_OPTION) {
                displayArea.setText("");
                setStatus("Cleared.");
            }
        }
    }

    private class SaveAction extends AbstractAction {
        SaveAction() {
            putValue(NAME, "Save Log…");
            putValue(SHORT_DESCRIPTION, "Save greetings to a text file");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (displayArea.getText().isEmpty()) {
                beep("There is nothing to save yet.");
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Log");
            chooser.setSelectedFile(new File("greetings.txt"));
            int result = chooser.showSaveDialog(SimpleSwingApp.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
                    out.write(displayArea.getText());
                    setStatus("Saved: " + file.getAbsolutePath());
                } catch (IOException ex) {
                    showError("Failed to save file:\n" + ex.getMessage()); // fixed
                }
            }
        }
    }

    private class ExitAction extends AbstractAction {
        ExitAction() {
            putValue(NAME, "Exit");
            putValue(SHORT_DESCRIPTION, "Close the application");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

    private class AboutAction extends AbstractAction {
        AboutAction() {
            putValue(NAME, "About");
            putValue(SHORT_DESCRIPTION, "Show app info");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(
                    SimpleSwingApp.this,
                    "<html><b>Simple Swing Example</b><br/>" +
                            "A tiny demo with menus, form, keyboard shortcuts,<br/>" +
                            "status bar, and file saving.</html>",
                    "About",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /* ----------------- Helpers ----------------- */

    private void beep(String message) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(
                this, message, "Notice", JOptionPane.WARNING_MESSAGE
        );
        setStatus(message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Error", JOptionPane.ERROR_MESSAGE
        );
        setStatus("Error: " + message);
    }

    private void setStatus(String msg) {
        statusBar.setText(" " + msg);
    }

    /* ----------------- Main ----------------- */

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimpleSwingApp app = new SimpleSwingApp();
            app.setVisible(true);
        });
    }
}
