/*
* ProjectExplorerPanel.java
*
* Copyright (C) 2005 Project SQUID, http://www.cs.helsinki.fi/group/squid/
*
* This file is part of Ikayaki.
*
* Ikayaki is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* Ikayaki is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Ikayaki; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/

package ikayaki.gui;

import ikayaki.*;
import ikayaki.util.LastExecutor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.io.File;
import java.io.*;
import java.text.DateFormat;
import java.util.Arrays;

/**
 * Creates a history/autocomplete field (browserField) for choosing the project directory, a listing of project files in
 * that directory (explorerTable) and in that listing a line for creating new project, which has a textbox for project
 * name, an AF/TH ComboBox and a "Create new" button (createNewProjectButton) for actuating the creation. Also has a
 * right-click popup menu for exporting project files.
 *
 * @author Samuli Kaipiainen
 */
public class ProjectExplorerPanel extends ProjectComponent {
/*
Event E: On ProjectEvent - hilight project whose measuring started, or unhilight one
whose measuring ended.
*/

    /**
     * The component whose setProject() method will be called on opening a new project file.
     */
    private ProjectComponent parent;

    /**
     * Holds browserField and browseButton
     */
    private JPanel browsePanel = new JPanel();

    /**
     * Text field for writing directory to change to. Autocomplete results appear to Combo Box� popup window, scheduled
     * by LastExecutor. Directory history appears to the same popup window when the down-arrow right to text field is
     * clicked.
     */
    private JComboBox browserField;
    private JTextField browserFieldEditor;

    /**
     * Tells whether the next-to-be-shown popup menu will be autocomplete list (and not directory history).
     */
    private boolean browserFieldNextPopupAutocomplete = false;

    private JButton browseButton;

    private JTable explorerTable;

    private ProjectExplorerTableModel explorerTableModel;

    private JScrollPane explorerTableScrollPane;

    /**
     * -1==undefined, 0==filename, 1==type, 2==last modified
     */
    // TODO: enum?
    private int explorerTableSortColumn = -1;

    private NewProjectPanel newProjectPanel;

    /**
     * LastExecutor for scheduling autocomplete results to separate thread (disk access and displaying).
     */
    private LastExecutor autocompleteExecutor = new LastExecutor(100, true);

    /**
     * Currently open directory.
     */
    private File directory = null;

    /**
     * Project files in current directory.
     */
    private File[] files = new File[0];

    /**
     * Selected project file index, or -1 if none selected in current directory.
     */
    private int selectedFile = -1;

    /**
     * Creates all components, sets directory as the last open directory, initializes files with files from that
     * directory.
     *
     * @param parent the component whose setProject() method will be called on opening a new project file.
     */
    public ProjectExplorerPanel(ProjectComponent parent) {
        this.parent = parent;

        // set current directory to latest directory history dir
        // note: getDirectoryHistory() always returns at least one dir (as File[0])
        setDirectory(getDirectoryHistory()[0]);

        // combo box / text field
        browserField = new JComboBox(getDirectoryHistory());
        browserField.setEditable(true);
        browserField.setBackground(Color.WHITE);
        browserFieldEditor = (JTextField) browserField.getEditor().getEditorComponent();
        // browserFieldEditor.setFocusTraversalKeysEnabled(false); // disable tab-exiting from browserField

        // scroll to the end of the combo box's text field
        setBrowserFieldCursorToEnd();

        // browse button
        browseButton = new JButton("Browse...");

        // add both into this
        browsePanel.setLayout(new BorderLayout());
        //browsePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        browsePanel.add(browserField, BorderLayout.CENTER);
        browsePanel.add(browseButton, BorderLayout.EAST);

        // project file table (and its table model)
        // TODO: these should be in inner class ProjectExplorerTable
        explorerTableModel = new ProjectExplorerTableModel();
        explorerTable = new JTable(explorerTableModel);
        explorerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        explorerTable.setShowGrid(false); // TODO: the grid still shows up when selecting rows. must make a custom cell renderer to change that
        explorerTableScrollPane = new JScrollPane(explorerTable);
        explorerTableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        explorerTableScrollPane.getViewport().setBackground(Color.WHITE);
        // TODO: set column sizes somehow automatically, according to table contents?
        explorerTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        explorerTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        explorerTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        explorerTable.setPreferredScrollableViewportSize(new Dimension(280, 400));

        this.setLayout(new BorderLayout());
        this.add(browsePanel, BorderLayout.NORTH);
        this.add(explorerTableScrollPane, BorderLayout.CENTER);

        // ProjectExplorer events

        /**
         * Event D: On browseButton click - open a FileChooser dialog for choosing new directory,
         * set it to directory, update files listing, update explorerTable and browserField.
         */
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(directory);
                fc.setDialogTitle("Change directory");
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (fc.showOpenDialog(browseButton) == JFileChooser.APPROVE_OPTION)
                        setDirectory(fc.getSelectedFile());
            }
        });

        /**
         * Event C: On browserField popup window click - set clicked line as directory, update files
         * listing, update explorerTable and browserField.
         */
        browserField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //System.out.println(e.getActionCommand());

                if (e.getActionCommand().equals("comboBoxEdited")) {
                    // the user pressed enter in the text field or selected an item by pressing enter
                    // TODO: causes problems for exapmle when clicking browse button
                    // doAutoComplete();

                } else if (e.getActionCommand().equals("comboBoxChanged")) {

//                    System.out.println(browserField.getSelectedItem());
//
//                    // TODO: changing JComboBox popup-list content fires ActionEvents which we don't want... argh.
//                    if (!setDirectory((String) browserField.getSelectedItem())) {
//                        // TODO: how to display error?
//                        browserField.getEditor().selectAll();
//                    }
                }
            }
        });

        /**
         * Event B: On browserField down-arrow-click - show directory history in browserField�s popup window.
         */
        browserField.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (browserFieldNextPopupAutocomplete) browserFieldNextPopupAutocomplete = false;
                else setBrowserFieldPopup(getDirectoryHistory());
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        /**
         * Event A: On browserField change - send autocomplete-results-finder with browserField�s text
         * to LastExecutor via autocompleteExecutor.execute(Runnable), which schedules disk access and
         * displaying autocomplete results in browserField�s popup window.
         */
        browserFieldEditor.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ESCAPE || e.getKeyChar() == KeyEvent.VK_ENTER) {
                    return;

                } else if (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyChar() == KeyEvent.VK_DELETE) {
                    // delete one directory name at a time
                    int pos = browserFieldEditor.getCaretPosition();
                    String text = browserFieldEditor.getText();
                    String textA = text.substring(0, pos);
                    String textB = text.substring(pos);
                    textA = textA.substring(0, Math.max(0, textA.lastIndexOf(System.getProperty("file.separator"))));
                    browserFieldEditor.setText(textA + textB);
                    browserFieldEditor.setCaretPosition(textA.length());
                    return;

                } else if ((e.getModifiers() & KeyEvent.ALT_MASK) != 0 || (e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    // avoid the popup menu from showing, when the Project Explorer tab is hidden with ALT+P
                    browserField.hidePopup();
                    return;
                    
                } else {
                    autocompleteExecutor.execute(new Runnable() {
                        public void run() {
                            doAutoComplete();
                        }
                    });
                    return;
                }
            }
        });

        // ProjectExplorerTable events
        // TODO: these should be in inner class ProjectExplorerTable

        /**
         * Event A: On table click - call Project.loadProject(File) with clicked project file, call
         * (MainViewPanel) parent.setProject(Project) with returned Project unless null, on which case
         * show error message and revert explorerTable selection to old project, if any.
         */
        explorerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // we only want the actually selected row, and don't want to react to an already selected line
                // (which could also mean that we had a load error, and that selection was reverted)
                if (e.getValueIsAdjusting() || explorerTable.getSelectedRow() == selectedFile) return;
                if (explorerTable.getSelectedRow() == -1) return; // otherwise will crash the program upon loading a file

                Project project = Project.loadProject(files[explorerTable.getSelectedRow()]);

                // load error, revert back to old selection
                if (project == null) {
                    // TODO: flash selected row red for 100 ms, perhaps? - might require a custom cell renderer
                    if (selectedFile == -1) explorerTable.clearSelection();
                    else explorerTable.setRowSelectionInterval(selectedFile, selectedFile);
                } else {
                    // super.setProject nod needed; MainViewPanel (parent) calls our setProject anyway
                    // ProjectExplorerPanel.super.setProject(project);
                    ProjectExplorerPanel.this.parent.setProject(project);
                }
            }
        });

        /**
         * Event B: On table mouse right-click - create a ProjectExplorerPopupMenu for rightclicked
         * project file.
         */
        explorerTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // only right-click brings popup menu
                if (e.getButton() != MouseEvent.BUTTON3) return;

                int row = explorerTable.rowAtPoint(e.getPoint());

                // construct the popupmenu for every click
                JPopupMenu explorerTablePopup = new JPopupMenu("Export");
                JMenuItem exportDAT = new JMenuItem("Export '" + files[row].getName() + "' to DAT file...");
                JMenuItem exportTDT = new JMenuItem("Export '" + files[row].getName() + "' to TDT file...");
                JMenuItem exportSRM = new JMenuItem("Export '" + files[row].getName() + "' to SRM file...");
                explorerTablePopup.add(exportDAT);
                explorerTablePopup.add(exportTDT);
                explorerTablePopup.add(exportSRM);

                // TODO

                explorerTablePopup.show(explorerTable, e.getX(), e.getY());
            }
        });

        /**
         * ExplorerTable sorting.
         */
        explorerTable.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // only left-click changes sorting
                if (e.getButton() != MouseEvent.BUTTON1) return;

                JTableHeader th = (JTableHeader) e.getSource();
                TableColumnModel cm = th.getColumnModel();
                int viewColumn = cm.getColumnIndexAtX(e.getX());
                explorerTableSortColumn = cm.getColumn(viewColumn).getModelIndex();
                // TODO: update table header somehow (to show the new sort column)
                // TODO: add proper table sorting here
                System.out.println("sort " + cm.getColumn(viewColumn).getHeaderValue());
            }
        });

        return; // TODO
    }

    /**
     * Call super.setProject(project), hilight selected project, or unhilight unselected project.
     *
     * @param project project opened.
     */
    public void setProject(Project project) {
        super.setProject(project);
        if (project != null) {
            setDirectory(project.getFile().getParentFile());
            project.addProjectListener(explorerTableModel);
        }
        else setDirectory(directory);
    }

    /**
     * Attempts to change to the given directory. Updates browserField and explorerTable with new directory.
     *
     * @param directory directory to change to.
     * @return true if succesful, false otherwise.
     */
    private boolean setDirectory(File directory) {
        if (directory == null || !directory.isDirectory()) return false;

        this.directory = directory;
        files = getProjectFiles(directory);

        // update browserField and explorerTable with new directory
        if (browserField != null) browserField.setSelectedItem(directory.getPath());
        if (explorerTableModel != null) explorerTableModel.fireTableDataChanged();
        // fireTableDataChanged messes with selection, set it again
        if (explorerTable != null && selectedFile != -1)
            explorerTable.setRowSelectionInterval(selectedFile, selectedFile);

        return true;
    }

    /**
     * Reads project file listing from given directory. Sets selected project index, or -1, to selectedFile.
     *
     * @param directory directory whose project file listing to read.
     * @return project files in that directory, sorted alphabetically.
     */
    private File[] getProjectFiles(File directory) {
        File[] files = directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                // TODO: shouldn't this return only a list of valid project files? so why is Ikayaki.FILE_TYPE commented out?
                // - for explorerTable testing, need some (working perhaps) project file expamples :)
                return (file.isFile() && file.getName().endsWith(/*Ikayaki.FILE_TYPE*/ ""));
            }
        });

        // TODO: sort according to explorerTableSortColumn (although this is a wrong way for sorting JTable, see
        // http://java.sun.com/docs/books/tutorial/uiswing/components/table.html for the right one).
        // Arrays.sort(files);
        switch (explorerTableSortColumn) {
            case 0: // filename
            case 1: // type
            case 2: // last modified
            default: // -1, no sort
        }

        // set current project file index to selectedFile
        selectedFile = -1;
        if (getProject() != null) for (int n = 0; n < files.length; n++)
            if (getProject().getFile().equals(files[n])) selectedFile = n;

        return files;
    }

    /**
     * Reads current directory history from Settings. If directory history is empty, returns current directory instead.
     *
     * @return current directory history, or if empty, only current directory (as File[0]).
     */
    private File[] getDirectoryHistory() {
        File[] dirhist = Settings.instance().getDirectoryHistory();

        if (dirhist == null || dirhist.length == 0) return new File[] { new File("").getAbsoluteFile() };
        else return dirhist;
    }

    /**
     * Reads matching directories from given directory name's parent.
     *
     * @param dirmatch beginning of directory to which match the directories in its parent directory...
     * @return matching directories.
     */
    private File[] getAutocompleteFiles(String dirmatch) {
        File dirfile = new File(dirmatch);
        if (!dirfile.isAbsolute()) return File.listRoots();
        File dir = dirfile.isDirectory() ? dirfile : dirfile.getParentFile();

        // protect against no-parent-null and invalid-dir-list-null
        if (dir == null) dir = directory;
        else if (!dir.isDirectory()) return new File[0];

        final String match = dirfile.isDirectory() ? "" : dirfile.getName();

        return dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return (file.isDirectory() && file.getName().toLowerCase().startsWith(match.toLowerCase()));
            }
        });
    }

    /**
     * Updates autocomplete popup-menu.
     */
    private void doAutoComplete() {
        File[] files = getAutocompleteFiles(browserField.getEditor().getItem().toString());
        //Arrays.sort(files); // filesystem sorts them nicely
        setBrowserFieldPopup(files);

        if (files.length > 0) {
            // gui updating must be done from event-dispatching thread
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // when the popup is hidden before showing, it will be automatically resized
                    if (browserField.isPopupVisible()) browserField.hidePopup();
                    browserFieldNextPopupAutocomplete = true;
                    browserField.showPopup();
                }
            });
        }
    }

    /**
     * Sets browserField popup-menu-list as given files; also clears any selection.
     *
     * @param files list of files to set the list to.
     */
    private void setBrowserFieldPopup(File[] files) {
        // purkkaillaan -- some hardcore bubblegum stitching
        String browserFieldEditorText = browserFieldEditor.getText();
        int browserFieldEditorCursorPosition = browserFieldEditor.getCaretPosition();

        browserField.removeAllItems();
        for (File file : files) browserField.addItem(file.getAbsolutePath());

        browserField.setSelectedIndex(-1);
        browserFieldEditor.setText(browserFieldEditorText);
        browserFieldEditor.setCaretPosition(browserFieldEditorCursorPosition);
    }

    /**
     * Sets browserField's cursor to text field's (right) end.
     */
    private void setBrowserFieldCursorToEnd() {
        browserFieldEditor.setCaretPosition(browserFieldEditor.getDocument().getLength());
    }

    /**
     * Creates a list of project files in directory. Handles loading selected projects and showing export popup menu
     * (ProjectExplorerPopupMenu). Inner class of ProjectExplorerPanel.
     *
     * @author Samuli Kaipiainen
     */
    // TODO: comment above awaiting for refactoring into ProjectExplorerTable class

    /**
     * TableModel which handles data from files (in upper-class ProjectExplorerPanel).
     */
    private class ProjectExplorerTableModel extends AbstractTableModel implements ProjectListener {
        private final String[] columns = { "filename", "type", "last modified" };

        public String getColumnName(int column) {
            return columns[column] + (column == explorerTableSortColumn ? " *" : "");
        }

        public int getRowCount() {
            return files.length;
        }

        public int getColumnCount() {
            return columns.length;
        }

        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0: return files[row].getName();
                case 1: return Project.getType(files[row]);
                case 2: return DateFormat.getInstance().format(files[row].lastModified());
                default: assert false; return null;
            }
        }

        /**
         * Updates the file list when a project file has been saved.
         */
        public void projectUpdated(ProjectEvent event) {
            if (event.getType() == ProjectEvent.Type.FILE_SAVED) {
                File saved = event.getProject().getFile();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].equals(saved)) {
                        fireTableRowsUpdated(i, i);
                        return;
                    }
                }
            }
        }
    }
}
