package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import module.AndroidString;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by Wesley on 11/26/14.
 */
public class ConvertToOtherLanguages extends AnAction {
    public void actionPerformed(AnActionEvent e) {

        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        VirtualFile clickedFile = (VirtualFile) e.getDataContext().getData(DataConstants.VIRTUAL_FILE);
        if (clickedFile.getExtension() == null || !clickedFile.getExtension().equals("xml")) {
            showErrorDialog(project, "Target file is not Android resource.");
            return;
        }

        List<AndroidString> androidStrings = null;
        try {
             androidStrings = AndroidString.getAndroidStringsList(clickedFile.contentsToByteArray());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (androidStrings == null || androidStrings.isEmpty()) {
            showErrorDialog(project, "Target file does not contain any strings.");
            return;
        }

        for (AndroidString androidString : androidStrings)
            System.out.println(androidString);








        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();


        if (editor == null) {
            return;
        }
        final Document document = editor.getDocument();
        if (document == null) {
            return;
        }
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (virtualFile == null) {
            return;
        }



        final String contents;
        try {
            BufferedReader br = new BufferedReader(new FileReader(virtualFile.getPath()));
            String currentLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((currentLine = br.readLine()) != null) {
                stringBuilder.append(currentLine);
                stringBuilder.append("\n");
            }
            contents = stringBuilder.toString();
        } catch (IOException e1) {
            return;
        }
        final Runnable readRunner = new Runnable() {
            @Override
            public void run() {
                document.setText(contents);
            }
        };
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(readRunner);
                    }
                }, "DiskRead", null);
            }
        });
    }

    private void showErrorDialog(Project project, String msg) {
        Messages.showErrorDialog(project, msg, "Error");
    }
}
