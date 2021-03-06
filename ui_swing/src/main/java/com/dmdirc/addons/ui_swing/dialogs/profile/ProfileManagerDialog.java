/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.dialogs.profile;

import com.dmdirc.addons.ui_swing.components.GenericListModel;
import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.addons.ui_swing.components.renderers.PropertyListCellRenderer;
import com.dmdirc.addons.ui_swing.components.reorderablelist.ReorderableJList;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.components.validating.ValidationFactory;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.interfaces.ui.ProfilesDialogModel;
import com.dmdirc.ui.core.profiles.MutableProfile;

import java.awt.Window;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class ProfileManagerDialog extends StandardDialog {

    /** Serial version UID. */
    private static final long serialVersionUID = 3;
    /** Model used to store state. */
    private final ProfilesDialogModel model;
    /** Icon manager. */
    private final IconManager iconManager;
    /** Adds a new nickname to the active profile. */
    private final JButton addNickname = new JButton("Add");
    /** Edits the active nickname in the active profile. */
    private final JButton editNickname = new JButton("Edit");
    /** Deletes the selected nickname from the active profile. */
    private final JButton deleteNickname = new JButton("Delete");
    /** Adds a new highlight to the active profile. */
    private final JButton addHighlight = new JButton("Add");
    /** Edits the active highlight in the active profile. */
    private final JButton editHighlight = new JButton("Edit");
    /** Deletes the selected highlight from the active profile. */
    private final JButton deleteHighlight = new JButton("Delete");
    /** Edits the name of the active profile. */
    private final JTextField name = new JTextField();
    /** Edits the realname for the active profile. */
    private final JTextField realname = new JTextField();
    /** Edits the ident for the active profile. */
    private final JTextField ident = new JTextField();
    /** Adds a new profile to the list. */
    private final JButton addProfile = new JButton("Add");
    /** Deletes the active profile. */
    private final JButton deleteProfile = new JButton("Delete");
    /** List of profiles. */
    private JList<MutableProfile> profileList;
    /** List of nicknames for a profile. */
    private ReorderableJList<String> nicknames;
    /** List of highlights for a profile. */
    private ReorderableJList<String> highlights;

    /**
     * Creates a new instance of ProfileEditorDialog.
     *
     * @param mainFrame   Main frame
     * @param model       Profiles model
     * @param iconManager Icon manager to retrieve icons
     */
    @Inject
    public ProfileManagerDialog(@MainWindow final Window mainFrame, final ProfilesDialogModel model,
            final IconManager iconManager) {
        super(mainFrame, ModalityType.DOCUMENT_MODAL);
        setTitle("Profile Manager");
        this.model = model;
        this.iconManager = iconManager;
        initComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        profileList = new JList<>(new GenericListModel<>());
        nicknames = new ReorderableJList<>(new GenericListModel<>());
        highlights = new ReorderableJList<>(new GenericListModel<>());
        profileList.setCellRenderer(new PropertyListCellRenderer<>(profileList.getCellRenderer(),
                MutableProfile.class, "name"));
        setLayout(new MigLayout("fill, wmin 700, wmax 700, flowy"));

        add(new TextLabel("Profiles describe the information needed to connect " +
                "to a server.  You can use a different profile for each " + "connection."),
                "spanx 3");
        add(new JScrollPane(profileList), "spany 7, split 3, growy, pushy, "
                + "wmin 200, wmax 200");
        add(addProfile, "growx");
        add(deleteProfile, "growx, wrap");
        add(new JLabel("Name: "), "align label, span 2, split 2, flowx, sgx label");
        add(ValidationFactory
                .getValidatorPanel(name, model.getSelectedProfileNameValidator(), iconManager),
                "growx, pushx, sgx textinput");
        add(new JLabel("Nicknames: "),
                "align label, span 2, split 2, flowx, sgx label, aligny 50%");
        add(ValidationFactory.getValidatorPanel(new JScrollPane(nicknames), nicknames,
                model.getNicknamesValidator(), iconManager), "grow, push");
        add(Box.createGlue(), "flowx, span 4, split 4, sgx label");
        add(addNickname, "grow");
        add(editNickname, "grow");
        add(deleteNickname, "grow");
        add(new JLabel("Realname: "), "align label, span 2, split 2, flowx, sgx label");
        add(ValidationFactory
                .getValidatorPanel(realname, model.getRealnameValidator(), iconManager),
                "growx, pushx, sgx textinput");
        add(new JLabel("Ident: "), "align label, span 2, split 2, flowx, sgx label");
        add(ValidationFactory.getValidatorPanel(ident, model.getIdentValidator(), iconManager),
                "growx, pushx, sgx textinput");
        add(new JLabel("Highlight: "),
                "align label, span 2, split 2, flowx, sgx label, aligny 50%");
        add(ValidationFactory.getValidatorPanel(new JScrollPane(highlights), highlights,
                model.getHighlightsValidator(), iconManager), "grow, push");
        add(Box.createGlue(), "flowx, span 4, split 4, sgx label");
        add(addHighlight, "grow");
        add(editHighlight, "grow");
        add(deleteHighlight, "grow");
        add(getLeftButton(), "flowx, split 2, right, sg button");
        add(getRightButton(), "right, sg button");
    }

    @Override
    public void display() {
        new ProfileManagerController(this, model, iconManager).init(profileList, addProfile,
                deleteProfile, name, nicknames, addNickname, editNickname, deleteNickname,
                realname, ident, highlights, addHighlight, editHighlight, deleteHighlight,
                getOkButton(), getCancelButton());
        super.display();
    }
}
