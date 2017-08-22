package com.frogshealth;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by yanbo on 17/6/19.
 */
public class dokuwiki {

    @SuppressWarnings("unchecked")
    public static void main(String args[]) throws IOException {
        FileReader reader = null;
        JSONParser parser = new JSONParser();
        try {
            reader = new FileReader(args[0] + "/api_data.json");
            JSONArray content = (JSONArray) parser.parse(reader);
            if(content == null) {
                return;
            }

            final String file = args[1];

            // 保存group 判断是否写入
            List<String> groupList = new ArrayList<String>();
            File catalogFile = new File(file, "catalog.txt");
            FileOutputStream out = null;
            OutputStreamWriter osw = null;

            try {
                out = new FileOutputStream(catalogFile);
                osw = new OutputStreamWriter(out);

                Iterator<Object> iterator = content.iterator();
                while(iterator.hasNext()) {
                    JSONObject obj = (JSONObject) iterator.next();

                    String group = (String)obj.get("group");
                    if (!groupList.contains(group)) {
                        groupList.add(group);
                        writeTitle(osw, 5, group, "=");
                    }
                    String title = (String)obj.get("title");
                    String name = (String)obj.get("name");
                    name = name.toLowerCase();
                    osw.write(" [[apidoc:" + name + "|" + title + "]]\n\n");
                    convert(obj, file);
                }
            } finally {
                if(osw != null) {
                    osw.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                reader.close();
            }
        }
    }

    private static void writeCatalog() {
    }

    private static void convert(JSONObject api, String dir) throws IOException {

        String fileName = (String)api.get("name");
        fileName =fileName.toLowerCase();
        File file = new File(dir, "./generated");
        if(!file.exists()) {
            file.mkdir();
        }
        file = new File(file, fileName + ".txt");
        file.createNewFile();

        FileOutputStream out = null;
        OutputStreamWriter osw = null;
        try {
            out = new FileOutputStream(file);
            osw = new OutputStreamWriter(out, "utf-8");
            String title = (String)api.get("title");
            String url = (String)api.get("url");
            String type = (String)api.get("type");

            String group = (String)api.get("group");


            writeTitle(osw, 5, title, "=");
            writeDescription(osw, api);

            writeTitle(osw, 4, "接口url", "=");
            writeNormal(osw, url);

            writeTitle(osw, 4, "请求方式", "=");
            writeNormal(osw, type);

            writeTitle(osw, 4, "接口参数", "=");
            writeParameter(osw, api);

            writeTitle(osw, 4, "返回结果参数说明", "=");
            writeResultParameter(osw, api);

            writeTitle(osw, 4, "接口返回", "=");
            writeResponse(osw, api);
        } finally {
            if(osw != null) {
                osw.close();
            }
        }

    }

    private static void writeDescription(OutputStreamWriter osw, JSONObject api) throws IOException {
        String description = (String)api.get("description");
        if(description ==null || description.isEmpty()) {
            return;
        }
        osw.write("<html>" + description + "</html>");
    }

    private static void writeParameter(OutputStreamWriter osw, JSONObject api) throws IOException {
        JSONObject parameter = (JSONObject)api.get("parameter");
        if(parameter == null) {
            return;
        }
        JSONObject fields = (JSONObject)parameter.get("fields");
        if(fields == null) {
            return;
        }
        JSONArray parameters = (JSONArray)fields.get("Parameter");
        if(parameters == null) {
            return;
        }
        try {
            writeForm(osw, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeTitle(OutputStreamWriter osw, int number, String content, String symbol) throws IOException {
        int num = 0;
        while (num < number) {
            osw.write(symbol);
            num++;
        }
        num = 0;
        osw.write(" ");
        osw.write(content);
        osw.write(" ");
        while (num < number) {
            osw.write(symbol);
            num++;
        }
        osw.write("\n\n");
    }

    private static void writeNormal(OutputStreamWriter osw, String content) throws IOException {
        osw.write("  " + content);
        osw.write("\n\n");
    }

    @SuppressWarnings("unchecked")
    private static void writeResponse(final OutputStreamWriter osw, JSONObject api) throws IOException {
        JSONObject success = (JSONObject)api.get("success");
        if(success == null) {
            return;
        }
        JSONArray examples = (JSONArray)success.get("examples");
        if(examples == null) {
            return;
        }
        Iterator<Object> iterator = examples.iterator();
        while(iterator.hasNext()) {
            JSONObject example = (JSONObject) iterator.next();
            osw.write("<code>");
            osw.write((String)example.get("content"));
            osw.write("</code>");
            osw.write("\n\n");
        }
    }

    private static void writeResultParameter(final OutputStreamWriter osw, JSONObject api) throws IOException {
        JSONObject success = (JSONObject)api.get("success");
        if(success == null) {
            return;
        }
        JSONObject fields = (JSONObject)success.get("fields");
        if(fields != null) {
            JSONArray responses = (JSONArray)fields.get("返回结果参数说明");
            if(responses == null) {
                return;
            }
            try {
                writeForm(osw, responses);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeForm(OutputStreamWriter osw, JSONArray json) throws Exception{
        osw.write("^ 字段 ^ 类型 ^ 是否必填 ^ 描述 ^");
        Iterator<Object> iterator = json.iterator();
        while(iterator.hasNext()) {
            JSONObject obj = (JSONObject) iterator.next();
            String optional = null;
            if((boolean)obj.get("optional")) {
                optional = "true";
            } else {
                optional = "false";
            }
            String description = ((String)obj.get("description")).replace("<p>", "");
            String content = "| " + (String)obj.get("field") + " | " + (String)obj.get("type") + " | " + optional + " | " + description.replace("</p>", "") + " |";
            osw.write("\n");
            osw.write(content);
        }
        osw.write("\n");
    }
}
