package com.darktornado.androidterminal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShellExecutor {

    private Context ctx;
    private File dir;

    public ShellExecutor(Context ctx, File dir) {
        this.ctx = ctx;
        this.dir = dir;
    }

    public Result execute(String cmd) {
        if (cmd.startsWith("cd ")) return moveDir(cmd.replaceFirst("cd ", ""));
        if (cmd.equals("dir")) cmd = "ls";
        Process pro = null;
        int pos = 0;
        try {
            pro = Runtime.getRuntime().exec(cmd, null, dir);
            pro.waitFor();
            pos = 1;
            String result = readStream(pro.getInputStream());
            pos = 2;
            pro.destroy();
            pos = 3;
            if (cmd.equals("ls")) result = separateData(result);
            return new Result(result, false);
        } catch (IOException e) {
            if (checkFile(cmd)) return new Result(null, false);
            return new Result("Cannot find command or file: " + cmd, true);
        } catch (Exception e) {
            if (pro != null) pro.destroy();
            return new Result(pos+", "+e.toString(), true);
        }
    }

    private boolean checkFile(String cmd) {
        File file = new File(dir, cmd);
        if (!file.exists()) return false;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (cmd.endsWith(".html")) intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setDataAndType(Uri.fromFile(file), getFileType(cmd));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(intent);
        return true;
    }

    public String getCurrentDirectory(){
        return dir.toString();
    }

    private Result moveDir(String dir) {
        if (dir.equals("..")) {
            if (this.dir.toString().equals("/")) {
                return new Result("Current directory is root.", true);
            }
            this.dir = this.dir.getParentFile();
        } else {
            File file = new File(this.dir, dir);
            if (!file.exists()) return new Result("Cannot find directory: " + file.toString(), true);
            else if (file.isDirectory()) this.dir = file;
            else return new Result(this.dir.toString() + " is not directory.", true);
        }
        return new Result(null, false);
    }

    private String separateData(String input) {
        if (input == null) return null;
        String[] names = input.split("\n");
        File[] files = new File[names.length];
        for (int n = 0; n < names.length; n++) {
            files[n] = new File(dir, names[n]);
        }
        StringBuilder dir1 = new StringBuilder();
        StringBuilder dir2 = new StringBuilder();
        for (File file : files) {
            if (file.isDirectory()) dir1.append(file.getName()).append("\n");
            else dir2.append(file.getName()).append("\n");
        }
        return dir1.append(dir2).toString().trim();
    }

    private String getFileType(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) return "*/*";
        String ext = fileName.substring(index).toLowerCase();
        switch (ext) {
            case ".png":
            case ".jpg":
            case ".jepg":
            case ".gif":
            case ".bmp":
            case ".tiff":
                return "image/*";
            case ".mp3":
            case ".wma":
            case ".wav":
                return "audio/*";
            case ".mp4":
            case ".avi":
            case ".wmv":
                return "video/*";
            case ".txt":
            case ".js":
            case ".ts":
            case ".coffee":
            case ".java":
            case ".jsp":
            case ".kt":
            case ".c":
            case ".cpp":
            case ".h":
            case ".py":
            case ".rb":
            case ".php":
            case ".vb":
            case ".vbs":
            case ".lua":
            case ".swift":
            case ".css":
                return "text/*";
            case ".html":
                return "text/html";
            case ".pdf":
                return "application/pdf";
            default:
                return "*/*";
        }
    }

    private String readStream(InputStream stream) {
        try {
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder str = new StringBuilder(br.readLine());
            String line = "";
            while ((line = br.readLine()) != null) {
                str.append("\n").append(line);
            }
            isr.close();
            br.close();
            return str.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static class Result {
        public boolean failed;
        public String data;

        public Result(String data, boolean failed) {
            this.failed = failed;
            this.data = data;
        }
    }

}
