package org.weebly.generator.forms;

import org.weebly.generator.actions.CreateAction;
import org.weebly.generator.services.TemplateLoader;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateFile extends JDialog {
    private JComboBox fileType;
    private JTextField objectName;
    private JComboBox moduleName;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField mainFileName;
    private JTextField testFileName;
    private JLabel warningLabel;
    private JLabel warningText;
    private List<String> allItems;

    private CreateAction createAction;
    private TemplateLoader templateLoader;

    private boolean isFileNameAvailable = false;
    private boolean isModuleNameAvailable = false;

    public CreateFile(CreateAction createAction, TemplateLoader templateLoader) {
        super();

        this.createAction = createAction;
        this.templateLoader = templateLoader;

        //add types
        fileType.removeAllItems();
        for (String item : templateLoader.getComponentTypes()) {
            fileType.addItem(item);
        }

        allItems = this.createAction.getModuleNameSuggestions();
        if (allItems.size() > 0) {
            moduleName.removeAllItems();
            for (String item : allItems) {
                moduleName.addItem(item);
            }
            isModuleNameAvailable = true;
        }

        setOkButtonState();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        moduleName.getEditor().getEditorComponent().addKeyListener(new KeyEventHandler());

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        fileType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });

        objectName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refresh();
                super.keyReleased(e);
            }
        });
    }

    private void refresh() {
        mainFileName.setText(templateLoader.getSrcFilename(getBaseName(), getObjectType()));
        testFileName.setText(templateLoader.getTestFilename(getBaseName(), getObjectType()));

        boolean exists = createAction.checkIfFileExists(mainFileName.getText()) && createAction.checkIfFileExists(testFileName.getText());

        if (exists) {
            isFileNameAvailable = false;
            setOkButtonState();
            warningLabel.setVisible(true);
            warningText.setText("File already exists!");
            warningText.setVisible(true);
        } else {
            isFileNameAvailable = true;
            setOkButtonState();
            warningLabel.setVisible(false);
            warningText.setVisible(false);
        }
    }

    //region helpers
    private String getBaseName() {
        return objectName.getText().trim();
    }

    private String getObjectType() {
        return (String) fileType.getSelectedItem();
    }

    private void setOkButtonState() {
        buttonOK.setEnabled(isModuleNameAvailable && isFileNameAvailable);
    }

    private void onOK() {

        this.createAction.createAngularComponentsHandler(getBaseName(), (String) fileType.getSelectedItem(), (String) moduleName.getSelectedItem());

        dispose();
    }

    //endregion

    private void onCancel() {
        dispose();
    }

    public void showDialog() {
        warningLabel.setVisible(false);
        warningText.setVisible(false);

        this.objectName.grabFocus();
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * Event Handler to handle key released event for the auto complete box.
     */
    private class KeyEventHandler extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            super.keyReleased(e);
            JTextComponent editor = ((JTextComponent) moduleName.getEditor().getEditorComponent());
            if (editor.getText().equals("")) {
                isModuleNameAvailable = false;
                setOkButtonState();
            } else {
                isModuleNameAvailable = true;
                setOkButtonState();
            }

            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                moduleName.setPopupVisible(false);
            } else if (String.valueOf(e.getKeyChar()).matches("\\w+")) {
                moduleName.hidePopup();
                String input = editor.getText();

                List<String> matchingItems = new ArrayList<>();
                for (String item : allItems) {
                    if (item.toLowerCase().startsWith(input)) {
                        matchingItems.add(item);
                    }
                }

                if (!matchingItems.isEmpty()) {
                    moduleName.removeAllItems();
                    Collections.sort(matchingItems);
                    for (String item : matchingItems) {
                        moduleName.addItem(item);
                    }
                    moduleName.setSelectedItem(moduleName.getItemAt(0));
                    editor.setText(input
                            + ((String) moduleName.getItemAt(0))
                            .substring(input.length()));
                    editor.setCaretPosition(input.length());
                    editor.setSelectionStart(input.length());
                    editor.setSelectionEnd(moduleName.getSelectedItem().toString().length());
                }
                //if nothing matching display complete list
                else {
                    moduleName.removeAllItems();
                    for (String item : allItems) {
                        moduleName.addItem(item);
                    }
                    editor.setText(input);
                }
                moduleName.showPopup();
            }
        }
    }


}
